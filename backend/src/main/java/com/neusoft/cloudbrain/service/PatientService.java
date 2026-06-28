package com.neusoft.cloudbrain.service;

import com.neusoft.cloudbrain.dto.LoginResponse;
import com.neusoft.cloudbrain.dto.PatientLoginRequest;
import com.neusoft.cloudbrain.dto.PatientRegisterRequest;
import com.neusoft.cloudbrain.dto.PhoneLoginRequest;
import com.neusoft.cloudbrain.entity.Patient;
import com.neusoft.cloudbrain.exception.BusinessException;
import com.neusoft.cloudbrain.repository.PatientRepository;
import com.neusoft.cloudbrain.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse register(PatientRegisterRequest request) {
        if (patientRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new BusinessException("手机号已注册");
        }

        Patient patient = new Patient();
        patient.setName(request.getName());
        patient.setPassword(passwordEncoder.encode(request.getPassword()));
        patient.setPhone(request.getPhone());
        patient.setGender(request.getGender());
        patient.setAge(request.getAge());
        patient.setIdCard(request.getIdCard());
        patient.setAddress(request.getAddress());

        Patient saved = patientRepository.save(patient);

        String token = jwtUtils.generateToken(saved.getId(), saved.getName(), "PATIENT");

        return LoginResponse.builder()
                .token(token)
                .userId(saved.getId())
                .username(saved.getName())
                .name(saved.getName())
                .role("PATIENT")
                .build();
    }

    public LoginResponse login(PatientLoginRequest request) {
        Patient patient = patientRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new BusinessException("手机号或密码错误"));

        if (!patient.getName().equals(request.getName())) {
            throw new BusinessException("姓名与手机号不匹配");
        }

        if (!passwordEncoder.matches(request.getPassword(), patient.getPassword())) {
            throw new BusinessException("手机号或密码错误");
        }

        String token = jwtUtils.generateToken(patient.getId(), patient.getName(), "PATIENT");

        return LoginResponse.builder()
                .token(token)
                .userId(patient.getId())
                .username(patient.getName())
                .name(patient.getName())
                .role("PATIENT")
                .build();
    }

    public LoginResponse getPatientInfo(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new BusinessException(404, "患者不存在"));

        return LoginResponse.builder()
                .userId(patient.getId())
                .username(patient.getName())
                .name(patient.getName())
                .role("PATIENT")
                .build();
    }

    public LoginResponse loginByPhone(PhoneLoginRequest request) {
        Patient patient = patientRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new BusinessException("手机号未注册"));

        if (!patient.getName().equals(request.getName())) {
            throw new BusinessException("姓名与手机号不匹配");
        }

        if (!passwordEncoder.matches(request.getPassword(), patient.getPassword())) {
            throw new BusinessException("密码错误");
        }

        String token = jwtUtils.generateToken(patient.getId(), patient.getName(), "PATIENT");

        return LoginResponse.builder()
                .token(token)
                .userId(patient.getId())
                .username(patient.getName())
                .name(patient.getName())
                .role("PATIENT")
                .build();
    }
}
