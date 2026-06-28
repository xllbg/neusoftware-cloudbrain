package com.neusoft.cloudbrain.repository;

import com.neusoft.cloudbrain.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByPhone(String phone);

    boolean existsByPhone(String phone);
}
