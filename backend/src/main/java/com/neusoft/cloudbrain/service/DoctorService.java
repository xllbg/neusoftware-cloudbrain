package com.neusoft.cloudbrain.service;

import com.neusoft.cloudbrain.dto.DoctorLoginRequest;
import com.neusoft.cloudbrain.dto.DoctorRegisterRequest;
import com.neusoft.cloudbrain.dto.DoctorStatusVO;
import com.neusoft.cloudbrain.dto.DoctorVO;
import com.neusoft.cloudbrain.dto.LoginResponse;
import com.neusoft.cloudbrain.entity.Doctor;
import com.neusoft.cloudbrain.exception.BusinessException;
import com.neusoft.cloudbrain.repository.DoctorRepository;
import com.neusoft.cloudbrain.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        String token = jwtUtils.generateToken(doctor.getId(), doctor.getUsername(), "DOCTOR");

        return LoginResponse.builder()
                .token(token)
                .userId(doctor.getId())
                .username(doctor.getUsername())
                .name(doctor.getName())
                .role("DOCTOR")
                .build();
    }

    // ===== 医生注册审批 =====

    @Transactional
    public String register(DoctorRegisterRequest request) {
        if (doctorRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException("用户名已存在");
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
                .orElseThrow(() -> new BusinessException(404, "未找到该医生信息"));

        if (!doctor.getName().equals(name)) {
            throw new BusinessException(404, "未找到该医生信息");
        }

        return toStatusVO(doctor);
    }

    public List<DoctorStatusVO> getPendingDoctors() {
        return doctorRepository.findByStatus("PENDING").stream()
                .map(this::toStatusVO)
                .collect(Collectors.toList());
    }

    public List<DoctorStatusVO> getAllDoctorsWithStatus() {
        return doctorRepository.findAll().stream()
                .map(this::toStatusVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "医生不存在"));
        doctor.setStatus("APPROVED");
        doctorRepository.save(doctor);
    }

    @Transactional
    public void rejectDoctor(Long id, String reason) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "医生不存在"));
        doctor.setStatus("REJECTED");
        doctor.setRejectReason(reason);
        doctorRepository.save(doctor);
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
