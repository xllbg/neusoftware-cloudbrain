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
@Schema(description = "病历响应")
public class MedicalRecordResponse {

    @Schema(description = "病历ID", example = "1")
    private Long id;

    @Schema(description = "患者ID", example = "1")
    private Long patientId;

    @Schema(description = "患者姓名", example = "张三")
    private String patientName;

    @Schema(description = "医生ID", example = "1")
    private Long doctorId;

    @Schema(description = "医生姓名", example = "王明")
    private String doctorName;

    @Schema(description = "挂号ID", example = "1")
    private Long registrationId;

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

    @Schema(description = "治疗意见", example = "1. 卧床休息\\n2. 吸氧\\n3. 药物治疗...")
    private String treatmentPlan;

    @Schema(description = "创建时间", example = "2026-06-22T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "AI生成结果")
    private AiMedicalRecordResult aiResult;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AiMedicalRecordResult {
        @Schema(description = "现病史", example = "患者3小时前无明显诱因出现胸痛...")
        private String presentIllness;

        @Schema(description = "既往史", example = "既往高血压病史5年...")
        private String pastHistory;

        @Schema(description = "体格检查", example = "体温36.5℃，血压140/90mmHg...")
        private String physicalExamination;

        @Schema(description = "初步诊断", example = "冠心病 急性冠脉综合征")
        private String diagnosis;

        @Schema(description = "治疗意见", example = "1. 卧床休息\\n2. 吸氧\\n3. 药物治疗...")
        private String treatmentPlan;

        @Schema(description = "AI原始结果")
        private String aiRawResult;
    }
}
