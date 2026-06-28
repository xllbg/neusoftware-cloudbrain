// 医生管理 API（对齐后端 DoctorController）
import { get } from "./index"
import type { DoctorInfo } from "@/types"

// 按科室获取医生列表 → GET /api/doctor/list?department=
export function getDoctorsByDepartment(department: string) {
  return get<DoctorInfo[]>("/doctor/list", { department })
}

// 获取医生详情 → GET /api/doctor/detail?id=
export function getDoctorDetail(id: number) {
  return get<DoctorInfo>("/doctor/detail", { id })
}
