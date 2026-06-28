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
        if (patientRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        Patient patient = new Patient();
        patient.setUsername(request.getUsername());
        patient.setPassword(passwordEncoder.encode(request.getPassword()));
        patient.setName(request.getName());
        patient.setGender(request.getGender());
        patient.setAge(request.getAge());
        patient.setPhone(request.getPhone());
        patient.setIdCard(request.getIdCard());
        patient.setAddress(request.getAddress());

        Patient saved = patientRepository.save(patient);

        String token = jwtUtils.generateToken(saved.getId(), saved.getUsername(), "PATIENT");

        return LoginResponse.builder()
                .token(token)
                .userId(saved.getId())
                .username(saved.getUsername())
                .name(saved.getName())
                .role("PATIENT")
                .build();
    }

    public LoginResponse login(PatientLoginRequest request) {
        Patient patient = patientRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), patient.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtils.generateToken(patient.getId(), patient.getUsername(), "PATIENT");

        return LoginResponse.builder()
                .token(token)
                .userId(patient.getId())
                .username(patient.getUsername())
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

        String token = jwtUtils.generateToken(patient.getId(), patient.getUsername(), "PATIENT");

        return LoginResponse.builder()
                .token(token)
                .userId(patient.getId())
                .username(patient.getUsername())
                .name(patient.getName())
                .role("PATIENT")
                .build();
    }
}
