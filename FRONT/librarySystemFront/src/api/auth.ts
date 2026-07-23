import http from './index'

export interface LoginParams {
  username: string
  password: string
  captchaKey?: string
  captchaCode?: string
}

export interface LoginResult {
  accessToken: string
  expiresIn: number
  refreshToken: string
  userId: number
  username: string
  realName: string
  roles: string[]
  permissions: string[]
}

export interface CaptchaResult {
  captchaKey: string
  captchaImage: string
}

export interface RegisterParams {
  firstName: string
  lastName: string
  email: string
  phone: string
  readerType: string
  password: string
  confirmPassword: string
}

/** Login */
export async function loginApi(params: LoginParams): Promise<LoginResult> {
  return http.post('/auth/login', params) as any
}

/** Logout */
export async function logoutApi(): Promise<void> {
  return http.post('/auth/logout') as any
}

/** Get captcha */
export async function getCaptchaApi(): Promise<CaptchaResult> {
  return http.get('/auth/captcha') as any
}

/** Refresh token */
export async function refreshTokenApi(refreshToken: string): Promise<{ accessToken: string; refreshToken: string; expiresIn: number }> {
  return http.post('/auth/refresh', { refreshToken }) as any
}

/** Register */
export async function registerApi(params: RegisterParams): Promise<any> {
  return http.post('/auth/register', params) as any
}
