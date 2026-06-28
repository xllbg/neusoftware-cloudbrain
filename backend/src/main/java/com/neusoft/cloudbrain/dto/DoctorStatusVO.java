package com.neusoft.cloudbrain.dto;

import com.neusoft.cloudbrain.entity.Doctor;
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
    private String status;
    private String rejectReason;

    public static DoctorStatusVO fromEntity(Doctor doctor) {
        return DoctorStatusVO.builder()
                .id(doctor.getId())
                .username(doctor.getUsername())
                .name(doctor.getName())
                .gender(doctor.getGender())
                .age(doctor.getAge())
                .department(doctor.getDepartment())
                .title(doctor.getTitle())
                .hospital(doctor.getHospital())
                .phone(doctor.getPhone())
                .introduction(doctor.getIntroduction())
                .status(doctor.getStatus())
                .rejectReason(doctor.getRejectReason())
                .build();
    }
}
