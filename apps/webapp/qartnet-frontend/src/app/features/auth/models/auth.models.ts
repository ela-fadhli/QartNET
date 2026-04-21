export type { ApiResponse } from '../../../shared/models/api-response.model';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  dateOfBirth?: string;
  phoneNumber?: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}
