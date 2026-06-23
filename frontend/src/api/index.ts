import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from "axios"
import { ElMessage, ElLoading, ElMessageBox } from "element-plus"
import { getToken, removeToken } from "@/utils/auth"
import router from "@/router"

// 加载状态管理
let loadingInstance: ReturnType<typeof ElLoading.service> | null = null
let loadingCount = 0

const request: AxiosInstance = axios.create({
  baseURL: "/api",
  timeout: 30000,
  headers: {
    "Content-Type": "application/json",
  },
})

// ===== 请求拦截器 =====
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token && config.headers) {
      config.headers["Authorization"] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// ===== 响应拦截器（含超时自动重试）=====
const MAX_RETRIES = 2  // 最多重试2次

request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code === 200 || res.code === 0) {
      return res
    }
    if (res.code === 401) {
      removeToken()
      ElMessage.error("登录已过期，请重新登录")
      router.push("/patient/login")
      return Promise.reject(new Error(res.message || "未授权"))
    }
    ElMessage.error(res.message || "请求失败")
    return Promise.reject(new Error(res.message || "请求失败"))
  },
  async (error) => {
    const config = error.config

    // === 超时自动重试逻辑 ===
    if (error.code === "ECONNABORTED" && config && !config._retryCount) {
      config._retryCount = config._retryCount || 0
      if (config._retryCount < MAX_RETRIES) {
        config._retryCount++
        ElMessage.warning(`请求超时，正在重试第 ${config._retryCount} 次...`)
        // 等待 1 秒后重试
        await new Promise((resolve) => setTimeout(resolve, 1000))
        return request(config)
      }
    }

    // 重试耗尽后统一报错
    if (error.code === "ECONNABORTED") {
      ElMessage.error("请求超时，请检查网络后重试")
    } else if (error.response) {
      const status = error.response.status
      switch (status) {
        case 401:
          removeToken()
          ElMessage.error("登录已过期，请重新登录")
          router.push("/patient/login")
          break
        case 403:
          ElMessage.error("没有权限访问")
          break
        case 404:
          ElMessage.error("请求的资源不存在")
          break
        case 500:
          ElMessage.error("服务器内部错误")
          break
        default:
          ElMessage.error(`请求失败 (${status})`)
      }
    } else if (error.message?.includes("Network Error")) {
      ElMessage.error("网络连接失败，请检查网络")
    } else {
      ElMessage.error("系统繁忙，请稍后重试")
    }
    return Promise.reject(error)
  }
)

// ===== 统一 GET 请求 =====
export function get<T = any>(url: string, params?: any, config?: AxiosRequestConfig) {
  return request.get<any, ApiResult<T>>(url, { params, ...config })
}

// ===== 统一 POST 请求 =====
export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig) {
  return request.post<any, ApiResult<T>>(url, data, config)
}

// ===== RSA 加密 POST（用于登录/注册）=====
export async function encryptedPost<T = any>(url: string, data: Record<string, unknown>) {
  const { encryptPayload } = await import("@/utils/crypto")
  const { body } = await encryptPayload(data)
  return request.post<any, ApiResult<T>>(url, body, {
    headers: {
      "Content-Type": "text/plain",
      "X-Encrypted": "true",
    },
  })
}

// ===== 统一 PUT 请求 =====
export function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig) {
  return request.put<any, ApiResult<T>>(url, data, config)
}

// ===== 统一 DELETE 请求 =====
export function del<T = any>(url: string, config?: AxiosRequestConfig) {
  return request.delete<any, ApiResult<T>>(url, config)
}

// ===== Loading 控制 =====
export function showLoading(text: string = "加载中...") {
  loadingCount++
  if (loadingCount === 1) {
    loadingInstance = ElLoading.service({
      lock: true,
      text,
      background: "rgba(0, 0, 0, 0.7)",
    })
  }
}

export function hideLoading() {
  loadingCount--
  if (loadingCount <= 0) {
    loadingCount = 0
    if (loadingInstance) {
      loadingInstance.close()
      loadingInstance = null
    }
  }
}

// ===== 带 Loading 的 AI 请求（含降级处理）=====
export async function requestWithLoading<T = any>(
  requestFn: () => Promise<ApiResult<T>>,
  loadingText: string = "处理中..."
): Promise<ApiResult<T>> {
  showLoading(loadingText)
  try {
    const result = await requestFn()
    return result
  } finally {
    hideLoading()
  }
}

// ===== AI 调用封装（含降级策略）=====
export async function callAIService<T = any>(
  requestFn: () => Promise<ApiResult<T>>,
  options?: {
    loadingText?: string
    fallbackMessage?: string
    allowFallback?: boolean
  }
): Promise<{ success: boolean; data?: T; fallback?: boolean }> {
  const { loadingText = "AI 处理中...", fallbackMessage = "AI 服务暂时不可用", allowFallback = true } = options || {}

  showLoading(loadingText)
  try {
    const result = await requestFn()
    return { success: true, data: result.data }
  } catch (error: any) {
    hideLoading()
    ElMessage.error(`${fallbackMessage}，请稍后重试`)

    // 如果允许降级，弹出确认框让用户选择是否回退到手动模式
    if (allowFallback) {
      try {
        await ElMessageBox.confirm(
          `${fallbackMessage}，是否切换为手动操作模式？`,
          "AI 服务提示",
          {
            confirmButtonText: "切换手动模式",
            cancelButtonText: "稍后重试",
            type: "warning",
          }
        )
        return { success: false, fallback: true }
      } catch {
        // 用户选择"稍后重试"
        return { success: false, fallback: false }
      }
    }
    return { success: false, fallback: false }
  } finally {
    hideLoading()
  }
}

export default request

interface ApiResult<T = any> {
  code: number
  message: string
  data: T
}
