import { defineStore } from "pinia"
import { ref } from "vue"
import type { MedicalRecord, MedicalRecordForm, AiMedicalRecordResult } from "@/types"
import {
  getMedicalRecordList,
  getMedicalRecordDetail,
  saveMedicalRecord,
  generateMedicalRecord,
} from "@/api/medicalRecord"

export const useMedicalRecordStore = defineStore("medicalRecord", () => {
  const records = ref<MedicalRecord[]>([])
  const currentRecord = ref<MedicalRecord | null>(null)
  const loading = ref(false)

  async function fetchList(params: { patientId?: number; doctorId?: number }) {
    loading.value = true
    try {
      const res = await getMedicalRecordList(params)
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
      const res = await getMedicalRecordDetail(id)
      currentRecord.value = res.data
      return res.data
    } finally {
      loading.value = false
    }
  }

  async function save(data: MedicalRecordForm) {
    const res = await saveMedicalRecord(data)
    return res.data
  }

  async function generate(patientId: number, dialogueText: string) {
    const res = await generateMedicalRecord(patientId, dialogueText)
    return res.data as AiMedicalRecordResult
  }

  return { records, currentRecord, loading, fetchList, fetchDetail, save, generate }
})
