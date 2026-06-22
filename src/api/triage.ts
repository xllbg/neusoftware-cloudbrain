// AI 分诊 API（对齐后端 TriageController）
import { post } from "./index"
import type { TriageRequest, TriageResult } from "@/types"

// AI 智能分诊 → POST /api/triage/consult { patientId, age, gender, symptoms }
export function triageConsult(data: TriageRequest) {
  return post<TriageResult>("/triage/consult", data)
}
