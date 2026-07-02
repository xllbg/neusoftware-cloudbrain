import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useRegistrationStore } from '@/stores/registration'

const mockGetList = vi.fn()
const mockCreate = vi.fn()
const mockCancel = vi.fn()
const mockStart = vi.fn()
const mockComplete = vi.fn()

vi.mock('@/api/registration', () => ({
  getRegistrationList: (...args: any[]) => mockGetList(...args),
  createRegistration: (...args: any[]) => mockCreate(...args),
  cancelRegistration: (...args: any[]) => mockCancel(...args),
  startConsultation: (...args: any[]) => mockStart(...args),
  completeConsultation: (...args: any[]) => mockComplete(...args),
}))

const mockRecord = {
  id: 1,
  patientId: 10,
  patientName: '测试患者',
  doctorId: 5,
  doctorName: '张医生',
  department: '心内科',
  doctorTitle: '主任医师',
  hospital: '东软医院',
  registrationDate: '2026-07-01',
  timeSlot: 'MORNING',
  status: 'pending',
  symptom: '头痛',
  triageResult: '',
  createdAt: '2026-07-01T08:00:00',
}

describe('registration store', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('should initialize with default values', () => {
    const store = useRegistrationStore()
    expect(store.records).toEqual([])
    expect(store.loading).toBe(false)
  })

  it('should fetch list and update records', async () => {
    mockGetList.mockResolvedValue({ data: [mockRecord] })
    const store = useRegistrationStore()

    const result = await store.fetchList({ doctorId: 5 })

    expect(mockGetList).toHaveBeenCalledWith({ doctorId: 5 })
    expect(store.records).toEqual([mockRecord])
    expect(result).toEqual([mockRecord])
    expect(store.loading).toBe(false)
  })

  it('should handle empty fetch list', async () => {
    mockGetList.mockResolvedValue({ data: [] })
    const store = useRegistrationStore()

    const result = await store.fetchList({ doctorId: 5 })

    expect(store.records).toEqual([])
    expect(result).toEqual([])
  })

  it('should handle fetch list error and reset loading', async () => {
    mockGetList.mockRejectedValue(new Error('Network error'))
    const store = useRegistrationStore()

    await expect(store.fetchList({ doctorId: 5 })).rejects.toThrow('Network error')
    expect(store.loading).toBe(false)
  })

  it('should create registration and prepend to records', async () => {
    mockCreate.mockResolvedValue({ data: mockRecord })
    const store = useRegistrationStore()

    const form = {
      patientId: 10,
      doctorId: 5,
      department: '心内科',
      registrationDate: '2026-07-01',
      timeSlot: 'MORNING',
    }

    const result = await store.create(form)

    expect(mockCreate).toHaveBeenCalledWith(form)
    expect(store.records[0]).toEqual(mockRecord)
    expect(result).toEqual(mockRecord)
  })

  it('should cancel registration and update record in list', async () => {
    const cancelledRecord = { ...mockRecord, status: 'cancelled' }
    mockGetList.mockResolvedValue({ data: [mockRecord] })
    mockCancel.mockResolvedValue({ data: cancelledRecord })

    const store = useRegistrationStore()
    await store.fetchList({ doctorId: 5 })
    expect(store.records[0].status).toBe('pending')

    await store.cancel(1, 10)

    expect(mockCancel).toHaveBeenCalledWith(1, 10)
    expect(store.records[0].status).toBe('cancelled')
  })

  it('should start consultation and update record in list', async () => {
    const inProgressRecord = { ...mockRecord, status: 'in_progress' }
    mockGetList.mockResolvedValue({ data: [mockRecord] })
    mockStart.mockResolvedValue({ data: inProgressRecord })

    const store = useRegistrationStore()
    await store.fetchList({ doctorId: 5 })

    await store.startConsultation(1, 5)

    expect(mockStart).toHaveBeenCalledWith(1, 5)
    expect(store.records[0].status).toBe('in_progress')
  })

  it('should complete consultation and update record in list', async () => {
    const completedRecord = { ...mockRecord, status: 'completed' }
    mockGetList.mockResolvedValue({ data: [mockRecord] })
    mockComplete.mockResolvedValue({ data: completedRecord })

    const store = useRegistrationStore()
    await store.fetchList({ doctorId: 5 })

    await store.completeConsultation(1, 5)

    expect(mockComplete).toHaveBeenCalledWith(1, 5)
    expect(store.records[0].status).toBe('completed')
  })

  it('should not modify records if cancel id not found', async () => {
    mockGetList.mockResolvedValue({ data: [mockRecord] })
    mockCancel.mockResolvedValue({ data: { ...mockRecord, status: 'cancelled' } })

    const store = useRegistrationStore()
    await store.fetchList({ doctorId: 5 })

    await store.cancel(999, 10)

    expect(store.records[0].status).toBe('pending')
  })

  it('should create registration with symptom (patient fills symptom)', async () => {
    const recordWithSymptom = { ...mockRecord, symptom: '头痛三天，伴有发热' }
    mockCreate.mockResolvedValue({ data: recordWithSymptom })
    const store = useRegistrationStore()

    const form = {
      patientId: 10,
      doctorId: 5,
      department: '呼吸内科',
      registrationDate: '2026-07-02',
      timeSlot: 'AFTERNOON',
      symptom: '头痛三天，伴有发热',
    }

    const result = await store.create(form)

    expect(mockCreate).toHaveBeenCalledWith(form)
    expect(store.records[0].symptom).toBe('头痛三天，伴有发热')
    expect(result.symptom).toBe('头痛三天，伴有发热')
  })

  it('should fetch registrations by patientId', async () => {
    mockGetList.mockResolvedValue({ data: [mockRecord] })
    const store = useRegistrationStore()

    await store.fetchList({ patientId: 10 })

    expect(mockGetList).toHaveBeenCalledWith({ patientId: 10 })
    expect(store.records).toHaveLength(1)
  })

  it('should fetch registrations by status filter', async () => {
    mockGetList.mockResolvedValue({ data: [mockRecord] })
    const store = useRegistrationStore()

    await store.fetchList({ patientId: 10, status: 'pending' })

    expect(mockGetList).toHaveBeenCalledWith({ patientId: 10, status: 'pending' })
  })

  it('should create registration for emergency department (doctorId=0)', async () => {
    const emergencyRecord = { ...mockRecord, doctorId: null, doctorName: '', department: '急诊科' }
    mockCreate.mockResolvedValue({ data: emergencyRecord })
    const store = useRegistrationStore()

    const form = {
      patientId: 10,
      doctorId: 0,
      department: '急诊科',
      registrationDate: '2026-07-01',
      timeSlot: 'MORNING',
      symptom: '腹痛',
    }

    const result = await store.create(form)

    expect(mockCreate).toHaveBeenCalledWith(form)
    expect(result.department).toBe('急诊科')
  })

  it('should handle multiple registrations in list', async () => {
    const record2 = { ...mockRecord, id: 2, department: '呼吸内科', registrationDate: '2026-07-02' }
    mockGetList.mockResolvedValue({ data: [mockRecord, record2] })
    const store = useRegistrationStore()

    await store.fetchList({ patientId: 10 })

    expect(store.records).toHaveLength(2)
    expect(store.records[0].department).toBe('心内科')
    expect(store.records[1].department).toBe('呼吸内科')
  })

  it('should create registration and keep existing records', async () => {
    mockGetList.mockResolvedValue({ data: [mockRecord] })
    mockCreate.mockResolvedValue({ data: { ...mockRecord, id: 2 } })
    const store = useRegistrationStore()

    await store.fetchList({ patientId: 10 })
    expect(store.records).toHaveLength(1)

    await store.create({
      patientId: 10,
      doctorId: 6,
      department: '呼吸内科',
      registrationDate: '2026-07-03',
      timeSlot: 'MORNING',
    })

    expect(store.records).toHaveLength(2)
    expect(store.records[0].id).toBe(2)
  })
})
