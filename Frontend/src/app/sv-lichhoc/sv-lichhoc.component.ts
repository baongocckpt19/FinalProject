import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';

import {
  StudentScheduleService,
  StudentScheduleItem
} from '../services/student-schedule.service';

import {
  AttendanceService,
  StudentHistoryItem,
  StudentStatus
} from '../services/attendance.service';

import { AuthService } from '../services/auth.service';
import { NotificationService } from '../services/notification.service';
import { Account } from '../model/account';

interface StudentHistoryView {
  scheduleId: number;
  date: string;        // "yyyy-MM-dd"
  sessionTime: string; // "HH:mm - HH:mm"
  status: StudentStatus;
  attendanceTime: string | null;
}

interface StudentDetail {
  studentId: number;
  fullName: string;
  username: string;
  email?: string | null;
  phone?: string | null;
  classId: number;
  classCode: string;
  className: string;
  totalSessions: number;
  presentSessions: number;
  lateSessions: number;
  absentSessions: number;
  attendanceRate: number;
  history: StudentHistoryView[];
}

@Component({
  selector: 'app-sv-lichhoc',
  standalone: true,
  imports: [CommonModule, HttpClientModule],
  templateUrl: './sv-lichhoc.component.html',
  styleUrls: ['./sv-lichhoc.component.scss']
})
export class SvLichHocComponent implements OnInit {
  // ============ LỊCH HỌC THEO THÁNG ============
  scheduleData: StudentScheduleItem[] = [];
  classesOfSelectedDay: StudentScheduleItem[] = [];

  currentDate: Date = new Date();
  selectedDate: string = '';

