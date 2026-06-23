package com.neusoft.cloudbrain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorStatusVO {
    private Long id;
    private String username;
    private String name;
    private String gender;
    private Integer age;
    private String department;
    private String title;
    private String hospital;
    private String phone;
    private String introduction;
    private String status;       // PENDING / APPROVED / REJECTED
    private String rejectReason;
}
