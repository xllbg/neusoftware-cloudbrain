package com.neusoft.cloudbrain.repository;

import com.neusoft.cloudbrain.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<Prescription> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);

    List<Prescription> findByRegistrationId(Long registrationId);

    List<Prescription> findByPatientIdAndStatus(Long patientId, String status);
}
