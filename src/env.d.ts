/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

// 扩展 Axios 请求配置类型，支持重试计数
import 'axios'
declare module 'axios' {
  interface InternalAxiosRequestConfig {
    _retryCount?: number
  }
}
