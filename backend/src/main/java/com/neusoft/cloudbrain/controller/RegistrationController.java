package com.neusoft.cloudbrain.controller;

import com.neusoft.cloudbrain.dto.CommonResult;
import com.neusoft.cloudbrain.dto.RegistrationRequest;
import com.neusoft.cloudbrain.dto.RegistrationResponse;
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
    public CommonResult<RegistrationResponse> create(@Valid @RequestBody RegistrationRequest request) {
        RegistrationResponse response = registrationService.createRegistration(request);
        return CommonResult.success("挂号成功", response);
    }

    @GetMapping("/list")
    @Operation(summary = "挂号列表", description = "获取患者或医生的挂号记录列表")
    public CommonResult<List<RegistrationResponse.RegistrationListItem>> list(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String date) {
        List<RegistrationResponse.RegistrationListItem> list;
        if (doctorId != null) {
            list = registrationService.getDoctorRegistrations(doctorId, status, department, date);
        } else if (patientId != null) {
            list = registrationService.getPatientRegistrations(patientId);
        } else {
            list = List.of();
        }
        return CommonResult.success(list);
    }

    @PostMapping("/cancel/{id}")
    @Operation(summary = "取消挂号", description = "患者取消自己的挂号记录")
    public CommonResult<RegistrationResponse> cancel(@PathVariable Long id, @RequestParam Long patientId) {
        RegistrationResponse response = registrationService.cancelRegistration(id, patientId);
        return CommonResult.success("取消成功", response);
    }

    @PostMapping("/start/{id}")
    @Operation(summary = "开始问诊", description = "医生开始接诊，更新挂号状态为接诊中")
    public CommonResult<RegistrationResponse.RegistrationListItem> startConsultation(
            @PathVariable Long id,
            @RequestParam Long doctorId) {
        RegistrationResponse.RegistrationListItem response = registrationService.startConsultation(id, doctorId);
        return CommonResult.success("接诊成功", response);
    }

    @PostMapping("/complete/{id}")
    @Operation(summary = "完成问诊", description = "医生完成问诊，更新挂号状态为已完成")
    public CommonResult<RegistrationResponse.RegistrationListItem> completeConsultation(
            @PathVariable Long id,
            @RequestParam Long doctorId) {
        RegistrationResponse.RegistrationListItem response = registrationService.completeConsultation(id, doctorId);
        return CommonResult.success("问诊完成", response);
    }
}
