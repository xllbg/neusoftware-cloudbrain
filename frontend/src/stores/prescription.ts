import { defineStore } from "pinia"
import { ref } from "vue"
import type { PrescriptionRecord, PrescriptionForm, AiCheckResult, PrescriptionMedicineItem } from "@/types"
import { createPrescription, getPrescriptionList, getPrescriptionDetail, checkPrescription, recommendMedicine } from "@/api/prescription"

export const usePrescriptionStore = defineStore("prescription", () => {
  const records = ref<PrescriptionRecord[]>([])
  const currentRecord = ref<PrescriptionRecord | null>(null)
  const loading = ref(false)

  async function fetchList(params: { patientId?: number; doctorId?: number }) {
    loading.value = true
    try {
      const res = await getPrescriptionList(params)
      const list = (res.data || []).map((item: any) => ({
        ...item,
        createTime: item.createTime || item.createdAt,
      }))
      records.value = list
      return list
    } finally {
      loading.value = false
    }
  }

  async function fetchDetail(id: number) {
    loading.value = true
    try {
      const res = await getPrescriptionDetail(id)
      currentRecord.value = res.data
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
    return res.data as AiCheckResult
  }

  async function recommend(params: { symptoms?: string; diagnosis?: string; department?: string }) {
    const res = await recommendMedicine(params)
    return res.data as PrescriptionMedicineItem[]
  }

  return { records, currentRecord, loading, fetchList, fetchDetail, create, check, recommend }
})
