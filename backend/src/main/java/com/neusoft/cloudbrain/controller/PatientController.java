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
    public Result<LoginResponse> register(@Valid @RequestBody PatientRegisterRequest request) {
        LoginResponse response = patientService.register(request);
        return Result.success(response);
    }

    @PostMapping("/login")
    @Operation(summary = "患者登录")
    public Result<LoginResponse> login(@Valid @RequestBody PatientLoginRequest request) {
        LoginResponse response = patientService.login(request);
        return Result.success(response);
    }
}
