package com.neusoft.cloudbrain.controller;

import com.neusoft.cloudbrain.dto.*;
import com.neusoft.cloudbrain.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
@Tag(name = "医生管理")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping("/list")
    @Operation(summary = "医生列表（仅已批准）")
    public CommonResult<List<DoctorVO>> list(
            @Parameter(description = "科室名称") @RequestParam(required = false) String department) {
        return CommonResult.success(doctorService.listDoctors(department));
    }

    @GetMapping("/detail")
    @Operation(summary = "医生详情")
    public CommonResult<DoctorVO> detail(@Parameter(description = "医生ID") @RequestParam Long id) {
        return CommonResult.success(doctorService.getDoctorDetail(id));
    }

    @PostMapping("/login")
    @Operation(summary = "医生登录（用户名+密码）")
    public CommonResult<LoginResponse> login(@Valid @RequestBody DoctorLoginRequest request) {
        return CommonResult.success(doctorService.login(request));
    }

    @PostMapping("/login/phone")
    @Operation(summary = "医生登录（姓名+手机号+密码）")
    public CommonResult<LoginResponse> loginByPhone(@Valid @RequestBody PhoneLoginRequest request) {
        return CommonResult.success(doctorService.loginByPhone(request));
    }

    @PostMapping("/register")
    @Operation(summary = "医生注册申请")
    public CommonResult<String> register(@Valid @RequestBody DoctorRegisterRequest request) {
        return CommonResult.success(doctorService.register(request));
    }

    @GetMapping("/status")
    @Operation(summary = "查询医生审核状态（姓名+手机号）")
    public CommonResult<DoctorStatusVO> getStatus(
            @RequestParam String name,
            @RequestParam String phone) {
        return CommonResult.success(doctorService.getDoctorStatus(name, phone));
    }

    @GetMapping("/pending")
    @Operation(summary = "获取待审批医生列表（管理员）")
    public CommonResult<List<DoctorStatusVO>> pendingList() {
        return CommonResult.success(doctorService.getPendingDoctors());
    }

    @GetMapping("/all-with-status")
    @Operation(summary = "获取所有医生及审核状态（管理员）")
    public CommonResult<List<DoctorStatusVO>> allWithStatus() {
        return CommonResult.success(doctorService.getAllDoctorsWithStatus());
    }

    @PostMapping("/approve")
    @Operation(summary = "批准医生注册")
    public CommonResult<String> approve(@RequestBody Map<String, Long> body) {
        doctorService.approveDoctor(body.get("id"));
        return CommonResult.success("已批准");
    }

    @PostMapping("/reject")
    @Operation(summary = "拒绝医生注册")
    public CommonResult<String> reject(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        String reason = (String) body.getOrDefault("reason", "");
        doctorService.rejectDoctor(id, reason);
        return CommonResult.success("已拒绝");
    }
}
