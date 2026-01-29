// src/app/_services/teacher-class.service.ts

import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TeacherClassService {
  private apiUrl = 'http://localhost:8080/api/teacher-classes';
  private classApiUrl = 'http://localhost:8080/api/classes'; 

  constructor(private http: HttpClient) { }

  private getAuthHeaders(): HttpHeaders | undefined {
    const token = localStorage.getItem('jwt_token');
    if (!token) return undefined;
    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }

  getMyClasses(): Observable<any[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<any[]>(this.apiUrl, { headers });
  }

  exportMyClasses(): Observable<Blob> {
    const headers = this.getAuthHeaders();
    return this.http.get(this.apiUrl + '/export/excel', {
      headers,
      responseType: 'blob'
    });
  }

  // ====== cập nhật trạng thái lớp học ======
  updateClassStatus(classId: number, status: boolean): Observable<string> {
  const headers = this.getAuthHeaders();
  return this.http.put(
    `${this.classApiUrl}/${classId}/status`,
    {},
    {
      headers,
      params: { status },
      responseType: 'text'   
    }
  );
}

}
