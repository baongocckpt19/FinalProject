import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type StudentStatus = 'present' | 'absent' | 'late' | 'none';

export interface AttendanceStudentRow {
  classId: number;
  scheduleId: number;

  studentId: number;
  fullName: string;
  username: string;
  email: string | null;
  phone: string | null;

  attendanceId: number | null;
  status: StudentStatus;
  attendanceTime: string | null;

  // Các thống kê theo lớp (nếu backend trả về)
  totalSessions: number;
  presentSessions: number;
  lateSessions: number;
  absentSessions: number;
  attendanceRate: number;
}
// interface lịch sử trả về từ backend
export interface StudentHistoryItem {
  scheduleId: number;
  date: string;       // LocalDate -> string ISO "yyyy-MM-dd"
  startTime: string;  // "HH:mm:ss"
  endTime: string;    // "HH:mm:ss"
  status: StudentStatus;
  attendanceTime: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class AttendanceService {
  private apiUrl = 'http://localhost:8080/api/teacher/attendance';

  constructor(private http: HttpClient) {}

  /** Lấy danh sách điểm danh của 1 buổi học (scheduleId) */
  getClassAttendanceDetail(scheduleId: number): Observable<AttendanceStudentRow[]> {
    return this.http.get<AttendanceStudentRow[]>(
      `${this.apiUrl}/schedule/${scheduleId}`
    );
  }

  /** Xuất báo cáo CSV cho 1 buổi học (scheduleId) */
  exportAttendanceReport(scheduleId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/schedule/${scheduleId}/export`, {
      responseType: 'blob'
    });
  }

  /** Cập nhật trạng thái 1 sinh viên trong buổi (nếu bạn cần sau) */
  updateStudentStatus(
    scheduleId: number,
    studentId: number,
    status: StudentStatus
  ): Observable<any> {
    return this.http.put(`${this.apiUrl}/schedule/${scheduleId}/students/${studentId}`, {
      status
    });
  }

  /** Lịch sử điểm danh của 1 sinh viên trong 1 lớp */
  getStudentHistory(
    classId: number,
    studentId: number
  ): Observable<StudentHistoryItem[]> {
    return this.http.get<StudentHistoryItem[]>(
      `${this.apiUrl}/class/${classId}/student/${studentId}/history`
    );
  }
}
