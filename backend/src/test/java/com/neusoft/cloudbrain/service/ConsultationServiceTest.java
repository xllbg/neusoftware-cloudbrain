package com.neusoft.cloudbrain.service;

import com.neusoft.cloudbrain.dto.ConsultationMessageDTO;
import com.neusoft.cloudbrain.entity.ConsultationMessage;
import com.neusoft.cloudbrain.entity.Doctor;
import com.neusoft.cloudbrain.entity.Patient;
import com.neusoft.cloudbrain.entity.Registration;
import com.neusoft.cloudbrain.exception.BusinessException;
import com.neusoft.cloudbrain.repository.ConsultationMessageRepository;
import com.neusoft.cloudbrain.repository.DoctorRepository;
import com.neusoft.cloudbrain.repository.PatientRepository;
import com.neusoft.cloudbrain.repository.RegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsultationService 单元测试")
class ConsultationServiceTest {

    @Mock
    private ConsultationMessageRepository messageRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private ConsultationService consultationService;

    private Patient patient;
    private Doctor doctor;
    private Registration registration;
    private ConsultationMessage message;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);
        patient.setName("王小明");

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("张医生");

        registration = new Registration();
        registration.setId(1L);
        registration.setPatientId(1L);
        registration.setDoctorId(1L);

        message = new ConsultationMessage();
        message.setId(1L);
        message.setRegistrationId(1L);
        message.setPatientId(1L);
        message.setDoctorId(1L);
        message.setSenderType("PATIENT");
        message.setSenderId(1L);
        message.setSenderName("王小明");
        message.setContent("医生您好，我头疼三天了");
        message.setType("text");
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("发送消息测试")
    class SendMessageTests {

        @Test
        @DisplayName("患者发送消息成功")
        void sendMessage_asPatient_success() {
            when(registrationRepository.findById(1L)).thenReturn(java.util.Optional.of(registration));
            when(patientRepository.findById(1L)).thenReturn(java.util.Optional.of(patient));
            when(messageRepository.save(any(ConsultationMessage.class))).thenAnswer(invocation -> {
                ConsultationMessage m = invocation.getArgument(0);
                m.setId(1L);
                return m;
            });

            ConsultationMessageDTO result = consultationService.sendMessage(1L, "PATIENT", 1L, "医生您好");

            assertNotNull(result);
            assertEquals("王小明", result.getSenderName());
            assertEquals("医生您好", result.getContent());
            assertEquals("PATIENT", result.getSenderType());
        }

        @Test
        @DisplayName("医生发送消息成功")
        void sendMessage_asDoctor_success() {
            when(registrationRepository.findById(1L)).thenReturn(java.util.Optional.of(registration));
            when(doctorRepository.findById(1L)).thenReturn(java.util.Optional.of(doctor));
            when(messageRepository.save(any(ConsultationMessage.class))).thenAnswer(invocation -> {
                ConsultationMessage m = invocation.getArgument(0);
                m.setId(1L);
                return m;
            });

            ConsultationMessageDTO result = consultationService.sendMessage(1L, "DOCTOR", 1L, "您好，我是张医生");

            assertNotNull(result);
            assertEquals("张医生", result.getSenderName());
            assertEquals("DOCTOR", result.getSenderType());
        }

        @Test
        @DisplayName("挂号记录不存在发送消息失败")
        void sendMessage_registrationNotFound_throwsException() {
            when(registrationRepository.findById(99L)).thenReturn(java.util.Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> consultationService.sendMessage(99L, "PATIENT", 1L, "消息"));

            assertEquals(404, exception.getCode());
        }

        @Test
        @DisplayName("医生发送消息但医生不存在抛出异常")
        void sendMessage_doctorNotFound_throwsException() {
            when(registrationRepository.findById(1L)).thenReturn(java.util.Optional.of(registration));
            when(doctorRepository.findById(99L)).thenReturn(java.util.Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> consultationService.sendMessage(1L, "DOCTOR", 99L, "消息"));

            assertEquals(404, exception.getCode());
        }

        @Test
        @DisplayName("患者发送消息但患者不存在抛出异常")
        void sendMessage_patientNotFound_throwsException() {
            when(registrationRepository.findById(1L)).thenReturn(java.util.Optional.of(registration));
            when(patientRepository.findById(99L)).thenReturn(java.util.Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> consultationService.sendMessage(1L, "PATIENT", 99L, "消息"));

            assertEquals(404, exception.getCode());
        }
    }

    @Nested
    @DisplayName("查询消息列表测试")
    class GetMessageListTests {

        @Test
        @DisplayName("查询消息列表成功")
        void getMessageList_success() {
            when(messageRepository.findByRegistrationIdOrderByCreatedAtAsc(1L))
                    .thenReturn(List.of(message));

            List<ConsultationMessageDTO> result = consultationService.getMessageList(1L);

            assertEquals(1, result.size());
            assertEquals("王小明", result.get(0).getSenderName());
            assertEquals("医生您好，我头疼三天了", result.get(0).getContent());
        }

        @Test
        @DisplayName("查询无消息返回空列表")
        void getMessageList_empty() {
            when(messageRepository.findByRegistrationIdOrderByCreatedAtAsc(99L))
                    .thenReturn(List.of());

            List<ConsultationMessageDTO> result = consultationService.getMessageList(99L);

            assertTrue(result.isEmpty());
        }
    }
}
