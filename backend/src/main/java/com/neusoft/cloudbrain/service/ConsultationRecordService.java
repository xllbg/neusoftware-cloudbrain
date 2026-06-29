package com.neusoft.cloudbrain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.cloudbrain.dto.ConsultationRecordDTO;
import com.neusoft.cloudbrain.dto.ConsultationRecordRequest;
import com.neusoft.cloudbrain.entity.ConsultationRecord;
import com.neusoft.cloudbrain.entity.Registration;
import com.neusoft.cloudbrain.exception.BusinessException;
import com.neusoft.cloudbrain.repository.ConsultationRecordRepository;
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
public class ConsultationRecordService {

    private final ConsultationRecordRepository consultationRecordRepository;
    private final RegistrationRepository registrationRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ConsultationRecordDTO saveConsultationRecord(ConsultationRecordRequest request) {
        log.info("保存问诊记录 - 挂号ID: {}, 患者ID: {}, 医生ID: {}",
                request.getRegistrationId(), request.getPatientId(), request.getDoctorId());

        // 查找是否已存在问诊记录
        ConsultationRecord record = consultationRecordRepository
                .findByRegistrationId(request.getRegistrationId())
                .orElse(null);

        if (record == null) {
            // 创建新记录
            record = new ConsultationRecord();
            record.setRegistrationId(request.getRegistrationId());
            record.setPatientId(request.getPatientId());
            record.setDoctorId(request.getDoctorId());
        }

        // 更新字段
        record.setPresentIllness(request.getPresentIllness());
        record.setPastHistory(request.getPastHistory());
        record.setPhysicalExamination(request.getPhysicalExamination());
        record.setDiagnosis(request.getDiagnosis());
        record.setChiefComplaint(request.getChiefComplaint());
        record.setTreatmentPlan(request.getTreatmentPlan());
        if (request.getAiRecommended() != null) {
            record.setAiRecommended(request.getAiRecommended());
        }

        ConsultationRecord saved = consultationRecordRepository.save(record);
        log.info("问诊记录保存成功 - ID: {}", saved.getId());

        return toDTO(saved);
    }

    public ConsultationRecordDTO getConsultationRecordByRegistrationId(Long registrationId) {
        log.info("获取问诊记录 - 挂号ID: {}", registrationId);

        return consultationRecordRepository.findByRegistrationId(registrationId)
                .map(this::toDTO)
                .orElse(null);
    }

    public ConsultationRecordDTO getConsultationRecord(Long id) {
        log.info("获取问诊记录 - ID: {}", id);

        return consultationRecordRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    public List<ConsultationRecordDTO> getConsultationRecordsByDoctorId(Long doctorId) {
        log.info("获取医生问诊记录列表 - 医生ID: {}", doctorId);

        List<ConsultationRecord> records = consultationRecordRepository.findByDoctorIdOrderByUpdatedAtDesc(doctorId);
        return records.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ConsultationRecordDTO recommendByAi(Long registrationId) {
        log.info("========== AI推荐问诊疗法开始 ==========");
        log.info("挂号ID: {}", registrationId);

        // 获取挂号信息
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new BusinessException(404, "挂号记录不存在"));

        String symptom = registration.getSymptom() != null ? registration.getSymptom() : "";
        String triageResult = registration.getTriageResult() != null ? registration.getTriageResult() : "";
        String department = registration.getDepartment() != null ? registration.getDepartment() : "";

        // 构建AI prompt
        String prompt = String.format(
                "你是医疗问诊助手。根据以下患者信息，生成问诊记录建议。\n\n" +
                "患者自述症状：%s\n" +
                "AI分诊结果：%s\n" +
                "科室：%s\n\n" +
                "请生成以下问诊字段的建议内容：\n" +
                "1. 现病史：根据症状描述发病经过\n" +
                "2. 既往史：根据症状推断可能的既往病史\n" +
                "3. 体格检查：根据症状建议相关检查项目\n" +
                "4. 初步诊断：根据症状给出可能的诊断\n" +
                "5. 主诉：简明扼要概括主要症状\n" +
                "6. 治疗意见：根据诊断给出治疗建议\n\n" +
                "请以JSON格式返回，包含字段：presentIllness, pastHistory, physicalExamination, diagnosis, chiefComplaint, treatmentPlan\n" +
                "只返回JSON，不要其他文字。",
                symptom, triageResult, department
        );

        try {
            String aiResponse = aiService.callDeepSeekApiForConsultation(prompt);
            String content = parseAiContent(aiResponse);

            log.info("AI返回内容: {}", content);

            // 解析JSON
            JsonNode root = objectMapper.readTree(content);

            // 构建建议对象
            ConsultationRecordDTO.RecommendationDTO recommendation = ConsultationRecordDTO.RecommendationDTO.builder()
                    .presentIllness(getJsonText(root, "presentIllness"))
                    .pastHistory(getJsonText(root, "pastHistory"))
                    .physicalExamination(getJsonText(root, "physicalExamination"))
                    .diagnosis(getJsonText(root, "diagnosis"))
                    .chiefComplaint(getJsonText(root, "chiefComplaint"))
                    .treatmentPlan(getJsonText(root, "treatmentPlan"))
                    .build();

            log.info("========== AI推荐问诊疗法结束 ==========");
            return ConsultationRecordDTO.builder()
                    .aiRecommended(true)
                    .recommendation(recommendation)
                    .build();

        } catch (Exception e) {
            log.error("AI推荐失败: {}", e.getMessage());
            throw new BusinessException(500, "AI推荐失败: " + e.getMessage());
        }
    }

    private String parseAiContent(String aiResponse) {
        try {
            JsonNode root = objectMapper.readTree(aiResponse);
            String content = root.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();

            if (content == null || content.isBlank()) {
                return aiResponse;
            }

            // 清理markdown代码块
            String cleaned = content.trim();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            } else if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            return cleaned.trim();
        } catch (Exception e) {
            log.warn("解析AI响应失败: {}", e.getMessage());
            return aiResponse;
        }
    }

    private String getJsonText(JsonNode root, String field) {
        JsonNode node = root.path(field);
        if (node.isMissingNode() || node.isNull()) {
            return "";
        }
        return node.asText();
    }

    private ConsultationRecordDTO toDTO(ConsultationRecord record) {
        return ConsultationRecordDTO.builder()
                .id(record.getId())
                .registrationId(record.getRegistrationId())
                .patientId(record.getPatientId())
                .doctorId(record.getDoctorId())
                .presentIllness(record.getPresentIllness())
                .pastHistory(record.getPastHistory())
                .physicalExamination(record.getPhysicalExamination())
                .diagnosis(record.getDiagnosis())
                .chiefComplaint(record.getChiefComplaint())
                .treatmentPlan(record.getTreatmentPlan())
                .aiRecommended(record.getAiRecommended())
                .createdAt(record.getCreatedAt() != null ? record.getCreatedAt().toString() : null)
                .build();
    }
}
