package com.neusoft.cloudbrain.repository;

import com.neusoft.cloudbrain.entity.Triage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TriageRepository extends JpaRepository<Triage, Long> {

    List<Triage> findByPatientId(Long patientId);
}
