package com.neusoft.cloudbrain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "挂号请求")
public class RegistrationRequest {

    @NotNull(message = "患者ID不能为空")
    @Positive(message = "患者ID必须为正数")
    @Schema(description = "患者ID", example = "1")
    private Long patientId;

    @NotNull(message = "医生ID不能为空")
    @Positive(message = "医生ID必须为正数")
    @Schema(description = "医生ID", example = "3")
    private Long doctorId;

    @NotBlank(message = "科室不能为空")
    @Schema(description = "科室", example = "骨科")
    private String department;

    @NotNull(message = "挂号日期不能为空")
    @Schema(description = "挂号日期", example = "2026-06-25")
    private LocalDate registrationDate;

    @NotBlank(message = "时间段不能为空")
    @Schema(description = "时间段", example = "上午 9:00-10:00")
    private String timeSlot;
}
