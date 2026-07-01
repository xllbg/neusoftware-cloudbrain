import { describe, it, expect, beforeEach, vi } from 'vitest'
import { clearPublicKey } from '@/utils/crypto'

const mockGet = vi.fn()
vi.mock('@/api/index', () => ({
  get: (...args: any[]) => mockGet(...args),
}))

const mockEncryptFn = vi.hoisted(() => vi.fn())
vi.mock('jsencrypt', () => {
  const MockJSEncrypt = vi.fn().mockImplementation(() => ({
    setPublicKey: vi.fn(),
    encrypt: () => mockEncryptFn(),
  }))
  return { JSEncrypt: MockJSEncrypt }
})

describe('clearPublicKey', () => {
  it('should clear cached public key', () => {
    clearPublicKey()
  })
})

describe('encryptPayload', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should fetch public key and encrypt data', async () => {
    mockGet.mockResolvedValue({ data: { publicKey: 'test-public-key' } })
    mockEncryptFn.mockReturnValue('encrypted-string')

    const { encryptPayload } = await import('@/utils/crypto')

    const result = await encryptPayload({ username: 'testuser', password: '123456' })

    expect(mockGet).toHaveBeenCalledWith('/auth/public-key')
    expect(result).toEqual({ body: 'encrypted-string', encrypted: true })
  })

  it('should throw error when encryption fails', async () => {
    mockGet.mockResolvedValue({ data: { publicKey: 'test-public-key' } })
    mockEncryptFn.mockReturnValue(false)

    const { encryptPayload } = await import('@/utils/crypto')

    await expect(encryptPayload({ username: 'test' })).rejects.toThrow('RSA 加密失败')
  })

  it('should cache public key after first fetch', async () => {
    mockGet.mockResolvedValue({ data: { publicKey: 'cached-key' } })
    mockEncryptFn.mockReturnValue('encrypted')

    clearPublicKey()
    const { encryptPayload } = await import('@/utils/crypto')

    await encryptPayload({ data: 'first' })
    expect(mockGet).toHaveBeenCalledTimes(1)

    mockEncryptFn.mockReturnValue('encrypted-2')
    await encryptPayload({ data: 'second' })
    expect(mockGet).toHaveBeenCalledTimes(1)
  })
})
