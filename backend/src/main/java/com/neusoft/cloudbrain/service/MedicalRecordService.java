package com.neusoft.cloudbrain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.cloudbrain.entity.Doctor;
import com.neusoft.cloudbrain.entity.MedicalRecord;
import com.neusoft.cloudbrain.entity.Patient;
import com.neusoft.cloudbrain.exception.BusinessException;
import com.neusoft.cloudbrain.repository.DoctorRepository;
import com.neusoft.cloudbrain.repository.MedicalRecordRepository;
import com.neusoft.cloudbrain.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    @Transactional
    public MedicalRecord createMedicalRecord(Long patientId, Long doctorId, Long registrationId,
            String chiefComplaint, String presentIllness, String pastHistory,
            String physicalExamination, String diagnosis, String treatmentPlan) {

        log.info("创建病历 - 患者ID: {}, 医生ID: {}", patientId, doctorId);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new BusinessException(404, "患者不存在"));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(404, "医生不存在"));

        MedicalRecord record = new MedicalRecord();
        record.setPatientId(patientId);
        record.setDoctorId(doctorId);
        record.setRegistrationId(registrationId);
        record.setChiefComplaint(chiefComplaint);
        record.setPresentIllness(presentIllness);
        record.setPastHistory(pastHistory);
        record.setPhysicalExamination(physicalExamination);
        record.setDiagnosis(diagnosis);
        record.setTreatmentPlan(treatmentPlan);

        MedicalRecord saved = medicalRecordRepository.save(record);
        log.info("病历创建成功 - ID: {}", saved.getId());

        return saved;
    }

    public Map<String, Object> generateMedicalRecordByAi(Long patientId, String dialogueText) {
        return generateMedicalRecordByAi(patientId, dialogueText, null, null);
    }

    public Map<String, Object> generateMedicalRecordByAi(Long patientId, String dialogueText, 
            String symptoms, String department) {
        log.info("AI生成病历 - 患者ID: {}, 对话文本长度: {}", patientId, dialogueText != null ? dialogueText.length() : 0);
        log.info("患者自述症状: {}, 科室: {}", symptoms, department);

        String aiResult = aiService.generateMedicalRecordWithSymptoms(dialogueText, symptoms, department);

        Map<String, Object> result = parseAiMedicalRecordResult(aiResult);
        result.put("patientId", patientId);

        return result;
    }

    public List<MedicalRecord> getPatientMedicalRecords(Long patientId) {
        return medicalRecordRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    public List<MedicalRecord> getDoctorMedicalRecords(Long doctorId) {
        return medicalRecordRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId);
    }

    public MedicalRecord getMedicalRecordByRegistration(Long registrationId) {
        List<MedicalRecord> records = medicalRecordRepository.findByRegistrationId(registrationId);
        if (records.isEmpty()) {
            return null;
        }
        return records.get(0);
    }

    public MedicalRecord getMedicalRecordDetail(Long id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "病历不存在"));
    }

    private Map<String, Object> parseAiMedicalRecordResult(String aiResult) {
        Map<String, Object> result = new HashMap<>();
        result.put("chiefComplaint", "");
        result.put("presentIllness", "");
        result.put("pastHistory", "");
        result.put("physicalExamination", "");
        result.put("diagnosis", "");
        result.put("treatmentPlan", "");
        result.put("aiRawResult", aiResult);

        if (aiResult == null || aiResult.isBlank()) {
            log.warn("AI返回结果为空");
            return result;
        }

        try {
            String jsonStr = aiResult.trim();
            if (jsonStr.startsWith("```json")) {
                jsonStr = jsonStr.substring(7);
            }
            if (jsonStr.startsWith("```")) {
                jsonStr = jsonStr.substring(3);
            }
            if (jsonStr.endsWith("```")) {
                jsonStr = jsonStr.substring(0, jsonStr.length() - 3);
            }
            jsonStr = jsonStr.trim();

            JsonNode root = objectMapper.readTree(jsonStr);

            if (root.has("chiefComplaint")) {
                result.put("chiefComplaint", root.path("chiefComplaint").asText(""));
            }
            if (root.has("presentIllness")) {
                result.put("presentIllness", root.path("presentIllness").asText(""));
            }
            if (root.has("pastHistory")) {
                result.put("pastHistory", root.path("pastHistory").asText(""));
            }
            if (root.has("physicalExamination")) {
                result.put("physicalExamination", root.path("physicalExamination").asText(""));
            }
            if (root.has("diagnosis")) {
                result.put("diagnosis", root.path("diagnosis").asText(""));
            }
            if (root.has("treatmentPlan")) {
                result.put("treatmentPlan", root.path("treatmentPlan").asText(""));
            }

            log.info("AI病历解析成功 - 诊断: {}", result.get("diagnosis"));
        } catch (Exception e) {
            log.error("解析AI病历结果失败: {}", e.getMessage());
            log.debug("原始AI结果: {}", aiResult);
        }

        return result;
    }

    public Map<String, Object> optimizeMedicalRecord(String chiefComplaint, String presentIllness,
            String pastHistory, String physicalExamination, String diagnosis,
            String treatmentPlan) {
        log.info("AI优化病历");

        String aiResult = aiService.optimizeMedicalRecord(
                chiefComplaint, presentIllness, pastHistory,
                physicalExamination, diagnosis, treatmentPlan);

        Map<String, Object> result = parseAiMedicalRecordResult(aiResult);

        try {
            String jsonStr = aiResult.trim();
            if (jsonStr.startsWith("```json")) {
                jsonStr = jsonStr.substring(7);
            }
            if (jsonStr.startsWith("```")) {
                jsonStr = jsonStr.substring(3);
            }
            if (jsonStr.endsWith("```")) {
                jsonStr = jsonStr.substring(0, jsonStr.length() - 3);
            }
            jsonStr = jsonStr.trim();

            JsonNode root = objectMapper.readTree(jsonStr);
            if (root.has("chiefComplaint")) {
                result.put("chiefComplaint", root.path("chiefComplaint").asText(""));
            }
        } catch (Exception e) {
            log.warn("解析chiefComplaint失败: {}", e.getMessage());
        }

        return result;
    }
}
