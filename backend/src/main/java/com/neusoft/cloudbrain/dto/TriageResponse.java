package com.neusoft.cloudbrain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriageResponse {

    private String department;

    private String reasoning;

    private List<DoctorSimpleVO> doctors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DoctorSimpleVO {
        private Long id;
        private String name;
        private String title;
        private String hospital;
    }
}
