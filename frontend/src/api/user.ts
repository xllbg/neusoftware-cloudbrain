import { get, post, encryptedPost } from "./index"
import type { LoginForm, RegisterForm, LoginResult } from "@/types"

export function registerPatient(data: RegisterForm) {
  return encryptedPost<LoginResult>("/patient/register", data)
}

export function loginPatient(data: { username: string; password: string }) {
  return encryptedPost<LoginResult>("/patient/login", data)
}

export function loginPatientByPhone(data: { name: string; phone: string; password: string }) {
  return encryptedPost<LoginResult>("/patient/login/phone", data)
}

export function loginDoctor(data: { username: string; password: string }) {
  return encryptedPost<LoginResult>("/doctor/login", data)
}

export function loginDoctorByPhone(data: { name: string; phone: string; password: string }) {
  return encryptedPost<LoginResult>("/doctor/login/phone", data)
}

// 医生注册（加密传输）
export function registerDoctor(data: any) {
  return encryptedPost<string>("/doctor/register", data)
}

// 查询医生审核状态（姓名+手机号）
export function getDoctorStatus(name: string, phone: string) {
  return get<any>("/doctor/status", { name, phone })
}

// 获取待审批医生列表（管理员）
export function getPendingDoctors() {
  return get<any[]>("/doctor/pending")
}

// 批准医生
export function approveDoctor(id: number) {
  return post<string>("/doctor/approve", { id })
}

// 拒绝医生
export function rejectDoctor(id: number, reason: string) {
  return post<string>("/doctor/reject", { id, reason })
}
