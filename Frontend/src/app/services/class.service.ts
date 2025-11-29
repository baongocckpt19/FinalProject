import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ClassList {
  classId: number;
  classCode: string;
  className: string;
  teacherName: string | null;
  studentCount: number;
  createdDate: string;
  status: boolean;
  fingerprintedCount: number; // <-- BẮT BUỘC PHẢI CÓ
}

export interface ClassDetail {
  classId: number;
  classCode: string;
  className: string;
  teacherId: number | null;
  teacherName: string | null;
  createdDate: string;
  status: boolean;
}

export interface StudentOfClass {
  studentId: number;
  fullName: string;
  username: string;
  email: string;
  fingerCount: number;
}
/** LỊCH HỌC THEO NGÀY */
export interface ClassScheduleItem {
  scheduleId: number;
  classId: number;
  scheduleDate: string;
  startTime: string;
  endTime: string;
  room: string | null;
  isActive: boolean;
}
@Injectable({
  providedIn: 'root'
})
export class ClassService {
  private apiUrl = 'http://localhost:8080/api/classes';
  private scheduleApiUrl = 'http://localhost:8080/api/class-schedule';

  constructor(private http: HttpClient) { }

  getAllClasses(): Observable<ClassList[]> {
    return this.http.get<ClassList[]>(this.apiUrl);
  }

  deleteClass(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, { responseType: 'text' });
  }

  exportExcel() {
    return this.http.get(this.apiUrl + '/export/excel', {
      responseType: 'blob'
    });
  }
  // export danh sách sinh viên của lớp
  exportStudents(classId: number) {
    return this.http.get(this.apiUrl + '/' + classId + '/export/students', {
      responseType: 'blob'
    });
  }
  // lấy chi tiết lớp học để thưc hiện chỉnh sửa
  getClassById(id: number) {
    return this.http.get<ClassDetail>(this.apiUrl + '/' + id);
  }
  // cập nhật lớp học ở modal chỉnh sửa
  updateClass(id: number, payload: any) {
    return this.http.put(this.apiUrl + '/' + id, payload, {
      responseType: 'text'
    });
  }
  //lấy thông tin giáo viên theo id ở modal chỉnh sửa lớp
  getTeacherById(id: number) {
    return this.http.get<{ teacherId: number, fullName: string }>('http://localhost:8080/api/teachers/' + id);
  }

  // tạo lớp học mới
  createClass(payload: { classCode: string; className: string; teacherId?: number | null }) {
    return this.http.post(this.apiUrl, payload, {
      responseType: 'text'
    });
  }

  // lấy thông tin sinh viên theo id để hiển thị trong modal thêm sinh viên vào lớp
  getStudentById(studentId: number) {
    return this.http.get<any>('http://localhost:8080/api/students/' + studentId);
  }
  // thêm sinh viên vào lớp học
  addStudentsToClass(classId: number, studentIds: number[]) {
    return this.http.post('http://localhost:8080/api/classes/' + classId + '/students', {
      studentIds
    });
  }


  // class.service.ts
  importStudentsFromCsv(classId: number, formData: FormData) {
    return this.http.post(`${this.apiUrl}/${classId}/students/import`, formData);
  }

  // lấy danh sách SV của lớp (cho modal THUỘC PHẦN LỚP HỌC-GV, ADMIN)
  getStudentsOfClass(classId: number) {
    return this.http.get<StudentOfClass[]>(`${this.apiUrl}/${classId}/students`);
  }

  // xóa SV khỏi lớp
  removeStudentFromClass(classId: number, studentId: number) {
    return this.http.delete(`${this.apiUrl}/${classId}/students/${studentId}`, {
      responseType: 'text'
    });
  }
  // bật/tắt trạng thái lớp học
  toggleStatus(classId: number, newStatus: boolean) {
    return this.http.put(
      `${this.apiUrl}/${classId}/status?status=${newStatus}`,
      {},
      { responseType: 'text' }
    );
  }
  // ================== CLASS SCHEDULE của admin(LỊCH HỌC) ==================

  /** Lấy tất cả lịch học của 1 lớp */
  getSchedulesByClassId(classId: number): Observable<ClassScheduleItem[]> {
    // gợi ý API: GET /api/class-schedule/by-class/{classId}
    return this.http.get<ClassScheduleItem[]>(`${this.scheduleApiUrl}/by-class/${classId}`);
  }

  /** Tạo mới lịch học */
  createSchedule(payload: {
    classId: number;
    scheduleDate: string;
    startTime: string;
    endTime: string;
    room?: string;
    isActive: boolean;
  }) {
    // gợi ý API: POST /api/class-schedule
    return this.http.post(this.scheduleApiUrl, payload, {
      responseType: 'text'
    });
  }

  /** Cập nhật lịch học */
  updateSchedule(scheduleId: number, payload: any) {
    // gợi ý API: PUT /api/class-schedule/{id}
    return this.http.put(`${this.scheduleApiUrl}/${scheduleId}`, payload, {
      responseType: 'text'
    });
  }

  /** Bật/tắt IsActive (tạm hoãn) */
  updateScheduleActive(scheduleId: number, isActive: boolean) {
    // gợi ý API: PUT /api/class-schedule/{id}/active?isActive=true/false
    return this.http.put(
      `${this.scheduleApiUrl}/${scheduleId}/active?isActive=${isActive}`,
      {},
      { responseType: 'text' }
    );
  }

  /** Xóa 1 lịch học */
  deleteSchedule(scheduleId: number) {
    // gợi ý API: DELETE /api/class-schedule/{id}
    return this.http.delete(`${this.scheduleApiUrl}/${scheduleId}`, {
      responseType: 'text'
    });
  }


}
