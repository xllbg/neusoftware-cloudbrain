package com.neusoft.cloudbrain.repository;

import com.neusoft.cloudbrain.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByDepartment(String department);
    List<Doctor> findByDepartmentContaining(String departmentKeyword);
    Optional<Doctor> findByUsername(String username);
    Optional<Doctor> findByPhone(String phone);
    List<Doctor> findByStatus(String status);
}
