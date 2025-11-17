import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';

interface AttendanceItem {
  date: string; // dạng yyyy-mm-dd
  className?: string; // ví dụ: "Lớp 10A1" (nếu bạn có)
}
@Component({
  selector: 'app-gv-quanlydiemdanh',
  imports: [CommonModule],
  templateUrl: './gv-quanlydiemdanh.component.html',
  styleUrl: './gv-quanlydiemdanh.component.scss'
})
export class GvQuanlydiemdanhComponent implements OnInit {
  attendanceData: AttendanceItem[] = [];
  currentDate: Date = new Date(); // ngày giờ hiện tại
  selectedDate: string = this.formatDate(this.currentDate.toISOString().slice(0, 10)) // YYYY-MM-DD của hôm nay
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
status: string|string[]|Set<string>|{ [klass: string]: any; }|null|undefined;

  ngOnInit() {
    // Dữ liệu điểm danh mẫu (bạn thay bằng dữ liệu thật từ API)
    this.attendanceData = [
      { date: '2025-10-10' },
      { date: '2025-10-13' },
      { date: '2025-10-13' },
      { date: '2025-10-21' },
    ];

    this.generateCalendar();
  }

  /** Sinh lịch */
  generateCalendar() {
    this.calendarDays = [];

    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();

    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startingDayOfWeek = firstDay.getDay();

    // Thêm các ô trống đầu tháng
    for (let i = 0; i < startingDayOfWeek; i++) {
      this.calendarDays.push({ isEmpty: true });
    }

    // Thêm các ô ngày thực tế
    for (let day = 1; day <= daysInMonth; day++) {
      const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
      const dayAttendance = this.attendanceData.filter(item => item.date === dateStr);
      const today = new Date();

      this.calendarDays.push({
        dayNumber: day,
        dateStr,
        classCount: dayAttendance.length,
        hasAttendance: dayAttendance.length > 0,
        isToday: (
          year === today.getFullYear() &&
          month === today.getMonth() &&
          day === today.getDate()
        ),
        isSelected: dateStr === this.selectedDate,
        isEmpty: false
      });
    }
  }

  /** Chuyển tháng */
  previousMonth() {
    this.currentDate.setMonth(this.currentDate.getMonth() - 1);
    this.generateCalendar();
  }

  nextMonth() {
    this.currentDate.setMonth(this.currentDate.getMonth() + 1);
    this.generateCalendar();
  }

  /** Khi chọn ngày */
  selectDate(dateStr?: string) {
    if (!dateStr) return;

    this.selectedDate = dateStr;
    this.generateCalendar();

    const dayAttendance = this.attendanceData.filter(item => item.date === dateStr);

    // if (dayAttendance.length === 0) {
    //   alert(`Không có dữ liệu: Ngày ${this.formatDate(dateStr)} không có buổi học nào.`);
    // } else {
    //   alert(`Hiển thị ${dayAttendance.length} buổi học ngày ${this.formatDate(dateStr)}.`);
    // }
  }

