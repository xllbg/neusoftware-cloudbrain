import { defineStore } from "pinia"
import { ref } from "vue"
import type { RegistrationRecord, RegistrationForm } from "@/types"
import { createRegistration, getRegistrationList, cancelRegistration } from "@/api/registration"

export const useRegistrationStore = defineStore("registration", () => {
  const records = ref<RegistrationRecord[]>([])
  const loading = ref(false)

  async function fetchList(params: { patientId?: number; doctorId?: number; status?: string }) {
    loading.value = true
    try {
      const res = await getRegistrationList(params)
      records.value = res.data
      return res.data
    } finally {
      loading.value = false
    }
  }

  async function create(data: RegistrationForm) {
    const res = await createRegistration(data)
    records.value.unshift(res.data)
    return res.data
  }

  async function cancel(id: number, patientId: number) {
    const res = await cancelRegistration(id, patientId)
    const idx = records.value.findIndex((r) => r.id === id)
    if (idx !== -1) {
      records.value[idx] = res.data
    }
    return res.data
  }

  return { records, loading, fetchList, create, cancel }
})
