package com.neusoft.cloudbrain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.cloudbrain.dto.ConsultationRecordDTO;
import com.neusoft.cloudbrain.dto.ConsultationRecordRequest;
import com.neusoft.cloudbrain.entity.ConsultationMessage;
import com.neusoft.cloudbrain.entity.ConsultationRecord;
import com.neusoft.cloudbrain.entity.Registration;
import com.neusoft.cloudbrain.exception.BusinessException;
import com.neusoft.cloudbrain.repository.ConsultationMessageRepository;
import com.neusoft.cloudbrain.repository.ConsultationRecordRepository;
import com.neusoft.cloudbrain.repository.RegistrationRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ConsultationRecordService 单元测试")
class ConsultationRecordServiceTest {

    @Mock
    private ConsultationRecordRepository consultationRecordRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private ConsultationMessageRepository consultationMessageRepository;

    @Mock
    private AiService aiService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ConsultationRecordService consultationRecordService;

    private ConsultationRecord consultationRecord;
    private ConsultationRecordRequest request;
    private Registration registration;

    @BeforeEach
    void setUp() {
        consultationRecord = new ConsultationRecord();
        consultationRecord.setId(1L);
        consultationRecord.setRegistrationId(100L);
        consultationRecord.setPatientId(10L);
        consultationRecord.setDoctorId(20L);
        consultationRecord.setPresentIllness("头痛发热3天");
        consultationRecord.setPastHistory("无特殊");
        consultationRecord.setPhysicalExamination("体温38.5度");
        consultationRecord.setDiagnosis("上呼吸道感染");
        consultationRecord.setChiefComplaint("头痛发热");
        consultationRecord.setTreatmentPlan("多喝水，休息");
        consultationRecord.setAiRecommended(false);
        consultationRecord.setCreatedAt(LocalDateTime.now());

        request = new ConsultationRecordRequest();
        request.setRegistrationId(100L);
        request.setPatientId(10L);
        request.setDoctorId(20L);
        request.setPresentIllness("头痛发热3天");
        request.setPastHistory("无特殊");
        request.setPhysicalExamination("体温38.5度");
        request.setDiagnosis("上呼吸道感染");
        request.setChiefComplaint("头痛发热");
        request.setTreatmentPlan("多喝水，休息");

        registration = new Registration();
        registration.setId(100L);
        registration.setPatientId(10L);
        registration.setDoctorId(20L);
        registration.setSymptom("头痛发热");
        registration.setTriageResult("呼吸内科");
        registration.setDepartment("呼吸内科");
    }

    @Nested
    @DisplayName("保存问诊记录测试")
    class SaveConsultationRecordTests {

        @Test
        @DisplayName("创建新问诊记录成功")
        void saveConsultationRecord_newRecord_success() {
            when(consultationRecordRepository.findByRegistrationId(100L)).thenReturn(Optional.empty());
            when(consultationRecordRepository.save(any(ConsultationRecord.class))).thenAnswer(invocation -> {
                ConsultationRecord r = invocation.getArgument(0);
                r.setId(1L);
                return r;
            });

            ConsultationRecordDTO result = consultationRecordService.saveConsultationRecord(request);

            assertNotNull(result);
            assertEquals(100L, result.getRegistrationId());
            assertEquals(10L, result.getPatientId());
            assertEquals(20L, result.getDoctorId());
            assertEquals("头痛发热3天", result.getPresentIllness());
            verify(consultationRecordRepository).save(any(ConsultationRecord.class));
        }

        @Test
        @DisplayName("更新已有问诊记录成功")
        void saveConsultationRecord_existingRecord_updates() {
            when(consultationRecordRepository.findByRegistrationId(100L)).thenReturn(Optional.of(consultationRecord));
            when(consultationRecordRepository.save(any(ConsultationRecord.class))).thenReturn(consultationRecord);

            request.setDiagnosis("更新后的诊断");
            ConsultationRecordDTO result = consultationRecordService.saveConsultationRecord(request);

            assertNotNull(result);
            assertEquals("更新后的诊断", result.getDiagnosis());
            verify(consultationRecordRepository).save(any(ConsultationRecord.class));
        }

