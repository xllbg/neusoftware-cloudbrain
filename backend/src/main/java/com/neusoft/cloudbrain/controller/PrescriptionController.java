package com.neusoft.cloudbrain.controller;

import com.neusoft.cloudbrain.dto.CommonResult;
import com.neusoft.cloudbrain.dto.PrescriptionRequest;
import com.neusoft.cloudbrain.dto.PrescriptionResponse;
import com.neusoft.cloudbrain.entity.Doctor;
import com.neusoft.cloudbrain.entity.Patient;
import com.neusoft.cloudbrain.entity.Prescription;
import com.neusoft.cloudbrain.repository.DoctorRepository;
import com.neusoft.cloudbrain.repository.PatientRepository;
import com.neusoft.cloudbrain.service.PrescriptionService;
import com.neusoft.cloudbrain.service.AiService;
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

    @PostMapping("/recommend")
    @Operation(summary = "AI推荐药品", description = "根据症状和诊断推荐药品")
    public CommonResult<List<Map<String, String>>> recommendMedicine(
            @RequestParam(required = false) String symptoms,
            @RequestParam(required = false) String diagnosis,
            @RequestParam(required = false) String department) {
        String result = aiService.recommendMedicine(
                symptoms != null ? symptoms : "",
                diagnosis != null ? diagnosis : "",
                department != null ? department : ""
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
