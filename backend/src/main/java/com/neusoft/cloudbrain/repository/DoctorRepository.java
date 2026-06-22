package com.neusoft.cloudbrain.repository;

import com.neusoft.cloudbrain.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByDepartment(String department);

    List<Doctor> findByDepartmentContaining(String departmentKeyword);
}
