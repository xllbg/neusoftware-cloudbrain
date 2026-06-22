package com.neusoft.cloudbrain.controller;

import com.neusoft.cloudbrain.dto.DoctorLoginRequest;
import com.neusoft.cloudbrain.dto.DoctorVO;
import com.neusoft.cloudbrain.dto.LoginResponse;
import com.neusoft.cloudbrain.dto.Result;
import com.neusoft.cloudbrain.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
@Tag(name = "医生管理")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping("/list")
    @Operation(summary = "医生列表")
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
    @Operation(summary = "医生登录")
    public Result<LoginResponse> login(@Valid @RequestBody DoctorLoginRequest request) {
        return Result.success(doctorService.login(request));
    }
}
