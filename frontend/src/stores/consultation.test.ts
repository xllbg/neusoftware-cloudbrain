import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useConsultationStore } from '@/stores/consultation'

const mockGetMessages = vi.fn()
const mockSendMessage = vi.fn()

vi.mock('@/api/consultation', () => ({
  getConsultationMessages: (...args: any[]) => mockGetMessages(...args),
  sendConsultationMessage: (...args: any[]) => mockSendMessage(...args),
}))

const mockMsg = {
  id: 1,
  registrationId: 100,
  patientId: 10,
  doctorId: 5,
  senderType: 'DOCTOR',
  senderId: 5,
  senderName: '张医生',
  content: '您好，请问有什么不舒服？',
  type: 'text',
  isRead: true,
  createdAt: '2026-07-01T08:00:00',
}

const mockPatientMsg = {
  ...mockMsg,
  id: 2,
  senderType: 'PATIENT',
  senderId: 10,
  senderName: '患者甲',
  content: '我头痛好几天了',
  isRead: false,
  createdAt: '2026-07-01T08:05:00',
}

describe('consultation store', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('should initialize with default values', () => {
    const store = useConsultationStore()
    expect(store.messages).toEqual([])
    expect(store.loading).toBe(false)
    expect(store.sending).toBe(false)
  })

  it('should fetch messages for a registration', async () => {
    mockGetMessages.mockResolvedValue({ data: [mockMsg, mockPatientMsg] })
    const store = useConsultationStore()

    const result = await store.fetchMessages(100)

    expect(mockGetMessages).toHaveBeenCalledWith(100)
    expect(store.messages).toHaveLength(2)
    expect(store.messages[0].content).toBe('您好，请问有什么不舒服？')
    expect(result).toEqual([mockMsg, mockPatientMsg])
    expect(store.loading).toBe(false)
  })

  it('should handle empty messages', async () => {
    mockGetMessages.mockResolvedValue({ data: [] })
    const store = useConsultationStore()

    const result = await store.fetchMessages(100)

    expect(store.messages).toEqual([])
    expect(result).toEqual([])
  })

  it('should handle fetch error', async () => {
    mockGetMessages.mockRejectedValue(new Error('Network error'))
    const store = useConsultationStore()

    await expect(store.fetchMessages(100)).rejects.toThrow('Network error')
    expect(store.loading).toBe(false)
  })

  it('should send a message and append to messages', async () => {
    const newMsg = { ...mockMsg, id: 3, content: '我给您开个检查单' }
    mockSendMessage.mockResolvedValue({ data: newMsg })
    mockGetMessages.mockResolvedValue({ data: [mockMsg, mockPatientMsg] })

    const store = useConsultationStore()
    await store.fetchMessages(100)
    expect(store.messages).toHaveLength(2)

    const result = await store.sendMessage({
      registrationId: 100,
      senderType: 'DOCTOR',
      senderId: 5,
      content: '我给您开个检查单',
    })

    expect(mockSendMessage).toHaveBeenCalledWith({
      registrationId: 100,
      senderType: 'DOCTOR',
      senderId: 5,
      content: '我给您开个检查单',
    })
    expect(store.messages).toHaveLength(3)
    expect(store.messages[2].content).toBe('我给您开个检查单')
    expect(result).toEqual(newMsg)
    expect(store.sending).toBe(false)
  })

  it('should add message directly without API', () => {
    const store = useConsultationStore()
    store.addMessage(mockMsg)

    expect(store.messages).toHaveLength(1)
    expect(store.messages[0].content).toBe('您好，请问有什么不舒服？')
  })

  it('should clear all messages', () => {
    const store = useConsultationStore()
    store.addMessage(mockMsg)
    store.addMessage(mockPatientMsg)
    expect(store.messages).toHaveLength(2)

    store.clearMessages()

    expect(store.messages).toHaveLength(0)
  })
})
