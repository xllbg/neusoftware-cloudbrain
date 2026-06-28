package com.neusoft.cloudbrain.service;

import com.neusoft.cloudbrain.dto.*;
import com.neusoft.cloudbrain.entity.Doctor;
import com.neusoft.cloudbrain.exception.BusinessException;
import com.neusoft.cloudbrain.repository.DoctorRepository;
import com.neusoft.cloudbrain.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    public List<DoctorVO> listDoctors(String department) {
        List<Doctor> doctors;
        if (department != null && !department.isBlank()) {
            doctors = doctorRepository.findByDepartment(department);
        } else {
            doctors = doctorRepository.findAll();
        }
        return doctors.stream()
                .filter(d -> "APPROVED".equals(d.getStatus()))
                .map(DoctorVO::fromEntity)
                .collect(Collectors.toList());
    }

    public DoctorVO getDoctorDetail(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "医生不存在"));
        return DoctorVO.fromEntity(doctor);
    }

    public LoginResponse login(DoctorLoginRequest request) {
        Doctor doctor = doctorRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), doctor.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        if (!"APPROVED".equals(doctor.getStatus())) {
            throw new BusinessException("账户尚未通过审核，当前状态：" + getStatusText(doctor.getStatus()));
        }

        String token = jwtUtils.generateToken(doctor.getId(), doctor.getUsername(), "DOCTOR");

        return LoginResponse.builder()
                .token(token)
                .userId(doctor.getId())
                .username(doctor.getUsername())
                .name(doctor.getName())
                .role("DOCTOR")
                .build();
    }

    public LoginResponse loginByPhone(PhoneLoginRequest request) {
        Doctor doctor = doctorRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new BusinessException("手机号未注册"));

        if (!doctor.getName().equals(request.getName())) {
            throw new BusinessException("姓名与手机号不匹配");
        }

        if (!passwordEncoder.matches(request.getPassword(), doctor.getPassword())) {
            throw new BusinessException("密码错误");
        }

        if (!"APPROVED".equals(doctor.getStatus())) {
            throw new BusinessException("账户尚未通过审核，当前状态：" + getStatusText(doctor.getStatus()));
        }

        String token = jwtUtils.generateToken(doctor.getId(), doctor.getUsername(), "DOCTOR");

        return LoginResponse.builder()
                .token(token)
                .userId(doctor.getId())
                .username(doctor.getUsername())
                .name(doctor.getName())
                .role("DOCTOR")
                .build();
    }

    public String register(DoctorRegisterRequest request) {
        if (doctorRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException("用户名已存在");
        }
        if (request.getPhone() != null && doctorRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new BusinessException("手机号已注册");
        }

        Doctor doctor = new Doctor();
        doctor.setUsername(request.getUsername());
        doctor.setPassword(passwordEncoder.encode(request.getPassword()));
        doctor.setName(request.getName());
        doctor.setGender(request.getGender());
        doctor.setAge(request.getAge());
        doctor.setDepartment(request.getDepartment());
        doctor.setTitle(request.getTitle());
        doctor.setHospital(request.getHospital());
        doctor.setPhone(request.getPhone());
        doctor.setIntroduction(request.getIntroduction());
        doctor.setStatus("PENDING");

        doctorRepository.save(doctor);
        return "注册申请已提交，请等待管理员审核";
    }

    public DoctorStatusVO getDoctorStatus(String name, String phone) {
        Doctor doctor = doctorRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException(404, "未找到该手机号对应的注册信息"));

        if (!doctor.getName().equals(name)) {
            throw new BusinessException("姓名与手机号不匹配");
        }

        return DoctorStatusVO.fromEntity(doctor);
    }

    public List<DoctorStatusVO> getPendingDoctors() {
        return doctorRepository.findByStatus("PENDING").stream()
                .map(DoctorStatusVO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<DoctorStatusVO> getAllDoctorsWithStatus() {
        return doctorRepository.findAll().stream()
                .map(DoctorStatusVO::fromEntity)
                .collect(Collectors.toList());
    }

    public void approveDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "医生不存在"));
        doctor.setStatus("APPROVED");
        doctor.setRejectReason(null);
        doctorRepository.save(doctor);
    }

    public void rejectDoctor(Long id, String reason) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "医生不存在"));
        doctor.setStatus("REJECTED");
        doctor.setRejectReason(reason);
        doctorRepository.save(doctor);
    }

    private String getStatusText(String status) {
        return switch (status) {
            case "PENDING" -> "待审核";
            case "APPROVED" -> "已批准";
            case "REJECTED" -> "已拒绝";
            default -> status;
        };
    }
}
