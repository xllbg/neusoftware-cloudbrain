package com.neusoft.cloudbrain.service;

import com.neusoft.cloudbrain.dto.RegistrationRequest;
import com.neusoft.cloudbrain.dto.RegistrationResponse;
import com.neusoft.cloudbrain.entity.Doctor;
import com.neusoft.cloudbrain.entity.Patient;
import com.neusoft.cloudbrain.entity.Registration;
import com.neusoft.cloudbrain.exception.BusinessException;
import com.neusoft.cloudbrain.repository.DoctorRepository;
import com.neusoft.cloudbrain.repository.PatientRepository;
import com.neusoft.cloudbrain.repository.RegistrationRepository;
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

    @Transactional
    public RegistrationResponse createRegistration(RegistrationRequest request) {
        log.info("创建挂号 - 患者ID: {}, 医生ID: {}, 日期: {}, 时间段: {}",
                request.getPatientId(), request.getDoctorId(),
                request.getRegistrationDate(), request.getTimeSlot());

        // 校验患者存在
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new BusinessException(404, "患者不存在"));

        // 校验医生存在
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new BusinessException(404, "医生不存在"));

        // 创建挂号记录
        Registration registration = new Registration();
        registration.setPatientId(request.getPatientId());
        registration.setDoctorId(request.getDoctorId());
        registration.setDepartment(request.getDepartment());
        registration.setRegistrationDate(request.getRegistrationDate());
        registration.setTimeSlot(request.getTimeSlot());
        registration.setStatus("pending");

        Registration saved = registrationRepository.save(registration);
        log.info("挂号创建成功 - 挂号ID: {}", saved.getId());

        return RegistrationResponse.builder()
                .id(saved.getId())
                .patientId(saved.getPatientId())
                .patientName(patient.getName())
                .doctorId(saved.getDoctorId())
                .doctorName(doctor.getName())
                .department(saved.getDepartment())
                .registrationDate(saved.getRegistrationDate())
                .timeSlot(saved.getTimeSlot())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public List<RegistrationResponse.RegistrationListItem> getPatientRegistrations(Long patientId) {
        log.info("查询患者挂号列表 - 患者ID: {}", patientId);

        List<Registration> registrations = registrationRepository.findByPatientIdOrderByCreatedAtDesc(patientId);

        return registrations.stream()
                .map(reg -> {
                    Doctor doctor = doctorRepository.findById(reg.getDoctorId()).orElse(null);
                    return RegistrationResponse.RegistrationListItem.builder()
                            .id(reg.getId())
                            .doctorName(doctor != null ? doctor.getName() : "未知")
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

        Doctor doctor = doctorRepository.findById(saved.getDoctorId()).orElse(null);

        return RegistrationResponse.builder()
                .id(saved.getId())
                .patientId(saved.getPatientId())
                .doctorId(saved.getDoctorId())
                .doctorName(doctor != null ? doctor.getName() : "未知")
                .department(saved.getDepartment())
                .registrationDate(saved.getRegistrationDate())
                .timeSlot(saved.getTimeSlot())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
