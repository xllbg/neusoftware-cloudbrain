// ===== 统一响应类型（匹配后端 CommonResult.java）=====
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
  [key: string]: any
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

// ===== 挂号（匹配后端 RegistrationResponse）=====
export interface RegistrationForm {
  patientId: number
  doctorId: number
  department: string
  registrationDate: string
  timeSlot: string
  symptom?: string
}

export interface RegistrationRecord {
  id: number
  patientId: number
  patientName: string
  doctorId?: number
  doctorName: string
  department: string
  doctorTitle: string
  hospital: string
  registrationDate: string
  timeSlot: string
  status: string
  symptom: string
  triageResult: string
  createdAt: string
}

// ===== 问诊消息（匹配后端 ConsultationMessageDTO）=====
export interface ConsultationMessage {
  id: number
  registrationId: number
  patientId: number
  doctorId: number
  senderType: string
  senderId: number
  senderName: string
  content: string
  type: string
  isRead: boolean
  createdAt: string
}

// ===== 处方（匹配后端 PrescriptionRequest / PrescriptionResponse）=====
export interface PrescriptionForm {
  patientId: number
  doctorId: number
  registrationId?: number
  medicineList: string
  dosage?: string
  usage?: string
}

export interface PrescriptionMedicineItem {
  name: string
  dose: string
  frequency: string
}

export interface PrescriptionRecord {
  id: number
  patientId: number
  patientName: string
  doctorId: number
  doctorName: string
  registrationId?: number
  medicineList: string
  dosage?: string
  usage?: string
  diagnosis?: string
  status: string
  createdAt: string
  createTime?: string
  aiCheckResult?: AiCheckResult
  items?: any[]
}

export interface AiCheckResult {
  checkResult: string
  riskLevel: string
  medicationSuggestions: string
  interactionDetection: string
  riskHints: string
}

// ===== 病历（匹配后端 MedicalRecordRequest / MedicalRecordResponse）=====
export interface MedicalRecordForm {
  patientId: number
  doctorId: number
  registrationId?: number
  chiefComplaint: string
  presentIllness: string
  pastHistory: string
  physicalExamination: string
  diagnosis: string
  treatmentPlan: string
}

export interface MedicalRecord {
  id: number
  patientId: number
  patientName: string
  doctorId: number
  doctorName: string
  registrationId?: number
  chiefComplaint: string
  presentIllness: string
  pastHistory: string
  physicalExamination: string
  examinationResult?: string
  diagnosis: string
  treatmentPlan: string
  createdAt: string
  createTime?: string
  aiGenerated?: boolean
  aiResult?: AiMedicalRecordResult
}

export interface AiMedicalRecordResult {
  presentIllness: string
  pastHistory: string
  physicalExamination: string
  diagnosis: string
  treatmentPlan: string
  aiRawResult: string
}

export interface AiGenerateRecordRequest {
  patientId: number
  dialogueText: string
}
