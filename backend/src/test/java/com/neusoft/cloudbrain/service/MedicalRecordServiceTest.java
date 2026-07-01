package com.neusoft.cloudbrain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.cloudbrain.entity.Doctor;
import com.neusoft.cloudbrain.entity.MedicalRecord;
import com.neusoft.cloudbrain.entity.Patient;
import com.neusoft.cloudbrain.exception.BusinessException;
import com.neusoft.cloudbrain.repository.DoctorRepository;
import com.neusoft.cloudbrain.repository.MedicalRecordRepository;
import com.neusoft.cloudbrain.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicalRecordService 单元测试")
class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AiService aiService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    private Patient patient;
    private Doctor doctor;
    private MedicalRecord medicalRecord;

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

        medicalRecord = new MedicalRecord();
        medicalRecord.setId(1L);
        medicalRecord.setPatientId(1L);
        medicalRecord.setDoctorId(1L);
        medicalRecord.setRegistrationId(1L);
        medicalRecord.setChiefComplaint("头痛");
        medicalRecord.setPresentIllness("持续3天");
        medicalRecord.setPastHistory("无");
        medicalRecord.setPhysicalExamination("体温正常");
        medicalRecord.setDiagnosis("偏头痛");
        medicalRecord.setTreatmentPlan("休息+药物治疗");
    }

    @Nested
    @DisplayName("创建病历测试")
    class CreateMedicalRecordTests {

        @Test
        @DisplayName("创建病历成功")
        void createMedicalRecord_success() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
            when(medicalRecordRepository.save(any(MedicalRecord.class))).thenAnswer(invocation -> {
                MedicalRecord mr = invocation.getArgument(0);
                mr.setId(1L);
                return mr;
            });

            MedicalRecord result = medicalRecordService.createMedicalRecord(
                    1L, 1L, 1L,
                    "头痛", "持续3天", "无",
                    "体温正常", "偏头痛", "休息+药物治疗");

            assertNotNull(result);
            assertEquals("头痛", result.getChiefComplaint());
            assertEquals("偏头痛", result.getDiagnosis());
        }

        @Test
        @DisplayName("患者不存在，创建失败")
        void createMedicalRecord_patientNotFound_throwsException() {
            when(patientRepository.findById(99L)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    medicalRecordService.createMedicalRecord(
                            99L, 1L, 1L,
                            "头痛", "持续3天", "无",
                            "体温正常", "偏头痛", "休息+药物治疗"));

            assertEquals(404, exception.getCode());
        }

        @Test
        @DisplayName("医生不存在，创建失败")
        void createMedicalRecord_doctorNotFound_throwsException() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    medicalRecordService.createMedicalRecord(
                            1L, 99L, 1L,
                            "头痛", "持续3天", "无",
                            "体温正常", "偏头痛", "休息+药物治疗"));

            assertEquals(404, exception.getCode());
        }
    }

    @Nested
    @DisplayName("AI生成病历测试")
    class GenerateMedicalRecordByAiTests {

        @Test
        @DisplayName("AI生成病历成功")
        void generateMedicalRecordByAi_success() {
            String aiResult = "{\"chiefComplaint\":\"头痛\",\"presentIllness\":\"持续3天头晕\",\"pastHistory\":\"无\",\"physicalExamination\":\"血压偏高\",\"diagnosis\":\"高血压\",\"treatmentPlan\":\"降压药\"}";
            when(aiService.generateMedicalRecordWithSymptoms(any(), any(), any())).thenReturn(aiResult);

            Map<String, Object> result = medicalRecordService.generateMedicalRecordByAi(1L, "患者头痛");

            assertNotNull(result);
            assertEquals("头痛", result.get("chiefComplaint"));
            assertEquals("高血压", result.get("diagnosis"));
            assertEquals(1L, result.get("patientId"));
        }

        @Test
        @DisplayName("AI返回空结果时返回默认值")
        void generateMedicalRecordByAi_emptyResult_returnsDefaults() {
            when(aiService.generateMedicalRecordWithSymptoms(any(), any(), any())).thenReturn("");

            Map<String, Object> result = medicalRecordService.generateMedicalRecordByAi(1L, "");

            assertNotNull(result);
            assertEquals("", result.get("diagnosis"));
        }

        @Test
        @DisplayName("AI返回非JSON格式，处理不崩溃")
        void generateMedicalRecordByAi_invalidJson_handledGracefully() {
            when(aiService.generateMedicalRecordWithSymptoms(any(), any(), any())).thenReturn("这是一段无效的返回文本");

            Map<String, Object> result = medicalRecordService.generateMedicalRecordByAi(1L, "头痛");

            assertNotNull(result);
            assertEquals("", result.get("diagnosis"));
        }
    }

    @Nested
    @DisplayName("查询病历测试")
    class GetMedicalRecordTests {

        @Test
        @DisplayName("查询患者所有病历")
        void getPatientMedicalRecords_success() {
            when(medicalRecordRepository.findByPatientIdOrderByCreatedAtDesc(1L))
                    .thenReturn(List.of(medicalRecord));

            List<MedicalRecord> result = medicalRecordService.getPatientMedicalRecords(1L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("查询医生所有病历")
        void getDoctorMedicalRecords_success() {
            when(medicalRecordRepository.findByDoctorIdOrderByCreatedAtDesc(1L))
                    .thenReturn(List.of(medicalRecord));

            List<MedicalRecord> result = medicalRecordService.getDoctorMedicalRecords(1L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("根据挂号ID查询病历")
        void getMedicalRecordByRegistration_exists() {
            when(medicalRecordRepository.findByRegistrationId(1L))
                    .thenReturn(List.of(medicalRecord));

            MedicalRecord result = medicalRecordService.getMedicalRecordByRegistration(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
        }

        @Test
        @DisplayName("根据挂号ID查询无病历返回null")
        void getMedicalRecordByRegistration_notExists() {
            when(medicalRecordRepository.findByRegistrationId(99L))
                    .thenReturn(List.of());

            MedicalRecord result = medicalRecordService.getMedicalRecordByRegistration(99L);

            assertNull(result);
        }

        @Test
        @DisplayName("查询病历详情成功")
        void getMedicalRecordDetail_success() {
            when(medicalRecordRepository.findById(1L)).thenReturn(Optional.of(medicalRecord));

            MedicalRecord result = medicalRecordService.getMedicalRecordDetail(1L);

            assertNotNull(result);
            assertEquals("偏头痛", result.getDiagnosis());
        }

        @Test
        @DisplayName("查询不存在的病历抛出异常")
        void getMedicalRecordDetail_notFound_throwsException() {
            when(medicalRecordRepository.findById(99L)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> medicalRecordService.getMedicalRecordDetail(99L));

            assertEquals(404, exception.getCode());
        }
    }
}
