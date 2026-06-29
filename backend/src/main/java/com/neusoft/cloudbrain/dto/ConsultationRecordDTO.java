package com.neusoft.cloudbrain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "问诊记录")
public class ConsultationRecordDTO {

    @Schema(description = "问诊记录ID")
    private Long id;

    @Schema(description = "挂号ID")
    private Long registrationId;

    @Schema(description = "患者ID")
    private Long patientId;

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

    @Schema(description = "创建时间")
    private String createdAt;

    @Schema(description = "AI推荐内容")
    private RecommendationDTO recommendation;

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationDTO {
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
    }
}
