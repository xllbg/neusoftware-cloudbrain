package com.neusoft.cloudbrain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "智能分诊响应")
public class TriageResponse {

    @Schema(description = "推荐科室", example = "心内科")
    private String department;

    @Schema(description = "推荐理由", example = "根据您描述的症状[胸痛伴气短]，推荐就诊心内科")
    private String reasoning;

    @Schema(description = "推荐医生列表")
    private List<DoctorSimpleVO> doctors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DoctorSimpleVO {

        @Schema(description = "医生ID", example = "1")
        private Long id;

        @Schema(description = "医生姓名", example = "王明")
        private String name;

        @Schema(description = "职称", example = "主任医师")
        private String title;

        @Schema(description = "医院", example = "东软医院")
        private String hospital;
    }
}
