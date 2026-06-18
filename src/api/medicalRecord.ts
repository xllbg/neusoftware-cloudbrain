import { get, post } from "./index"
import type { MedicalRecord, AIGenerateRecordRequest, MedicalRecordForm } from "@/types"

// AI 生成病历
export function generateMedicalRecord(data: AIGenerateRecordRequest) {
  return post<MedicalRecord>("/medical-record/generate", data)
}

// 保存病历
export function saveMedicalRecord(data: MedicalRecordForm) {
  return post<MedicalRecord>("/medical-record/save", data)
}

// 获取病历列表
export function getMedicalRecordList(params: { patientId?: number; doctorId?: number }) {
  return get<MedicalRecord[]>("/medical-record/list", params)
}

// 获取病历详情
export function getMedicalRecordDetail(id: number) {
  return get<MedicalRecord>(`/medical-record/detail/${id}`)
}
