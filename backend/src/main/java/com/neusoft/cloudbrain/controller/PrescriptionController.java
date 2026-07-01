package com.neusoft.cloudbrain.controller;

import com.neusoft.cloudbrain.dto.CommonResult;
import com.neusoft.cloudbrain.dto.PrescriptionRequest;
import com.neusoft.cloudbrain.dto.PrescriptionResponse;
import com.neusoft.cloudbrain.entity.Doctor;
import com.neusoft.cloudbrain.entity.Patient;
import com.neusoft.cloudbrain.entity.Prescription;
import com.neusoft.cloudbrain.entity.PrescriptionCheck;
import com.neusoft.cloudbrain.repository.DoctorRepository;
import com.neusoft.cloudbrain.repository.PatientRepository;
import com.neusoft.cloudbrain.service.PrescriptionService;
import com.neusoft.cloudbrain.service.AiService;
import com.neusoft.cloudbrain.service.ConsultationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/prescription")
@RequiredArgsConstructor
@Tag(name = "处方管理")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final AiService aiService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ConsultationService consultationService;
    private final ObjectMapper objectMapper;

    @PostMapping("/create")
    @Operation(summary = "创建处方", description = "医生开具处方")
    public CommonResult<PrescriptionResponse> create(@Valid @RequestBody PrescriptionRequest request) {
        Prescription prescription = prescriptionService.createPrescription(
                request.getPatientId(),
                request.getDoctorId(),
                request.getRegistrationId(),
                request.getMedicineList(),
                request.getDosage(),
                request.getUsage()
        );
        return CommonResult.success("处方创建成功", toResponse(prescription));
    }

    @PostMapping("/check/{id}")
    @Operation(summary = "AI审核处方", description = "AI审核处方用药安全")
    public CommonResult<PrescriptionResponse.AiCheckResult> check(@PathVariable Long id) {
        Map<String, Object> result = prescriptionService.checkPrescriptionByAi(id);

        PrescriptionResponse.AiCheckResult aiResult = PrescriptionResponse.AiCheckResult.builder()
                .checkResult((String) result.get("checkResult"))
                .riskLevel((String) result.getOrDefault("riskLevel", "medium"))
                .medicationSuggestions((String) result.get("medicationSuggestions"))
                .interactionDetection((String) result.get("interactionDetection"))
                .riskHints((String) result.get("riskHints"))
                .build();

        return CommonResult.success(aiResult);
    }

    @PostMapping("/ai-check")
    @Operation(summary = "AI审核处方（预览）", description = "直接AI审核处方内容，不保存处方")
    public CommonResult<PrescriptionResponse.AiCheckResult> aiCheck(
            @RequestParam String medicineText,
            @RequestParam String patientInfo) {
        String result = aiService.checkPrescription(medicineText, patientInfo);
        return parseAiCheckResult(result);
    }

    @PostMapping("/save-check")
    @Operation(summary = "保存处方审核结果", description = "保存AI审核结果到审核记录表")
    public CommonResult<Void> saveCheckResult(@RequestBody Map<String, String> request) {
        prescriptionService.saveCheckResult(
                Long.parseLong(request.get("prescriptionId")),
                request.get("checkResult"),
                request.get("medicationSuggestions"),
                request.get("interactionDetection"),
                request.get("riskLevel"),
                request.get("riskHints")
        );
        return CommonResult.success((Void) null);
    }

    @GetMapping("/check-result")
    @Operation(summary = "获取处方审核结果", description = "获取指定处方的AI审核结果")
    public CommonResult<PrescriptionCheck> getCheckResult(@RequestParam Long prescriptionId) {
        PrescriptionCheck check = prescriptionService.getCheckResult(prescriptionId);
        if (check == null) {
            return CommonResult.success(null);
        }
        return CommonResult.success(check);
    }

    private CommonResult<PrescriptionResponse.AiCheckResult> parseAiCheckResult(String aiResponse) {
        PrescriptionResponse.AiCheckResult result = new PrescriptionResponse.AiCheckResult();

        try {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(aiResponse);
            String content = root.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();

            if (content != null && !content.isBlank()) {
                String cleaned = content.trim();
                if (cleaned.startsWith("```json")) {
                    cleaned = cleaned.substring(7);
                }
                if (cleaned.startsWith("```")) {
                    cleaned = cleaned.substring(3);
                }
                if (cleaned.endsWith("```")) {
                    cleaned = cleaned.substring(0, cleaned.length() - 3);
                }
                cleaned = cleaned.trim();

                com.fasterxml.jackson.databind.JsonNode jsonResult = objectMapper.readTree(cleaned);

                result.setCheckResult(getJsonText(jsonResult, "checkResult", "需要审核"));
                result.setRiskLevel(getJsonText(jsonResult, "riskLevel", "中风险"));
                result.setMedicationSuggestions(getJsonText(jsonResult, "medicationSuggestions", ""));
                result.setInteractionDetection(getJsonText(jsonResult, "interactionDetection", ""));
                result.setRiskHints(getJsonText(jsonResult, "riskHints", ""));
            } else {
                result.setCheckResult("审核服务异常");
                result.setRiskLevel("未知");
                result.setMedicationSuggestions("");
                result.setInteractionDetection("");
                result.setRiskHints("AI服务暂时无法返回审核结果");
            }
        } catch (Exception e) {
            result.setCheckResult("审核解析失败");
            result.setRiskLevel("未知");
            result.setMedicationSuggestions("");
            result.setInteractionDetection("");
            result.setRiskHints("审核结果解析失败: " + e.getMessage());
        }

        return CommonResult.success(result);
    }

    private String getJsonText(com.fasterxml.jackson.databind.JsonNode node, String field, String defaultValue) {
        com.fasterxml.jackson.databind.JsonNode fieldNode = node.path(field);
        if (fieldNode.isMissingNode() || fieldNode.isNull()) {
            return defaultValue != null ? defaultValue : "";
        }
        String text = fieldNode.asText();
        return text != null && !text.isBlank() ? text : defaultValue != null ? defaultValue : "";
    }

    @GetMapping("/list")
    @Operation(summary = "处方列表", description = "获取患者或医生的处方列表")
    public CommonResult<List<PrescriptionResponse>> list(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId) {
        List<Prescription> prescriptions;
        if (doctorId != null) {
            prescriptions = prescriptionService.getDoctorPrescriptions(doctorId);
        } else if (patientId != null) {
            prescriptions = prescriptionService.getPatientPrescriptions(patientId);
        } else {
            prescriptions = List.of();
        }
        List<PrescriptionResponse> responses = prescriptions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return CommonResult.success(responses);
    }

    @GetMapping("/detail")
    @Operation(summary = "处方详情", description = "获取处方详情")
    public CommonResult<PrescriptionResponse> detail(@RequestParam Long id) {
        Prescription prescription = prescriptionService.getPrescriptionDetail(id);
        return CommonResult.success(toResponse(prescription));
    }

    @GetMapping("/by-registration/{registrationId}")
    @Operation(summary = "根据挂号ID获取处方", description = "根据挂号ID获取处方")
    public CommonResult<PrescriptionResponse> getByRegistration(@PathVariable Long registrationId) {
        Prescription prescription = prescriptionService.getPrescriptionByRegistration(registrationId);
        if (prescription == null) {
            return CommonResult.success(null);
        }
        return CommonResult.success(toResponse(prescription));
    }

    @PostMapping("/recommend")
    @Operation(summary = "AI推荐药品", description = "根据症状、诊断和对话记录推荐药品")
    public CommonResult<List<Map<String, String>>> recommendMedicine(
            @RequestParam(required = false) String symptoms,
            @RequestParam(required = false) String diagnosis,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Long registrationId) {
        
        String dialogueText = null;
        if (registrationId != null) {
            List<com.neusoft.cloudbrain.dto.ConsultationMessageDTO> messages = 
                    consultationService.getMessageList(registrationId);
            if (messages != null && !messages.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (com.neusoft.cloudbrain.dto.ConsultationMessageDTO msg : messages) {
                    String role = "DOCTOR".equals(msg.getSenderType()) ? "医生" : "患者";
                    sb.append(role).append("：").append(msg.getContent()).append("\n");
                }
                dialogueText = sb.toString();
            }
        }

        String result = aiService.recommendMedicineWithDialogue(
                symptoms != null ? symptoms : "",
                diagnosis != null ? diagnosis : "",
                department != null ? department : "",
                dialogueText
        );

        try {
            List<Map<String, String>> medicines = objectMapper.readValue(
                    result, new TypeReference<List<Map<String, String>>>() {}
            );
            return CommonResult.success(medicines);
        } catch (Exception e) {
            return CommonResult.success(List.of());
        }
    }

    private PrescriptionResponse toResponse(Prescription prescription) {
        String patientName = patientRepository.findById(prescription.getPatientId())
                .map(Patient::getName).orElse("未知患者");
        String doctorName = doctorRepository.findById(prescription.getDoctorId())
                .map(Doctor::getName).orElse("未知医生");

        return PrescriptionResponse.builder()
                .id(prescription.getId())
                .patientId(prescription.getPatientId())
                .patientName(patientName)
                .doctorId(prescription.getDoctorId())
                .doctorName(doctorName)
                .registrationId(prescription.getRegistrationId())
                .medicineList(prescription.getMedicineList())
                .dosage(prescription.getDosage())
                .usage(prescription.getUsage())
                .status(prescription.getStatus())
                .createdAt(prescription.getCreatedAt())
                .build();
    }
}
