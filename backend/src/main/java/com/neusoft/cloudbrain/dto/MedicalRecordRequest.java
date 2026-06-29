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
@Schema(description = "病历请求")
public class MedicalRecordRequest {

    @NotNull(message = "患者ID不能为空")
    @Schema(description = "患者ID", example = "1")
    private Long patientId;

    @NotNull(message = "医生ID不能为空")
    @Schema(description = "医生ID", example = "1")
    private Long doctorId;

    @Schema(description = "挂号ID", example = "1")
    private Long registrationId;

    @NotBlank(message = "主诉不能为空")
    @Schema(description = "主诉", example = "胸痛伴气短3小时")
    private String chiefComplaint;

    @Schema(description = "现病史", example = "患者3小时前无明显诱因出现胸痛...")
    private String presentIllness;

    @Schema(description = "既往史", example = "既往高血压病史5年...")
    private String pastHistory;

    @Schema(description = "体格检查", example = "体温36.5℃，血压140/90mmHg...")
    private String physicalExamination;

    @Schema(description = "初步诊断", example = "冠心病 急性冠脉综合征")
    private String diagnosis;

    @Schema(description = "治疗意见", example = "1. 卧床休息\n2. 吸氧\n3. 药物治疗...")
    private String treatmentPlan;
}
