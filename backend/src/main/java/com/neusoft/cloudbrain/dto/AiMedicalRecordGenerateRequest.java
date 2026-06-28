package com.neusoft.cloudbrain.dto;

import lombok.Data;

@Data
public class AiMedicalRecordGenerateRequest {
    private String chiefComplaint;
    private String presentIllness;
    private String pastHistory;
}
