export interface UserProfile {
  publicId: string;
  username: string;
  email: string;
  firstName: string | null;
  lastName: string | null;
  bio: string | null;
  profilePictureUrl: string | null;
  skills: string[];
  socialLinks: Record<string, string>;
  roles: string[];
  accountStatus: string;
  emailVerified: boolean;
  createdAt: string;
  lastLoginAt: string | null;
}

export interface ProfileResponse {
  username: string;
  bio: string | null;
  profilePictureUrl: string | null;
  firstName: string | null;
  lastName: string | null;
  dateOfBirth: string | null;
  phoneNumber: string | null;
}

export interface UpdateProfileRequest {
  username?: string;
  firstName?: string;
  lastName?: string;
  bio?: string;
  profilePictureUrl?: string;
  skills?: string[];
  socialLinks?: Record<string, string>;
  dateOfBirth?: string;
  phoneNumber?: string;
}
