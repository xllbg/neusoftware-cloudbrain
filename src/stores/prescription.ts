import { defineStore } from "pinia"
import { ref } from "vue"
import type { PrescriptionRecord, PrescriptionForm, AICheckResult } from "@/types"
import { createPrescription, getPrescriptionList, checkPrescription } from "@/api/prescription"

export const usePrescriptionStore = defineStore("prescription", () => {
  const records = ref<PrescriptionRecord[]>([])
  const loading = ref(false)

  async function fetchList(params: { patientId?: number; doctorId?: number }) {
    loading.value = true
    try {
      const res = await getPrescriptionList(params)
      records.value = res.data
      return res.data
    } finally {
      loading.value = false
    }
  }

  async function create(data: PrescriptionForm) {
    const res = await createPrescription(data)
    records.value.unshift(res.data)
    return res.data
  }

  async function check(id: number) {
    const res = await checkPrescription(id)
    return res.data as AICheckResult
  }

  return { records, loading, fetchList, create, check }
})
