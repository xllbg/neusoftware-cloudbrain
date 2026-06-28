package com.neusoft.cloudbrain.controller;

import com.neusoft.cloudbrain.dto.CommonResult;
import com.neusoft.cloudbrain.entity.Doctor;
import com.neusoft.cloudbrain.repository.DoctorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "密码工具")
@RestController
@RequestMapping("/util/password")
@RequiredArgsConstructor
public class PasswordUtilController {

    private final PasswordEncoder passwordEncoder;
    private final DoctorRepository doctorRepository;
    private final JdbcTemplate jdbcTemplate;

    @Operation(summary = "生成密码哈希")
    @GetMapping("/encode")
    public CommonResult<Map<String, String>> encode(@RequestParam String rawPassword) {
        Map<String, String> result = new HashMap<>();
        result.put("rawPassword", rawPassword);
        result.put("encodedPassword", passwordEncoder.encode(rawPassword));
        return CommonResult.success(result);
    }

    @Operation(summary = "重置所有医生密码")
    @PostMapping("/reset-all-doctors")
    public CommonResult<String> resetAllDoctors(@RequestParam String newPassword) {
        String encoded = passwordEncoder.encode(newPassword);
        var doctors = doctorRepository.findAll();
        for (Doctor doctor : doctors) {
            doctor.setPassword(encoded);
            doctorRepository.save(doctor);
        }
        return CommonResult.success("已重置 " + doctors.size() + " 个医生的密码为: " + newPassword);
    }

    @Operation(summary = "验证医生密码")
    @GetMapping("/verify-doctor")
    public CommonResult<Map<String, Object>> verifyDoctor(@RequestParam String phone, @RequestParam String name, @RequestParam String rawPassword) {
        Map<String, Object> result = new HashMap<>();
        result.put("phone", phone);
        result.put("inputName", name);
        result.put("rawPassword", rawPassword);
        
        var doctorOpt = doctorRepository.findByPhone(phone);
        if (doctorOpt.isEmpty()) {
            result.put("found", false);
            return CommonResult.success(result);
        }
        
        Doctor doctor = doctorOpt.get();
        result.put("found", true);
        result.put("storedName", doctor.getName());
        result.put("nameEquals", doctor.getName().equals(name));
        result.put("nameLength", doctor.getName().length());
        result.put("inputNameLength", name.length());
        result.put("storedPassword", doctor.getPassword());
        result.put("passwordMatches", passwordEncoder.matches(rawPassword, doctor.getPassword()));
        result.put("status", doctor.getStatus());
        
        return CommonResult.success(result);
    }

    @Operation(summary = "修复医生姓名编码")
    @PostMapping("/fix-doctor-name")
    public CommonResult<String> fixDoctorName(@RequestParam Long id, @RequestParam String correctName) {
        var doctorOpt = doctorRepository.findById(id);
        if (doctorOpt.isEmpty()) {
            return CommonResult.fail(404, "医生不存在");
        }
        Doctor doctor = doctorOpt.get();
        doctor.setName(correctName);
        doctorRepository.save(doctor);
        return CommonResult.success("已修复医生 " + id + " 的姓名为: " + correctName);
    }

    @Operation(summary = "创建测试医生")
    @PostMapping("/create-test-doctor")
    public CommonResult<Map<String, String>> createTestDoctor() {
        String name = "DoctorWang";
        String phone = "13900000002";
        String rawPassword = "123456";
        
        var existing = doctorRepository.findByPhone(phone);
        if (existing.isPresent()) {
            Doctor d = existing.get();
            d.setPassword(passwordEncoder.encode(rawPassword));
            d.setStatus("APPROVED");
            doctorRepository.save(d);
        } else {
            Doctor doctor = new Doctor();
            doctor.setName(name);
            doctor.setPhone(phone);
            doctor.setUsername("doctorwang");
            doctor.setPassword(passwordEncoder.encode(rawPassword));
            doctor.setGender("男");
            doctor.setAge(35);
            doctor.setDepartment("心内科");
            doctor.setTitle("主治医师");
            doctor.setHospital("测试医院");
            doctor.setStatus("APPROVED");
            doctorRepository.save(doctor);
        }
        
        Map<String, String> result = new HashMap<>();
        result.put("name", name);
        result.put("phone", phone);
        result.put("password", rawPassword);
        result.put("message", "测试医生已创建");
        return CommonResult.success(result);
    }

    @Operation(summary = "批量修复中文医生姓名")
    @PostMapping("/fix-chinese-doctors")
    public CommonResult<String> fixChineseDoctors() {
        String[][] doctorData = {
            {"13800138001", "王明", "心内科", "主任医师"},
            {"13800138002", "李芳", "呼吸内科", "副主任医师"},
            {"13800138003", "张勇", "骨科", "主任医师"},
            {"13800138004", "陈静", "儿科", "主治医师"},
            {"13800138005", "刘伟", "神经内科", "副主任医师"}
        };
        
        int count = 0;
        for (String[] data : doctorData) {
            var opt = doctorRepository.findByPhone(data[0]);
            if (opt.isPresent()) {
                Doctor d = opt.get();
                d.setName(data[1]);
                d.setDepartment(data[2]);
                d.setTitle(data[3]);
                d.setStatus("APPROVED");
                doctorRepository.save(d);
                count++;
            }
        }
        return CommonResult.success("已修复 " + count + " 位中文医生的姓名编码");
    }

    @Operation(summary = "诊断数据库字符集")
    @GetMapping("/db-charset")
    public CommonResult<Map<String, Object>> dbCharset() {
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> variables = jdbcTemplate.queryForList(
            "SHOW VARIABLES LIKE 'character_set%'");
        for (Map<String, Object> row : variables) {
            result.put(String.valueOf(row.get("Variable_name")), row.get("Value"));
        }
        
        List<Map<String, Object>> doctors = jdbcTemplate.queryForList(
            "SELECT id, name, HEX(name) as name_hex FROM doctor LIMIT 5");
        result.put("doctors", doctors);
        
        return CommonResult.success(result);
    }
}
