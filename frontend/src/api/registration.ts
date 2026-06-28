// 以下模块后端尚未实现，保留请求定义供后续对接
import { get, post } from "./index"
import type { RegistrationForm, RegistrationRecord } from "@/types"

export function createRegistration(data: RegistrationForm) {
  return post<RegistrationRecord>("/registration/create", data)
}

export function getRegistrationList(params: any) {
  return get<RegistrationRecord[]>("/registration/list", params)
}

export function cancelRegistration(id: number, patientId: number) {
  return post<RegistrationRecord>(`/registration/cancel/${id}?patientId=${patientId}`)
}
