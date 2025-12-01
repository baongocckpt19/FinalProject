import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';

import {
  TeacherScheduleService,
  ScheduleItem
} from '../services/teacher-schedule.service';

import {
  AttendanceService,
  AttendanceStudentRow,
  StudentStatus,
  StudentHistoryItem
} from '../services/attendance.service';

import { NotificationService } from '../services/notification.service';


interface StudentHistoryView {
  scheduleId: number;
  date: string;          // "yyyy-MM-dd"
  sessionTime: string;   // "HH:mm - HH:mm"
  status: StudentStatus;
  attendanceTime: string | null;
}

interface StudentDetail {
  studentId: number;
  studentCode: string;
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
  selector: 'app-gv-lichday',
  standalone: true,
  imports: [CommonModule, HttpClientModule],
  templateUrl: './gv-lichday.component.html',
  styleUrl: './gv-lichday.component.scss'
})
export class GvLichDayComponent implements OnInit {
  // Dữ liệu lịch dạy (toàn bộ tháng đang xem)
  scheduleData: ScheduleItem[] = [];

  // Danh sách lớp của ngày đang chọn
  classesOfSelectedDay: ScheduleItem[] = [];

  // Ngày hiện tại
  currentDate: Date = new Date();

  // Ngày đang chọn (yyyy-MM-dd)
  selectedDate: string = '';

  // Tên tháng hiển thị
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

  // Mảng dùng cho grid lịch
  calendarDays: {
    dayNumber?: number;
    dateStr?: string;
    isToday?: boolean;
    isSelected?: boolean;
    hasAttendance?: boolean;
    isEmpty?: boolean;
    classCount?: number;
  }[] = [];

  // ====== PHẦN CHI TIẾT ĐIỂM DANH ======

  // Lớp đang chọn để xem chi tiết điểm danh
  selectedClass: ScheduleItem | null = null;

  // Danh sách điểm danh sinh viên cho lớp + ngày
  attendanceRows: AttendanceStudentRow[] = [];

  attendanceLoading = false;

  attendanceStats = {
    total: 0,
    present: 0,
    absent: 0,
    late: 0,
    rate: 0
  };

  constructor(
    private scheduleService: TeacherScheduleService,
    private attendanceService: AttendanceService,
    private notify: NotificationService
  ) { }

  ngOnInit(): void {
    // mặc định chọn hôm nay
    this.selectedDate = this.formatDateKey(this.currentDate);
    this.loadSchedulesForCurrentMonth();
  }

  /** Gọi API lấy lịch dạy cho tháng đang xem */
  private loadSchedulesForCurrentMonth(): void {
    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth() + 1;

    this.scheduleService.getSchedulesByMonth(year, month).subscribe({
      next: (data) => {
        // Map lại key date cho tiện dùng
        this.scheduleData = data.map((item) => ({
          ...item,
          date: (item as any).date ?? this.toDateString(item) // backup nếu backend trả LocalDate khác key
        }));

        // khi đổi tháng, chọn luôn ngày 1 của tháng đó
        this.selectedDate = this.formatDateKey(
          new Date(year, this.currentDate.getMonth(), 1)
        );

        // reset phần chi tiết điểm danh
        this.selectedClass = null;
        this.attendanceRows = [];
        this.attendanceStats = { total: 0, present: 0, absent: 0, late: 0, rate: 0 };

        this.generateCalendar();
        this.updateClassesOfSelectedDay();
      },
      error: (err) => {
        console.error('Error loading schedules', err);
        this.notify.error('Không tải được lịch dạy.');

        this.scheduleData = [];
        this.selectedClass = null;
        this.attendanceRows = [];
        this.attendanceStats = { total: 0, present: 0, absent: 0, late: 0, rate: 0 };

        this.generateCalendar();
        this.updateClassesOfSelectedDay();
      }
    });
  }

  /** Sinh lịch cho tháng hiện tại */
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

    // Các ngày thực tế
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

