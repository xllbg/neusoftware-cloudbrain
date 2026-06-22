// ===== 通用响应类型 =====
export interface ApiResult<T = any> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

// ===== 用户相关 =====
export interface UserInfo {
  id: number
  username: string
  name: string
  role: 'PATIENT' | 'DOCTOR'
  phone: string
  gender: string
  age: number
}

export interface LoginForm {
  username: string
  password: string
  role: 'PATIENT' | 'DOCTOR'
}

export interface RegisterForm {
  username: string
  password: string
  name: string
  phone: string
  gender: string
  age: number
}

export interface LoginResult {
  token: string
  userInfo: UserInfo
}

// ===== 医生相关 =====
export interface DoctorInfo {
  id: number
  name: string
  department: string
  title: string
  description: string
  available: boolean
}

// ===== 挂号相关 =====
export interface RegistrationForm {
  patientId: number
  doctorId: number
  registrationDate: string
  timeSlot: string
  symptom: string
}

export interface RegistrationRecord {
  id: number
  patientId: number
  patientName: string
  doctorId: number
  doctorName: string
  department: string
  registrationDate: string
  timeSlot: string
  status: 'PENDING' | 'COMPLETED' | 'CANCELLED'
  symptom: string
  triageResult: string
  createTime: string
}

// ===== 分诊相关 =====
export interface TriageRequest {
  symptom: string
  patientId?: number
}

export interface TriageResult {
  department: string
  doctorId: number
  doctorName: string
  doctorTitle: string
  confidence: number
  reason: string
  suggestions: string[]
}

// ===== 处方相关 =====
export interface PrescriptionItem {
  drugName: string
  dosage: string
  frequency: string
  duration: string
  note: string
}

export interface PrescriptionForm {
  patientId: number
  doctorId: number
  diagnosis: string
  items: PrescriptionItem[]
}

export interface PrescriptionRecord {
  id: number
  patientId: number
  patientName: string
  doctorId: number
  doctorName: string
  diagnosis: string
  items: PrescriptionItem[]
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  aiCheckResult: string
  createTime: string
}

export interface AICheckResult {
  approved: boolean
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH'
  suggestions: string[]
  warnings: string[]
}

// ===== 病历相关 =====
export interface MedicalRecordForm {
  patientId: number
  doctorId: number
  diagnosis: string
  chiefComplaint: string
  presentIllness: string
  pastHistory: string
  examinationResult: string
  treatmentPlan: string
}

export interface MedicalRecord {
  id: number
  patientId: number
  patientName: string
  doctorId: number
  doctorName: string
  chiefComplaint: string
  presentIllness: string
  pastHistory: string
  examinationResult: string
  diagnosis: string
  treatmentPlan: string
  aiGenerated: boolean
  createTime: string
}

export interface AIGenerateRecordRequest {
  chiefComplaint: string
  presentIllness: string
  pastHistory?: string
}
