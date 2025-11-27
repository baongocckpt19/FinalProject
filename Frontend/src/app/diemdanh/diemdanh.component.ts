// diemdanh.component.ts
import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { ClassService, StudentOfClass } from '../services/class.service';
import { TeacherClassService } from '../services/teacher-class.service';
import {
  AttendanceService,
  AttendanceStudentRow
} from '../services/attendance.service';

type LiveStatus = 'present' | 'not_checked';

interface LiveAttendanceRow {
  studentId: number;        // MSSV = studentId
  fullName: string;
  attendanceTime: string | null; // "HH:mm:ss" hoặc null
  status: LiveStatus;
}

interface MyClassItem {
  classId: number;
  classCode: string;
  className: string;
  status?: boolean;
  // ... các field khác nếu có
}

@Component({
  selector: 'app-diemdanh',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './diemdanh.component.html',
  styleUrl: './diemdanh.component.scss'
})
export class DiemdanhComponent implements OnInit {
  ngOnInit(): void {
    throw new Error('Method not implemented.');
  }

//   // danh sách lớp của giảng viên (chỉ lớp đang hoạt động)
//   activeClasses: MyClassItem[] = [];

//   // lớp đang chọn
//   selectedClassId: number | null = null;

//   // danh sách dòng hiển thị trên bảng
//   rows: LiveAttendanceRow[] = [];

//   constructor(
//     private teacherClassService: TeacherClassService,
//     private classService: ClassService,
//     private attendanceService: AttendanceService
//   ) { }

//   ngOnInit(): void {
//     this.loadMyActiveClasses();
//   }

//   private loadMyActiveClasses(): void {
//     this.teacherClassService.getMyClasses().subscribe({
//       next: (data: any[]) => {
//         // giả sử API trả về { classId, classCode, className, status, ... }
//         this.activeClasses = (data || []).filter(c => c.status === true);
//       },
//       error: (err) => {
//         console.error('Lỗi load lớp của giảng viên:', err);
//         this.activeClasses = [];
//       }
//     });
//   }

//   // yyyy-MM-dd
//   private toIsoDate(d: Date): string {
//     const y = d.getFullYear();
//     const m = String(d.getMonth() + 1).padStart(2, '0');
//     const day = String(d.getDate()).padStart(2, '0');
//     return `${y}-${m}-${day}`;
//   }

//   onClassChange(): void {
//     if (!this.selectedClassId) {
//       this.rows = [];
//       return;
//     }
//     this.loadAttendanceForClass(this.selectedClassId);
//   }

//   reloadCurrentClass(): void {
//     if (this.selectedClassId) {
//       this.loadAttendanceForClass(this.selectedClassId);
//     }
//   }

//   private loadAttendanceForClass(classId: number): void {
//     const today = this.toIsoDate(new Date());

//     forkJoin({
//       students: this.classService.getStudentsOfClass(classId),
//       attendance: this.attendanceService.getStudentsAttendanceByDate(classId, today)
//     }).subscribe({
//       next: ({ students, attendance }) => {
//         // map attendance theo studentId
//         const attMap = new Map<number, AttendanceStudentRow>();
//         (attendance || []).forEach(a => {
//           attMap.set(a.studentId, a);
//         });

//         this.rows = (students || []).map((s: StudentOfClass) => {
//           const att = attMap.get(s.studentId);
//           return {
//             studentId: s.studentId,
//             fullName: s.fullName,
//             attendanceTime: att?.attendanceTime ?? null,
//             status: att ? 'present' : 'not_checked'
//           } as LiveAttendanceRow;
//         });
//       },
//       error: (err) => {
//         console.error('Lỗi load dữ liệu điểm danh:', err);
//         this.rows = [];
//       }
//     });
//   }

//   // Toggle trạng thái khi click vào badge
//   toggleStatus(row: LiveAttendanceRow): void {
//     if (row.status === 'present') {
//       // chuyển về chưa điểm danh
//       row.status = 'not_checked';
//       row.attendanceTime = null;
//     } else {
//       // chuyển sang có mặt
//       row.status = 'present';
//       if (!row.attendanceTime) {
//         row.attendanceTime = this.getCurrentTimeString();
//       }
//     }

//     // TODO: nếu cần update ngay lên backend, gọi API updateAttendanceStatus ở đây
//     // (tùy bạn muốn logic "live" hay nhấn nút Lưu điểm danh mới gửi)
//   }

//   private getCurrentTimeString(): string {
//     const now = new Date();
//     const hh = String(now.getHours()).padStart(2, '0');
//     const mm = String(now.getMinutes()).padStart(2, '0');
//     const ss = String(now.getSeconds()).padStart(2, '0');
//     return `${hh}:${mm}:${ss}`;
//   }

//   // ================== Thống kê ==================
//   get totalStudents(): number {
//     return this.rows.length;
//   }

//   get checkedCount(): number {
//     return this.rows.filter(r => r.status === 'present').length;
//   }

//   get notCheckedCount(): number {
//     return this.rows.filter(r => r.status === 'not_checked').length;
//   }

//   // ================== Save ==================
//   saveAttendance(): void {
//     // Chỗ này tuỳ thiết kế backend của bạn
//     // Ví dụ: gửi danh sách studentId có status = 'present' lên 1 API /api/device-attendance/save
//     // Tạm thời mình để TODO để bạn nối với backend sau.
//     console.log('Dữ liệu cần lưu:', this.rows);
//   }
// }
}
