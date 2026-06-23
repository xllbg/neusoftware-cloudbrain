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
    PENDING: "warning",
    COMPLETED: "success",
    CANCELLED: "info",
    APPROVED: "success",
    REJECTED: "danger",
  }
  return map[status] || "info"
}

export function getStatusLabel(status: string): string {
  const map: Record<string, string> = {
    PENDING: "待处理",
    COMPLETED: "已完成",
    CANCELLED: "已取消",
    APPROVED: "已通过",
    REJECTED: "已拒绝",
  }
  return map[status] || status
}