  /** Lùi 1 tháng */
  previousMonth(): void {
    this.currentDate = new Date(
      this.currentDate.getFullYear(),
      this.currentDate.getMonth() - 1,
      1
    );
    this.loadSchedulesForCurrentMonth();
  }

  /** Tới 1 tháng */
  nextMonth(): void {
    this.currentDate = new Date(
      this.currentDate.getFullYear(),
      this.currentDate.getMonth() + 1,
      1
    );
    this.loadSchedulesForCurrentMonth();
  }

  /** Khi click chọn ngày trên lịch */
  selectDate(dateStr?: string): void {
    if (!dateStr) return;
    this.selectedDate = dateStr;

    // Mỗi lần đổi ngày, ẩn chi tiết điểm danh cũ
    this.selectedClass = null;
    this.attendanceRows = [];
    this.attendanceStats = { total: 0, present: 0, absent: 0, late: 0, rate: 0 };

    this.generateCalendar(); // cập nhật lại isSelected
    this.updateClassesOfSelectedDay(); // cập nhật list lớp
  }

  /** Cập nhật danh sách lớp của ngày đang chọn */
  private updateClassesOfSelectedDay(): void {
    this.classesOfSelectedDay = this.scheduleData.filter(
      (item) => item.date === this.selectedDate
    );
  }

