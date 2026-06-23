// ===== 统一响应类型（匹配后端 Result.java）=====
export interface ApiResult<T = any> {
  code: number
  message: string
  data: T
}

// ===== 登录/注册（匹配后端 LoginResponse）=====
export interface LoginResult {
  token: string
  userId: number
  username: string
  name: string
  role: string
}

export interface LoginForm {
  name: string
  phone: string
  password: string
}

export interface RegisterForm {
  username: string
  password: string
  name: string
  gender: string
  age: number
  phone: string
  idCard?: string
  address?: string
}

export interface DoctorInfo {
  id: number
  name: string
  gender: string
  age: number
  department: string
  title: string
  hospital: string
  phone: string
  avatar: string
  introduction: string
}

// ===== 分诊（匹配后端 TriageRequest / TriageResponse）=====
export interface TriageRequest {
  patientId: number
  age: number
  gender: string
  symptoms: string
}

export interface TriageResult {
  department: string
  reasoning: string
  doctors: TriageDoctor[]
}

export interface TriageDoctor {
  id: number
  name: string
  title: string
  hospital: string
}

// ===== 以下模块后端尚未实现，保留类型定义供后续对接 =====

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
  status: string
  symptom: string
  triageResult: string
  createTime: string
}

export interface PrescriptionForm {
  patientId: number
  doctorId: number
  diagnosis: string
  items: PrescriptionItem[]
}

export interface PrescriptionItem {
  drugName: string
  dosage: string
  frequency: string
  duration: string
  note: string
}

export interface PrescriptionRecord {
  id: number
  patientId: number
  patientName: string
  doctorId: number
  doctorName: string
  diagnosis: string
  items: PrescriptionItem[]
  status: string
  aiCheckResult: string
  createTime: string
}

export interface AICheckResult {
  approved: boolean
  riskLevel: string
  suggestions: string[]
  warnings: string[]
}

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
