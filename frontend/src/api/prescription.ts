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

export function checkPrescription(prescriptionId: number) {
  return post<AiCheckResult>(`/prescription/check/${prescriptionId}`)
}

export function recommendMedicine(params: { symptoms?: string; diagnosis?: string; department?: string }) {
  return post<any[]>("/prescription/recommend", null, { params })
}
