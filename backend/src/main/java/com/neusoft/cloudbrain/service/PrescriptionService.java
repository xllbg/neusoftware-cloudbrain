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
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionCheckRepository prescriptionCheckRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AiService aiService;

    @Transactional
    public Prescription createPrescription(Long patientId, Long doctorId, Long registrationId,
            String medicineList, String dosage, String usage) {

        log.info("创建处方 - 患者ID: {}, 医生ID: {}", patientId, doctorId);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new BusinessException(404, "患者不存在"));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(404, "医生不存在"));

        Prescription prescription = new Prescription();
        prescription.setPatientId(patientId);
        prescription.setDoctorId(doctorId);
        prescription.setRegistrationId(registrationId);
        prescription.setMedicineList(medicineList);
        prescription.setDosage(dosage);
        prescription.setUsage(usage);
        prescription.setStatus("submitted");

        Prescription saved = prescriptionRepository.save(prescription);
        log.info("处方创建成功 - ID: {}", saved.getId());

        return saved;
    }

    public Map<String, Object> checkPrescriptionByAi(Long prescriptionId) {
        log.info("AI审核处方 - 处方ID: {}", prescriptionId);

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new BusinessException(404, "处方不存在"));

        Patient patient = patientRepository.findById(prescription.getPatientId()).orElse(null);
        Doctor doctor = doctorRepository.findById(prescription.getDoctorId()).orElse(null);

        String patientInfo = String.format("年龄: %s, 性别: %s",
                patient != null ? patient.getAge() : "未知",
                patient != null ? patient.getGender() : "未知");

        String aiResult = aiService.checkPrescription(prescription.getMedicineList(), patientInfo);

        Map<String, Object> result = new HashMap<>();
        result.put("prescriptionId", prescriptionId);
        result.put("checkResult", aiResult);
        result.put("riskLevel", "medium");
        result.put("medicationSuggestions", "");
        result.put("interactionDetection", "");
        result.put("riskHints", "");

        return result;
    }

    public List<Prescription> getPatientPrescriptions(Long patientId) {
        return prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    public List<Prescription> getDoctorPrescriptions(Long doctorId) {
        return prescriptionRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId);
    }

    public Prescription getPrescriptionDetail(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "处方不存在"));
    }

    @Transactional
    public PrescriptionCheck saveCheckResult(Long prescriptionId, String checkResult,
            String medicationSuggestions, String interactionDetection,
            String riskLevel, String riskHints) {

        PrescriptionCheck check = new PrescriptionCheck();
        check.setPrescriptionId(prescriptionId);
        check.setCheckResult(checkResult);
        check.setMedicationSuggestions(medicationSuggestions);
        check.setInteractionDetection(interactionDetection);
        check.setRiskLevel(riskLevel);
        check.setRiskHints(riskHints);

        PrescriptionCheck saved = prescriptionCheckRepository.save(check);

        Prescription prescription = prescriptionRepository.findById(prescriptionId).orElse(null);
        if (prescription != null) {
            prescription.setStatus("checked");
            prescriptionRepository.save(prescription);
        }

        return saved;
    }
}
