package com.neusoft.cloudbrain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "历史就诊记录响应")
public class MedicalHistoryResponse {

    @Schema(description = "挂号信息")
    private RegistrationResponse.RegistrationListItem registration;

    @Schema(description = "病历信息（可为空）")
    private MedicalRecordResponse medicalRecord;

    @Schema(description = "处方信息（可为空）")
    private PrescriptionResponse prescription;
}
