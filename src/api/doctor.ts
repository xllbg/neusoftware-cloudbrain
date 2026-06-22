import { get } from "./index"
import type { DoctorInfo } from "@/types"

// 获取所有科室列表
export function getDepartments() {
  return get<string[]>("/doctor/departments")
}

// 按科室获取医生列表
export function getDoctorsByDepartment(department: string) {
  return get<DoctorInfo[]>("/doctor/list", { department })
}

// 获取医生详情
export function getDoctorDetail(doctorId: number) {
  return get<DoctorInfo>(`/doctor/detail/${doctorId}`)
}