        @Test
        @DisplayName("保存时设置aiRecommended为true")
        void saveConsultationRecord_withAiRecommended_setsTrue() {
            request.setAiRecommended(true);
            when(consultationRecordRepository.findByRegistrationId(100L)).thenReturn(Optional.empty());
            when(consultationRecordRepository.save(any(ConsultationRecord.class))).thenAnswer(invocation -> {
                ConsultationRecord r = invocation.getArgument(0);
                r.setId(1L);
                return r;
            });

            ConsultationRecordDTO result = consultationRecordService.saveConsultationRecord(request);

            assertNotNull(result);
            verify(consultationRecordRepository).save(argThat(r -> Boolean.TRUE.equals(r.getAiRecommended())));
        }
    }

    @Nested
    @DisplayName("查询问诊记录测试")
    class GetConsultationRecordTests {

        @Test
        @DisplayName("根据挂号ID查询问诊记录成功")
        void getConsultationRecordByRegistrationId_exists_returnsRecord() {
            when(consultationRecordRepository.findByRegistrationId(100L)).thenReturn(Optional.of(consultationRecord));

            ConsultationRecordDTO result = consultationRecordService.getConsultationRecordByRegistrationId(100L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(100L, result.getRegistrationId());
        }

        @Test
        @DisplayName("根据挂号ID查询无结果返回null")
        void getConsultationRecordByRegistrationId_notFound_returnsNull() {
            when(consultationRecordRepository.findByRegistrationId(999L)).thenReturn(Optional.empty());

            ConsultationRecordDTO result = consultationRecordService.getConsultationRecordByRegistrationId(999L);

            assertNull(result);
        }

        @Test
        @DisplayName("根据ID查询问诊记录成功")
        void getConsultationRecord_exists_returnsRecord() {
            when(consultationRecordRepository.findById(1L)).thenReturn(Optional.of(consultationRecord));

            ConsultationRecordDTO result = consultationRecordService.getConsultationRecord(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
        }

        @Test
        @DisplayName("根据ID查询无结果返回null")
        void getConsultationRecord_notFound_returnsNull() {
            when(consultationRecordRepository.findById(999L)).thenReturn(Optional.empty());

            ConsultationRecordDTO result = consultationRecordService.getConsultationRecord(999L);

            assertNull(result);
        }

        @Test
        @DisplayName("根据医生ID查询问诊记录列表成功")
        void getConsultationRecordsByDoctorId_success() {
            ConsultationRecord record2 = new ConsultationRecord();
            record2.setId(2L);
            record2.setRegistrationId(101L);
            record2.setPatientId(11L);
            record2.setDoctorId(20L);

            when(consultationRecordRepository.findByDoctorIdOrderByUpdatedAtDesc(20L))
                    .thenReturn(List.of(consultationRecord, record2));

            List<ConsultationRecordDTO> results = consultationRecordService.getConsultationRecordsByDoctorId(20L);

            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("根据医生ID查询无结果返回空列表")
        void getConsultationRecordsByDoctorId_empty() {
            when(consultationRecordRepository.findByDoctorIdOrderByUpdatedAtDesc(99L))
                    .thenReturn(List.of());

            List<ConsultationRecordDTO> results = consultationRecordService.getConsultationRecordsByDoctorId(99L);

            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("AI推荐问诊疗法测试")
    class RecommendByAiTests {

        @Test
        @DisplayName("AI推荐成功")
        void recommendByAi_success() throws Exception {
            when(registrationRepository.findById(100L)).thenReturn(Optional.of(registration));
            when(consultationMessageRepository.findByRegistrationIdOrderByCreatedAtAsc(100L))
                    .thenReturn(List.of());

            ConsultationMessage msg = new ConsultationMessage();
            msg.setSenderType("PATIENT");
            msg.setContent("我头痛");
            when(consultationMessageRepository.findByRegistrationIdOrderByCreatedAtAsc(100L))
                    .thenReturn(List.of(msg));

            String aiResponse = "{\"choices\":[{\"message\":{\"content\":\"{\\\"presentIllness\\\":\\\"头痛\\\",\\\"pastHistory\\\":\\\"无\\\",\\\"physicalExamination\\\":\\\"体温正常\\\",\\\"diagnosis\\\":\\\"偏头痛\\\",\\\"chiefComplaint\\\":\\\"头痛\\\",\\\"treatmentPlan\\\":\\\"休息\\\"}\"}}]}";
            when(aiService.callDeepSeekApiForConsultation(anyString())).thenReturn(aiResponse);

            JsonNode mockRoot = new ObjectMapper().readTree(aiResponse);
            when(objectMapper.readTree(anyString())).thenReturn(mockRoot);

            ConsultationRecordDTO result = consultationRecordService.recommendByAi(100L);

            assertNotNull(result);
            assertTrue(result.getAiRecommended());
            assertNotNull(result.getRecommendation());
        }

        @Test
        @DisplayName("AI推荐-挂号记录不存在抛出异常")
        void recommendByAi_registrationNotFound_throwsException() {
            when(registrationRepository.findById(999L)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> consultationRecordService.recommendByAi(999L));
            assertEquals(404, exception.getCode());
        }

        @Test
        @DisplayName("AI推荐-无对话消息")
        void recommendByAi_noMessages_success() throws Exception {
            when(registrationRepository.findById(100L)).thenReturn(Optional.of(registration));
            when(consultationMessageRepository.findByRegistrationIdOrderByCreatedAtAsc(100L))
                    .thenReturn(List.of());

            String aiResponse = "{\"choices\":[{\"message\":{\"content\":\"{\\\"presentIllness\\\":\\\"头痛\\\"}\"}}]}";
            when(aiService.callDeepSeekApiForConsultation(anyString())).thenReturn(aiResponse);

            JsonNode mockRoot = new ObjectMapper().readTree(aiResponse);
            when(objectMapper.readTree(anyString())).thenReturn(mockRoot);

            ConsultationRecordDTO result = consultationRecordService.recommendByAi(100L);

            assertNotNull(result);
        }

        @Test
        @DisplayName("AI推荐-AI服务异常抛出业务异常")
        void recommendByAi_aiServiceException_throwsBusinessException() {
            when(registrationRepository.findById(100L)).thenReturn(Optional.of(registration));
            when(consultationMessageRepository.findByRegistrationIdOrderByCreatedAtAsc(100L))
                    .thenReturn(List.of());
            when(aiService.callDeepSeekApiForConsultation(anyString()))
                    .thenThrow(new RuntimeException("AI服务不可用"));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> consultationRecordService.recommendByAi(100L));
            assertEquals(500, exception.getCode());
            assertTrue(exception.getMessage().contains("AI推荐失败"));
        }

        @Test
        @DisplayName("AI推荐-JSON解析失败时抛出业务异常")
        void recommendByAi_jsonParseFailure_throwsBusinessException() throws Exception {
            when(registrationRepository.findById(100L)).thenReturn(Optional.of(registration));
            when(consultationMessageRepository.findByRegistrationIdOrderByCreatedAtAsc(100L))
                    .thenReturn(List.of());
            when(aiService.callDeepSeekApiForConsultation(anyString())).thenReturn("invalid json");
            when(objectMapper.readTree("invalid json")).thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "Invalid JSON"));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> consultationRecordService.recommendByAi(100L));
            assertEquals(500, exception.getCode());
            assertTrue(exception.getMessage().contains("AI推荐失败"));
        }
    }
}
