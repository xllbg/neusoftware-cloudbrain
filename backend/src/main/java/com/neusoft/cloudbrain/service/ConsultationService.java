package com.neusoft.cloudbrain.service;

import com.neusoft.cloudbrain.dto.ConsultationMessageDTO;
import com.neusoft.cloudbrain.entity.ConsultationMessage;
import com.neusoft.cloudbrain.entity.Doctor;
import com.neusoft.cloudbrain.entity.Patient;
import com.neusoft.cloudbrain.entity.Registration;
import com.neusoft.cloudbrain.exception.BusinessException;
import com.neusoft.cloudbrain.repository.ConsultationMessageRepository;
import com.neusoft.cloudbrain.repository.DoctorRepository;
import com.neusoft.cloudbrain.repository.PatientRepository;
import com.neusoft.cloudbrain.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationService {

    private final ConsultationMessageRepository messageRepository;
    private final RegistrationRepository registrationRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    @Transactional
    public ConsultationMessageDTO sendMessage(Long registrationId, String senderType, Long senderId, String content) {
        log.info("发送问诊消息 - 挂号ID: {}, 发送者类型: {}, 发送者ID: {}", registrationId, senderType, senderId);

        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new BusinessException(404, "挂号记录不存在"));

        String senderName;
        if ("DOCTOR".equals(senderType)) {
            Doctor doctor = doctorRepository.findById(senderId)
                    .orElseThrow(() -> new BusinessException(404, "医生不存在"));
            senderName = doctor.getName();
        } else {
            Patient patient = patientRepository.findById(senderId)
                    .orElseThrow(() -> new BusinessException(404, "患者不存在"));
            senderName = patient.getName();
        }

        ConsultationMessage message = new ConsultationMessage();
        message.setRegistrationId(registrationId);
        message.setPatientId(registration.getPatientId());
        message.setDoctorId(registration.getDoctorId());
        message.setSenderType(senderType);
        message.setSenderId(senderId);
        message.setSenderName(senderName);
        message.setContent(content);
        message.setType("text");
        message.setIsRead(false);

        ConsultationMessage saved = messageRepository.save(message);
        log.info("消息发送成功 - 消息ID: {}", saved.getId());

        return toDTO(saved);
    }

    public List<ConsultationMessageDTO> getMessageList(Long registrationId) {
        log.info("查询问诊消息列表 - 挂号ID: {}", registrationId);

        List<ConsultationMessage> messages = messageRepository.findByRegistrationIdOrderByCreatedAtAsc(registrationId);

        return messages.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private ConsultationMessageDTO toDTO(ConsultationMessage message) {
        return ConsultationMessageDTO.builder()
                .id(message.getId())
                .registrationId(message.getRegistrationId())
                .patientId(message.getPatientId())
                .doctorId(message.getDoctorId())
                .senderType(message.getSenderType())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .content(message.getContent())
                .type(message.getType())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
