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
@Schema(description = "处方响应")
public class PrescriptionResponse {

    @Schema(description = "处方ID", example = "1")
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

    @Schema(description = "药品列表", example = "[{\"name\":\"阿司匹林\",\"dose\":\"100mg\"}]")
    private String medicineList;

    @Schema(description = "剂量说明", example = "根据病情调整剂量")
    private String dosage;

    @Schema(description = "用法说明", example = "饭后服用")
    private String usage;

    @Schema(description = "状态", example = "submitted")
    private String status;

    @Schema(description = "创建时间", example = "2026-06-22T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "AI审核结果")
    private AiCheckResult aiCheckResult;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AiCheckResult {
        @Schema(description = "审核结果", example = "处方审核完成...")
        private String checkResult;

        @Schema(description = "风险等级", example = "medium")
        private String riskLevel;

        @Schema(description = "用药建议", example = "建议密切监测血压...")
        private String medicationSuggestions;

        @Schema(description = "药物相互作用检测", example = "未发现严重相互作用")
        private String interactionDetection;

        @Schema(description = "风险提示", example = "注意监测不良反应")
        private String riskHints;
    }
}
