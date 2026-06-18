import { get, post } from "./index"
import type { RegistrationForm, RegistrationRecord } from "@/types"

// 创建挂号
export function createRegistration(data: RegistrationForm) {
  return post<RegistrationRecord>("/registration/create", data)
}

// 获取挂号记录列表
export function getRegistrationList(params: { patientId?: number; doctorId?: number; status?: string }) {
  return get<RegistrationRecord[]>("/registration/list", params)
}

// 取消挂号
export function cancelRegistration(id: number) {
  return post<RegistrationRecord>(`/registration/cancel/${id}`)
}
