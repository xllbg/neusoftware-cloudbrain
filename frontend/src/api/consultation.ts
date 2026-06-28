import { get, post } from "./index"
import type { ConsultationMessage } from "@/types"

export function sendConsultationMessage(data: {
  registrationId: number
  senderType: string
  senderId: number
  content: string
}) {
  return post<ConsultationMessage>("/consultation/send", data)
}

export function getConsultationMessages(registrationId: number) {
  return get<ConsultationMessage[]>("/consultation/messages", { registrationId })
}
