import { get, post } from "./index"
import type { MedicalRecord, MedicalRecordForm, AiMedicalRecordResult } from "@/types"

export function generateMedicalRecord(patientId: number, dialogueText: string) {
  return post<AiMedicalRecordResult>(`/medical-record/generate?patientId=${patientId}`, { dialogueText })
}

export function optimizeMedicalRecord(data: {
  chiefComplaint?: string
  presentIllness?: string
  pastHistory?: string
  physicalExamination?: string
  diagnosis?: string
  treatmentPlan?: string
}) {
  return post<AiMedicalRecordResult>("/medical-record/optimize", data)
}

export function saveMedicalRecord(data: MedicalRecordForm) {
  return post<MedicalRecord>("/medical-record/save", data)
}

export function getMedicalRecordList(params: { patientId?: number; doctorId?: number }) {
  return get<MedicalRecord[]>("/medical-record/list", params)
}

export function getMedicalRecordByRegistration(registrationId: number) {
  return get<MedicalRecord>(`/medical-record/by-registration/${registrationId}`)
}

export function getMedicalRecordDetail(id: number) {
  return get<MedicalRecord>(`/medical-record/detail/${id}`)
}
