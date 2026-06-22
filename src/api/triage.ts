import { post } from "./index"
import type { TriageRequest, TriageResult } from "@/types"

// AI 智能分诊
export function triageConsult(data: TriageRequest) {
  return post<TriageResult>("/triage/consult", data)
}
