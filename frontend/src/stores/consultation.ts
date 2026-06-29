import { defineStore } from "pinia"
import { ref } from "vue"
import type { ConsultationMessage } from "@/types"
import { sendConsultationMessage, getConsultationMessages } from "@/api/consultation"

export const useConsultationStore = defineStore("consultation", () => {
  const messages = ref<ConsultationMessage[]>([])
  const loading = ref(false)
  const sending = ref(false)

  async function fetchMessages(registrationId: number) {
    loading.value = true
    try {
      const res = await getConsultationMessages(registrationId)
      messages.value = res.data
      return res.data
    } finally {
      loading.value = false
    }
  }

  async function sendMessage(data: {
    registrationId: number
    senderType: string
    senderId: number
    content: string
  }) {
    sending.value = true
    try {
      const res = await sendConsultationMessage(data)
      messages.value.push(res.data)
      return res.data
    } finally {
      sending.value = false
    }
  }

  function addMessage(msg: ConsultationMessage) {
    messages.value.push(msg)
  }

  function clearMessages() {
    messages.value = []
  }

  return { messages, loading, sending, fetchMessages, sendMessage, addMessage, clearMessages }
})
