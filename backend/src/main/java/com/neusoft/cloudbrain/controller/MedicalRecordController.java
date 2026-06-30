package com.neusoft.cloudbrain.controller;

import com.neusoft.cloudbrain.dto.CommonResult;
import com.neusoft.cloudbrain.dto.MedicalRecordRequest;
import com.neusoft.cloudbrain.dto.MedicalRecordResponse;
import com.neusoft.cloudbrain.entity.Doctor;
import com.neusoft.cloudbrain.entity.MedicalRecord;
import com.neusoft.cloudbrain.entity.Patient;
import com.neusoft.cloudbrain.repository.DoctorRepository;
import com.neusoft.cloudbrain.repository.PatientRepository;
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
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    @PostMapping("/generate")
    @Operation(summary = "AI生成病历", description = "根据对话文本AI生成结构化病历")
    public CommonResult<MedicalRecordResponse.AiMedicalRecordResult> generate(
            @RequestParam Long patientId,
            @RequestBody Map<String, String> request) {
        String dialogueText = request.get("dialogueText");
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

    @PostMapping("/optimize")
    @Operation(summary = "AI优化病历", description = "根据问诊记录AI优化生成规范病历")
    public CommonResult<MedicalRecordResponse.AiMedicalRecordResult> optimize(
            @RequestBody Map<String, String> request) {
        String chiefComplaint = request.getOrDefault("chiefComplaint", "");
        String presentIllness = request.getOrDefault("presentIllness", "");
        String pastHistory = request.getOrDefault("pastHistory", "");
        String physicalExamination = request.getOrDefault("physicalExamination", "");
        String diagnosis = request.getOrDefault("diagnosis", "");
        String treatmentPlan = request.getOrDefault("treatmentPlan", "");

        Map<String, Object> result = medicalRecordService.optimizeMedicalRecord(
                chiefComplaint, presentIllness, pastHistory,
                physicalExamination, diagnosis, treatmentPlan);

        MedicalRecordResponse.AiMedicalRecordResult aiResult = MedicalRecordResponse.AiMedicalRecordResult.builder()
                .chiefComplaint((String) result.getOrDefault("chiefComplaint", ""))
                .presentIllness((String) result.get("presentIllness"))
                .pastHistory((String) result.get("pastHistory"))
                .physicalExamination((String) result.get("physicalExamination"))
                .diagnosis((String) result.get("diagnosis"))
                .treatmentPlan((String) result.get("treatmentPlan"))
                .aiRawResult((String) result.get("aiRawResult"))
                .build();

        return CommonResult.success(aiResult);
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
    @Operation(summary = "病历列表", description = "获取患者或医生的病历列表")
    public CommonResult<List<MedicalRecordResponse>> list(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId) {
        List<MedicalRecord> records;
        if (doctorId != null) {
            records = medicalRecordService.getDoctorMedicalRecords(doctorId);
        } else if (patientId != null) {
            records = medicalRecordService.getPatientMedicalRecords(patientId);
        } else {
            records = List.of();
        }
        List<MedicalRecordResponse> responses = records.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return CommonResult.success(responses);
    }

    @GetMapping("/by-registration/{registrationId}")
    @Operation(summary = "根据挂号ID获取病历", description = "根据挂号ID获取已保存的病历")
    public CommonResult<MedicalRecordResponse> getByRegistration(@PathVariable Long registrationId) {
        MedicalRecord record = medicalRecordService.getMedicalRecordByRegistration(registrationId);
        if (record == null) {
            return CommonResult.success(null);
        }
        return CommonResult.success(toResponse(record));
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "病历详情", description = "获取病历详情")
    public CommonResult<MedicalRecordResponse> detail(@PathVariable Long id) {
        MedicalRecord record = medicalRecordService.getMedicalRecordDetail(id);
        return CommonResult.success(toResponse(record));
    }

    private MedicalRecordResponse toResponse(MedicalRecord record) {
        String patientName = patientRepository.findById(record.getPatientId())
                .map(Patient::getName).orElse("未知患者");
        String doctorName = doctorRepository.findById(record.getDoctorId())
                .map(Doctor::getName).orElse("未知医生");

        return MedicalRecordResponse.builder()
                .id(record.getId())
                .patientId(record.getPatientId())
                .patientName(patientName)
                .doctorId(record.getDoctorId())
                .doctorName(doctorName)
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
