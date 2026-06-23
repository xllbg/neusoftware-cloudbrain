package com.neusoft.cloudbrain.service;

import com.neusoft.cloudbrain.entity.Doctor;
import com.neusoft.cloudbrain.entity.MedicalRecord;
import com.neusoft.cloudbrain.entity.Patient;
import com.neusoft.cloudbrain.exception.BusinessException;
import com.neusoft.cloudbrain.repository.DoctorRepository;
import com.neusoft.cloudbrain.repository.MedicalRecordRepository;
import com.neusoft.cloudbrain.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AiService aiService;

    @Transactional
    public MedicalRecord createMedicalRecord(Long patientId, Long doctorId, Long registrationId,
            String chiefComplaint, String presentIllness, String pastHistory,
            String physicalExamination, String diagnosis, String treatmentPlan) {

        log.info("创建病历 - 患者ID: {}, 医生ID: {}", patientId, doctorId);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new BusinessException(404, "患者不存在"));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(404, "医生不存在"));

        MedicalRecord record = new MedicalRecord();
        record.setPatientId(patientId);
        record.setDoctorId(doctorId);
        record.setRegistrationId(registrationId);
        record.setChiefComplaint(chiefComplaint);
        record.setPresentIllness(presentIllness);
        record.setPastHistory(pastHistory);
        record.setPhysicalExamination(physicalExamination);
        record.setDiagnosis(diagnosis);
        record.setTreatmentPlan(treatmentPlan);

        MedicalRecord saved = medicalRecordRepository.save(record);
        log.info("病历创建成功 - ID: {}", saved.getId());

        return saved;
    }

    public Map<String, Object> generateMedicalRecordByAi(Long patientId, String dialogueText) {
        log.info("AI生成病历 - 患者ID: {}, 对话文本: {}", patientId, dialogueText);

        String aiResult = aiService.generateMedicalRecord(dialogueText);

        Map<String, Object> result = parseAiMedicalRecordResult(aiResult);
        result.put("patientId", patientId);

        return result;
    }

    public List<MedicalRecord> getPatientMedicalRecords(Long patientId) {
        return medicalRecordRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    public List<MedicalRecord> getDoctorMedicalRecords(Long doctorId) {
        return medicalRecordRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId);
    }

    public MedicalRecord getMedicalRecordDetail(Long id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "病历不存在"));
    }

    private Map<String, Object> parseAiMedicalRecordResult(String aiResult) {
        Map<String, Object> result = new HashMap<>();
        result.put("presentIllness", "");
        result.put("pastHistory", "");
        result.put("physicalExamination", "");
        result.put("diagnosis", "");
        result.put("treatmentPlan", "");
        result.put("aiRawResult", aiResult);
        return result;
    }
}
