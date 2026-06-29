package com.neusoft.cloudbrain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "prescription_check")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PrescriptionCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prescription_id", nullable = false)
    private Long prescriptionId;

    @Column(name = "check_result", columnDefinition = "TEXT")
    private String checkResult;

    @Column(name = "medication_suggestions", columnDefinition = "TEXT")
    private String medicationSuggestions;

    @Column(name = "interaction_detection", columnDefinition = "TEXT")
    private String interactionDetection;

    @Column(name = "risk_level", length = 20)
    private String riskLevel = "low";

    @Column(name = "risk_hints", columnDefinition = "TEXT")
    private String riskHints;

    @CreatedDate
    @Column(name = "checked_at", updatable = false)
    private LocalDateTime checkedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", insertable = false, updatable = false)
    private Prescription prescription;
}
