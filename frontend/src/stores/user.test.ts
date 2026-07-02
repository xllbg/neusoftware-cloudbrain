import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '@/stores/user'
import { setDoctorToken, setDoctorUser, getDoctorToken, getDoctorUser, setPatientToken, setPatientUser, getPatientToken, getPatientUser } from '@/utils/auth'

const mockLoginDoctor = vi.fn()
const mockLoginPatient = vi.fn()
const mockRegisterPatient = vi.fn()
const mockLoginPatientByPhone = vi.fn()
const mockLoginDoctorByPhone = vi.fn()

vi.mock('@/api/user', () => ({
  loginDoctor: (...args: any[]) => mockLoginDoctor(...args),
  loginPatient: (...args: any[]) => mockLoginPatient(...args),
  registerPatient: (...args: any[]) => mockRegisterPatient(...args),
  loginPatientByPhone: (...args: any[]) => mockLoginPatientByPhone(...args),
  loginDoctorByPhone: (...args: any[]) => mockLoginDoctorByPhone(...args),
}))

const mockRoutePath = vi.hoisted(() => ({ value: { path: '/doctor' } }))
vi.mock('@/router', () => ({
  default: {
    currentRoute: mockRoutePath,
    push: vi.fn(),
  }
}))

describe('user store - doctor', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('should initialize with default values', () => {
    const store = useUserStore()
    expect(store.token).toBeNull()
    expect(store.userInfo).toBeNull()
    expect(store.isLoggedIn).toBe(false)
    expect(store.isDoctor).toBe(false)
    expect(store.isPatient).toBe(false)
    expect(store.userName).toBe('')
    expect(store.userId).toBeUndefined()
  })

  it('should reflect doctor login state from storage on doctor path', () => {
    setDoctorToken('doc-token-123')
    setDoctorUser({ userId: 5, name: '李医生', role: 'DOCTOR', department: '心内科' })

    const store = useUserStore()
    store.refreshFromStorage()

    expect(store.token).toBe('doc-token-123')
    expect(store.isLoggedIn).toBe(true)
    expect(store.isDoctor).toBe(true)
    expect(store.isPatient).toBe(false)
    expect(store.userName).toBe('李医生')
    expect(store.userId).toBe(5)
  })

  it('should return correct userName from userInfo', () => {
    setDoctorUser({ userId: 3, name: '王医生', role: 'DOCTOR' })

    const store = useUserStore()
    store.refreshFromStorage()

    expect(store.userName).toBe('王医生')
  })

  it('should return empty userName when no userInfo', () => {
    const store = useUserStore()
    expect(store.userName).toBe('')
  })

  it('isLoggedIn should be true when token exists', () => {
    setDoctorToken('some-token')

    const store = useUserStore()
    store.refreshFromStorage()

    expect(store.isLoggedIn).toBe(true)
  })

  it('isLoggedIn should be false when token is null', () => {
    const store = useUserStore()
    expect(store.isLoggedIn).toBe(false)
  })

  it('isDoctor should be true for doctor role', () => {
    setDoctorUser({ userId: 1, name: '张医生', role: 'DOCTOR' })

    const store = useUserStore()
    store.refreshFromStorage()

    expect(store.isDoctor).toBe(true)
  })

  it('isDoctor should be false for patient role', () => {
    localStorage.clear()
    localStorage.setItem('cloud-brain-doctor-user', JSON.stringify({ userId: 2, name: '患者', role: 'PATIENT' }))

    const store = useUserStore()
    store.refreshFromStorage()

    expect(store.isDoctor).toBe(false)
  })

  it('isPatient should be false on doctor path with doctor user', () => {
    setDoctorUser({ userId: 1, name: '张医生', role: 'DOCTOR' })

    const store = useUserStore()
    store.refreshFromStorage()

    expect(store.isPatient).toBe(false)
  })

  it('should logout doctor correctly', () => {
    setDoctorToken('doc-token')
    setDoctorUser({ userId: 1, name: '张医生', role: 'DOCTOR' })

    const store = useUserStore()
    store.refreshFromStorage()
    expect(store.isLoggedIn).toBe(true)

    store.logout()

    expect(store.token).toBeNull()
    expect(store.userInfo).toBeNull()
    expect(store.isLoggedIn).toBe(false)
    expect(getDoctorToken()).toBeNull()
    expect(getDoctorUser()).toBeNull()
  })

  it('should not throw when logging out without user', () => {
    const store = useUserStore()
    expect(() => store.logout()).not.toThrow()
  })

  it('should login doctor via API and update store', async () => {
    const loginData = { token: 'new-doc-token', userId: 5, name: '新医生', role: 'DOCTOR', username: 'doc01' }
    mockLoginDoctor.mockResolvedValue({ data: loginData })

    const store = useUserStore()
    const result = await store.doctorLogin({ username: 'doc01', password: '123' })

    expect(mockLoginDoctor).toHaveBeenCalledWith({ username: 'doc01', password: '123' })
    expect(store.token).toBe('new-doc-token')
    expect(store.isLoggedIn).toBe(true)
    expect(store.isDoctor).toBe(true)
    expect(result).toEqual(loginData)
  })

  it('should login doctor by phone via API and update store', async () => {
    const loginData = { token: 'phone-doc-token', userId: 6, name: '李医生', role: 'DOCTOR' }
    mockLoginDoctorByPhone.mockResolvedValue({ data: loginData })

    const store = useUserStore()
    const result = await store.doctorLoginByPhone({ name: '李医生', phone: '13800000000', password: '123' })

    expect(mockLoginDoctorByPhone).toHaveBeenCalledWith({ name: '李医生', phone: '13800000000', password: '123' })
    expect(store.token).toBe('phone-doc-token')
    expect(result.role).toBe('DOCTOR')
  })
})

