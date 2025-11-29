// ====================== FILE: fingerprint.service.ts ======================
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
// import { environment } from '../../environments/environment';

export interface DeviceFingerprintInfo {
    deviceId: number;
    deviceCode: string;
    deviceName: string;
    room?: string;
    sensorSlot: number;
}

export interface StudentFingerprintInfo {
    studentId: number;
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
    // Nếu bạn có environment.apiUrl thì dùng dòng dưới:
    // private apiBase = environment.apiUrl + '/api';

    // Tạm thời dùng cứng (sửa lại cho khớp backend của bạn)
    private apiBase = 'http://localhost:8080/api';

    constructor(private http: HttpClient) { }

    /**
     * Lấy thông tin sinh viên + trạng thái vân tay + danh sách thiết bị
     * GET /api/students/{studentId}/fingerprint
     */
    getStudentFingerprintInfo(studentId: number): Observable<StudentFingerprintInfo> {
        return this.http.get<StudentFingerprintInfo>(
            `${this.apiBase}/students/${studentId}/fingerprint`
        );
    }

    /**
     * Tạo phiên enroll vân tay cho sinh viên
     * POST /api/fingerprint/enroll/session
     * body: { studentId }
     * response: { sessionCode }
     */
    // 🔹 SỬA: cho phép truyền kèm deviceCode
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
     * (tuỳ chọn) Kiểm tra phiên enroll đã nhận sensorSlot chưa
     * GET /api/fingerprint/enroll/temp?sessionCode=...
     * nếu bạn có API này sẵn.
     */
    checkEnrollTemp(sessionCode: string): Observable<{ found: boolean; sensorSlot?: number }> {
        return this.http.get<{ found: boolean; sensorSlot?: number }>(
            `${this.apiBase}/fingerprint/enroll/temp`,
            { params: { sessionCode } }
        );
    }
}
