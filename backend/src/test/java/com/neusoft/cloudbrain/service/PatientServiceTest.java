package com.neusoft.cloudbrain.service;

import com.neusoft.cloudbrain.dto.LoginResponse;
import com.neusoft.cloudbrain.dto.PatientLoginRequest;
import com.neusoft.cloudbrain.dto.PatientRegisterRequest;
import com.neusoft.cloudbrain.dto.PhoneLoginRequest;
import com.neusoft.cloudbrain.entity.Patient;
import com.neusoft.cloudbrain.exception.BusinessException;
import com.neusoft.cloudbrain.repository.PatientRepository;
import com.neusoft.cloudbrain.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PatientService 单元测试")
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PatientService patientService;

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);
        patient.setUsername("patient1");
        patient.setPassword("encodedPassword");
        patient.setName("王小明");
        patient.setGender("男");
        patient.setAge(25);
        patient.setPhone("13800138001");
        patient.setIdCard("110101199001011234");
        patient.setAddress("北京市朝阳区");
    }

    @Nested
    @DisplayName("患者注册测试")
    class RegisterTests {

        @Test
        @DisplayName("注册成功，返回token")
        void register_success() {
            PatientRegisterRequest request = new PatientRegisterRequest();
            request.setUsername("newpatient");
            request.setPassword("password123");
            request.setName("新患者");
            request.setGender("女");
            request.setAge(30);
            request.setPhone("13900000001");

            when(patientRepository.existsByUsername("newpatient")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
                Patient p = invocation.getArgument(0);
                p.setId(2L);
                return p;
            });
            when(jwtUtils.generateToken(2L, "newpatient", "PATIENT")).thenReturn("jwt-token");

            LoginResponse response = patientService.register(request);

            assertNotNull(response);
            assertEquals("jwt-token", response.getToken());
            assertEquals(2L, response.getUserId());
            assertEquals("PATIENT", response.getRole());
        }

        @Test
        @DisplayName("用户名已存在，注册失败")
        void register_usernameExists_throwsException() {
            PatientRegisterRequest request = new PatientRegisterRequest();
            request.setUsername("patient1");
            request.setPassword("password");

            when(patientRepository.existsByUsername("patient1")).thenReturn(true);

            BusinessException exception = assertThrows(BusinessException.class, () -> patientService.register(request));
            assertEquals("用户名已存在", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("患者登录测试")
    class LoginTests {

        @Test
        @DisplayName("用户名密码正确，登录成功")
        void login_success() {
            PatientLoginRequest request = new PatientLoginRequest("patient1", "password123");
            when(patientRepository.findByUsername("patient1")).thenReturn(Optional.of(patient));
            when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
            when(jwtUtils.generateToken(1L, "patient1", "PATIENT")).thenReturn("jwt-token");

            LoginResponse response = patientService.login(request);

            assertNotNull(response);
            assertEquals("jwt-token", response.getToken());
            assertEquals(1L, response.getUserId());
            assertEquals("PATIENT", response.getRole());
        }

        @Test
        @DisplayName("用户名不存在，抛出异常")
        void login_userNotFound_throwsException() {
            PatientLoginRequest request = new PatientLoginRequest("nonexistent", "password");
            when(patientRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class, () -> patientService.login(request));
            assertEquals("用户名或密码错误", exception.getMessage());
        }

        @Test
        @DisplayName("密码错误，抛出异常")
        void login_wrongPassword_throwsException() {
            PatientLoginRequest request = new PatientLoginRequest("patient1", "wrongPassword");
            when(patientRepository.findByUsername("patient1")).thenReturn(Optional.of(patient));
            when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

            BusinessException exception = assertThrows(BusinessException.class, () -> patientService.login(request));
            assertEquals("用户名或密码错误", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("手机号登录测试")
    class PhoneLoginTests {

        @BeforeEach
        void setUpPhoneLogin() {
            reset(patientRepository);
            lenient().when(patientRepository.findByPhone("13800138001")).thenReturn(Optional.of(patient));
        }

        @Test
        @DisplayName("手机号登录成功")
        void loginByPhone_success() {
            when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
            when(jwtUtils.generateToken(1L, "patient1", "PATIENT")).thenReturn("jwt-token");

            // Constructor order: (name, phone, password)
            PhoneLoginRequest request = new PhoneLoginRequest("王小明", "13800138001", "password123");
            LoginResponse response = patientService.loginByPhone(request);

            assertNotNull(response);
            assertEquals("jwt-token", response.getToken());
        }

        @Test
        @DisplayName("手机号未注册，抛出异常")
        void loginByPhone_phoneNotRegistered_throwsException() {
            when(patientRepository.findByPhone("13900000000")).thenReturn(Optional.empty());

            PhoneLoginRequest request = new PhoneLoginRequest("王小明", "13900000000", "password");
            BusinessException exception = assertThrows(BusinessException.class, () -> patientService.loginByPhone(request));
            assertEquals("手机号未注册", exception.getMessage());
        }

        @Test
        @DisplayName("姓名与手机号不匹配，抛出异常")
        void loginByPhone_nameMismatch_throwsException() {
            PhoneLoginRequest request = new PhoneLoginRequest("王小红", "13800138001", "password");
            BusinessException exception = assertThrows(BusinessException.class, () -> patientService.loginByPhone(request));
            assertEquals("姓名与手机号不匹配", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("患者详情查询测试")
    class GetPatientDetailTests {

        @Test
        @DisplayName("患者存在，返回详情")
        void getPatientDetail_exists_returnsPatient() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

            Patient result = patientService.getPatientDetail(1L);

            assertNotNull(result);
            assertEquals("王小明", result.getName());
        }

        @Test
        @DisplayName("患者不存在，抛出异常")
        void getPatientDetail_notFound_throwsException() {
            when(patientRepository.findById(99L)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class, () -> patientService.getPatientDetail(99L));
            assertEquals(404, exception.getCode());
            assertEquals("患者不存在", exception.getMessage());
        }
    }
}
