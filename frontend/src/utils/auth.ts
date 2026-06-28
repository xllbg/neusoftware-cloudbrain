// Token 存储键名
const TOKEN_KEY = "cloud-brain-token"
const USER_KEY = "cloud-brain-user"

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function getUser(): any | null {
  const userStr = localStorage.getItem(USER_KEY)
  if (userStr) {
    try {
      return JSON.parse(userStr)
    } catch {
      return null
    }
  }
  return null
}

export function setUser(user: any): void {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function isLoggedIn(): boolean {
  return !!getToken()
}

export function isPatient(): boolean {
  const user = getUser()
  return user?.role === "PATIENT"
}

export function isDoctor(): boolean {
  const user = getUser()
  return user?.role === "DOCTOR"
}
