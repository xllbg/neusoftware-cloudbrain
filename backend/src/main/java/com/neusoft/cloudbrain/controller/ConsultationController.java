package com.neusoft.cloudbrain.controller;

import com.neusoft.cloudbrain.dto.CommonResult;
import com.neusoft.cloudbrain.dto.ConsultationMessageDTO;
import com.neusoft.cloudbrain.service.ConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultation")
@RequiredArgsConstructor
@Tag(name = "问诊对话")
public class ConsultationController {

    private final ConsultationService consultationService;

    @PostMapping("/send")
    @Operation(summary = "发送问诊消息", description = "医生或患者发送问诊消息")
    public CommonResult<ConsultationMessageDTO> sendMessage(@RequestBody Map<String, Object> request) {
        Long registrationId = Long.valueOf(request.get("registrationId").toString());
        String senderType = (String) request.get("senderType");
        Long senderId = Long.valueOf(request.get("senderId").toString());
        String content = (String) request.get("content");

        ConsultationMessageDTO message = consultationService.sendMessage(registrationId, senderType, senderId, content);
        return CommonResult.success("消息发送成功", message);
    }

    @GetMapping("/messages")
    @Operation(summary = "获取问诊消息列表", description = "根据挂号ID获取全部问诊消息")
    public CommonResult<List<ConsultationMessageDTO>> getMessageList(@RequestParam Long registrationId) {
        List<ConsultationMessageDTO> messages = consultationService.getMessageList(registrationId);
        return CommonResult.success(messages);
    }
}
