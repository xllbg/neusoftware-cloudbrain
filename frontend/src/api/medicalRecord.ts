import { get, post } from "./index"
import type { MedicalRecord, MedicalRecordForm, AiMedicalRecordResult } from "@/types"
import { getDoctorToken, getPatientToken } from "@/utils/auth"
import router from "@/router"

function getToken() {
  const path = router.currentRoute.value.path
  if (path.startsWith("/doctor")) return getDoctorToken()
  if (path.startsWith("/patient")) return getPatientToken()
  return getDoctorToken() || getPatientToken()
}

export async function streamGenerateMedicalRecord(
  params: { patientId?: number; dialogueText: string; symptoms?: string; department?: string },
  onChunk: (text: string) => void
): Promise<string> {
  const token = getToken()
  const response = await fetch("/api/medical-record/generate-stream", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify({
      dialogueText: params.dialogueText,
      symptoms: params.symptoms || "",
      department: params.department || "",
    }),
  })

  if (!response.ok) {
    throw new Error(`请求失败: ${response.status}`)
  }

  const reader = response.body?.getReader()
  if (!reader) throw new Error("无法读取响应流")

  const decoder = new TextDecoder("utf-8")
  let fullText = ""
  let buffer = ""
  let currentEvent = ""

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split("\n")
    buffer = lines.pop() || ""

    for (const line of lines) {
      const trimmed = line.trim()
      if (!trimmed) {
        currentEvent = ""
        continue
      }
      if (trimmed.startsWith("event:")) {
        currentEvent = trimmed.slice(6).trim()
        continue
      }
      if (trimmed.startsWith("data:")) {
        const data = trimmed.slice(5).trim()
        if (!data || data === "[DONE]") continue

        if (currentEvent === "chunk") {
          let chunkText = data
          try {
            const parsed = JSON.parse(data)
            if (typeof parsed === "string") chunkText = parsed
          } catch {}
          fullText += chunkText
          onChunk(chunkText)
        } else if (currentEvent === "done") {
          let finalText = data
          try {
            const parsed = JSON.parse(data)
            if (typeof parsed === "string") finalText = parsed
          } catch {}
          if (finalText && finalText.length > fullText.length) {
            const diff = finalText.slice(fullText.length)
            if (diff) onChunk(diff)
            fullText = finalText
          }
        } else if (currentEvent === "error") {
          throw new Error(data)
        }
      }
    }
  }

  return fullText
}

export function generateMedicalRecord(patientId: number, dialogueText: string) {
  return post<AiMedicalRecordResult>(`/medical-record/generate?patientId=${patientId}`, { dialogueText })
}

export function optimizeMedicalRecord(data: {
  chiefComplaint?: string
  presentIllness?: string
  pastHistory?: string
  physicalExamination?: string
  diagnosis?: string
  treatmentPlan?: string
}) {
  return post<AiMedicalRecordResult>("/medical-record/optimize", data)
}

export function saveMedicalRecord(data: MedicalRecordForm) {
  return post<MedicalRecord>("/medical-record/save", data)
}

export function getMedicalRecordList(params: { patientId?: number; doctorId?: number }) {
  return get<MedicalRecord[]>("/medical-record/list", params)
}

export function getMedicalRecordByRegistration(registrationId: number) {
  return get<MedicalRecord>(`/medical-record/by-registration/${registrationId}`)
}

export function getMedicalRecordDetail(id: number) {
  return get<MedicalRecord>(`/medical-record/detail/${id}`)
}
