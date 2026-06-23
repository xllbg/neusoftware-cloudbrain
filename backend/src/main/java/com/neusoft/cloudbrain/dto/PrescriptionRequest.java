package com.neusoft.cloudbrain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "处方请求")
public class PrescriptionRequest {

    @NotNull(message = "患者ID不能为空")
    @Schema(description = "患者ID", example = "1")
    private Long patientId;

    @NotNull(message = "医生ID不能为空")
    @Schema(description = "医生ID", example = "1")
    private Long doctorId;

    @Schema(description = "挂号ID", example = "1")
    private Long registrationId;

    @NotBlank(message = "药品列表不能为空")
    @Schema(description = "药品列表（JSON格式）", example = "[{\"name\":\"阿司匹林\",\"dose\":\"100mg\",\"frequency\":\"每日一次\"},{\"name\":\"硝酸甘油\",\"dose\":\"5mg\",\"frequency\":\"必要时舌下含服\"}]")
    private String medicineList;

    @Schema(description = "剂量说明", example = "根据病情调整剂量")
    private String dosage;

    @Schema(description = "用法说明", example = "饭后服用")
    private String usage;
}
