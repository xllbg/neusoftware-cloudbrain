package com.neusoft.cloudbrain.repository;

import com.neusoft.cloudbrain.entity.ConsultationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultationMessageRepository extends JpaRepository<ConsultationMessage, Long> {

    List<ConsultationMessage> findByRegistrationIdOrderByCreatedAtAsc(Long registrationId);

    List<ConsultationMessage> findByPatientIdAndDoctorIdOrderByCreatedAtAsc(Long patientId, Long doctorId);
}
