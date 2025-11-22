import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';

import { AttendanceService, AttendanceCalendarDay, AttendanceClassSummary } from '../services/attendance.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-gv-quanlydiemdanh',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gv-quanlydiemdanh.component.html',
  styleUrl: './gv-quanlydiemdanh.component.scss'
})
export class GvQuanlydiemdanhComponent implements OnInit {

  currentDate: Date = new Date();               // ngày hiện tại

  /** ngày đang chọn (dạng yyyy-MM-dd – dùng để gọi API) */
  selectedDate: string = '';

  /** Chuỗi hiển thị ngày đang chọn (dd/MM/yyyy) */
  selectedDateLabel: string = '';

  /** Dữ liệu calendar lấy từ API */
  attendanceCalendar: AttendanceCalendarDay[] = [];

  /** Danh sách lớp điểm danh trong ngày được chọn */
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

  // các phần modal cũ của bạn giữ nguyên
  showAttendanceDetailModal = false;
  selectClass: any = null;

  showStudentDetailModal = false;

  constructor(private attendanceService: AttendanceService) { }

  ngOnInit() {
    // Khởi tạo ngày đang chọn = hôm nay
    const todayIso = this.toIsoDate(this.currentDate); // yyyy-MM-dd
    this.selectedDate = todayIso;
    this.selectedDateLabel = this.formatDate(todayIso);

    // Load calendar cho tháng hiện tại + dữ liệu lớp của ngày hôm nay
    this.loadCalendarForCurrentMonth();
  }

  /** Đổi Date -> yyyy-MM-dd */
  private toIsoDate(d: Date): string {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }

  /** Lấy 1–>cuối tháng hiện tại từ currentDate, rồi gọi API calendar */
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
        // Sau khi sinh lịch, cũng load danh sách lớp của ngày đang chọn
        this.loadClassesForDate(this.selectedDate);
      },
      error: (err) => {
        console.error('Lỗi load calendar:', err);
        this.attendanceCalendar = [];
        this.generateCalendar();
      }
    });
  }

  /** Sinh lịch tháng dựa trên currentDate + attendanceCalendar */
  generateCalendar() {
    this.calendarDays = [];

    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();

    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startingDayOfWeek = firstDay.getDay(); // 0 = CN

    // map nhanh: yyyy-MM-dd -> classCount
    const attendanceMap = new Map<string, number>();
    this.attendanceCalendar.forEach(item => {
      attendanceMap.set(item.date, item.classCount || 0);
    });

    // Ô trống đầu tháng
    for (let i = 0; i < startingDayOfWeek; i++) {
      this.calendarDays.push({ isEmpty: true });
    }

    // Ô ngày thực tế
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

  /** Chuyển tháng */
  previousMonth() {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() - 1, 1);
    this.loadCalendarForCurrentMonth();
  }

  nextMonth() {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, 1);
    this.loadCalendarForCurrentMonth();
  }

  /** Khi click vào 1 ngày */
  selectDate(dateStr?: string) {
    if (!dateStr) return;

    this.selectedDate = dateStr;
    this.selectedDateLabel = this.formatDate(dateStr);
    this.generateCalendar();           // cập nhật lại isSelected trên calendar
    this.loadClassesForDate(dateStr);  // gọi API lấy danh sách lớp
  }

  /** Gọi API lấy danh sách lớp điểm danh trong 1 ngày */
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

  // Số "thực sự có mặt" = Có mặt + Muộn
  getEffectivePresent(c: AttendanceClassSummary): number {
    const present = c.present ?? 0;
    const late = c.late ?? 0;
    return present + late;
  }

  // Tỉ lệ điểm danh = (Có mặt + Muộn) / Tổng * 100
  getEffectiveRate(c: AttendanceClassSummary): number {
    const total = c.total ?? 0;
    if (!total) return 0;
    return (this.getEffectivePresent(c) * 100) / total;
  }


  /** Định dạng ngày dd/MM/yyyy cho hiển thị */
  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    const d = String(date.getDate()).padStart(2, '0');
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const y = date.getFullYear();
    return `${d}/${m}/${y}`;
  }

  /** Tên tháng hiển thị */
  get currentMonthLabel(): string {
    return `${this.monthNames[this.currentDate.getMonth()]}, ${this.currentDate.getFullYear()}`;
  }

  // ================== MODAL LỚP (giữ nguyên logic cũ) ==================
  openAttendanceDetailModal(attendanceClass: AttendanceClassSummary) {
    this.selectClass = attendanceClass;
    this.showAttendanceDetailModal = true;
  }
  closeAttendanceDetailModal() {
    this.showAttendanceDetailModal = false;
  }

  // xem chi tiết sinh viên
  openStudentDetailModal() {
    this.showStudentDetailModal = true;
  }
  closeStudentDetailModal() {
    this.showStudentDetailModal = false;
  }

  // phần students + toggleStudentStatus bạn có thể giữ nguyên hoặc sau này load thêm từ API attendance chi tiết
  students = [
    { id: 1, ho: 'Nguyễn Văn', ten: 'A', mssv: '2021001', status: 'present', rate: 95, time: '07:32', absent: 2 },
    { id: 2, ho: 'Trần Thị', ten: 'B', mssv: '2021002', status: 'absent', rate: 80, time: '-', absent: 5 },
  ];

  toggleStudentStatus(id: number): void {
    const student = this.students.find(s => s.id === id);
    if (!student) return;
    student.status = student.status === 'present' ? 'absent' : 'present';
  }

  // Chuỗi tìm kiếm
searchTerm: string = '';

// Danh sách lớp sau khi filter theo searchTerm
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
