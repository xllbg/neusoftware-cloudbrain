package com.neusoft.cloudbrain.service;

import com.neusoft.cloudbrain.dto.*;
import com.neusoft.cloudbrain.entity.Doctor;
import com.neusoft.cloudbrain.exception.BusinessException;
import com.neusoft.cloudbrain.repository.DoctorRepository;
import com.neusoft.cloudbrain.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final JwtUtils jwtUtils;

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
        // 按手机号查找
        Doctor doctor = doctorRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new BusinessException("姓名、手机号或密码错误"));

        // 验证姓名
        if (!doctor.getName().equals(request.getName())) {
            throw new BusinessException("姓名、手机号或密码错误");
        }

        // 验证密码
        if (!doctor.getPassword().equals(request.getPassword())) {
            throw new BusinessException("姓名、手机号或密码错误");
        }

        // 检查审核状态
        if (!"APPROVED".equals(doctor.getStatus())) {
            throw new BusinessException("您的账户暂未通过审核，请等待管理员审批");
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

    // 医生注册（仍用用户名注册，管理员录入）
    public String register(DoctorRegisterRequest request) {
        if (doctorRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException("该用户名已被注册");
        }

        Doctor doctor = new Doctor();
        doctor.setUsername(request.getUsername());
        doctor.setPassword(request.getPassword());
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

    // 获取待审批医生列表
    public List<DoctorStatusVO> getPendingDoctors() {
        return doctorRepository.findByStatus("PENDING").stream()
                .map(this::toStatusVO)
                .collect(Collectors.toList());
    }

    // 获取全部医生（含状态）
    public List<DoctorStatusVO> getAllDoctorsWithStatus() {
        return doctorRepository.findAll().stream()
                .map(this::toStatusVO)
                .collect(Collectors.toList());
    }

    // 批准医生
    public void approveDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "医生不存在"));
        doctor.setStatus("APPROVED");
        doctorRepository.save(doctor);
    }

    // 拒绝医生
    public void rejectDoctor(Long id, String reason) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "医生不存在"));
        doctor.setStatus("REJECTED");
        doctor.setRejectReason(reason);
        doctorRepository.save(doctor);
    }

    // 按手机号和姓名查询审核状态
    public DoctorStatusVO getDoctorStatus(String name, String phone) {
        Doctor doctor = doctorRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException(404, "未找到该医生信息"));

        if (!doctor.getName().equals(name)) {
            throw new BusinessException(404, "未找到该医生信息");
        }

        return toStatusVO(doctor);
    }

    private DoctorStatusVO toStatusVO(Doctor doctor) {
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
