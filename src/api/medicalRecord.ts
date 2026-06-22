import { get, post } from "./index"
import type { MedicalRecord, AIGenerateRecordRequest, MedicalRecordForm } from "@/types"

export function generateMedicalRecord(data: AIGenerateRecordRequest) {
  return post<MedicalRecord>("/medical-record/generate", data)
}

export function saveMedicalRecord(data: MedicalRecordForm) {
  return post<MedicalRecord>("/medical-record/save", data)
}

export function getMedicalRecordList(params: any) {
  return get<MedicalRecord[]>("/medical-record/list", params)
}

export function getMedicalRecordDetail(id: number) {
  return get<MedicalRecord>(`/medical-record/detail/${id}`)
}
