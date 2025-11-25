import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  AttendanceService,
  AttendanceCalendarDay,
  AttendanceClassSummary,
  AttendanceStudentRow,
  StudentAttendanceDetail,
  StudentAttendanceHistoryRow,
  StudentStatus
} from '../services/attendance.service';
import { NotificationService } from '../services/notification.service';

@Component({
  selector: 'app-gv-quanlydiemdanh',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gv-quanlydiemdanh.component.html',
  styleUrl: './gv-quanlydiemdanh.component.scss'
})
export class GvQuanlydiemdanhComponent implements OnInit {

  currentDate: Date = new Date();

  selectedDate: string = '';
  selectedDateLabel: string = '';

  attendanceCalendar: AttendanceCalendarDay[] = [];
  attendanceClasses: AttendanceClassSummary[] = [];

  monthNames = [
    'Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
    'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'
  ];

  calendarDays: {
    dayNumber?: number;
    dateStr?: string;
    isToday?: boolean;
    isSelected?: boolean;
    hasAttendance?: boolean;
    isEmpty?: boolean;
    classCount?: number;
  }[] = [];

  showAttendanceDetailModal = false;
   selectClass: AttendanceClassSummary | null = null;

  showStudentDetailModal = false;

  // danh sách sinh viên trong lớp (chi tiết ngày)
  attendanceStudents: AttendanceStudentRow[] = [];

  // chi tiết 1 sinh viên trong modal
  studentDetail: StudentAttendanceDetail | null = null;

  constructor(
    private attendanceService: AttendanceService,
    private notify: NotificationService
  ) { }

  ngOnInit() {
    const todayIso = this.toIsoDate(this.currentDate);
    this.selectedDate = todayIso;
    this.selectedDateLabel = this.formatDate(todayIso);
    this.loadCalendarForCurrentMonth();
  }

  private toIsoDate(d: Date): string {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }

  private loadCalendarForCurrentMonth(): void {
    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();

    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);

    const start = this.toIsoDate(firstDay);
    const end = this.toIsoDate(lastDay);

