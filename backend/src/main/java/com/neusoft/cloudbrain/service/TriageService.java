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

import java.util.Collections;
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

        // 使用增强版AI分析
        AiService.TriageAnalysisResult analysis = aiService.analyzeSymptomsForTriage(
                request.getSymptoms(),
                request.getAge(),
                request.getGender()
        );

        log.info("AI 分析结果: department={}, confidence={}, needFollowUp={}, invalidInput={}",
                analysis.getDepartment(), analysis.getConfidence(), analysis.getNeedFollowUp(), analysis.getInvalidInput());

        // 如果是无效输入，直接返回追问结果（不查找医生）
        if (Boolean.TRUE.equals(analysis.getInvalidInput())) {
            TriageResponse response = TriageResponse.builder()
                    .department(analysis.getDepartment())
                    .reasoning(analysis.getReasoning())
                    .confidence(analysis.getConfidence())
                    .needFollowUp(true)
                    .followUpQuestions(analysis.getFollowUpQuestions())
                    .doctors(Collections.emptyList())
                    .build();
            return response;
        }

        // 如果需要追问，直接返回追问结果（不查找医生）
        if (Boolean.TRUE.equals(analysis.getNeedFollowUp())) {
            TriageResponse response = TriageResponse.builder()
                    .department(analysis.getDepartment())
                    .reasoning(analysis.getReasoning())
                    .confidence(analysis.getConfidence())
                    .needFollowUp(true)
                    .followUpQuestions(analysis.getFollowUpQuestions())
                    .doctors(Collections.emptyList())
                    .build();
            return response;
        }

        // 正常情况：查找推荐科室的医生
        String recommendedDepartment = analysis.getDepartment();
        List<Doctor> doctors = doctorRepository.findByDepartment(recommendedDepartment);

        // 如果推荐的科室没有医生（特别是急诊科），改为推荐内科
        if (doctors.isEmpty()) {
            log.warn("科室[{}]没有医生，改为推荐内科", recommendedDepartment);
            recommendedDepartment = "内科";
            doctors = doctorRepository.findByDepartment(recommendedDepartment);
            analysis.setReasoning(analysis.getReasoning() + "（该科室暂无可挂号医生，已为您推荐内科）");
        }

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
                .reasoning(analysis.getReasoning())
                .confidence(analysis.getConfidence())
                .needFollowUp(false)
                .followUpQuestions(Collections.emptyList())
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
