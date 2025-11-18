import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TeacherDashboardStats {
  totalStudents: number;
  activeClasses: number;
  averageScore: number;
  attendanceRate: number;
}

export interface ScoreDistribution {
  gioi: number;
  kha: number;
  trungBinh: number;
  yeu: number;
}

export interface TopScoreClass {
  classId: number;
  classCode: string;
  className: string;
  averageScore: number;
}

export interface TopAttendanceClass {
  classId: number;
  classCode: string;
  className: string;
  attendanceRate: number;
}

@Injectable({ providedIn: 'root' })
export class TeacherDashboardService {
  private baseUrl = 'http://localhost:8080/api/teacher-dashboard';

  constructor(private http: HttpClient) {}

  getStats(): Observable<TeacherDashboardStats> {
    return this.http.get<TeacherDashboardStats>(`${this.baseUrl}/stats`);
  }

  // Top 5 lớp có điểm trung bình cao nhất
  getTopScoreClasses(): Observable<TopScoreClass[]> {
    return this.http.get<TopScoreClass[]>(`${this.baseUrl}/top-score-classes`);
  }

  // Phân bố điểm
  getScoreDistribution(): Observable<ScoreDistribution> {
    return this.http.get<ScoreDistribution>(`${this.baseUrl}/score-distribution`);
  }

  // Top 5 lớp có tỉ lệ điểm danh cao nhất
  getTopAttendanceClasses(): Observable<TopAttendanceClass[]> {
    return this.http.get<TopAttendanceClass[]>(`${this.baseUrl}/top-attendance-classes`);
  }
}
