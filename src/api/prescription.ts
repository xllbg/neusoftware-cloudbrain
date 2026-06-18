import { get, post } from "./index"
import type { PrescriptionForm, PrescriptionRecord, AICheckResult } from "@/types"

// 创建处方
export function createPrescription(data: PrescriptionForm) {
  return post<PrescriptionRecord>("/prescription/create", data)
}

// 获取处方列表
export function getPrescriptionList(params: { patientId?: number; doctorId?: number }) {
  return get<PrescriptionRecord[]>("/prescription/list", params)
}

// AI 审核处方
export function checkPrescription(prescriptionId: number) {
  return post<AICheckResult>(`/prescription/check/${prescriptionId}`)
}
