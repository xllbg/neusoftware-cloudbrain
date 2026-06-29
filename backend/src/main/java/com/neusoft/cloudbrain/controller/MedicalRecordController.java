package com.neusoft.cloudbrain.controller;

import com.neusoft.cloudbrain.dto.AiMedicalRecordGenerateRequest;
import com.neusoft.cloudbrain.dto.CommonResult;
import com.neusoft.cloudbrain.dto.MedicalRecordRequest;
import com.neusoft.cloudbrain.dto.MedicalRecordResponse;
import com.neusoft.cloudbrain.entity.MedicalRecord;
import com.neusoft.cloudbrain.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/medical-record")
@RequiredArgsConstructor
@Tag(name = "病历管理")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping("/generate")
    @Operation(summary = "AI生成病历", description = "根据主诉、现病史、既往史AI生成结构化病历")
    public CommonResult<MedicalRecordResponse.AiMedicalRecordResult> generate(
            @RequestParam Long patientId,
            @RequestBody AiMedicalRecordGenerateRequest request) {
        String dialogueText = buildDialogueText(request);
        Map<String, Object> result = medicalRecordService.generateMedicalRecordByAi(patientId, dialogueText);

        MedicalRecordResponse.AiMedicalRecordResult aiResult = MedicalRecordResponse.AiMedicalRecordResult.builder()
                .presentIllness((String) result.get("presentIllness"))
                .pastHistory((String) result.get("pastHistory"))
                .physicalExamination((String) result.get("physicalExamination"))
                .diagnosis((String) result.get("diagnosis"))
                .treatmentPlan((String) result.get("treatmentPlan"))
                .aiRawResult((String) result.get("aiRawResult"))
                .build();

        return CommonResult.success(aiResult);
    }

    private String buildDialogueText(AiMedicalRecordGenerateRequest request) {
        StringBuilder sb = new StringBuilder();
        if (request.getChiefComplaint() != null && !request.getChiefComplaint().isBlank()) {
            sb.append("主诉: ").append(request.getChiefComplaint()).append("\n");
        }
        if (request.getPresentIllness() != null && !request.getPresentIllness().isBlank()) {
            sb.append("现病史: ").append(request.getPresentIllness()).append("\n");
        }
        if (request.getPastHistory() != null && !request.getPastHistory().isBlank()) {
            sb.append("既往史: ").append(request.getPastHistory()).append("\n");
        }
        return sb.toString();
    }

    @PostMapping("/save")
    @Operation(summary = "保存病历", description = "保存（编辑后的）病历")
    public CommonResult<MedicalRecord> save(@Valid @RequestBody MedicalRecordRequest request) {
        MedicalRecord record = medicalRecordService.createMedicalRecord(
                request.getPatientId(),
                request.getDoctorId(),
                request.getRegistrationId(),
                request.getChiefComplaint(),
                request.getPresentIllness(),
                request.getPastHistory(),
                request.getPhysicalExamination(),
                request.getDiagnosis(),
                request.getTreatmentPlan()
        );
        return CommonResult.success("病历保存成功", record);
    }

    @GetMapping("/list")
    @Operation(summary = "病历列表", description = "获取患者的病历列表")
    public CommonResult<List<MedicalRecordResponse>> list(@RequestParam Long patientId) {
        List<MedicalRecord> records = medicalRecordService.getPatientMedicalRecords(patientId);
        List<MedicalRecordResponse> responses = records.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return CommonResult.success(responses);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "病历详情", description = "获取病历详情")
    public CommonResult<MedicalRecordResponse> detail(@PathVariable Long id) {
        MedicalRecord record = medicalRecordService.getMedicalRecordDetail(id);
        return CommonResult.success(toResponse(record));
    }

    private MedicalRecordResponse toResponse(MedicalRecord record) {
        return MedicalRecordResponse.builder()
                .id(record.getId())
                .patientId(record.getPatientId())
                .doctorId(record.getDoctorId())
                .registrationId(record.getRegistrationId())
                .chiefComplaint(record.getChiefComplaint())
                .presentIllness(record.getPresentIllness())
                .pastHistory(record.getPastHistory())
                .physicalExamination(record.getPhysicalExamination())
                .diagnosis(record.getDiagnosis())
                .treatmentPlan(record.getTreatmentPlan())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
