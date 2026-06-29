package com.neusoft.cloudbrain.controller;

import com.neusoft.cloudbrain.dto.*;
import com.neusoft.cloudbrain.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
@Tag(name = "患者管理")
public class PatientController {

    private final PatientService patientService;

    @PostMapping("/register")
    @Operation(summary = "患者注册")
    public CommonResult<LoginResponse> register(@Valid @RequestBody PatientRegisterRequest request) {
        return CommonResult.success(patientService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "患者登录（用户名+密码）")
    public CommonResult<LoginResponse> login(@Valid @RequestBody PatientLoginRequest request) {
        return CommonResult.success(patientService.login(request));
    }

    @PostMapping("/login/phone")
    @Operation(summary = "患者登录（姓名+手机号+密码）")
    public CommonResult<LoginResponse> loginByPhone(@Valid @RequestBody PhoneLoginRequest request) {
        return CommonResult.success(patientService.loginByPhone(request));
    }
}
