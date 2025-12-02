import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface StudentScheduleItem {
  scheduleId: number;
  classId: number;
  date: string;        // yyyy-MM-dd
  className: string;
  classCode: string;
  startTime: string;   // HH:mm:ss
  endTime: string;     // HH:mm:ss
  room?: string;
  studentCount: number;
  isActive: boolean;
}

@Injectable({
  providedIn: 'root'
})


export class StudentScheduleService {
  // TODO: backend cần tạo endpoint này, giống hệt TeacherSchedule nhưng filter theo sinh viên
  // ví dụ: GET /api/student/schedules?year=2025&month=11
  private apiUrl = 'http://localhost:8080/api/student/schedules';

  constructor(private http: HttpClient) {}

  getSchedulesByMonth(year: number, month: number): Observable<StudentScheduleItem[]> {
    const params = new HttpParams()
      .set('year', year.toString())
      .set('month', month.toString());

    return this.http.get<StudentScheduleItem[]>(this.apiUrl, { params });
  }
}