  /** Định dạng ngày dd/mm/yyyy */
  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    return `${date.getDate()}/${date.getMonth() + 1}/${date.getFullYear()}`;
  }

  /** Lấy tên tháng hiển thị */
  get currentMonthLabel(): string {
    return `${this.monthNames[this.currentDate.getMonth()]}, ${this.currentDate.getFullYear()}`;
  }


  attendanceClass = [
    { date: '2025-10-13', classCode: 'IT301', className: 'Lập trình Web', time: '07:30 - 09:30', status: 'Đang hoạt động', rate: 93, present: 14, total: 15, absent: 1 },
    { date: '2025-10-10', classCode: 'IT302', className: 'Cơ sở dữ liệu', time: '10:00 - 12:00', status: 'Đã kết thúc', rate: 85, present: 12, total: 15, absent: 3 },
    { date: '2025-10-21', classCode: 'IT303', className: 'Mạng máy tính', time: '13:00 - 15:00', status: 'Đang hoạt động', rate: 0, present: 0, total: 15, absent: 0 },
    { date: '2025-10-21', classCode: 'IT304', className: 'Hệ điều hành', time: '15:30 - 17:30', status: 'Đang hoạt động', rate: 0, present: 0, total: 15, absent: 0 },
    { date: '2025-10-21', classCode: 'IT305', className: 'Lập trình nâng cao', time: '18:00 - 20:00', status: 'Đã kết thúc', rate: 0, present: 0, total: 15, absent: 0 },
  ]
  
  //xem chi tiết điểm danh
  showAttendanceDetailModal = false;
  selectClass: any = null;
  openAttendanceDetailModal(attendanceClass: any) {
    this.selectClass = attendanceClass;
    this.showAttendanceDetailModal = true;
  }
  closeAttendanceDetailModal() {
    this.showAttendanceDetailModal = false;
  }

  // xem chi tiết sinh viên
  showStudentDetailModal = false;
  openStudentDetailModal() {
   // this.selectClass = attendanceClass;
    this.showStudentDetailModal = true;
  }
  closeStudentDetailModal() {
    this.showStudentDetailModal = false;
  }
// chuẩn hóa trạng thái nội bộ là 'present' / 'absent'
students = [
  { id: 1, ho: "Nguyễn Văn", ten: "A", mssv: "2021001", status: 'present', rate :95, time:'07:32',absent: 2 },
  { id: 2, ho: "Trần Thị", ten: "B", mssv: "2021002", status: 'absent', rate :80, time:'-',absent: 5 },
  { id: 3, ho: "Lê Văn", ten: "C", mssv: "2021003", status: 'present', rate :90, time:'07:35',absent: 3 },
  { id: 4, ho: "Phạm Thị", ten: "D", mssv: "2021004", status: 'present', rate :100, time:'07:30',absent: 0 },
  { id: 5, ho: "Hoàng Văn", ten: "E", mssv: "2021005", status: 'absent', rate :70, time:'-',absent: 6 },
  { id: 6, ho: "Vũ Thị", ten: "F", mssv: "2021006", status: 'present', rate :85, time:'07:40',absent: 4 },
  { id: 7, ho: "Đặng Văn", ten: "G", mssv: "2021007", status: 'present', rate :95, time:'07:33',absent: 2 },  
  { id: 8, ho: "Trần Thị", ten: "H", mssv: "2021008", status: 'absent', rate :60, time:'-',absent: 7 },
  { id: 9, ho: "Lý Văn", ten: "I", mssv: "2021009", status: 'present', rate :88, time:'07:36',absent: 3 },
  { id: 10, ho: "Võ Thị", ten: "K", mssv: "2021010", status: 'present', rate :92, time:'07:31',absent: 2 },
  { id: 11, ho: "Đỗ Văn", ten: "L", mssv: "2021011", status: 'absent', rate :75, time:'-',absent: 5 },
  { id: 12, ho: "Ngô Thị", ten: "M", mssv: "2021012", status: 'present', rate :89, time:'07:34',absent: 3 },
  { id: 13, ho: "Bùi Văn", ten: "N", mssv: "2021013", status: 'present', rate :94, time:'07:32',absent: 2 },
  { id: 14, ho: "Trương Thị", ten: "O", mssv: "2021014", status: 'absent', rate :65, time:'-',absent: 6 },
  { id: 15, ho: "Mai Văn", ten: "P", mssv: "2021015", status: 'present', rate :91, time:'07:35',absent: 3 }
  
];

toggleStudentStatus(id: number): void {
  const student = this.students.find(s => s.id === id);
  if (!student) return;

  // đổi trạng thái giữa 'present' và 'absent'
  student.status = student.status === 'present' ? 'absent' : 'present';

  // nếu component dùng ChangeDetectionStrategy.OnPush, thay array để kích hoạt detection:
  // this.students = [...this.students];
}

}