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
  bio?: string;
  profilePictureUrl?: string;
  firstName?: string;
  lastName?: string;
  dateOfBirth?: string;
  phoneNumber?: string;
}
