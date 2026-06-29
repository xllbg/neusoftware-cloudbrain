package com.neusoft.cloudbrain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "智能分诊请求")
public class TriageRequest {

    @NotNull(message = "患者ID不能为空")
    @Positive(message = "患者ID必须为正数")
    @Schema(description = "患者ID", example = "1")
    private Long patientId;

    @NotNull(message = "年龄不能为空")
    @Min(value = 0, message = "年龄不能为负数")
    @Max(value = 150, message = "年龄超出合理范围")
    @Schema(description = "年龄", example = "30")
    private Integer age;

    @NotBlank(message = "性别不能为空")
    @Pattern(regexp = "^(男|女)$", message = "性别只能为男或女")
    @Schema(description = "性别", example = "男")
    private String gender;

    @NotBlank(message = "症状描述不能为空")
    @Size(min = 2, max = 500, message = "症状描述长度应在2-500字之间")
    @Schema(description = "症状描述", example = "胸痛伴气短，持续约10分钟")
    private String symptoms;
}
