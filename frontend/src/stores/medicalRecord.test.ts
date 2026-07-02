import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useMedicalRecordStore } from '@/stores/medicalRecord'

const mockGetList = vi.fn()
const mockGetDetail = vi.fn()
const mockGetByRegistration = vi.fn()
const mockSave = vi.fn()
const mockGenerate = vi.fn()

vi.mock('@/api/medicalRecord', () => ({
  getMedicalRecordList: (...args: any[]) => mockGetList(...args),
  getMedicalRecordDetail: (...args: any[]) => mockGetDetail(...args),
  getMedicalRecordByRegistration: (...args: any[]) => mockGetByRegistration(...args),
  saveMedicalRecord: (...args: any[]) => mockSave(...args),
  generateMedicalRecord: (...args: any[]) => mockGenerate(...args),
}))

const mockRecord = {
  id: 1,
  patientId: 10,
  patientName: '测试患者',
  doctorId: 5,
  doctorName: '张医生',
  registrationId: 100,
  chiefComplaint: '头痛',
  presentIllness: '头痛3天',
  pastHistory: '无',
  physicalExamination: '无异常',
  diagnosis: '偏头痛',
  treatmentPlan: '口服止痛药',
  createdAt: '2026-07-01T08:00:00',
}

const mockAiResult = {
  presentIllness: '患者自述头痛3天',
  pastHistory: '无特殊病史',
  physicalExamination: '未见明显异常',
  diagnosis: '紧张性头痛',
  treatmentPlan: '建议休息',
  aiRawResult: 'AI生成的原始结果',
}

describe('medicalRecord store', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('should initialize with default values', () => {
    const store = useMedicalRecordStore()
    expect(store.records).toEqual([])
    expect(store.currentRecord).toBeNull()
    expect(store.loading).toBe(false)
  })

  it('should fetch list and update records', async () => {
    const recordWithCreateTime = { ...mockRecord, createTime: mockRecord.createdAt }
    mockGetList.mockResolvedValue({ data: [mockRecord] })
    const store = useMedicalRecordStore()

    const result = await store.fetchList({ patientId: 10 })

    expect(mockGetList).toHaveBeenCalledWith({ patientId: 10 })
    expect(store.records).toEqual([recordWithCreateTime])
    expect(store.loading).toBe(false)
  })

  it('should fetch list with createTime fallback', async () => {
    const recordWithOnlyCreatedAt = { ...mockRecord, createTime: undefined }
    delete recordWithOnlyCreatedAt.createTime
    mockGetList.mockResolvedValue({ data: [recordWithOnlyCreatedAt] })
    const store = useMedicalRecordStore()

    await store.fetchList({ patientId: 10 })

    expect(store.records[0].createTime).toBe(mockRecord.createdAt)
  })

  it('should handle empty list', async () => {
    mockGetList.mockResolvedValue({ data: [] })
    const store = useMedicalRecordStore()

    const result = await store.fetchList({ patientId: 10 })

    expect(store.records).toEqual([])
    expect(result).toEqual([])
  })

  it('should handle fetch list error', async () => {
    mockGetList.mockRejectedValue(new Error('Network error'))
    const store = useMedicalRecordStore()

    await expect(store.fetchList({ patientId: 10 })).rejects.toThrow('Network error')
    expect(store.loading).toBe(false)
  })

  it('should fetch detail and set currentRecord', async () => {
    mockGetDetail.mockResolvedValue({ data: mockRecord })
    const store = useMedicalRecordStore()

    const result = await store.fetchDetail(1)

    expect(mockGetDetail).toHaveBeenCalledWith(1)
    expect(store.currentRecord).toEqual(mockRecord)
    expect(result).toEqual(mockRecord)
    expect(store.loading).toBe(false)
  })

  it('should save record and return data', async () => {
    const savedRecord = { ...mockRecord, id: 2 }
    mockSave.mockResolvedValue({ data: savedRecord })
    const store = useMedicalRecordStore()

    const form = {
      patientId: 10,
      doctorId: 5,
      chiefComplaint: '头痛',
      presentIllness: '头痛3天',
      pastHistory: '无',
      physicalExamination: '无异常',
      diagnosis: '偏头痛',
      treatmentPlan: '口服止痛药',
    }

    const result = await store.save(form)

    expect(mockSave).toHaveBeenCalledWith(form)
    expect(result).toEqual(savedRecord)
  })

  it('should fetch by registration id', async () => {
    mockGetByRegistration.mockResolvedValue({ data: mockRecord })
    const store = useMedicalRecordStore()

    const result = await store.fetchByRegistration(100)

    expect(mockGetByRegistration).toHaveBeenCalledWith(100)
    expect(result).toEqual(mockRecord)
    expect(store.loading).toBe(false)
  })

  it('should handle fetch by registration error', async () => {
    mockGetByRegistration.mockRejectedValue(new Error('Not found'))
    const store = useMedicalRecordStore()

    await expect(store.fetchByRegistration(999)).rejects.toThrow('Not found')
    expect(store.loading).toBe(false)
  })

  it('should generate medical record with AI', async () => {
    mockGenerate.mockResolvedValue({ data: mockAiResult })
    const store = useMedicalRecordStore()

    const result = await store.generate(10, '患者说头痛')

    expect(mockGenerate).toHaveBeenCalledWith(10, '患者说头痛')
    expect(result.diagnosis).toBe('紧张性头痛')
    expect(result.aiRawResult).toBe('AI生成的原始结果')
  })
})