  /** format key yyyy-MM-dd từ Date */
  private formatDateKey(date: Date): string {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  /** backup nếu cần convert từ scheduleDate LocalDate */
  private toDateString(item: any): string {
    // nếu backend trả { scheduleDate: '2025-11-27' } thì bạn chỉnh lại cho phù hợp
    if (item.scheduleDate) return String(item.scheduleDate);
    return this.formatDateKey(new Date());
  }

  /** Label tháng hiện tại */
  get currentMonthLabel(): string {
    return `${this.monthNames[this.currentDate.getMonth()]}, ${this.currentDate.getFullYear()
      }`;
  }

  // =========================
  //  TRẠNG THÁI LỚP THEO THỜI GIAN
  // =========================

  /** Trả về 'FINISHED' | 'HAPPENING' | 'UPCOMING' */
  private getRawStatus(item: ScheduleItem): 'FINISHED' | 'HAPPENING' | 'UPCOMING' {
    const now = new Date();

    const start = new Date(`${item.date}T${item.startTime.substring(0, 5)}:00`);
    const end = new Date(`${item.date}T${item.endTime.substring(0, 5)}:00`);

    if (now > end) return 'FINISHED';
    if (now >= start && now <= end) return 'HAPPENING';
    return 'UPCOMING';
  }


  getStatusText(item: ScheduleItem): string {
    if (item.isActive === false) {
      return 'Tạm hoãn';
    }

    const s = this.getRawStatus(item);
    if (s === 'FINISHED') return 'Đã kết thúc';
    if (s === 'HAPPENING') return 'Đang diễn ra';
    return 'Sắp diễn ra';
  }

  getStatusClass(item: ScheduleItem): string {
    if (item.isActive === false) {
      return 'cancelled';
    }

    const s = this.getRawStatus(item);
    if (s === 'FINISHED') return 'finished';
    if (s === 'HAPPENING') return 'happening';
    return 'upcoming';
  }


  // =========================
  //   CHI TIẾT ĐIỂM DANH LỚP
  // =========================

  /** Khi click vào 1 lớp trong bảng ngày */
onClassRowClick(cls: ScheduleItem): void {
  // ⭐ lớp tạm hoãn -> không mở modal
  if (cls.isActive === false) {
    this.notify.error('Buổi học này đã tạm hoãn, không có dữ liệu điểm danh.');
    return;
  }

  const raw = this.getRawStatus(cls);

  // Chỉ xem điểm danh khi lớp đã diễn ra hoặc đang diễn ra
  if (raw === 'UPCOMING') {
    this.notify.error('Lớp này chưa diễn ra, chưa có dữ liệu điểm danh.');
    return;
  }

  this.selectedClass = cls;
  this.loadAttendanceForSelectedClass();
}


  private loadAttendanceForSelectedClass(): void {
    if (!this.selectedClass) return;

    this.attendanceLoading = true;

    this.attendanceService
      .getClassAttendanceDetail(this.selectedClass.scheduleId)   // ⬅️ chỉ còn scheduleId
      .subscribe({
        next: (rows) => {
          this.attendanceRows = rows;
          this.calcAttendanceStats();
          this.attendanceLoading = false;
        },
        error: (err) => {
          console.error('Error loading class attendance detail', err);
          this.notify.error('Không tải được chi tiết điểm danh lớp.');
          this.attendanceRows = [];
          this.attendanceStats = {
            total: 0,
            present: 0,
            absent: 0,
            late: 0,
            rate: 0
          };
          this.attendanceLoading = false;
        }
      });
  }


  /** Tính thống kê từ attendanceRows */
  private calcAttendanceStats(): void {
    const total = this.attendanceRows.length;
    let present = 0;
    let absent = 0;
    let late = 0;

    this.attendanceRows.forEach((r) => {
      if (r.status === 'present') present++;
      else if (r.status === 'absent') absent++;
      else if (r.status === 'late') late++;
    });

    const rate = total ? Math.round(((present + late) / total) * 100) : 0;

    this.attendanceStats = { total, present, absent, late, rate };
  }

  /** Xuất báo cáo CSV */
  onExportReport(): void {
    if (!this.selectedClass) return;

    this.attendanceService
      .exportAttendanceReport(this.selectedClass.scheduleId)   // ⬅️ chỉ còn scheduleId
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `attendance_${this.selectedClass!.classCode}_${this.selectedDate}.csv`;
          a.click();
          window.URL.revokeObjectURL(url);
          this.notify.success('Xuất báo cáo thành công');
        },
        error: (err) => {
          console.error('Error export attendance report', err);
          this.notify.error('Xuất báo cáo thất bại');
        }
      });
  }


  /** Click vào trạng thái để đổi status & lưu DB */
  onToggleStatus(row: AttendanceStudentRow): void {
    if (!this.selectedClass) return;

    const current: StudentStatus = row.status;
    let newStatus: StudentStatus;

    // ⭐ Thứ tự xoay vòng: none/absent -> present -> late -> absent -> present ...
    switch (current) {
      case 'present':
        newStatus = 'late';
        break;
      case 'late':
        newStatus = 'absent';
        break;
      case 'absent':
      case 'none':
      default:
        newStatus = 'present';
        break;
    }

    const oldStatus = row.status;

    // Có thể optimisic update UI trước
    row.status = newStatus;

    this.attendanceService
      .updateStudentStatus(
        this.selectedClass.scheduleId,
        row.studentId,
        newStatus
      )
      .subscribe({
        next: () => {
          // Sau khi lưu thành công, load lại danh sách để đồng bộ giờ điểm danh & thống kê
          this.loadAttendanceForSelectedClass();
          this.notify.success('Cập nhật trạng thái điểm danh thành công');
        },
        error: (err) => {
          console.error('Update status error', err);
          // rollback nếu lỗi
          row.status = oldStatus;
          this.notify.error('Cập nhật trạng thái điểm danh thất bại');
        }
      });
  }

  // ====== MODAL CHI TIẾT SINH VIÊN ======
  showStudentDetailModal = false;
  studentDetail: StudentDetail | null = null;

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    const dd = String(d.getDate()).padStart(2, '0');
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const yyyy = d.getFullYear();
    return `${dd}/${mm}/${yyyy}`;
  }
  /** Mở modal chi tiết sinh viên + load lịch sử từ backend */
  openStudentDetail(row: AttendanceStudentRow, event: MouseEvent): void {
    event.stopPropagation();
    if (!this.selectedClass) return;

    // Tạo skeleton trước, chưa có history
    this.studentDetail = {
      studentId: row.studentId,
      studentCode: row.studentCode, 
      fullName: row.fullName,
      username: row.username,
      email: row.email || '',
      phone: row.phone || '',
      classId: this.selectedClass.classId,
      classCode: this.selectedClass.classCode,
      className: this.selectedClass.className,
      totalSessions: 0,
      presentSessions: 0,
      lateSessions: 0,
      absentSessions: 0,
      attendanceRate: 0,
      history: []
    };

    this.showStudentDetailModal = true;

    // Gọi API lấy lịch sử
    this.attendanceService
      .getStudentHistory(this.selectedClass.classId, row.studentId)
      .subscribe({
        next: (hist: StudentHistoryItem[]) => {
          if (!this.studentDetail) return;

          // Map sang view (thêm sessionTime)
          const historyView: StudentHistoryView[] = hist.map((h) => ({
            scheduleId: h.scheduleId,
            date: h.date,
            sessionTime:
              `${h.startTime?.substring(0, 5)} - ${h.endTime?.substring(0, 5)}`,
            status: h.status,
            attendanceTime: h.attendanceTime
          }));

          // Tính thống kê từ history
          const total = historyView.length;
          const present = historyView.filter((h) => h.status === 'present').length;
          const late = historyView.filter((h) => h.status === 'late').length;
          const absent = historyView.filter((h) => h.status === 'absent').length;
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
          this.notify.error('Không tải được lịch sử điểm danh của sinh viên');
        }
      });
  }


  /** Đóng modal chi tiết sinh viên */
  closeStudentDetailModal(): void {
    this.showStudentDetailModal = false;
  }

  onToggleHistoryStatus(h: StudentHistoryView, event: MouseEvent): void {
    event.stopPropagation();
    if (!this.selectedClass || !this.studentDetail) return;

    // ⭐ Lưu lại giá trị ra biến local để TS khỏi kêu null
    const currentScheduleId = this.selectedClass.scheduleId;
    const studentId = this.studentDetail.studentId;
    const classId = this.studentDetail.classId;

    const current: StudentStatus = h.status;
    let newStatus: StudentStatus;

    // Xoay vòng trạng thái: none/absent -> present -> late -> absent -> present ...
    switch (current) {
      case 'present':
        newStatus = 'late';
        break;
      case 'late':
        newStatus = 'absent';
        break;
      case 'absent':
      case 'none':
      default:
        newStatus = 'present';
        break;
    }

    const oldStatus = h.status;

    // Optimistic update
    h.status = newStatus;

    this.attendanceService
      .updateStudentStatus(
        h.scheduleId,   // buổi tương ứng trong lịch sử
        studentId,
        newStatus
      )
      .subscribe({
        next: () => {
          // Reload lại lịch sử để thống kê, giờ điểm danh chính xác
          this.attendanceService
            .getStudentHistory(classId, studentId)
            .subscribe({
              next: (hist: StudentHistoryItem[]) => {
                if (!this.studentDetail) return;

                const historyView: StudentHistoryView[] = hist.map((x) => ({
                  scheduleId: x.scheduleId,
                  date: x.date,
                  sessionTime:
                    `${x.startTime?.substring(0, 5)} - ${x.endTime?.substring(0, 5)}`,
                  status: x.status,
                  attendanceTime: x.attendanceTime
                }));

                const total = historyView.length;
                const present = historyView.filter((z) => z.status === 'present').length;
                const late = historyView.filter((z) => z.status === 'late').length;
                const absent = historyView.filter((z) => z.status === 'absent').length;
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
              error: (e2) => {
                console.error('Reload history error', e2);
              }
            });

          // Nếu buổi đang đổi là buổi đang mở ở bảng chính, reload luôn bảng chính
          if (h.scheduleId === currentScheduleId) {
            this.loadAttendanceForSelectedClass();
          }

          this.notify.success('Cập nhật trạng thái buổi học thành công');
        },
        error: (err) => {
          console.error('Update history status error', err);
          // rollback nếu lỗi
          h.status = oldStatus;
          this.notify.error('Cập nhật trạng thái buổi học thất bại');
        }
      });
  }


}
