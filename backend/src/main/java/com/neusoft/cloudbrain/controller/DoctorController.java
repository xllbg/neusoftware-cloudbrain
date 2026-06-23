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
    public Result<List<DoctorVO>> list(
            @Parameter(description = "科室名称") @RequestParam(required = false) String department) {
        return Result.success(doctorService.listDoctors(department));
    }

    @GetMapping("/detail")
    @Operation(summary = "医生详情")
    public Result<DoctorVO> detail(@Parameter(description = "医生ID") @RequestParam Long id) {
        return Result.success(doctorService.getDoctorDetail(id));
    }

    @PostMapping("/login")
    @Operation(summary = "医生登录（姓名+手机号+密码）")
    public Result<LoginResponse> login(@Valid @RequestBody DoctorLoginRequest request) {
        return Result.success(doctorService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "医生注册申请")
    public Result<String> register(@Valid @RequestBody DoctorRegisterRequest request) {
        String msg = doctorService.register(request);
        return Result.success(msg);
    }

    @GetMapping("/status")
    @Operation(summary = "查询医生审核状态（姓名+手机号）")
    public Result<DoctorStatusVO> getStatus(
            @RequestParam String name,
            @RequestParam String phone) {
        return Result.success(doctorService.getDoctorStatus(name, phone));
    }

    // ===== 管理员接口 =====

    @GetMapping("/pending")
    @Operation(summary = "获取待审批医生列表（管理员）")
    public Result<List<DoctorStatusVO>> pendingList() {
        return Result.success(doctorService.getPendingDoctors());
    }

    @GetMapping("/all-with-status")
    @Operation(summary = "获取所有医生及审核状态（管理员）")
    public Result<List<DoctorStatusVO>> allWithStatus() {
        return Result.success(doctorService.getAllDoctorsWithStatus());
    }

    @PostMapping("/approve")
    @Operation(summary = "批准医生注册")
    public Result<String> approve(@RequestBody Map<String, Long> body) {
        doctorService.approveDoctor(body.get("id"));
        return Result.success("已批准");
    }

    @PostMapping("/reject")
    @Operation(summary = "拒绝医生注册")
    public Result<String> reject(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        String reason = (String) body.getOrDefault("reason", "");
        doctorService.rejectDoctor(id, reason);
        return Result.success("已拒绝");
    }
}
