package com.neusoft.cloudbrain.service;

import com.neusoft.cloudbrain.entity.Doctor;
import com.neusoft.cloudbrain.entity.Patient;
import com.neusoft.cloudbrain.entity.Prescription;
import com.neusoft.cloudbrain.entity.PrescriptionCheck;
import com.neusoft.cloudbrain.exception.BusinessException;
import com.neusoft.cloudbrain.repository.DoctorRepository;
import com.neusoft.cloudbrain.repository.PatientRepository;
import com.neusoft.cloudbrain.repository.PrescriptionCheckRepository;
import com.neusoft.cloudbrain.repository.PrescriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PrescriptionService 单元测试")
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private PrescriptionCheckRepository prescriptionCheckRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AiService aiService;

    @InjectMocks
    private PrescriptionService prescriptionService;

    private Patient patient;
    private Doctor doctor;
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

        prescription = new Prescription();
        prescription.setId(1L);
        prescription.setPatientId(1L);
        prescription.setDoctorId(1L);
        prescription.setRegistrationId(1L);
        prescription.setMedicineList("[{\"name\":\"阿司匹林\",\"dose\":\"100mg\"}]");
        prescription.setDosage("每日一次");
        prescription.setUsage("饭后服用");
        prescription.setStatus("submitted");
    }

    @Nested
    @DisplayName("创建处方测试")
    class CreatePrescriptionTests {

        @Test
        @DisplayName("创建处方成功")
        void createPrescription_success() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
            when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(invocation -> {
                Prescription p = invocation.getArgument(0);
                p.setId(1L);
                return p;
            });

            Prescription result = prescriptionService.createPrescription(
                    1L, 1L, 1L,
                    "[{\"name\":\"阿司匹林\"}]",
                    "100mg", "每日一次");

            assertNotNull(result);
            assertEquals("submitted", result.getStatus());
        }

        @Test
        @DisplayName("患者不存在创建处方失败")
        void createPrescription_patientNotFound_throwsException() {
            when(patientRepository.findById(99L)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    prescriptionService.createPrescription(
                            99L, 1L, 1L,
                            "[{\"name\":\"阿司匹林\"}]",
                            "100mg", "每日一次"));

            assertEquals(404, exception.getCode());
        }

        @Test
        @DisplayName("医生不存在创建处方失败")
        void createPrescription_doctorNotFound_throwsException() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    prescriptionService.createPrescription(
                            1L, 99L, 1L,
                            "[{\"name\":\"阿司匹林\"}]",
                            "100mg", "每日一次"));

            assertEquals(404, exception.getCode());
        }
    }

    @Nested
    @DisplayName("AI审核处方测试")
    class CheckPrescriptionByAiTests {

        @Test
        @DisplayName("AI审核处方成功")
        void checkPrescriptionByAi_success() {
            when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
            when(aiService.checkPrescription(any(), any())).thenReturn("审核通过");

            Map<String, Object> result = prescriptionService.checkPrescriptionByAi(1L);

            assertNotNull(result);
            assertEquals(1L, result.get("prescriptionId"));
            assertEquals("审核通过", result.get("checkResult"));
        }

        @Test
        @DisplayName("处方不存在审核失败")
        void checkPrescriptionByAi_prescriptionNotFound_throwsException() {
            when(prescriptionRepository.findById(99L)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> prescriptionService.checkPrescriptionByAi(99L));

            assertEquals(404, exception.getCode());
        }
    }

    @Nested
    @DisplayName("查询处方测试")
    class GetPrescriptionTests {

        @Test
        @DisplayName("查询患者所有处方")
        void getPatientPrescriptions_success() {
            when(prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(1L))
                    .thenReturn(List.of(prescription));

            List<Prescription> result = prescriptionService.getPatientPrescriptions(1L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("查询医生所有处方")
        void getDoctorPrescriptions_success() {
            when(prescriptionRepository.findByDoctorIdOrderByCreatedAtDesc(1L))
                    .thenReturn(List.of(prescription));

            List<Prescription> result = prescriptionService.getDoctorPrescriptions(1L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("查询处方详情成功")
        void getPrescriptionDetail_success() {
            when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));

            Prescription result = prescriptionService.getPrescriptionDetail(1L);

            assertNotNull(result);
            assertEquals("submitted", result.getStatus());
        }

        @Test
        @DisplayName("根据挂号ID查询处方")
        void getPrescriptionByRegistration_exists() {
            when(prescriptionRepository.findByRegistrationId(1L))
                    .thenReturn(List.of(prescription));

            Prescription result = prescriptionService.getPrescriptionByRegistration(1L);

            assertNotNull(result);
        }

        @Test
        @DisplayName("根据挂号ID查询无处方返回null")
        void getPrescriptionByRegistration_notExists() {
            when(prescriptionRepository.findByRegistrationId(99L))
                    .thenReturn(List.of());

            Prescription result = prescriptionService.getPrescriptionByRegistration(99L);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("保存审核结果测试")
    class SaveCheckResultTests {

        @Test
        @DisplayName("保存审核结果成功")
        void saveCheckResult_success() {
            PrescriptionCheck check = new PrescriptionCheck();
            check.setId(1L);

            when(prescriptionCheckRepository.save(any(PrescriptionCheck.class))).thenReturn(check);
            when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));
            when(prescriptionRepository.save(any(Prescription.class))).thenReturn(prescription);

            PrescriptionCheck result = prescriptionService.saveCheckResult(
                    1L, "审核通过", "建议饭后服用", "", "low", "");

            assertNotNull(result);
            verify(prescriptionRepository).save(argThat(p -> "checked".equals(p.getStatus())));
        }

        @Test
        @DisplayName("查询审核结果成功")
        void getCheckResult_success() {
            PrescriptionCheck check = new PrescriptionCheck();
            check.setPrescriptionId(1L);

            when(prescriptionCheckRepository.findByPrescriptionId(1L))
                    .thenReturn(Optional.of(check));

            PrescriptionCheck result = prescriptionService.getCheckResult(1L);

            assertNotNull(result);
            assertEquals(1L, result.getPrescriptionId());
        }

        @Test
        @DisplayName("查询无审核结果返回null")
        void getCheckResult_notFound() {
            when(prescriptionCheckRepository.findByPrescriptionId(99L))
                    .thenReturn(Optional.empty());

            PrescriptionCheck result = prescriptionService.getCheckResult(99L);

            assertNull(result);
        }
    }
}
