import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export type StudentStatus = 'present' | 'absent' | 'late';

export interface AttendanceCalendarDay {
  date: string;      // yyyy-MM-dd
  classCount: number;
}

export interface AttendanceClassSummary {
  classId: number;
  classCode: string;
  className: string;
  time: string;
  status: string;
  total: number;
  present: number;
  absent: number;
  late: number;
  rate: number;
}

export interface AttendanceStudentRow {
  attendanceId: number;
  studentId: number;
  fullName: string;
  username: string;
  status: StudentStatus;
  attendanceTime: string | null;

  totalSessions: number;
  presentSessions: number;
  lateSessions: number;
  absentSessions: number;
  attendanceRate: number;
}

export interface StudentAttendanceHistoryRow {
  attendanceId: number;
  date: string;        // yyyy-MM-dd
  sessionTime: string; // "HH:mm - HH:mm"
  status: StudentStatus;
  attendanceTime: string | null;
}

export interface StudentAttendanceDetail {
  studentId: number;
  fullName: string;
  username: string;
  email: string;
  phone: string;

  classId: number;
  classCode: string;
  className: string;

  totalSessions: number;
  presentSessions: number;
  lateSessions: number;
  absentSessions: number;
  attendanceRate: number;

  history: StudentAttendanceHistoryRow[];
}

@Injectable({
  providedIn: 'root'
})
export class AttendanceService {

  private apiUrl = 'http://localhost:8080/api/teacher-attendance';

  constructor(private http: HttpClient) {}

  // Lấy dữ liệu cho Calendar (trong khoảng start–end)
  getCalendar(start: string, end: string): Observable<AttendanceCalendarDay[]> {
    const params = new HttpParams()
      .set('start', start)
      .set('end', end);
    return this.http.get<AttendanceCalendarDay[]>(`${this.apiUrl}/calendar`, { params });
  }

  // Lấy danh sách lớp điểm danh trong 1 ngày
  getClassesByDate(date: string): Observable<AttendanceClassSummary[]> {
    const params = new HttpParams().set('date', date);
    return this.http.get<AttendanceClassSummary[]>(`${this.apiUrl}/day`, { params });
  }

  // Lấy chi tiết điểm danh theo sinh viên trong 1 lớp ở 1 ngày
  getClassAttendanceDetail(classId: number, date: string): Observable<AttendanceStudentRow[]> {
    const params = new HttpParams().set('date', date);
    return this.http.get<AttendanceStudentRow[]>(`${this.apiUrl}/day/${classId}/students`, { params });
  }

  // Cập nhật trạng thái 1 bản ghi điểm danh
  updateAttendanceStatus(attendanceId: number, status: StudentStatus): Observable<any> {
    return this.http.put(`${this.apiUrl}/attendance/${attendanceId}/status`, { status });
  }

  // Export CSV báo cáo điểm danh lớp
  exportAttendanceReport(classId: number, date: string): Observable<Blob> {
    const params = new HttpParams().set('date', date);
    return this.http.get(`${this.apiUrl}/day/${classId}/export`, {
      params,
      responseType: 'blob'
    });
  }

  // Lấy chi tiết 1 sinh viên trong lớp (Student Detail Modal)
  getStudentAttendanceDetail(classId: number, studentId: number): Observable<StudentAttendanceDetail> {
    return this.http.get<StudentAttendanceDetail>(`${this.apiUrl}/classes/${classId}/students/${studentId}`);
  }

   getStudentsAttendanceByDate(
    classId: number,
    date: string
  ): Observable<AttendanceStudentRow[]> {
    const params = new HttpParams().set('date', date);
    return this.http.get<AttendanceStudentRow[]>(
      `${this.apiUrl}/day/${classId}/students`,
      { params }
    );
  }
}
