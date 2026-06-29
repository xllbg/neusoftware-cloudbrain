package com.neusoft.cloudbrain.controller;

import com.neusoft.cloudbrain.dto.CommonResult;
import com.neusoft.cloudbrain.dto.ConsultationRecordDTO;
import com.neusoft.cloudbrain.dto.ConsultationRecordRequest;
import com.neusoft.cloudbrain.service.ConsultationRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultation-record")
@RequiredArgsConstructor
@Tag(name = "问诊记录")
public class ConsultationRecordController {

    private final ConsultationRecordService consultationRecordService;

    @PostMapping("/save")
    @Operation(summary = "保存问诊记录", description = "保存或更新问诊记录")
    public CommonResult<ConsultationRecordDTO> save(@Valid @RequestBody ConsultationRecordRequest request) {
        ConsultationRecordDTO result = consultationRecordService.saveConsultationRecord(request);
        return CommonResult.success("问诊记录保存成功", result);
    }

    @GetMapping("/get")
    @Operation(summary = "获取问诊记录", description = "通过挂号ID获取问诊记录")
    public CommonResult<ConsultationRecordDTO> getByRegistrationId(@RequestParam Long registrationId) {
        ConsultationRecordDTO result = consultationRecordService.getConsultationRecordByRegistrationId(registrationId);
        if (result == null) {
            return CommonResult.success("暂无问诊记录", null);
        }
        return CommonResult.success(result);
    }

    @GetMapping("/list")
    @Operation(summary = "获取医生问诊记录列表", description = "获取指定医生的所有问诊记录")
    public CommonResult<List<ConsultationRecordDTO>> listByDoctorId(@RequestParam Long doctorId) {
        List<ConsultationRecordDTO> result = consultationRecordService.getConsultationRecordsByDoctorId(doctorId);
        return CommonResult.success(result);
    }

    @GetMapping("/recommend")
    @Operation(summary = "AI推荐问诊疗法", description = "根据患者症状AI生成问诊建议")
    public CommonResult<ConsultationRecordDTO.RecommendationDTO> recommendByAi(@RequestParam Long registrationId) {
        ConsultationRecordDTO result = consultationRecordService.recommendByAi(registrationId);
        if (result == null || result.getRecommendation() == null) {
            return CommonResult.fail("AI推荐失败");
        }
        return CommonResult.success("AI推荐成功", result.getRecommendation());
    }
}
