import { defineStore } from "pinia"
import { ref } from "vue"
import type { PrescriptionRecord, PrescriptionForm, AiCheckResult, PrescriptionMedicineItem } from "@/types"
import { createPrescription, getPrescriptionList, getPrescriptionDetail, checkPrescription, recommendMedicine, aiCheckPrescription, getPrescriptionCheckResult } from "@/api/prescription"

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

  async function checkWithAi(medicineText: string, patientInfo: string): Promise<AiCheckResult> {
    const res = await aiCheckPrescription(medicineText, patientInfo)
    return res.data as AiCheckResult
  }

  async function recommend(params: { symptoms?: string; diagnosis?: string; department?: string; registrationId?: number }) {
    const res = await recommendMedicine(params)
    return res.data as PrescriptionMedicineItem[]
  }

  async function saveCheckResult(data: {
    prescriptionId: number
    checkResult: string
    medicationSuggestions: string
    interactionDetection: string
    riskLevel: string
    riskHints: string
  }) {
    const res = await fetch("/api/prescription/save-check", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    })
    return res.json()
  }

  async function getCheckResult(prescriptionId: number): Promise<AiCheckResult | null> {
    const res = await getPrescriptionCheckResult(prescriptionId)
    if (res.code === 200 && res.data) {
      return res.data as AiCheckResult
    }
    return null
  }

  return { records, currentRecord, loading, fetchList, fetchDetail, create, check, checkWithAi, recommend, saveCheckResult, getCheckResult }
})
