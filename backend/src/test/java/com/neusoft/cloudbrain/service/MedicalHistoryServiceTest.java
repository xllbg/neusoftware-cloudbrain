package com.neusoft.cloudbrain.service;

import com.neusoft.cloudbrain.dto.MedicalHistoryResponse;
import com.neusoft.cloudbrain.dto.MedicalRecordResponse;
import com.neusoft.cloudbrain.dto.PrescriptionResponse;
import com.neusoft.cloudbrain.dto.RegistrationResponse;
import com.neusoft.cloudbrain.entity.Doctor;
import com.neusoft.cloudbrain.entity.MedicalRecord;
import com.neusoft.cloudbrain.entity.Patient;
import com.neusoft.cloudbrain.entity.Prescription;
import com.neusoft.cloudbrain.entity.Registration;
import com.neusoft.cloudbrain.exception.BusinessException;
import com.neusoft.cloudbrain.repository.DoctorRepository;
import com.neusoft.cloudbrain.repository.MedicalRecordRepository;
import com.neusoft.cloudbrain.repository.PatientRepository;
import com.neusoft.cloudbrain.repository.PrescriptionRepository;
import com.neusoft.cloudbrain.repository.RegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicalHistoryService 单元测试")
class MedicalHistoryServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private MedicalHistoryService medicalHistoryService;

    private Patient patient;
    private Doctor doctor;
    private Registration registration;
    private MedicalRecord medicalRecord;
    private Prescription prescription;

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
        registration.setStatus("completed");
        registration.setSymptom("胸痛");
        registration.setCreatedAt(LocalDateTime.now());

        medicalRecord = new MedicalRecord();
        medicalRecord.setId(1L);
        medicalRecord.setPatientId(1L);
        medicalRecord.setDoctorId(1L);
        medicalRecord.setRegistrationId(1L);
        medicalRecord.setChiefComplaint("胸痛");
        medicalRecord.setDiagnosis("冠心病");
        medicalRecord.setPresentIllness("持续性胸痛3天");
        medicalRecord.setPastHistory("高血压病史");
        medicalRecord.setPhysicalExamination("血压偏高");
        medicalRecord.setTreatmentPlan("药物治疗");
        medicalRecord.setCreatedAt(LocalDateTime.now());

        prescription = new Prescription();
        prescription.setId(1L);
        prescription.setPatientId(1L);
        prescription.setDoctorId(1L);
        prescription.setRegistrationId(1L);
        prescription.setMedicineList("[{\"name\":\"阿司匹林\",\"dose\":\"100mg\"}]");
        prescription.setDosage("每日一次");
        prescription.setUsage("饭后服用");
        prescription.setStatus("checked");
        prescription.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("查询患者历史就诊记录测试")
    class GetPatientMedicalHistoryTests {

        @Test
        @DisplayName("查询患者历史就诊记录成功-含病历和处方")
        void getPatientMedicalHistory_withRecords_success() {
            when(registrationRepository.findByPatientIdOrderByCreatedAtDesc(1L))
                    .thenReturn(List.of(registration));
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
            when(medicalRecordRepository.findByRegistrationId(1L))
                    .thenReturn(List.of(medicalRecord));
            when(prescriptionRepository.findByRegistrationId(1L))
                    .thenReturn(List.of(prescription));

            List<MedicalHistoryResponse> result = medicalHistoryService.getPatientMedicalHistory(1L);

            assertEquals(1, result.size());
            MedicalHistoryResponse history = result.get(0);

            assertNotNull(history.getRegistration());
            assertEquals("王小明", history.getRegistration().getPatientName());
            assertEquals("张医生", history.getRegistration().getDoctorName());

            assertNotNull(history.getMedicalRecord());
            assertEquals("冠心病", history.getMedicalRecord().getDiagnosis());

            assertNotNull(history.getPrescription());
            assertEquals("checked", history.getPrescription().getStatus());
        }

        @Test
        @DisplayName("查询患者历史就诊记录成功-无病历处方")
        void getPatientMedicalHistory_noRecords_success() {
            when(registrationRepository.findByPatientIdOrderByCreatedAtDesc(1L))
                    .thenReturn(List.of(registration));
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
            when(medicalRecordRepository.findByRegistrationId(1L))
                    .thenReturn(List.of());
            when(prescriptionRepository.findByRegistrationId(1L))
                    .thenReturn(List.of());

            List<MedicalHistoryResponse> result = medicalHistoryService.getPatientMedicalHistory(1L);

            assertEquals(1, result.size());
            assertNull(result.get(0).getMedicalRecord());
            assertNull(result.get(0).getPrescription());
        }

        @Test
        @DisplayName("患者无就诊记录返回空列表")
        void getPatientMedicalHistory_noRecords_empty() {
            when(registrationRepository.findByPatientIdOrderByCreatedAtDesc(99L))
                    .thenReturn(List.of());

            List<MedicalHistoryResponse> result = medicalHistoryService.getPatientMedicalHistory(99L);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("查询就诊详情测试")
    class GetMedicalHistoryDetailTests {

        @Test
        @DisplayName("查询就诊详情成功")
        void getMedicalHistoryDetail_success() {
            when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
            when(medicalRecordRepository.findByRegistrationId(1L))
                    .thenReturn(List.of(medicalRecord));
            when(prescriptionRepository.findByRegistrationId(1L))
                    .thenReturn(List.of(prescription));

            MedicalHistoryResponse result = medicalHistoryService.getMedicalHistoryDetail(1L, 1L);

            assertNotNull(result);
            assertNotNull(result.getRegistration());
            assertNotNull(result.getMedicalRecord());
            assertNotNull(result.getPrescription());
        }

        @Test
        @DisplayName("挂号记录不存在抛出异常")
        void getMedicalHistoryDetail_registrationNotFound_throwsException() {
            when(registrationRepository.findById(99L)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> medicalHistoryService.getMedicalHistoryDetail(1L, 99L));

            assertEquals(404, exception.getCode());
        }

        @Test
        @DisplayName("无权查看他人就诊记录抛出异常")
        void getMedicalHistoryDetail_notOwner_throwsException() {
            when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> medicalHistoryService.getMedicalHistoryDetail(99L, 1L));

            assertEquals(403, exception.getCode());
            assertEquals("无权查看此就诊记录", exception.getMessage());
        }
    }
}
