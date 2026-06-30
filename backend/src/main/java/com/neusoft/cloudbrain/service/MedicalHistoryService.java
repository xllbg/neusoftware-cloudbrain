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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalHistoryService {

    private final RegistrationRepository registrationRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public List<MedicalHistoryResponse> getPatientMedicalHistory(Long patientId) {
        log.info("查询患者历史就诊记录 - 患者ID: {}", patientId);

        List<Registration> registrations = registrationRepository.findByPatientIdOrderByCreatedAtDesc(patientId);

        List<MedicalHistoryResponse> result = new ArrayList<>();
        for (Registration reg : registrations) {
            MedicalHistoryResponse response = buildMedicalHistoryResponse(reg);
            result.add(response);
        }

        return result;
    }

    public MedicalHistoryResponse getMedicalHistoryDetail(Long patientId, Long registrationId) {
        log.info("查询就诊详情 - 患者ID: {}, 挂号ID: {}", patientId, registrationId);

        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new BusinessException(404, "挂号记录不存在"));

        if (!registration.getPatientId().equals(patientId)) {
            throw new BusinessException(403, "无权查看此就诊记录");
        }

        return buildMedicalHistoryResponse(registration);
    }

    private MedicalHistoryResponse buildMedicalHistoryResponse(Registration registration) {
        Long regId = registration.getId();
        Long doctorId = registration.getDoctorId();

        Patient patient = patientRepository.findById(registration.getPatientId()).orElse(null);
        Doctor doctor = doctorId != null && doctorId > 0
                ? doctorRepository.findById(doctorId).orElse(null)
                : null;

        RegistrationResponse.RegistrationListItem registrationItem = RegistrationResponse.RegistrationListItem.builder()
                .id(registration.getId())
                .patientId(registration.getPatientId())
                .patientName(patient != null ? patient.getName() : "未知患者")
                .doctorName(doctor != null ? doctor.getName() : "待分配")
                .department(registration.getDepartment())
                .doctorTitle(doctor != null ? doctor.getTitle() : null)
                .hospital(doctor != null ? doctor.getHospital() : null)
                .registrationDate(registration.getRegistrationDate())
                .timeSlot(registration.getTimeSlot())
                .status(registration.getStatus())
                .symptom(registration.getSymptom())
                .triageResult(registration.getTriageResult())
                .createdAt(registration.getCreatedAt())
                .build();

        MedicalRecordResponse medicalRecordResponse = null;
        PrescriptionResponse prescriptionResponse = null;

        List<MedicalRecord> medicalRecords = medicalRecordRepository.findByRegistrationId(regId);
        if (!medicalRecords.isEmpty()) {
            MedicalRecord record = medicalRecords.get(0);
            medicalRecordResponse = toMedicalRecordResponse(record);
        }

        List<Prescription> prescriptions = prescriptionRepository.findByRegistrationId(regId);
        if (!prescriptions.isEmpty()) {
            Prescription prescription = prescriptions.get(0);
            prescriptionResponse = toPrescriptionResponse(prescription);
        }

        return MedicalHistoryResponse.builder()
                .registration(registrationItem)
                .medicalRecord(medicalRecordResponse)
                .prescription(prescriptionResponse)
                .build();
    }

    private MedicalRecordResponse toMedicalRecordResponse(MedicalRecord record) {
        String patientName = patientRepository.findById(record.getPatientId())
                .map(Patient::getName).orElse("未知患者");
        String doctorName = doctorRepository.findById(record.getDoctorId())
                .map(Doctor::getName).orElse("未知医生");

        return MedicalRecordResponse.builder()
                .id(record.getId())
                .patientId(record.getPatientId())
                .patientName(patientName)
                .doctorId(record.getDoctorId())
                .doctorName(doctorName)
                .registrationId(record.getRegistrationId())
                .chiefComplaint(record.getChiefComplaint())
                .presentIllness(record.getPresentIllness())
                .pastHistory(record.getPastHistory())
                .physicalExamination(record.getPhysicalExamination())
                .diagnosis(record.getDiagnosis())
                .treatmentPlan(record.getTreatmentPlan())
                .createdAt(record.getCreatedAt())
                .build();
    }

    private PrescriptionResponse toPrescriptionResponse(Prescription prescription) {
        String patientName = patientRepository.findById(prescription.getPatientId())
                .map(Patient::getName).orElse("未知患者");
        String doctorName = doctorRepository.findById(prescription.getDoctorId())
                .map(Doctor::getName).orElse("未知医生");

        return PrescriptionResponse.builder()
                .id(prescription.getId())
                .patientId(prescription.getPatientId())
                .patientName(patientName)
                .doctorId(prescription.getDoctorId())
                .doctorName(doctorName)
                .registrationId(prescription.getRegistrationId())
                .medicineList(prescription.getMedicineList())
                .dosage(prescription.getDosage())
                .usage(prescription.getUsage())
                .status(prescription.getStatus())
                .createdAt(prescription.getCreatedAt())
                .build();
    }
}
