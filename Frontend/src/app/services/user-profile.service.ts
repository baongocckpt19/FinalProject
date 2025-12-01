// src/app/services/user-profile.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';

export type Gender = 'Nam' | 'Nữ' | 'Khác' | null;

export interface UserProfile {
  accountId: number;
  username: string;
  roleName: string;
  fullName: string;

  studentId?: number | null;
  teacherId?: number | null;
    userCode: string | null;
  email: string | null;
  phone: string | null;
  address: string | null;
  birthDate: string | null; // yyyy-MM-dd
  gender: Gender;
}

export interface UpdateProfileRequest {
  fullName: string;
    roleName: string;

      userCode: string | null;
  email: string | null;
  phone: string | null;
  address: string | null;
  birthDate: string | null;
  gender: Gender;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserProfileService {
  private apiUrl = 'http://localhost:8080/api/account';

  constructor(private http: HttpClient) {}

  /** Lấy thông tin hồ sơ người dùng hiện tại */
 /** Lấy thông tin hồ sơ người dùng hiện tại */
  getMyProfile(): Observable<UserProfile> {
    return this.http.get<{ account: any }>(this.apiUrl).pipe(
      map((res) => {
        const a = res.account;

        const profile: UserProfile = {
          accountId: a.accountId,
          username: a.username,
          roleName: a.roleName,
          fullName: a.fullName,

          studentId: a.studentId ?? null,
          teacherId: a.teacherId ?? null,

          userCode: a.userCode ?? null,

          email: a.email ?? null,
          phone: a.phone ?? null,
          address: a.address ?? null,
          birthDate: a.dateOfBirth ?? null,
          gender: (a.gender as Gender) ?? null
        };

        return profile;
      })
    );
  }

  /** Cập nhật thông tin cá nhân (không đụng mã số / role) */
  updateMyProfile(payload: UpdateProfileRequest): Observable<any> {
    return this.http.put(`${this.apiUrl}/profile`, payload);
  }

  /** Đổi mật khẩu */
  changePassword(req: ChangePasswordRequest): Observable<any> {
    return this.http.put(`${this.apiUrl}/change-password`, req);
  }

  /**
   * Kiểm tra mật khẩu hiện tại (dùng cho dấu tick/X khi blur ô "mật khẩu cũ")
   * YÊU CẦU BACKEND:
   *  POST /api/account/check-password  { currentPassword: string }
   *  -> { valid: boolean }
   */
  checkCurrentPassword(currentPassword: string): Observable<{ valid: boolean }> {
    return this.http.post<{ valid: boolean }>(
      `${this.apiUrl}/check-password`,
      { currentPassword }
    );
  }
}
