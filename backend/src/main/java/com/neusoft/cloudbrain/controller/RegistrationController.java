package com.neusoft.cloudbrain.controller;

import com.neusoft.cloudbrain.dto.RegistrationRequest;
import com.neusoft.cloudbrain.dto.RegistrationResponse;
import com.neusoft.cloudbrain.dto.Result;
import com.neusoft.cloudbrain.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
@Tag(name = "挂号管理")
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/create")
    @Operation(summary = "创建挂号", description = "患者选择医生和时间段进行挂号")
    public Result<RegistrationResponse> create(@Valid @RequestBody RegistrationRequest request) {
        RegistrationResponse response = registrationService.createRegistration(request);
        return Result.success("挂号成功", response);
    }

    @GetMapping("/list")
    @Operation(summary = "挂号列表", description = "获取患者的挂号记录列表")
    public Result<List<RegistrationResponse.RegistrationListItem>> list(@RequestParam Long patientId) {
        List<RegistrationResponse.RegistrationListItem> list = registrationService.getPatientRegistrations(patientId);
        return Result.success(list);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消挂号", description = "患者取消自己的挂号记录")
    public Result<RegistrationResponse> cancel(@RequestParam Long registrationId, @RequestParam Long patientId) {
        RegistrationResponse response = registrationService.cancelRegistration(registrationId, patientId);
        return Result.success("取消成功", response);
    }
}
