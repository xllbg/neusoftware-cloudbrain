package com.neusoft.cloudbrain.service;

import com.neusoft.cloudbrain.dto.RegistrationRequest;
import com.neusoft.cloudbrain.dto.RegistrationResponse;
import com.neusoft.cloudbrain.entity.Doctor;
import com.neusoft.cloudbrain.entity.Patient;
import com.neusoft.cloudbrain.entity.Registration;
import com.neusoft.cloudbrain.entity.Triage;
import com.neusoft.cloudbrain.exception.BusinessException;
import com.neusoft.cloudbrain.repository.DoctorRepository;
import com.neusoft.cloudbrain.repository.PatientRepository;
import com.neusoft.cloudbrain.repository.RegistrationRepository;
import com.neusoft.cloudbrain.repository.TriageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TriageRepository triageRepository;

    @Transactional
    public RegistrationResponse createRegistration(RegistrationRequest request) {
        log.info("创建挂号 - 患者ID: {}, 医生ID: {}, 日期: {}, 时间段: {}",
                request.getPatientId(), request.getDoctorId(),
                request.getRegistrationDate(), request.getTimeSlot());

        // 校验患者存在
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new BusinessException(404, "患者不存在"));

        // 校验医生存在（急诊科 doctorId=0 跳过校验）
        Doctor doctor = null;
        if (request.getDoctorId() != null && request.getDoctorId() > 0) {
            doctor = doctorRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new BusinessException(404, "医生不存在"));
        }

        // 创建挂号记录
        Registration registration = new Registration();
        registration.setPatientId(request.getPatientId());
        // 急诊科 doctorId=0 转为 null 存储（表示待分配医生）
        registration.setDoctorId(request.getDoctorId() != null && request.getDoctorId() > 0 ? request.getDoctorId() : null);
        registration.setDepartment(request.getDepartment());
        registration.setRegistrationDate(request.getRegistrationDate());
        registration.setTimeSlot(request.getTimeSlot());
        registration.setStatus("pending");
        registration.setSymptom(request.getSymptom());

        Registration saved = registrationRepository.save(registration);
        log.info("挂号创建成功 - 挂号ID: {}, 症状: {}", saved.getId(), saved.getSymptom());

        return RegistrationResponse.builder()
                .id(saved.getId())
                .patientId(saved.getPatientId())
                .patientName(patient.getName())
                .doctorId(saved.getDoctorId())
                .doctorName(doctor != null ? doctor.getName() : "急诊科（待分配）")
                .department(saved.getDepartment())
                .registrationDate(saved.getRegistrationDate())
                .timeSlot(saved.getTimeSlot())
                .status(saved.getStatus())
                .symptom(saved.getSymptom())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public List<RegistrationResponse.RegistrationListItem> getPatientRegistrations(Long patientId) {
        log.info("查询患者挂号列表 - 患者ID: {}", patientId);

        List<Registration> registrations = registrationRepository.findByPatientIdOrderByCreatedAtDesc(patientId);

        return registrations.stream()
                .map(reg -> {
                    Long doctorId = reg.getDoctorId();
                    Doctor doctor = (doctorId != null) ? doctorRepository.findById(doctorId).orElse(null) : null;
                    String doctorName = doctor != null ? doctor.getName() : (doctorId == null ? "急诊科（待分配）" : "未知");
                    return RegistrationResponse.RegistrationListItem.builder()
                            .id(reg.getId())
                            .doctorName(doctorName)
                            .department(reg.getDepartment())
                            .doctorTitle(doctor != null ? doctor.getTitle() : "")
                            .hospital(doctor != null ? doctor.getHospital() : "")
                            .registrationDate(reg.getRegistrationDate())
                            .timeSlot(reg.getTimeSlot())
                            .status(reg.getStatus())
                            .createdAt(reg.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<RegistrationResponse.RegistrationListItem> getDoctorRegistrations(Long doctorId, String status, String department, String date) {
        log.info("查询医生挂号列表 - 医生ID: {}, 状态: {}, 科室: {}, 日期: {}", doctorId, status, department, date);

        // 获取医生信息，判断是否为急诊科医生
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        String doctorDepartment = doctor != null ? doctor.getDepartment() : null;

        List<Registration> registrations = registrationRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId);

        // 急诊科医生：同时查询急诊科待分配挂号（doctorId为NULL且科室为急诊科）
        if ("急诊科".equals(doctorDepartment)) {
            List<Registration> emergencyRegs = registrationRepository.findByDepartmentOrderByCreatedAtDesc("急诊科");
            for (Registration reg : emergencyRegs) {
                // 检查是否已存在于列表中（避免重复）
                boolean exists = registrations.stream().anyMatch(r -> r.getId().equals(reg.getId()));
                // 只添加待分配的挂号（doctorId为NULL）
                if (!exists && reg.getDoctorId() == null) {
                    registrations.add(reg);
                }
            }
            // 按创建时间降序排序
            registrations.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));
        }

        return registrations.stream()
                .filter(reg -> {
                    if (status != null && !status.isEmpty()) {
                        return status.equalsIgnoreCase(reg.getStatus());
                    }
                    return true;
                })
                .filter(reg -> {
                    if (department != null && !department.isEmpty()) {
                        return department.equals(reg.getDepartment());
                    }
                    return true;
                })
                .filter(reg -> {
                    if (date != null && !date.isEmpty()) {
                        try {
                            java.time.LocalDate filterDate = java.time.LocalDate.parse(date);
                            return filterDate.equals(reg.getRegistrationDate());
                        } catch (Exception e) {
                            log.warn("日期解析失败: {}", date);
                            return false;
                        }
                    }
                    return true;
                })
                .map(reg -> {
                    Patient patient = patientRepository.findById(reg.getPatientId()).orElse(null);

                    // 查询分诊结果
                    String triageResult = reg.getTriageResult();
                    if (triageResult == null || triageResult.isBlank()) {
                        // 从Triage表查询该患者最新的分诊记录
                        List<Triage> triageList = triageRepository.findByPatientIdOrderByCreatedAtDesc(reg.getPatientId());
                        if (triageList != null && !triageList.isEmpty()) {
                            Triage latestTriage = triageList.get(0);
                            triageResult = "建议" + latestTriage.getRecommendedDepartment() + "就诊";
                        }
                    }

                    return RegistrationResponse.RegistrationListItem.builder()
                            .id(reg.getId())
                            .patientId(reg.getPatientId())
                            .patientName(patient != null ? patient.getName() : "未知")
                            .department(reg.getDepartment())
                            .registrationDate(reg.getRegistrationDate())
                            .timeSlot(reg.getTimeSlot())
                            .status(reg.getStatus())
                            .symptom(reg.getSymptom())
                            .triageResult(triageResult)
                            .createdAt(reg.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public RegistrationResponse cancelRegistration(Long registrationId, Long patientId) {
        log.info("取消挂号 - 挂号ID: {}, 患者ID: {}", registrationId, patientId);

        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new BusinessException(404, "挂号记录不存在"));

        if (!registration.getPatientId().equals(patientId)) {
            throw new BusinessException(403, "无权取消此挂号");
        }

        if ("cancelled".equals(registration.getStatus())) {
            throw new BusinessException(400, "该挂号已取消");
        }

        if ("completed".equals(registration.getStatus())) {
            throw new BusinessException(400, "该挂号已完成，无法取消");
        }

        registration.setStatus("cancelled");
        Registration saved = registrationRepository.save(registration);
        log.info("挂号取消成功 - 挂号ID: {}", registrationId);

        Doctor doctor = saved.getDoctorId() != null ? doctorRepository.findById(saved.getDoctorId()).orElse(null) : null;

        return RegistrationResponse.builder()
                .id(saved.getId())
                .patientId(saved.getPatientId())
                .doctorId(saved.getDoctorId())
                .doctorName(doctor != null ? doctor.getName() : (saved.getDoctorId() == null ? "急诊科（待分配）" : "未知"))
                .department(saved.getDepartment())
                .registrationDate(saved.getRegistrationDate())
                .timeSlot(saved.getTimeSlot())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional
    public RegistrationResponse.RegistrationListItem startConsultation(Long registrationId, Long doctorId) {
        log.info("开始问诊 - 挂号ID: {}, 医生ID: {}", registrationId, doctorId);

        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new BusinessException(404, "挂号记录不存在"));

        if (registration.getDoctorId() == null) {
            // 急诊科挂号（doctorId为null），任何医生都可以接诊
            registration.setDoctorId(doctorId);
        } else if (!registration.getDoctorId().equals(doctorId)) {
            throw new BusinessException(403, "无权接诊此挂号");
        }

        if (!"pending".equals(registration.getStatus())) {
            throw new BusinessException(400, "该挂号状态不允许接诊");
        }

        registration.setStatus("in_progress");
        Registration saved = registrationRepository.save(registration);
        log.info("接诊成功 - 挂号ID: {}", registrationId);

        Patient patient = patientRepository.findById(saved.getPatientId()).orElse(null);

        return RegistrationResponse.RegistrationListItem.builder()
                .id(saved.getId())
                .patientId(saved.getPatientId())
                .patientName(patient != null ? patient.getName() : "未知")
                .department(saved.getDepartment())
                .registrationDate(saved.getRegistrationDate())
                .timeSlot(saved.getTimeSlot())
                .status(saved.getStatus())
                .symptom(saved.getSymptom())
                .triageResult(saved.getTriageResult())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional
    public RegistrationResponse.RegistrationListItem completeConsultation(Long registrationId, Long doctorId) {
        log.info("完成问诊 - 挂号ID: {}, 医生ID: {}", registrationId, doctorId);

        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new BusinessException(404, "挂号记录不存在"));

        if (registration.getDoctorId() == null || !registration.getDoctorId().equals(doctorId)) {
            throw new BusinessException(403, "无权操作此挂号");
        }

        if ("completed".equals(registration.getStatus())) {
            throw new BusinessException(400, "该挂号已完成");
        }

        registration.setStatus("completed");
        Registration saved = registrationRepository.save(registration);
        log.info("问诊完成 - 挂号ID: {}", registrationId);

        Patient patient = patientRepository.findById(saved.getPatientId()).orElse(null);

        return RegistrationResponse.RegistrationListItem.builder()
                .id(saved.getId())
                .patientId(saved.getPatientId())
                .patientName(patient != null ? patient.getName() : "未知")
                .department(saved.getDepartment())
                .registrationDate(saved.getRegistrationDate())
                .timeSlot(saved.getTimeSlot())
                .status(saved.getStatus())
                .symptom(saved.getSymptom())
                .triageResult(saved.getTriageResult())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
