package com.neusoft.cloudbrain.controller;

import com.neusoft.cloudbrain.dto.CommonResult;
import com.neusoft.cloudbrain.dto.LoginResponse;
import com.neusoft.cloudbrain.dto.PatientLoginRequest;
import com.neusoft.cloudbrain.dto.PatientRegisterRequest;
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
        LoginResponse response = patientService.register(request);
        return CommonResult.success(response);
    }

    @PostMapping("/login")
    @Operation(summary = "患者登录")
    public CommonResult<LoginResponse> login(@Valid @RequestBody PatientLoginRequest request) {
        LoginResponse response = patientService.login(request);
        return CommonResult.success(response);
    }
}
