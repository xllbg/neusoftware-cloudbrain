package com.neusoft.cloudbrain.service;

import com.neusoft.cloudbrain.dto.TriageRequest;
import com.neusoft.cloudbrain.dto.TriageResponse;
import com.neusoft.cloudbrain.entity.Doctor;
import com.neusoft.cloudbrain.entity.Triage;
import com.neusoft.cloudbrain.repository.DoctorRepository;
import com.neusoft.cloudbrain.repository.TriageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TriageService 单元测试")
class TriageServiceTest {

    @Mock
    private AiService aiService;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private TriageRepository triageRepository;

    @InjectMocks
    private TriageService triageService;

    private Doctor doctor;

    @BeforeEach
    void setUp() {
        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("张医生");
        doctor.setTitle("主任医师");
        doctor.setDepartment("心内科");
        doctor.setHospital("第一医院");
    }

    @Nested
    @DisplayName("分诊测试")
    class TriageTests {

        @Test
        @DisplayName("分诊成功，返回推荐科室和医生列表")
        void triage_success() {
            TriageRequest request = new TriageRequest();
            request.setPatientId(1L);
            request.setAge(35);
            request.setGender("男");
            request.setSymptoms("胸痛");

            when(aiService.recommendDepartment("胸痛", 35, "男")).thenReturn("心内科");
            when(doctorRepository.findByDepartment("心内科")).thenReturn(List.of(doctor));
            when(triageRepository.save(any(Triage.class))).thenAnswer(invocation -> {
                Triage t = invocation.getArgument(0);
                t.setId(1L);
                return t;
            });

            TriageResponse result = triageService.triage(request);

            assertNotNull(result);
            assertEquals("心内科", result.getDepartment());
            assertNotNull(result.getDoctors());
            assertEquals(1, result.getDoctors().size());
            assertEquals("张医生", result.getDoctors().get(0).getName());
            assertTrue(result.getReasoning().contains("心内科"));
        }

        @Test
        @DisplayName("分诊成功但无对应科室医生")
        void triage_noDoctorsAvailable() {
            TriageRequest request = new TriageRequest();
            request.setPatientId(1L);
            request.setAge(50);
            request.setGender("女");
            request.setSymptoms("罕见症状");

            when(aiService.recommendDepartment("罕见症状", 50, "女")).thenReturn("内科");
            when(doctorRepository.findByDepartment("内科")).thenReturn(List.of());
            when(triageRepository.save(any(Triage.class))).thenAnswer(invocation -> {
                Triage t = invocation.getArgument(0);
                t.setId(1L);
                return t;
            });

            TriageResponse result = triageService.triage(request);

            assertNotNull(result);
            assertEquals("内科", result.getDepartment());
            assertTrue(result.getDoctors().isEmpty());
        }
    }

    @Nested
    @DisplayName("保存分诊记录测试")
    class SaveTriageRecordTests {

        @Test
        @DisplayName("保存分诊记录成功")
        void saveTriageRecord_success() {
            TriageRequest request = new TriageRequest();
            request.setPatientId(1L);
            request.setAge(35);
            request.setGender("男");
            request.setSymptoms("胸痛");

            when(triageRepository.save(any(Triage.class))).thenAnswer(invocation -> {
                Triage t = invocation.getArgument(0);
                t.setId(1L);
                return t;
            });

            Triage result = triageService.saveTriageRecord(1L, request, "心内科", "1");

            assertNotNull(result);
            assertEquals("胸痛", result.getChiefComplaint());
            assertEquals("心内科", result.getRecommendedDepartment());
            assertEquals("1", result.getRecommendedDoctorIds());
        }
    }
}
