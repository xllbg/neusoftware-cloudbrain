package com.neusoft.cloudbrain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "保存问诊记录请求")
public class ConsultationRecordRequest {

    @NotNull(message = "挂号ID不能为空")
    @Schema(description = "挂号ID")
    private Long registrationId;

    @NotNull(message = "患者ID不能为空")
    @Schema(description = "患者ID")
    private Long patientId;

    @NotNull(message = "医生ID不能为空")
    @Schema(description = "医生ID")
    private Long doctorId;

    @Schema(description = "现病史")
    private String presentIllness;

    @Schema(description = "既往史")
    private String pastHistory;

    @Schema(description = "体格检查")
    private String physicalExamination;

    @Schema(description = "初步诊断")
    private String diagnosis;

    @Schema(description = "主诉")
    private String chiefComplaint;

    @Schema(description = "治疗意见")
    private String treatmentPlan;

    @Schema(description = "是否AI推荐")
    private Boolean aiRecommended;
}
