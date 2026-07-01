import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { usePrescriptionStore } from '@/stores/prescription'

const mockGetList = vi.fn()
const mockGetDetail = vi.fn()
const mockGetByRegistration = vi.fn()
const mockCreate = vi.fn()
const mockCheck = vi.fn()
const mockCheckWithAi = vi.fn()
const mockRecommend = vi.fn()

vi.mock('@/api/prescription', () => ({
  getPrescriptionList: (...args: any[]) => mockGetList(...args),
  getPrescriptionDetail: (...args: any[]) => mockGetDetail(...args),
  getPrescriptionByRegistration: (...args: any[]) => mockGetByRegistration(...args),
  createPrescription: (...args: any[]) => mockCreate(...args),
  checkPrescription: (...args: any[]) => mockCheck(...args),
  aiCheckPrescription: (...args: any[]) => mockCheckWithAi(...args),
  recommendMedicine: (...args: any[]) => mockRecommend(...args),
}))

const mockPrescription = {
  id: 1,
  patientId: 10,
  patientName: '测试患者',
  doctorId: 5,
  doctorName: '张医生',
  registrationId: 100,
  medicineList: '阿莫西林 0.5g x 3天',
  dosage: '0.5g',
  usage: '每日三次，饭后服用',
  status: 'pending',
  createdAt: '2026-07-01T08:00:00',
}

const mockAiCheck = {
  checkResult: '处方合理，无严重问题',
  riskLevel: 'low',
  medicationSuggestions: '建议饭后服用',
  interactionDetection: '无药物相互作用',
  riskHints: '注意观察过敏反应',
}

const mockMedicineItems = [
  { name: '阿莫西林', dose: '0.5g', frequency: '每日三次' },
  { name: '布洛芬', dose: '0.3g', frequency: '每日两次' },
]

describe('prescription store', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
    global.fetch = vi.fn()
  })

  it('should initialize with default values', () => {
    const store = usePrescriptionStore()
    expect(store.records).toEqual([])
    expect(store.currentRecord).toBeNull()
    expect(store.loading).toBe(false)
  })

  it('should fetch list and update records', async () => {
    const prescWithCreateTime = { ...mockPrescription, createTime: mockPrescription.createdAt }
    mockGetList.mockResolvedValue({ data: [mockPrescription] })
    const store = usePrescriptionStore()

    const result = await store.fetchList({ doctorId: 5 })

    expect(mockGetList).toHaveBeenCalledWith({ doctorId: 5 })
    expect(store.records).toEqual([prescWithCreateTime])
    expect(store.loading).toBe(false)
  })

  it('should handle empty list', async () => {
    mockGetList.mockResolvedValue({ data: [] })
    const store = usePrescriptionStore()

    const result = await store.fetchList({ doctorId: 5 })

    expect(store.records).toEqual([])
    expect(result).toEqual([])
  })

  it('should handle fetch list error', async () => {
    mockGetList.mockRejectedValue(new Error('Network error'))
    const store = usePrescriptionStore()

    await expect(store.fetchList({ doctorId: 5 })).rejects.toThrow('Network error')
    expect(store.loading).toBe(false)
  })

  it('should fetch detail and set currentRecord', async () => {
    mockGetDetail.mockResolvedValue({ data: mockPrescription })
    const store = usePrescriptionStore()

    const result = await store.fetchDetail(1)

    expect(mockGetDetail).toHaveBeenCalledWith(1)
    expect(store.currentRecord).toEqual(mockPrescription)
    expect(result).toEqual(mockPrescription)
    expect(store.loading).toBe(false)
  })

  it('should fetch by registration id', async () => {
    mockGetByRegistration.mockResolvedValue({ data: mockPrescription })
    const store = usePrescriptionStore()

    const result = await store.fetchByRegistration(100)

    expect(mockGetByRegistration).toHaveBeenCalledWith(100)
    expect(result).toEqual(mockPrescription)
    expect(store.loading).toBe(false)
  })

  it('should create prescription and prepend to records', async () => {
    mockCreate.mockResolvedValue({ data: mockPrescription })
    const store = usePrescriptionStore()

    const form = {
      patientId: 10,
      doctorId: 5,
      registrationId: 100,
      medicineList: '阿莫西林 0.5g x 3天',
    }

    const result = await store.create(form)

    expect(mockCreate).toHaveBeenCalledWith(form)
    expect(store.records[0]).toEqual(mockPrescription)
    expect(result).toEqual(mockPrescription)
  })

  it('should check prescription by id', async () => {
    mockCheck.mockResolvedValue({ data: mockAiCheck })
    const store = usePrescriptionStore()

    const result = await store.check(1)

    expect(mockCheck).toHaveBeenCalledWith(1)
    expect(result.riskLevel).toBe('low')
    expect(result.checkResult).toBe('处方合理，无严重问题')
  })

  it('should check with AI', async () => {
    mockCheckWithAi.mockResolvedValue({ data: mockAiCheck })
    const store = usePrescriptionStore()

    const result = await store.checkWithAi('阿莫西林 0.5g', '患者男30岁')

    expect(mockCheckWithAi).toHaveBeenCalledWith('阿莫西林 0.5g', '患者男30岁')
    expect(result.interactionDetection).toBe('无药物相互作用')
  })

  it('should recommend medicine', async () => {
    mockRecommend.mockResolvedValue({ data: mockMedicineItems })
    const store = usePrescriptionStore()

    const result = await store.recommend({ symptoms: '头痛发热', department: '内科' })

    expect(mockRecommend).toHaveBeenCalledWith({ symptoms: '头痛发热', department: '内科' })
    expect(result).toHaveLength(2)
    expect(result[0].name).toBe('阿莫西林')
  })

  it('should save check result via fetch', async () => {
    const mockFetch = vi.fn().mockResolvedValue({
      json: () => Promise.resolve({ code: 200, data: { id: 1 } }),
    })
    global.fetch = mockFetch

    const store = usePrescriptionStore()
    const result = await store.saveCheckResult({
      prescriptionId: 1,
      checkResult: '合理',
      medicationSuggestions: '饭后服用',
      interactionDetection: '无',
      riskLevel: 'low',
      riskHints: '无',
    })

    expect(mockFetch).toHaveBeenCalledWith('/api/prescription/save-check', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        prescriptionId: 1,
        checkResult: '合理',
        medicationSuggestions: '饭后服用',
        interactionDetection: '无',
        riskLevel: 'low',
        riskHints: '无',
      }),
    })
  })
})
