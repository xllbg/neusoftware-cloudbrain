package com.neusoft.cloudbrain.service;

import com.neusoft.cloudbrain.dto.DoctorVO;
import com.neusoft.cloudbrain.entity.Doctor;
import com.neusoft.cloudbrain.exception.BusinessException;
import com.neusoft.cloudbrain.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public List<DoctorVO> listDoctors(String department) {
        List<Doctor> doctors;
        if (department != null && !department.isBlank()) {
            doctors = doctorRepository.findByDepartment(department);
        } else {
            doctors = doctorRepository.findAll();
        }

        return doctors.stream()
                .map(DoctorVO::fromEntity)
                .collect(Collectors.toList());
    }

    public DoctorVO getDoctorDetail(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "医生不存在"));
        return DoctorVO.fromEntity(doctor);
    }
}
