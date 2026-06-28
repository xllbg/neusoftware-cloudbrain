import { get } from "@/api/index"

let cachedPublicKey: string | null = null

async function fetchPublicKey(): Promise<string> {
  if (cachedPublicKey) return cachedPublicKey

  const res = await get<{ publicKey: string }>("/auth/public-key")
  cachedPublicKey = res.data.publicKey
  return cachedPublicKey
}

export async function encryptPayload(data: Record<string, unknown>): Promise<{ body: string; encrypted: boolean }> {
  const publicKey = await fetchPublicKey()
  const { JSEncrypt } = await import("jsencrypt")

  const encryptor = new JSEncrypt()
  encryptor.setPublicKey(publicKey)

  const json = JSON.stringify(data)
  const encrypted = encryptor.encrypt(json)

  if (!encrypted) {
    throw new Error("RSA 加密失败")
  }

  return { body: encrypted, encrypted: true }
}

export function clearPublicKey(): void {
  cachedPublicKey = null
}
