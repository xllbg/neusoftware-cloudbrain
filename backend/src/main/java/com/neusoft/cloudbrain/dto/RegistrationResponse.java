package com.neusoft.cloudbrain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "挂号响应")
public class RegistrationResponse {

    @Schema(description = "挂号ID", example = "1")
    private Long id;

    @Schema(description = "患者ID", example = "1")
    private Long patientId;

    @Schema(description = "患者姓名", example = "张三")
    private String patientName;

    @Schema(description = "医生ID", example = "3")
    private Long doctorId;

    @Schema(description = "医生姓名", example = "张勇")
    private String doctorName;

    @Schema(description = "科室", example = "骨科")
    private String department;

    @Schema(description = "挂号日期", example = "2026-06-25")
    private LocalDate registrationDate;

    @Schema(description = "时间段", example = "上午 9:00-10:00")
    private String timeSlot;

    @Schema(description = "状态", example = "pending")
    private String status;

    @Schema(description = "创建时间", example = "2026-06-22T10:30:00")
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegistrationListItem {

        @Schema(description = "挂号ID", example = "1")
        private Long id;

        @Schema(description = "医生姓名", example = "张勇")
        private String doctorName;

        @Schema(description = "科室", example = "骨科")
        private String department;

        @Schema(description = "医生职称", example = "主任医师")
        private String doctorTitle;

        @Schema(description = "医院", example = "东软医院")
        private String hospital;

        @Schema(description = "挂号日期", example = "2026-06-25")
        private LocalDate registrationDate;

        @Schema(description = "时间段", example = "上午 9:00-10:00")
        private String timeSlot;

        @Schema(description = "状态", example = "pending")
        private String status;

        @Schema(description = "创建时间", example = "2026-06-22T10:30:00")
        private LocalDateTime createdAt;
    }
}
