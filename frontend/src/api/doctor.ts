// 医生管理 API（对齐后端 DoctorController）
import { get, post } from "./index"
import type { DoctorInfo } from "@/types"

// 按科室获取医生列表 → GET /api/doctor/list?department=
export function getDoctorsByDepartment(department: string) {
  return get<DoctorInfo[]>("/doctor/list", { department })
}

// 获取医生详情 → GET /api/doctor/detail?id=
export function getDoctorDetail(id: number) {
  return get<DoctorInfo>("/doctor/detail", { id })
}

// 问诊记录相关 API
export interface ConsultationRecordData {
  registrationId: number
  patientId: number
  doctorId: number
  presentIllness?: string
  pastHistory?: string
  physicalExamination?: string
  diagnosis?: string
  chiefComplaint?: string
  treatmentPlan?: string
  aiRecommended?: boolean
}

export interface ConsultationRecordRecommendData {
  presentIllness?: string
  pastHistory?: string
  physicalExamination?: string
  diagnosis?: string
  chiefComplaint?: string
  treatmentPlan?: string
}

// 获取问诊记录
export function getConsultationRecord(registrationId: number) {
  return get("/consultation-record/get", { registrationId })
}

// 保存问诊记录
export function saveConsultationRecord(data: ConsultationRecordData) {
  return post("/consultation-record/save", data)
}

// AI推荐问诊疗法
export function recommendConsultationByAi(registrationId: number) {
  return get<ConsultationRecordRecommendData>("/consultation-record/recommend", { registrationId })
}

// 获取医生问诊记录列表
export function getConsultationRecordList(doctorId: number) {
  return get<ConsultationRecordData[]>("/consultation-record/list", { doctorId })
}
