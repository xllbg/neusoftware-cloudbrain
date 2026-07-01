package com.neusoft.cloudbrain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "JWT令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "角色", example = "PATIENT")
    private String role;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "性别", example = "男")
    private String gender;

    @Schema(description = "年龄", example = "30")
    private Integer age;

    @Schema(description = "身份证号", example = "110101199001011234")
    private String idCard;

    @Schema(description = "地址", example = "北京市朝阳区")
    private String address;
}
