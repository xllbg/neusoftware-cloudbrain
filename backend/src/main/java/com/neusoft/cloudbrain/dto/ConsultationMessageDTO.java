package com.neusoft.cloudbrain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "问诊消息")
public class ConsultationMessageDTO {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "挂号ID")
    private Long registrationId;

    @Schema(description = "患者ID")
    private Long patientId;

    @Schema(description = "医生ID")
    private Long doctorId;

    @Schema(description = "发送者类型: DOCTOR/PATIENT")
    private String senderType;

    @Schema(description = "发送者ID")
    private Long senderId;

    @Schema(description = "发送者姓名")
    private String senderName;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息类型: text/image")
    private String type;

    @Schema(description = "是否已读")
    private Boolean isRead;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
