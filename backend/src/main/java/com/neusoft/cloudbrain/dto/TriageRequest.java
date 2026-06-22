package com.neusoft.cloudbrain.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TriageRequest {

    @NotNull(message = "患者ID不能为空")
    @Positive(message = "患者ID必须为正数")
    private Long patientId;

    @NotNull(message = "年龄不能为空")
    @Min(value = 0, message = "年龄不能为负数")
    @Max(value = 150, message = "年龄超出合理范围")
    private Integer age;

    @NotBlank(message = "性别不能为空")
    @Pattern(regexp = "^(男|女)$", message = "性别只能为男或女")
    private String gender;

    @NotBlank(message = "症状描述不能为空")
    @Size(min = 2, max = 500, message = "症状描述长度应在2-500字之间")
    private String symptoms;
}
