package com.neusoft.cloudbrain.service;

import com.neusoft.cloudbrain.dto.TriageRequest;
import com.neusoft.cloudbrain.dto.TriageResponse;
import com.neusoft.cloudbrain.entity.Doctor;
import com.neusoft.cloudbrain.entity.Triage;
import com.neusoft.cloudbrain.repository.DoctorRepository;
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
public class TriageService {

    private final AiService aiService;
    private final DoctorRepository doctorRepository;
    private final TriageRepository triageRepository;

    public TriageResponse triage(TriageRequest request) {
        log.info("开始分诊 - 患者ID: {}, 年龄: {}, 性别: {}, 症状: {}",
                request.getPatientId(), request.getAge(), request.getGender(), request.getSymptoms());

        String recommendedDepartment = aiService.recommendDepartment(
                request.getSymptoms(),
                request.getAge(),
                request.getGender()
        );

        log.info("AI 推荐科室: {}", recommendedDepartment);

        List<Doctor> doctors = doctorRepository.findByDepartment(recommendedDepartment);

        List<TriageResponse.DoctorSimpleVO> doctorVOS = doctors.stream()
                .map(doc -> TriageResponse.DoctorSimpleVO.builder()
                        .id(doc.getId())
                        .name(doc.getName())
                        .title(doc.getTitle())
                        .hospital(doc.getHospital())
                        .build())
                .collect(Collectors.toList());

        // 保存分诊记录
        String doctorIds = doctorVOS.stream()
                .map(doc -> doc.getId().toString())
                .collect(Collectors.joining(","));
        saveTriageRecord(request.getPatientId(), request, recommendedDepartment, doctorIds);

        TriageResponse response = TriageResponse.builder()
                .department(recommendedDepartment)
                .reasoning("根据您描述的症状[" + request.getSymptoms() + "]，" +
                        "推荐就诊" + recommendedDepartment)
                .doctors(doctorVOS)
                .build();

        return response;
    }

    @Transactional
    public Triage saveTriageRecord(Long patientId, TriageRequest request,
            String recommendedDepartment, String recommendedDoctorIds) {
        Triage triage = new Triage();
        triage.setPatientId(patientId);
        triage.setChiefComplaint(request.getSymptoms());
        triage.setRecommendedDepartment(recommendedDepartment);
        triage.setRecommendedDoctorIds(recommendedDoctorIds);
        return triageRepository.save(triage);
    }
}
