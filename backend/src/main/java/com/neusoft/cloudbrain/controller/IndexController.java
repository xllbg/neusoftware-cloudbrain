package com.neusoft.cloudbrain.controller;

import com.neusoft.cloudbrain.dto.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "首页")
public class IndexController {

    @GetMapping("/")
    @Operation(summary = "欢迎页", description = "智慧云脑诊疗平台服务已启动")
    public CommonResult<String> index() {
        return CommonResult.success("欢迎使用智慧云脑诊疗平台 API 服务");
    }
}
