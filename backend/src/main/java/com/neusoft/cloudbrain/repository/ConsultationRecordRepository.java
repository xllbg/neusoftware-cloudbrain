package com.neusoft.cloudbrain.repository;

import com.neusoft.cloudbrain.entity.ConsultationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationRecordRepository extends JpaRepository<ConsultationRecord, Long> {

    Optional<ConsultationRecord> findByRegistrationId(Long registrationId);

    Optional<ConsultationRecord> findByRegistrationIdAndPatientIdAndDoctorId(
            Long registrationId, Long patientId, Long doctorId);

    List<ConsultationRecord> findByDoctorIdOrderByUpdatedAtDesc(Long doctorId);

    List<ConsultationRecord> findByPatientIdOrderByUpdatedAtDesc(Long patientId);

    boolean existsByRegistrationId(Long registrationId);
}
