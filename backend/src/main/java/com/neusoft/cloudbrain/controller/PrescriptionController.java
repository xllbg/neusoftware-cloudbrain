package com.neusoft.cloudbrain.controller;

import com.neusoft.cloudbrain.dto.CommonResult;
import com.neusoft.cloudbrain.dto.PrescriptionRequest;
import com.neusoft.cloudbrain.dto.PrescriptionResponse;
import com.neusoft.cloudbrain.entity.Prescription;
import com.neusoft.cloudbrain.service.PrescriptionService;
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

    @PostMapping("/check")
    @Operation(summary = "AI审核处方", description = "AI审核处方用药安全")
    public CommonResult<PrescriptionResponse.AiCheckResult> check(@RequestParam Long prescriptionId) {
        Map<String, Object> result = prescriptionService.checkPrescriptionByAi(prescriptionId);

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
    @Operation(summary = "处方列表", description = "获取患者的处方列表")
    public CommonResult<List<PrescriptionResponse>> list(@RequestParam Long patientId) {
        List<Prescription> prescriptions = prescriptionService.getPatientPrescriptions(patientId);
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

    private PrescriptionResponse toResponse(Prescription prescription) {
        return PrescriptionResponse.builder()
                .id(prescription.getId())
                .patientId(prescription.getPatientId())
                .doctorId(prescription.getDoctorId())
                .registrationId(prescription.getRegistrationId())
                .medicineList(prescription.getMedicineList())
                .dosage(prescription.getDosage())
                .usage(prescription.getUsage())
                .status(prescription.getStatus())
                .createdAt(prescription.getCreatedAt())
                .build();
    }
}
