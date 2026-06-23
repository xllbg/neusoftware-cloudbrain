package com.neusoft.cloudbrain.repository;

import com.neusoft.cloudbrain.entity.PrescriptionCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrescriptionCheckRepository extends JpaRepository<PrescriptionCheck, Long> {

    Optional<PrescriptionCheck> findByPrescriptionId(Long prescriptionId);
}
