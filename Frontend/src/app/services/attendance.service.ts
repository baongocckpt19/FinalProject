// src/app/services/attendance.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  NotificationService
} from '../services/notification.service';

type StudentStatus = 'present' | 'absent' | 'late'; 

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


@Injectable({
  providedIn: 'root'
})
export class AttendanceService {

  // Nếu bạn có environment thì đổi lại cho đúng
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
}
