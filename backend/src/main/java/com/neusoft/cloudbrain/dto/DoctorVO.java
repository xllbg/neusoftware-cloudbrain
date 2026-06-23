package com.neusoft.cloudbrain.dto;

import com.neusoft.cloudbrain.entity.Doctor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "医生信息")
public class DoctorVO {

    @Schema(description = "医生ID", example = "1")
    private Long id;

    @Schema(description = "姓名", example = "王明")
    private String name;

    @Schema(description = "性别", example = "男")
    private String gender;

    @Schema(description = "年龄", example = "45")
    private Integer age;

    @Schema(description = "科室", example = "心内科")
    private String department;

    @Schema(description = "职称", example = "主任医师")
    private String title;

    @Schema(description = "医院", example = "东软医院")
    private String hospital;

    @Schema(description = "手机号", example = "13800138001")
    private String phone;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "简介", example = "从事心血管内科临床工作20余年，擅长冠心病、高血压等疾病的诊治")
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
