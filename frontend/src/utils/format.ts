export function formatDate(dateStr: string): string {
  if (!dateStr) return ""
  const date = new Date(dateStr)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, "0")
  const d = String(date.getDate()).padStart(2, "0")
  return `${y}-${m}-${d}`
}

export function formatDateTime(dateStr: string): string {
  if (!dateStr) return ""
  const date = new Date(dateStr)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, "0")
  const d = String(date.getDate()).padStart(2, "0")
  const h = String(date.getHours()).padStart(2, "0")
  const min = String(date.getMinutes()).padStart(2, "0")
  return `${y}-${m}-${d} ${h}:${min}`
}

export function getStatusTag(status: string): string {
  const map: Record<string, string> = {
    pending: "warning",
    in_progress: "primary",
    completed: "success",
    cancelled: "info",
    approved: "success",
    rejected: "danger",
  }
  return map[status?.toLowerCase()] || "info"
}

export function getStatusLabel(status: string): string {
  const map: Record<string, string> = {
    pending: "待接诊",
    in_progress: "接诊中",
    completed: "已完成",
    cancelled: "已取消",
    approved: "已通过",
    rejected: "已拒绝",
  }
  return map[status?.toLowerCase()] || status
}
