package com.neusoft.cloudbrain.service;

import com.neusoft.cloudbrain.dto.DoctorVO;
import com.neusoft.cloudbrain.dto.LoginResponse;
import com.neusoft.cloudbrain.dto.DoctorLoginRequest;
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

        if (!doctor.getPassword().equals(request.getPassword())) {
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
}
