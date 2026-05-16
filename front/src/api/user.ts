import request from '@/utils/request'

export interface UserProfileVO {
  username: string
  nickname: string
  phone?: string
  role: string
  enabled?: boolean
  lastLoginAt?: string
  createdAt?: string
  updatedAt?: string
}

export interface UpdateProfileRequest {
  nickname?: string
  remark?: string
}

export interface ResetPasswordRequest {
  oldPassword: string
  newPassword: string
}

export interface UpdatePhoneRequest {
  phone: string
  captcha: string
}

export const getProfile = () => {
  return request.get<UserProfileVO>('/api/v1/user/profile')
}

export const updateProfile = (data: UpdateProfileRequest) => {
  return request.put('/api/v1/user/profile', data)
}

export const resetPassword = (data: ResetPasswordRequest) => {
  return request.put('/api/v1/user/password', data)
}

export const updatePhone = (data: UpdatePhoneRequest) => {
  return request.put('/api/v1/user/phone', data)
}
