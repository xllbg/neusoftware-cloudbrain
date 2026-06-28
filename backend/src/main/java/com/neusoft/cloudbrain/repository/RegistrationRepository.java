package com.neusoft.cloudbrain.repository;

import com.neusoft.cloudbrain.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    List<Registration> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<Registration> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);

    List<Registration> findByPatientIdAndStatus(Long patientId, String status);
}
