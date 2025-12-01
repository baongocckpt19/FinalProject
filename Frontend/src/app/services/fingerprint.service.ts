// src/app/services/fingerprint.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DeviceFingerprintInfo {
  deviceId: number;
  deviceCode: string;
  deviceName: string;
  room?: string;
  sensorSlot: number;
}

export interface StudentFingerprintInfo {
  studentId: number;
  studentCode: string;      // 🔹 THÊM MỚI
  fullName: string;
  username: string;
  email?: string;
  hasFingerprint: boolean;
  fingerprintDevicesCount: number;
  devices: DeviceFingerprintInfo[];
}

export interface ConfirmEnrollResponse {
  success: boolean;
  message: string;
  sensorSlot: number;
  studentId: number;
}

@Injectable({
  providedIn: 'root'
})
export class FingerprintService {
  private apiBase = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  /**
   * Lấy thông tin sinh viên + trạng thái vân tay + danh sách thiết bị
   * GET /api/students/{studentId}/fingerprint
   */
  getStudentFingerprintInfoById(studentId: number): Observable<StudentFingerprintInfo> {
    return this.http.get<StudentFingerprintInfo>(
      `${this.apiBase}/students/${studentId}/fingerprint`
    );
  }

  // Giữ lại alias cũ nếu chỗ khác đang dùng
  getStudentFingerprintInfo(studentId: number): Observable<StudentFingerprintInfo> {
    return this.getStudentFingerprintInfoById(studentId);
  }

  /**
   * 🔹 MỚI: Lấy thông tin sinh viên theo MSSV (studentCode)
   * GET /api/students/by-code/{studentCode}/fingerprint
   */
  getStudentFingerprintInfoByCode(studentCode: string): Observable<StudentFingerprintInfo> {
    return this.http.get<StudentFingerprintInfo>(
      `${this.apiBase}/students/by-code/${encodeURIComponent(studentCode)}/fingerprint`
    );
  }

  /**
   * Tạo phiên enroll vân tay cho sinh viên
   * POST /api/fingerprint/enroll/session
   * body: { studentId, deviceCode? }
   */
  createEnrollSession(
    studentId: number,
    deviceCode?: string
  ): Observable<{ sessionCode: string }> {
    const body: any = { studentId };
    if (deviceCode) {
      body.deviceCode = deviceCode;
    }
    return this.http.post<{ sessionCode: string }>(
      `${this.apiBase}/fingerprint/enroll/session`,
      body
    );
  }

  /**
   * Confirm lưu vân tay từ sessionCode cho sinh viên
   * POST /api/fingerprint/enroll/confirm
   * body: { studentId, sessionCode }
   */
  confirmEnroll(studentId: number, sessionCode: string): Observable<ConfirmEnrollResponse> {
    const body = { studentId, sessionCode };
    return this.http.post<ConfirmEnrollResponse>(
      `${this.apiBase}/fingerprint/enroll/confirm`,
      body
    );
  }

  /**
   * Kiểm tra phiên enroll đã nhận sensorSlot chưa
   * GET /api/fingerprint/enroll/temp?sessionCode=...
   */
  checkEnrollTemp(sessionCode: string): Observable<{ found: boolean; sensorSlot?: number }> {
    return this.http.get<{ found: boolean; sensorSlot?: number }>(
      `${this.apiBase}/fingerprint/enroll/temp`,
      { params: { sessionCode } }
    );
  }

  /** Lấy danh sách thiết bị đang hoạt động */
getActiveDevices(): Observable<DeviceFingerprintInfo[]> {
  return this.http.get<DeviceFingerprintInfo[]>(`${this.apiBase}/devices/active`);
}

}