describe('user store - patient', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
    setActivePinia(createPinia())
    mockRoutePath.value.path = '/patient'
  })

  it('should reflect patient login state from storage on patient path', () => {
    setPatientToken('patient-token-456')
    setPatientUser({ userId: 10, name: '患者甲', role: 'PATIENT' })

    const store = useUserStore()
    store.refreshFromStorage()

    expect(store.token).toBe('patient-token-456')
    expect(store.isLoggedIn).toBe(true)
    expect(store.isPatient).toBe(true)
    expect(store.isDoctor).toBe(false)
    expect(store.userName).toBe('患者甲')
    expect(store.userId).toBe(10)
  })

  it('isPatient should be true for patient role', () => {
    setPatientUser({ userId: 10, name: '患者甲', role: 'PATIENT' })

    const store = useUserStore()
    store.refreshFromStorage()

    expect(store.isPatient).toBe(true)
    expect(store.isDoctor).toBe(false)
  })

  it('should logout patient correctly', () => {
    setPatientToken('patient-token')
    setPatientUser({ userId: 10, name: '患者甲', role: 'PATIENT' })

    const store = useUserStore()
    store.refreshFromStorage()
    expect(store.isLoggedIn).toBe(true)

    store.logout()

    expect(store.token).toBeNull()
    expect(store.userInfo).toBeNull()
    expect(store.isLoggedIn).toBe(false)
    expect(getPatientToken()).toBeNull()
    expect(getPatientUser()).toBeNull()
  })

  it('should not affect doctor storage when patient logs out', () => {
    setDoctorToken('doc-token')
    setDoctorUser({ userId: 5, name: '张医生', role: 'DOCTOR' })
    setPatientToken('patient-token')
    setPatientUser({ userId: 10, name: '患者甲', role: 'PATIENT' })

    const store = useUserStore()
    store.refreshFromStorage()
    store.logout()

    expect(getDoctorToken()).toBe('doc-token')
    expect(getDoctorUser()).not.toBeNull()
  })

  it('should login patient via API and update store', async () => {
    const loginData = { token: 'patient-login-token', userId: 20, name: '患者乙', role: 'PATIENT', username: 'pat01' }
    mockLoginPatient.mockResolvedValue({ data: loginData })

    const store = useUserStore()
    const result = await store.patientLogin({ username: 'pat01', password: '123' })

    expect(mockLoginPatient).toHaveBeenCalledWith({ username: 'pat01', password: '123' })
    expect(store.token).toBe('patient-login-token')
    expect(store.isLoggedIn).toBe(true)
    expect(store.isPatient).toBe(true)
    expect(store.isDoctor).toBe(false)
    expect(store.userName).toBe('患者乙')
    expect(store.userId).toBe(20)
    expect(result).toEqual(loginData)
  })

  it('should login patient by phone via API and update store', async () => {
    const loginData = { token: 'phone-patient-token', userId: 21, name: '患者丙', role: 'PATIENT' }
    mockLoginPatientByPhone.mockResolvedValue({ data: loginData })

    const store = useUserStore()
    const result = await store.patientLoginByPhone({ name: '患者丙', phone: '13900000000', password: '123' })

    expect(mockLoginPatientByPhone).toHaveBeenCalledWith({ name: '患者丙', phone: '13900000000', password: '123' })
    expect(store.token).toBe('phone-patient-token')
    expect(store.userName).toBe('患者丙')
    expect(result.role).toBe('PATIENT')
  })

  it('should register patient via API and update store', async () => {
    const registerData = { token: 'register-token', userId: 22, name: '新患者', role: 'PATIENT', username: 'newpat' }
    mockRegisterPatient.mockResolvedValue({ data: registerData })

    const store = useUserStore()
    const form = {
      username: 'newpat',
      password: '123456',
      name: '新患者',
      gender: '男',
      age: 25,
      phone: '13900000001',
    }
    const result = await store.patientRegister(form)

    expect(mockRegisterPatient).toHaveBeenCalledWith(form)
    expect(store.token).toBe('register-token')
    expect(store.isPatient).toBe(true)
    expect(store.userId).toBe(22)
    expect(result).toEqual(registerData)
  })

  it('should handle patient login API error', async () => {
    mockLoginPatient.mockRejectedValue(new Error('用户名或密码错误'))

    const store = useUserStore()
    await expect(store.patientLogin({ username: 'wrong', password: 'wrong' })).rejects.toThrow('用户名或密码错误')
    expect(store.isLoggedIn).toBe(false)
  })
})
