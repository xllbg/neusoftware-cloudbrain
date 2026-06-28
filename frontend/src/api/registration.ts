import { get, post } from "./index"
import type { RegistrationForm, RegistrationRecord } from "@/types"

export function createRegistration(data: RegistrationForm) {
  return post<RegistrationRecord>("/registration/create", data)
}

export function getRegistrationList(params: {
  patientId?: number
  doctorId?: number
  status?: string
  department?: string
  date?: string
}) {
  return get<RegistrationRecord[]>("/registration/list", params)
}

export function cancelRegistration(id: number, patientId: number) {
  return post<RegistrationRecord>(`/registration/cancel/${id}?patientId=${patientId}`)
}

export function startConsultation(id: number, doctorId: number) {
  return post<RegistrationRecord>(`/registration/start/${id}?doctorId=${doctorId}`)
}

export function completeConsultation(id: number, doctorId: number) {
  return post<RegistrationRecord>(`/registration/complete/${id}?doctorId=${doctorId}`)
}
