import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TeacherClass {
  classId: number;
  classCode: string;
  className: string;
}

export interface StudentGradeApi {
  studentId: number;
  fullName: string;
  username: string;
  attendanceGrade: number | null;
  midtermGrade: number | null;
  finalGrade: number | null;
  averageGrade: number | null;
}

export interface StudentGradeSaveDto {
  studentId: number;
  attendanceGrade: number;
  midtermGrade: number;
  finalGrade: number;
}

@Injectable({
  providedIn: 'root'
})
export class GradeService {

  private apiBase = 'http://localhost:8080';

  constructor(private http: HttpClient) { }

  /** Lấy danh sách lớp của giảng viên đang đăng nhập */
  getTeacherClasses(): Observable<TeacherClass[]> {
    return this.http.get<TeacherClass[]>(`${this.apiBase}/api/teacher-classes`);
  }

  /** Lấy danh sách điểm của 1 lớp */
  getClassGrades(classId: number): Observable<StudentGradeApi[]> {
    return this.http.get<StudentGradeApi[]>(
      `${this.apiBase}/api/classes/${classId}/grades`
    );
  }

  /** Lưu điểm 1 sinh viên trong lớp */
  updateStudentGrade(classId: number, dto: StudentGradeSaveDto): Observable<any> {
    return this.http.put(
      `${this.apiBase}/api/classes/${classId}/grades/${dto.studentId}`,
      dto
    );
  }

  /** Lưu tất cả điểm trong lớp */
  updateAllGrades(classId: number, list: StudentGradeSaveDto[]): Observable<any> {
    return this.http.put(
      `${this.apiBase}/api/classes/${classId}/grades`,
      list
    );
  }

  /** Export báo cáo điểm của lớp ra CSV (backend sinh file CSV) */
  exportClassGrades(classId: number): Observable<Blob> {
    return this.http.get(
      `${this.apiBase}/api/classes/${classId}/export/grades`,
      { responseType: 'blob' }
    );
  }


  /** Import điểm từ file CSV */
  importGradesFromCsv(
    classId: number,
    gradeType: string,
    file: File
  ): Observable<any> {
    const form = new FormData();
    form.append('file', file);
    form.append('gradeType', gradeType);

    return this.http.post(
      `${this.apiBase}/api/classes/${classId}/grades/import`,
      form
    );
  }

}
