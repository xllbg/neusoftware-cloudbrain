package com.neusoft.cloudbrain.controller;

import com.neusoft.cloudbrain.config.RsaService;
import com.neusoft.cloudbrain.dto.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "加密认证")
public class CryptoController {

    private final RsaService rsaService;

    @GetMapping("/public-key")
    @Operation(summary = "获取 RSA 公钥", description = "前端用此公钥加密登录/注册请求体")
    public CommonResult<Map<String, String>> getPublicKey() {
        return CommonResult.success(Map.of("publicKey", rsaService.getPublicKey()));
    }
}
