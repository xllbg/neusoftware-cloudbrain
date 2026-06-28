package com.neusoft.cloudbrain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRegisterRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String gender;
    private Integer age;

    @NotBlank(message = "科室不能为空")
    private String department;

    private String title;
    private String hospital;
    private String phone;
    private String introduction;
}
