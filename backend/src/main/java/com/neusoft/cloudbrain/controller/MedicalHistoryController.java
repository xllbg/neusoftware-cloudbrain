package com.neusoft.cloudbrain.controller;

import com.neusoft.cloudbrain.dto.CommonResult;
import com.neusoft.cloudbrain.dto.MedicalHistoryResponse;
import com.neusoft.cloudbrain.service.MedicalHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medical-history")
@RequiredArgsConstructor
@Tag(name = "历史就诊记录")
public class MedicalHistoryController {

    private final MedicalHistoryService medicalHistoryService;

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "获取患者历史就诊记录列表", description = "获取指定患者的所有历史就诊记录，包含挂号、病历、处方信息")
    public CommonResult<List<MedicalHistoryResponse>> getPatientMedicalHistory(@PathVariable Long patientId) {
        List<MedicalHistoryResponse> result = medicalHistoryService.getPatientMedicalHistory(patientId);
        return CommonResult.success(result);
    }

    @GetMapping("/patient/{patientId}/detail/{registrationId}")
    @Operation(summary = "获取就诊详情", description = "获取指定患者的某次就诊详细信息，包含挂号、病历、处方")
    public CommonResult<MedicalHistoryResponse> getMedicalHistoryDetail(
            @PathVariable Long patientId,
            @PathVariable Long registrationId) {
        MedicalHistoryResponse result = medicalHistoryService.getMedicalHistoryDetail(patientId, registrationId);
        return CommonResult.success(result);
    }
}