    this.attendanceService.getCalendar(start, end).subscribe({
      next: (data) => {
        this.attendanceCalendar = data;
        this.generateCalendar();
        this.loadClassesForDate(this.selectedDate);
      },
      error: (err) => {
        console.error('Lỗi load calendar:', err);
        this.attendanceCalendar = [];
        this.generateCalendar();
      }
    });
  }

  generateCalendar() {
    this.calendarDays = [];

    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();

    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startingDayOfWeek = firstDay.getDay();

    const attendanceMap = new Map<string, number>();
    this.attendanceCalendar.forEach(item => {
      attendanceMap.set(item.date, item.classCount || 0);
    });

    for (let i = 0; i < startingDayOfWeek; i++) {
      this.calendarDays.push({ isEmpty: true });
    }

    const today = new Date();
    const todayIso = this.toIsoDate(today);

    for (let day = 1; day <= daysInMonth; day++) {
      const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
      const classCount = attendanceMap.get(dateStr) || 0;

      this.calendarDays.push({
        dayNumber: day,
        dateStr,
        classCount,
        hasAttendance: classCount > 0,
        isToday: dateStr === todayIso,
        isSelected: dateStr === this.selectedDate,
        isEmpty: false
      });
    }
  }

  previousMonth() {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() - 1, 1);
    this.loadCalendarForCurrentMonth();
  }

  nextMonth() {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, 1);
    this.loadCalendarForCurrentMonth();
  }

  selectDate(dateStr?: string) {
    if (!dateStr) return;

    this.selectedDate = dateStr;
    this.selectedDateLabel = this.formatDate(dateStr);
    this.generateCalendar();
    this.loadClassesForDate(dateStr);

    // Khi chọn ngày khác thì đóng tất cả modal như yêu cầu
    this.closeAttendanceDetailModal();
    this.closeStudentDetailModal();
    this.attendanceStudents = [];
    this.studentDetail = null;
    this.selectClass = null;
  }

  private loadClassesForDate(dateStr: string): void {
    this.attendanceService.getClassesByDate(dateStr).subscribe({
      next: (classes) => {
        this.attendanceClasses = classes;
      },
      error: (err) => {
        console.error('Lỗi load lớp điểm danh:', err);
        this.attendanceClasses = [];
      }
    });
  }

  getEffectivePresent(c: AttendanceClassSummary): number {
    const present = c.present ?? 0;
    const late = c.late ?? 0;
    return present + late;
  }

  getEffectiveRate(c: AttendanceClassSummary): number {
    const total = c.total ?? 0;
    if (!total) return 0;
    return (this.getEffectivePresent(c) * 100) / total;
  }

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    const d = String(date.getDate()).padStart(2, '0');
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const y = date.getFullYear();
    return `${d}/${m}/${y}`;
  }

  get currentMonthLabel(): string {
    return `${this.monthNames[this.currentDate.getMonth()]}, ${this.currentDate.getFullYear()}`;
  }

  // ================== MODAL LỚP ==================
  openAttendanceDetailModal(attendanceClass: AttendanceClassSummary, event?: MouseEvent) {
    if (event) {
      event.stopPropagation();
    }
    this.selectClass = attendanceClass;
    this.showAttendanceDetailModal = true;

    this.loadAttendanceDetailForClass(attendanceClass.classId);
  }

  private loadAttendanceDetailForClass(classId: number): void {
    if (!this.selectedDate) return;

    this.attendanceService.getClassAttendanceDetail(classId, this.selectedDate).subscribe({
      next: (rows) => {
        this.attendanceStudents = rows;
      },
      error: (err) => {
        console.error('Lỗi load chi tiết sinh viên:', err);
        this.attendanceStudents = [];
      }
    });
  }

  closeAttendanceDetailModal() {
    this.showAttendanceDetailModal = false;
  }

  // ================== MODAL SINH VIÊN ==================
  openStudentDetailModal(student: AttendanceStudentRow, event?: MouseEvent) {
    if (event) {
      event.stopPropagation();
    }
    if (!this.selectClass) return;

    this.showStudentDetailModal = true;

    this.attendanceService.getStudentAttendanceDetail(this.selectClass.classId, student.studentId)
      .subscribe({
        next: (detail) => {
          this.studentDetail = detail;
        },
        error: (err) => {
          console.error('Lỗi load chi tiết sinh viên:', err);
          this.studentDetail = null;
        }
      });
  }

  closeStudentDetailModal() {
    this.showStudentDetailModal = false;
  }

  // ================== Toggle trạng thái (3 trạng thái) ==================
  private getNextStatus(current: StudentStatus): StudentStatus {
    if (current === 'present') return 'late';
    if (current === 'late') return 'absent';
    return 'present';
  }

  onToggleStudentStatus(student: AttendanceStudentRow, event: MouseEvent) {
    event.stopPropagation();
    const nextStatus = this.getNextStatus(student.status);

    this.attendanceService.updateAttendanceStatus(student.attendanceId, nextStatus)
      .subscribe({
        next: () => {
          student.status = nextStatus;
          this.notify.success('Cập nhật trạng thái điểm danh thành công');
        },
        error: (err) => {
          console.error('Lỗi cập nhật trạng thái:', err);
          this.notify.error('Cập nhật trạng thái điểm danh thất bại');
        }
      });
  }

  onToggleHistoryStatus(row: StudentAttendanceHistoryRow, event: MouseEvent) {
    event.stopPropagation();
    const nextStatus = this.getNextStatus(row.status);

    this.attendanceService.updateAttendanceStatus(row.attendanceId, nextStatus)
      .subscribe({
        next: () => {
          row.status = nextStatus;
          this.notify.success('Cập nhật trạng thái điểm danh thành công');
        },
        error: (err) => {
          console.error('Lỗi cập nhật trạng thái:', err);
          this.notify.error('Cập nhật trạng thái điểm danh thất bại');
        }
      });
  }

  // ================== Export CSV ==================
exportAttendanceReport(event?: MouseEvent) {
  if (event) {
    event.stopPropagation();
  }

  // Lưu lại tham chiếu class hiện tại vào biến cục bộ
  const selectedClass = this.selectClass;
  const selectedDate = this.selectedDate;

  // Nếu chưa chọn lớp hoặc chưa có ngày thì không export
  if (!selectedClass || !selectedDate) return;

  this.attendanceService.exportAttendanceReport(selectedClass.classId, selectedDate)
    .subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `attendance_${selectedClass.classCode}_${selectedDate}.csv`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.notify.success('Xuất báo cáo điểm danh thành công');
      },
      error: (err) => {
        console.error('Lỗi export báo cáo:', err);
        this.notify.error('Xuất báo cáo điểm danh thất bại');
      }
    });
}


  // ================== Tìm kiếm lớp ==================
  searchTerm: string = '';

  get filteredAttendanceClasses(): AttendanceClassSummary[] {
    const term = this.searchTerm.trim().toLowerCase();
    if (!term) {
      return this.attendanceClasses;
    }

    return this.attendanceClasses.filter(c => {
      const code = c.classCode?.toLowerCase() || '';
      const name = c.className?.toLowerCase() || '';
      return code.includes(term) || name.includes(term);
    });
  }
}
