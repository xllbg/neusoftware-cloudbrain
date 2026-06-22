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
public class DoctorVO {

    private Long id;
    private String name;
    private String gender;
    private Integer age;
    private String department;
    private String title;
    private String hospital;
    private String phone;
    private String avatar;
    private String introduction;

    public static DoctorVO fromEntity(Doctor doctor) {
        return DoctorVO.builder()
                .id(doctor.getId())
                .name(doctor.getName())
                .gender(doctor.getGender())
                .age(doctor.getAge())
                .department(doctor.getDepartment())
                .title(doctor.getTitle())
                .hospital(doctor.getHospital())
                .phone(doctor.getPhone())
                .avatar(doctor.getAvatar())
                .introduction(doctor.getIntroduction())
                .build();
    }
}