  monthNames = [
    'Tháng 1',
    'Tháng 2',
    'Tháng 3',
    'Tháng 4',
    'Tháng 5',
    'Tháng 6',
    'Tháng 7',
    'Tháng 8',
    'Tháng 9',
    'Tháng 10',
    'Tháng 11',
    'Tháng 12'
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

  // ============ THÔNG TIN ACCOUNT HIỆN TẠI ============
  currentAccount: Account | null = null;
  currentStudentId: number | null = null;

  // ============ MODAL CHI TIẾT SINH VIÊN (TRONG 1 LỚP) ============
  showStudentDetailModal = false;
  studentDetail: StudentDetail | null = null;
  selectedClass: StudentScheduleItem | null = null;

  constructor(
    private studentScheduleService: StudentScheduleService,
    private attendanceService: AttendanceService,
    private authService: AuthService,
    private notify: NotificationService
  ) {}

  ngOnInit(): void {
    // Lấy account hiện tại
    this.authService.currentUser$.subscribe((acc) => {
      this.currentAccount = acc;
      // Tùy vào Account model của bạn, chỉnh lại field studentId cho đúng
      if (acc && (acc as any).studentId) {
        this.currentStudentId = (acc as any).studentId;
      }
    });

    // Mặc định chọn hôm nay
    this.selectedDate = this.formatDateKey(this.currentDate);
    this.loadSchedulesForCurrentMonth();
  }

  // ================== LOAD LỊCH HỌC THEO THÁNG ==================

  private loadSchedulesForCurrentMonth(): void {
    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth() + 1;

    this.studentScheduleService.getSchedulesByMonth(year, month).subscribe({
      next: (data) => {
        // Map lại field date cho đồng bộ (nếu backend trả scheduleDate)
        this.scheduleData = data.map((item: any) => ({
          ...item,
          date: item.date ?? item.scheduleDate // tùy backend
        }));

        // khi đổi tháng, chọn luôn ngày 1 trong tháng
        this.selectedDate = this.formatDateKey(
          new Date(year, this.currentDate.getMonth(), 1)
        );

        this.generateCalendar();
        this.updateClassesOfSelectedDay();
      },
      error: (err) => {
        console.error('Error loading student schedules', err);
        this.notify.error('Không tải được lịch học.');
        this.scheduleData = [];
        this.generateCalendar();
        this.updateClassesOfSelectedDay();
      }
    });
  }

  // ================== SINH LỊCH CALENDAR ==================

  generateCalendar(): void {
    this.calendarDays = [];

    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();

    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startingDayOfWeek = firstDay.getDay(); // 0 = CN

    // Ô trống đầu tháng
    for (let i = 0; i < startingDayOfWeek; i++) {
      this.calendarDays.push({ isEmpty: true });
    }

    const today = new Date();
    const todayKey = this.formatDateKey(today);

    for (let day = 1; day <= daysInMonth; day++) {
      const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(
        day
      ).padStart(2, '0')}`;

      const daySchedules = this.scheduleData.filter((item) => item.date === dateStr);

      this.calendarDays.push({
        dayNumber: day,
        dateStr,
        classCount: daySchedules.length,
        hasAttendance: daySchedules.length > 0,
        isToday: dateStr === todayKey,
        isSelected: dateStr === this.selectedDate,
        isEmpty: false
      });
    }
  }

  previousMonth(): void {
    this.currentDate = new Date(
      this.currentDate.getFullYear(),
      this.currentDate.getMonth() - 1,
      1
    );
    this.loadSchedulesForCurrentMonth();
  }

  nextMonth(): void {
    this.currentDate = new Date(
      this.currentDate.getFullYear(),
      this.currentDate.getMonth() + 1,
      1
    );
    this.loadSchedulesForCurrentMonth();
  }

  selectDate(dateStr?: string): void {
    if (!dateStr) return;
    this.selectedDate = dateStr;

    this.selectedClass = null;
    this.studentDetail = null;
    this.showStudentDetailModal = false;

    this.generateCalendar();
    this.updateClassesOfSelectedDay();
  }

  private updateClassesOfSelectedDay(): void {
    this.classesOfSelectedDay = this.scheduleData.filter(
      (item) => item.date === this.selectedDate
    );
  }

  private formatDateKey(date: Date): string {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  get currentMonthLabel(): string {
    return `${this.monthNames[this.currentDate.getMonth()]}, ${this.currentDate.getFullYear()}`;
  }

  // ================== TRẠNG THÁI BUỔI HỌC (LỚP) ==================

  private getRawStatus(
    item: StudentScheduleItem
  ): 'FINISHED' | 'HAPPENING' | 'UPCOMING' {
    const now = new Date();

    const start = new Date(`${item.date}T${item.startTime.substring(0, 5)}:00`);
    const end = new Date(`${item.date}T${item.endTime.substring(0, 5)}:00`);

    if (now > end) return 'FINISHED';
    if (now >= start && now <= end) return 'HAPPENING';
    return 'UPCOMING';
  }

  getStatusText(item: StudentScheduleItem): string {
    if (item.isActive === false) {
      return 'Tạm hoãn';
    }

    const s = this.getRawStatus(item);
    if (s === 'FINISHED') return 'Đã kết thúc';
    if (s === 'HAPPENING') return 'Đang diễn ra';
    return 'Sắp diễn ra';
  }

  getStatusClass(item: StudentScheduleItem): string {
    if (item.isActive === false) {
      return 'cancelled';
    }

    const s = this.getRawStatus(item);
    if (s === 'FINISHED') return 'finished';
    if (s === 'HAPPENING') return 'happening';
    return 'upcoming';
  }

  // ================== CLICK 1 LỚP TRONG NGÀY ==================

  onClassRowClick(cls: StudentScheduleItem): void {
    // Lớp tạm hoãn -> không xem chi tiết
    if (cls.isActive === false) {
      this.notify.error('Buổi học này đã tạm hoãn.');
      return;
    }

    const raw = this.getRawStatus(cls);

    // Bạn có thể cho phép xem lịch sử dù buổi sắp diễn ra.
    // Ở đây mình chặn giống trang GV:
    if (raw === 'UPCOMING') {
      this.notify.error('Buổi học này chưa diễn ra, chưa có dữ liệu điểm danh.');
      return;
    }

    this.selectedClass = cls;
    this.openStudentDetailForClass(cls);
  }

  // ================== MODAL CHI TIẾT SINH VIÊN ==================

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    const dd = String(d.getDate()).padStart(2, '0');
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const yyyy = d.getFullYear();
    return `${dd}/${mm}/${yyyy}`;
  }

  private openStudentDetailForClass(cls: StudentScheduleItem): void {
    if (!this.currentStudentId || !this.currentAccount) {
      this.notify.error('Không xác định được thông tin sinh viên hiện tại.');
      return;
    }

    const studentId = this.currentStudentId;

    // Skeleton ban đầu
    this.studentDetail = {
      studentId,
      fullName: (this.currentAccount as any).fullName || this.currentAccount.userName,
      username: this.currentAccount.userName,
      email: (this.currentAccount as any).email || '',
      phone: (this.currentAccount as any).phone || '',
      classId: cls.classId,
      classCode: cls.classCode,
      className: cls.className,
      totalSessions: 0,
      presentSessions: 0,
      lateSessions: 0,
      absentSessions: 0,
      attendanceRate: 0,
      history: []
    };

    this.showStudentDetailModal = true;

    // Gọi API lấy lịch sử điểm danh của sinh viên trong lớp
    this.attendanceService.getStudentHistory(cls.classId, studentId).subscribe({
      next: (hist: StudentHistoryItem[]) => {
        if (!this.studentDetail) return;

        const historyView: StudentHistoryView[] = hist.map((h) => ({
          scheduleId: h.scheduleId,
          date: h.date,
          sessionTime: `${h.startTime?.substring(0, 5)} - ${h.endTime?.substring(0, 5)}`,
          status: h.status,
          attendanceTime: h.attendanceTime
        }));

        const total = historyView.length;
        const present = historyView.filter((x) => x.status === 'present').length;
        const late = historyView.filter((x) => x.status === 'late').length;
        const absent = historyView.filter((x) => x.status === 'absent').length;
        const rate = total ? Math.round(((present + late) / total) * 100) : 0;

        this.studentDetail = {
          ...this.studentDetail,
          totalSessions: total,
          presentSessions: present,
          lateSessions: late,
          absentSessions: absent,
          attendanceRate: rate,
          history: historyView
        };
      },
      error: (err) => {
        console.error('Load student history error', err);
        this.notify.error('Không tải được lịch sử điểm danh của bạn trong lớp này.');
      }
    });
  }

  closeStudentDetailModal(): void {
    this.showStudentDetailModal = false;
  }
}
