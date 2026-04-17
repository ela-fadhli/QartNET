export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string | null;
  data: T | null;
  timestamp: string;
}
