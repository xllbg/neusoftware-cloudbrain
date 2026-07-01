import { get, post } from "./index"
import type { PrescriptionForm, PrescriptionRecord, AiCheckResult } from "@/types"

export function createPrescription(data: PrescriptionForm) {
  return post<PrescriptionRecord>("/prescription/create", data)
}

export function getPrescriptionList(params: { patientId?: number; doctorId?: number }) {
  return get<PrescriptionRecord[]>("/prescription/list", params)
}

export function getPrescriptionDetail(id: number) {
  return get<PrescriptionRecord>("/prescription/detail", { id })
}

export function getPrescriptionByRegistration(registrationId: number) {
  return get<PrescriptionRecord>(`/prescription/by-registration/${registrationId}`)
}

export function checkPrescription(prescriptionId: number) {
  return post<AiCheckResult>(`/prescription/check/${prescriptionId}`)
}

// 直接AI审核处方（不保存）
export function aiCheckPrescription(medicineText: string, patientInfo: string) {
  return post<AiCheckResult>("/prescription/ai-check", null, {
    params: {
      medicineText,
      patientInfo,
    },
  })
}

// 获取处方审核结果
export function getPrescriptionCheckResult(prescriptionId: number) {
  return get<AiCheckResult>("/prescription/check-result", { prescriptionId })
}

export function recommendMedicine(params: { symptoms?: string; diagnosis?: string; department?: string; registrationId?: number }) {
  return post<any[]>("/prescription/recommend", null, { params })
}
