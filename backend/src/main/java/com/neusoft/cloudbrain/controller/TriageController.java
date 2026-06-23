package com.neusoft.cloudbrain.controller;

import com.neusoft.cloudbrain.dto.CommonResult;
import com.neusoft.cloudbrain.dto.TriageRequest;
import com.neusoft.cloudbrain.dto.TriageResponse;
import com.neusoft.cloudbrain.service.TriageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/triage")
@RequiredArgsConstructor
@Tag(name = "AI分诊")
public class TriageController {

    private final TriageService triageService;

    @PostMapping("/consult")
    @Operation(summary = "AI智能分诊", description = "根据患者症状智能推荐科室和医生")
    public CommonResult<TriageResponse> consult(@Valid @RequestBody TriageRequest request) {
        TriageResponse response = triageService.triage(request);
        return CommonResult.success(response);
    }
}
