package com.neusoft.cloudbrain.service;

import com.neusoft.cloudbrain.dto.RegistrationRequest;
import com.neusoft.cloudbrain.dto.RegistrationResponse;
import com.neusoft.cloudbrain.entity.Doctor;
import com.neusoft.cloudbrain.entity.Patient;
import com.neusoft.cloudbrain.entity.Registration;
import com.neusoft.cloudbrain.exception.BusinessException;
import com.neusoft.cloudbrain.repository.DoctorRepository;
import com.neusoft.cloudbrain.repository.PatientRepository;
import com.neusoft.cloudbrain.repository.RegistrationRepository;
import com.neusoft.cloudbrain.repository.TriageRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RegistrationService 单元测试")
class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private TriageRepository triageRepository;

    @InjectMocks
    private RegistrationService registrationService;

    private Patient patient;
    private Doctor doctor;
    private Registration registration;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);
        patient.setName("王小明");
        patient.setGender("男");
        patient.setAge(25);

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("张医生");
        doctor.setDepartment("心内科");
        doctor.setTitle("主任医师");
        doctor.setHospital("第一医院");

        registration = new Registration();
        registration.setId(1L);
        registration.setPatientId(1L);
        registration.setDoctorId(1L);
        registration.setDepartment("心内科");
        registration.setRegistrationDate(LocalDate.now());
        registration.setTimeSlot("上午 9:00-10:00");
        registration.setStatus("pending");
        registration.setSymptom("头痛");
    }

    @Nested
    @DisplayName("创建挂号测试")
    class CreateRegistrationTests {

        @Test
        @DisplayName("创建挂号成功")
        void createRegistration_success() {
            RegistrationRequest request = new RegistrationRequest();
            request.setPatientId(1L);
            request.setDoctorId(1L);
            request.setDepartment("心内科");
            request.setRegistrationDate(LocalDate.now());
            request.setTimeSlot("上午 9:00-10:00");
            request.setSymptom("头痛");

            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
            when(registrationRepository.save(any(Registration.class))).thenAnswer(invocation -> {
                Registration r = invocation.getArgument(0);
                r.setId(1L);
                return r;
            });

            RegistrationResponse result = registrationService.createRegistration(request);

            assertNotNull(result);
            assertEquals("王小明", result.getPatientName());
            assertEquals("张医生", result.getDoctorName());
            assertEquals("pending", result.getStatus());
        }

        @Test
        @DisplayName("急诊科挂号doctorId传0成功")
        void createRegistration_emergency_success() {
            RegistrationRequest request = new RegistrationRequest();
            request.setPatientId(1L);
            request.setDoctorId(0L);
            request.setDepartment("急诊科");
            request.setRegistrationDate(LocalDate.now());
            request.setTimeSlot("全天");
            request.setSymptom("外伤");

            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(registrationRepository.save(any(Registration.class))).thenAnswer(invocation -> {
                Registration r = invocation.getArgument(0);
                r.setId(1L);
                return r;
            });

            RegistrationResponse result = registrationService.createRegistration(request);

            assertNotNull(result);
            assertEquals("急诊科（待分配）", result.getDoctorName());
        }

        @Test
        @DisplayName("患者不存在创建挂号失败")
        void createRegistration_patientNotFound_throwsException() {
            RegistrationRequest request = new RegistrationRequest();
            request.setPatientId(99L);
            request.setDoctorId(1L);

            when(patientRepository.findById(99L)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> registrationService.createRegistration(request));

            assertEquals(404, exception.getCode());
        }

        @Test
        @DisplayName("医生不存在创建挂号失败")
        void createRegistration_doctorNotFound_throwsException() {
            RegistrationRequest request = new RegistrationRequest();
            request.setPatientId(1L);
            request.setDoctorId(99L);

            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> registrationService.createRegistration(request));

            assertEquals(404, exception.getCode());
        }
    }

    @Nested
    @DisplayName("查询挂号列表测试")
    class GetRegistrationsTests {

        @Test
        @DisplayName("查询患者挂号列表成功")
        void getPatientRegistrations_success() {
            when(registrationRepository.findByPatientIdOrderByCreatedAtDesc(1L))
                    .thenReturn(List.of(registration));
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

            List<RegistrationResponse.RegistrationListItem> result =
                    registrationService.getPatientRegistrations(1L);

            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getId());
            assertEquals("张医生", result.get(0).getDoctorName());
            assertEquals("心内科", result.get(0).getDepartment());
            assertEquals("pending", result.get(0).getStatus());
        }

        @Test
        @DisplayName("查询医生挂号列表成功")
        void getDoctorRegistrations_success() {
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
            when(registrationRepository.findByDoctorIdOrderByCreatedAtDesc(1L))
                    .thenReturn(List.of(registration));
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(triageRepository.findByPatientIdOrderByCreatedAtDesc(any())).thenReturn(List.of());

            List<RegistrationResponse.RegistrationListItem> result =
                    registrationService.getDoctorRegistrations(1L, null, null, null);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("按状态筛选挂号列表")
        void getDoctorRegistrations_filterByStatus() {
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
            when(registrationRepository.findByDoctorIdOrderByCreatedAtDesc(1L))
                    .thenReturn(List.of(registration));
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(triageRepository.findByPatientIdOrderByCreatedAtDesc(any())).thenReturn(List.of());

            List<RegistrationResponse.RegistrationListItem> result =
                    registrationService.getDoctorRegistrations(1L, "pending", null, null);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("取消挂号测试")
    class CancelRegistrationTests {

        @Test
        @DisplayName("患者取消自己的挂号成功")
        void cancelRegistration_success() {
            when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));
            when(registrationRepository.save(any(Registration.class))).thenReturn(registration);
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

            RegistrationResponse result = registrationService.cancelRegistration(1L, 1L);

            assertNotNull(result);
            verify(registrationRepository).save(argThat(r -> "cancelled".equals(r.getStatus())));
        }

        @Test
        @DisplayName("无权取消他人挂号抛出异常")
        void cancelRegistration_notOwner_throwsException() {
            when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> registrationService.cancelRegistration(1L, 99L));

            assertEquals(403, exception.getCode());
        }

        @Test
        @DisplayName("取消已取消的挂号抛出异常")
        void cancelRegistration_alreadyCancelled_throwsException() {
            registration.setStatus("cancelled");
            when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> registrationService.cancelRegistration(1L, 1L));

            assertEquals(400, exception.getCode());
            assertEquals("该挂号已取消", exception.getMessage());
        }

        @Test
        @DisplayName("取消已完成的挂号抛出异常")
        void cancelRegistration_alreadyCompleted_throwsException() {
            registration.setStatus("completed");
            when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> registrationService.cancelRegistration(1L, 1L));

            assertEquals(400, exception.getCode());
            assertEquals("该挂号已完成，无法取消", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("接诊测试")
    class StartConsultationTests {

        @Test
        @DisplayName("医生接诊成功")
        void startConsultation_success() {
            when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
            when(registrationRepository.save(any(Registration.class))).thenReturn(registration);
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

            RegistrationResponse.RegistrationListItem result =
                    registrationService.startConsultation(1L, 1L);

            assertNotNull(result);
            verify(registrationRepository).save(argThat(r -> "in_progress".equals(r.getStatus())));
        }

        @Test
        @DisplayName("急诊科医生接诊急诊号成功")
        void startConsultation_emergencyDepartment_success() {
            registration.setDepartment("急诊科");
            registration.setDoctorId(null);
            doctor.setDepartment("急诊科");

            when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
            when(registrationRepository.save(any(Registration.class))).thenReturn(registration);
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

            RegistrationResponse.RegistrationListItem result =
                    registrationService.startConsultation(1L, 1L);

            assertNotNull(result);
        }

        @Test
        @DisplayName("非急诊科医生接诊急诊号抛出异常")
        void startConsultation_nonEmergencyDoctor_throwsException() {
            registration.setDepartment("急诊科");
            doctor.setDepartment("心内科");

            when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> registrationService.startConsultation(1L, 1L));

            assertEquals(403, exception.getCode());
            assertEquals("只有急诊科医生可以接诊急诊号", exception.getMessage());
        }

        @Test
        @DisplayName("无权接诊他人挂号抛出异常")
        void startConsultation_notAssignedDoctor_throwsException() {
            when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));
            when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> registrationService.startConsultation(1L, 2L));

            assertEquals(403, exception.getCode());
        }

        @Test
        @DisplayName("挂号状态不允许接诊抛出异常")
        void startConsultation_invalidStatus_throwsException() {
            registration.setStatus("in_progress");
            when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> registrationService.startConsultation(1L, 1L));

            assertEquals(400, exception.getCode());
        }
    }

    @Nested
    @DisplayName("完成问诊测试")
    class CompleteConsultationTests {

        @Test
        @DisplayName("完成问诊成功")
        void completeConsultation_success() {
            registration.setStatus("in_progress");
            when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));
            when(registrationRepository.save(any(Registration.class))).thenReturn(registration);
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

            RegistrationResponse.RegistrationListItem result =
                    registrationService.completeConsultation(1L, 1L);

            assertNotNull(result);
            verify(registrationRepository).save(argThat(r -> "completed".equals(r.getStatus())));
        }

        @Test
        @DisplayName("无权操作他人挂号抛出异常")
        void completeConsultation_notAssignedDoctor_throwsException() {
            when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> registrationService.completeConsultation(1L, 99L));

            assertEquals(403, exception.getCode());
        }

        @Test
        @DisplayName("重复完成问诊抛出异常")
        void completeConsultation_alreadyCompleted_throwsException() {
            registration.setStatus("completed");
            when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> registrationService.completeConsultation(1L, 1L));

            assertEquals(400, exception.getCode());
            assertEquals("该挂号已完成", exception.getMessage());
        }
    }
}
