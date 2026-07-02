package com.neusoft.cloudbrain.service;

import com.neusoft.cloudbrain.dto.*;
import com.neusoft.cloudbrain.entity.Doctor;
import com.neusoft.cloudbrain.exception.BusinessException;
import com.neusoft.cloudbrain.repository.DoctorRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DoctorService 单元测试")
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor approvedDoctor;
    private Doctor pendingDoctor;

    @BeforeEach
    void setUp() {
        approvedDoctor = new Doctor();
        approvedDoctor.setId(1L);
        approvedDoctor.setUsername("doctor1");
        approvedDoctor.setPassword("encodedPassword");
        approvedDoctor.setName("张三");
        approvedDoctor.setGender("男");
        approvedDoctor.setAge(35);
        approvedDoctor.setDepartment("心内科");
        approvedDoctor.setTitle("主任医师");
        approvedDoctor.setHospital("第一医院");
        approvedDoctor.setPhone("13800138000");
        approvedDoctor.setStatus("APPROVED");

        pendingDoctor = new Doctor();
        pendingDoctor.setId(2L);
        pendingDoctor.setUsername("doctor2");
        pendingDoctor.setPassword("encodedPassword");
        pendingDoctor.setName("李四");
        pendingDoctor.setDepartment("骨科");
        pendingDoctor.setStatus("PENDING");
    }

    @Nested
    @DisplayName("登录测试")
    class LoginTests {

        @Test
        @DisplayName("用户名密码正确且已审核，登录成功")
        void login_success() {
            DoctorLoginRequest request = new DoctorLoginRequest("doctor1", "password123");
            when(doctorRepository.findByUsername("doctor1")).thenReturn(Optional.of(approvedDoctor));
            when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
            when(jwtUtils.generateToken(1L, "doctor1", "DOCTOR")).thenReturn("jwt-token");

            LoginResponse response = doctorService.login(request);

            assertNotNull(response);
            assertEquals("jwt-token", response.getToken());
            assertEquals(1L, response.getUserId());
            assertEquals("doctor1", response.getUsername());
            assertEquals("DOCTOR", response.getRole());
        }

        @Test
        @DisplayName("用户名不存在，抛出异常")
        void login_userNotFound_throwsException() {
            DoctorLoginRequest request = new DoctorLoginRequest("nonexistent", "password");
            when(doctorRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class, () -> doctorService.login(request));
            assertEquals("用户名或密码错误", exception.getMessage());
        }

        @Test
        @DisplayName("密码错误，抛出异常")
        void login_wrongPassword_throwsException() {
            DoctorLoginRequest request = new DoctorLoginRequest("doctor1", "wrongPassword");
            when(doctorRepository.findByUsername("doctor1")).thenReturn(Optional.of(approvedDoctor));
            when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

            BusinessException exception = assertThrows(BusinessException.class, () -> doctorService.login(request));
            assertEquals("用户名或密码错误", exception.getMessage());
        }

        @Test
        @DisplayName("账户未审核，抛出异常")
        void login_notApproved_throwsException() {
            DoctorLoginRequest request = new DoctorLoginRequest("doctor2", "password");
            when(doctorRepository.findByUsername("doctor2")).thenReturn(Optional.of(pendingDoctor));
            when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

            BusinessException exception = assertThrows(BusinessException.class, () -> doctorService.login(request));
            assertTrue(exception.getMessage().contains("账户尚未通过审核"));
        }
    }

    @Nested
    @DisplayName("手机号登录测试")
    class PhoneLoginTests {

        @Test
        @DisplayName("手机号登录成功")
        void loginByPhone_success() {
            reset(doctorRepository);
            when(doctorRepository.findByPhone("13811111111")).thenReturn(Optional.of(approvedDoctor));
            when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
            when(jwtUtils.generateToken(1L, "doctor1", "DOCTOR")).thenReturn("jwt-token");

            // Constructor order: (name, phone, password)
            PhoneLoginRequest request = new PhoneLoginRequest("张三", "13811111111", "password");
            LoginResponse response = doctorService.loginByPhone(request);

            assertNotNull(response);
            assertEquals("jwt-token", response.getToken());
        }

        @Test
        @DisplayName("手机号未注册，抛出异常")
        void loginByPhone_phoneNotRegistered_throwsException() {
            reset(doctorRepository);
            when(doctorRepository.findByPhone("13900000000")).thenReturn(Optional.empty());

            PhoneLoginRequest request = new PhoneLoginRequest("张三", "13900000000", "password");
            BusinessException exception = assertThrows(BusinessException.class, () -> doctorService.loginByPhone(request));
            assertEquals("手机号未注册", exception.getMessage());
        }

        @Test
        @DisplayName("姓名与手机号不匹配，抛出异常")
        void loginByPhone_nameMismatch_throwsException() {
            reset(doctorRepository);
            when(doctorRepository.findByPhone("13811111111")).thenReturn(Optional.of(approvedDoctor));

            PhoneLoginRequest request = new PhoneLoginRequest("王五", "13811111111", "password");
            BusinessException exception = assertThrows(BusinessException.class, () -> doctorService.loginByPhone(request));
            assertEquals("姓名与手机号不匹配", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("注册测试")
    class RegisterTests {

        @Test
        @DisplayName("注册成功")
        void register_success() {
            DoctorRegisterRequest request = new DoctorRegisterRequest();
            request.setUsername("newdoctor");
            request.setPassword("password123");
            request.setName("王医生");
            request.setGender("男");
            request.setAge(30);
            request.setDepartment("内科");
            request.setTitle("主治医师");
            request.setHospital("第二医院");
            request.setPhone("13700137000");

            when(doctorRepository.findByUsername("newdoctor")).thenReturn(Optional.empty());
            when(doctorRepository.findByPhone("13700137000")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> {
                Doctor d = invocation.getArgument(0);
                d.setId(3L);
                return d;
            });

            String result = doctorService.register(request);

            assertEquals("注册申请已提交，请等待管理员审核", result);
            verify(doctorRepository).save(any(Doctor.class));
        }

        @Test
        @DisplayName("用户名已存在，注册失败")
        void register_usernameExists_throwsException() {
            DoctorRegisterRequest request = new DoctorRegisterRequest();
            request.setUsername("doctor1");
            request.setPassword("password");

            when(doctorRepository.findByUsername("doctor1")).thenReturn(Optional.of(approvedDoctor));

            BusinessException exception = assertThrows(BusinessException.class, () -> doctorService.register(request));
            assertEquals("用户名已存在", exception.getMessage());
        }

        @Test
        @DisplayName("手机号已注册，注册失败")
        void register_phoneExists_throwsException() {
            DoctorRegisterRequest request = new DoctorRegisterRequest();
            request.setUsername("newdoctor");
            request.setPassword("password");
            request.setPhone("13800138000");

            when(doctorRepository.findByUsername("newdoctor")).thenReturn(Optional.empty());
            when(doctorRepository.findByPhone("13800138000")).thenReturn(Optional.of(approvedDoctor));

            BusinessException exception = assertThrows(BusinessException.class, () -> doctorService.register(request));
            assertEquals("手机号已注册", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("医生列表查询测试")
    class ListDoctorsTests {

        @Test
        @DisplayName("查询所有已审核医生")
        void listDoctors_noFilter_returnsApprovedOnly() {
            when(doctorRepository.findAll()).thenReturn(List.of(approvedDoctor, pendingDoctor));

            List<DoctorVO> result = doctorService.listDoctors(null);

            assertEquals(1, result.size());
            assertEquals("张三", result.get(0).getName());
        }

        @Test
        @DisplayName("按科室筛选医生")
        void listDoctors_byDepartment_returnsFilteredList() {
            when(doctorRepository.findByDepartment("心内科")).thenReturn(List.of(approvedDoctor));

            List<DoctorVO> result = doctorService.listDoctors("心内科");

            assertEquals(1, result.size());
            assertEquals("心内科", result.get(0).getDepartment());
        }
    }

    @Nested
    @DisplayName("医生详情查询测试")
    class GetDoctorDetailTests {

        @Test
        @DisplayName("医生存在，返回详情")
        void getDoctorDetail_exists_returnsDoctor() {
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(approvedDoctor));

            DoctorVO result = doctorService.getDoctorDetail(1L);

            assertEquals("张三", result.getName());
        }

        @Test
        @DisplayName("医生不存在，抛出异常")
        void getDoctorDetail_notFound_throwsException() {
            when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class, () -> doctorService.getDoctorDetail(99L));
            assertEquals(404, exception.getCode());
        }
    }

    @Nested
    @DisplayName("医生审核状态查询测试")
    class GetDoctorStatusTests {

        @Test
        @DisplayName("查询审核状态成功")
        void getDoctorStatus_success() {
            when(doctorRepository.findByPhone("13800138000")).thenReturn(Optional.of(approvedDoctor));

            DoctorStatusVO result = doctorService.getDoctorStatus("张三", "13800138000");

            assertNotNull(result);
            assertEquals("APPROVED", result.getStatus());
        }

        @Test
        @DisplayName("手机号不存在，抛出异常")
        void getDoctorStatus_phoneNotFound_throwsException() {
            when(doctorRepository.findByPhone("13900000000")).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class, () -> doctorService.getDoctorStatus("张三", "13900000000"));
            assertEquals(404, exception.getCode());
        }

        @Test
        @DisplayName("姓名与手机号不匹配，抛出异常")
        void getDoctorStatus_nameMismatch_throwsException() {
            when(doctorRepository.findByPhone("13800138000")).thenReturn(Optional.of(approvedDoctor));

            BusinessException exception = assertThrows(BusinessException.class, () -> doctorService.getDoctorStatus("李四", "13800138000"));
            assertEquals("姓名与手机号不匹配", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("审核操作测试")
    class ApprovalTests {

        @Test
        @DisplayName("批准医生成功")
        void approveDoctor_success() {
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(pendingDoctor));
            when(doctorRepository.save(any(Doctor.class))).thenReturn(pendingDoctor);

            doctorService.approveDoctor(1L);

            verify(doctorRepository).save(argThat(doctor -> "APPROVED".equals(doctor.getStatus())));
        }

        @Test
        @DisplayName("批准不存在的医生，抛出异常")
        void approveDoctor_notFound_throwsException() {
            when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class, () -> doctorService.approveDoctor(99L));
            assertEquals(404, exception.getCode());
        }

        @Test
        @DisplayName("拒绝医生成功")
        void rejectDoctor_success() {
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(pendingDoctor));
            when(doctorRepository.save(any(Doctor.class))).thenReturn(pendingDoctor);

            doctorService.rejectDoctor(1L, "资质不符");

            verify(doctorRepository).save(argThat(doctor ->
                "REJECTED".equals(doctor.getStatus()) && "资质不符".equals(doctor.getRejectReason())));
        }
    }

    @Nested
    @DisplayName("待审核医生列表测试")
    class PendingDoctorsTests {

        @Test
        @DisplayName("获取待审核医生列表")
        void getPendingDoctors_success() {
            when(doctorRepository.findByStatus("PENDING")).thenReturn(List.of(pendingDoctor));

            List<DoctorStatusVO> result = doctorService.getPendingDoctors();

            assertEquals(1, result.size());
            assertEquals("PENDING", result.get(0).getStatus());
        }
    }
}
