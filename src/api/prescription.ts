import { get, post } from "./index"
import type { PrescriptionForm, PrescriptionRecord, AICheckResult } from "@/types"

export function createPrescription(data: PrescriptionForm) {
  return post<PrescriptionRecord>("/prescription/create", data)
}

export function getPrescriptionList(params: any) {
  return get<PrescriptionRecord[]>("/prescription/list", params)
}

export function checkPrescription(prescriptionId: number) {
  return post<AICheckResult>(`/prescription/check/${prescriptionId}`)
}
