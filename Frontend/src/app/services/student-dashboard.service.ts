// src/app/services/student-dashboard.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class StudentDashboardService {

  private readonly apiUrl = 'http://localhost:8080/api/student/dashboard';

  constructor(private http: HttpClient) {}

  /**
   * Lấy dữ liệu dashboard cho sinh viên đang đăng nhập.
   **/
  getStudentDashboard(): Observable<any> {
    return this.http.get<any>(this.apiUrl);
  }
}
