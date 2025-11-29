// src/app/sv-trangchu/sv-trangchu.component.ts
import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { StudentDashboardService } from '../services/student-dashboard.service';

@Component({
  selector: 'app-sv-trangchu',
  standalone: true,                     // Standalone component
  imports: [CommonModule],
  templateUrl: './sv-trangchu.component.html',
  styleUrls: ['./sv-trangchu.component.scss']
})
export class SvTrangchuComponent implements OnInit {

  /**
   * Dữ liệu dashboard nhận từ backend.
   *  - Gpa, passRate, absentCount, attendanceRate
   *  - grades[]
   *  - attendanceSummary
   * Không tạo model file, nên để kiểu any (hoặc object literal) cho đơn giản.
   */
  dashboard: any = null;

  isLoading = false;
  errorMessage: string | null = null;

  constructor(
    private studentDashboardService: StudentDashboardService
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  /**
   * Gọi service lấy dashboard cho sinh viên hiện tại.
   */
  loadDashboard(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.studentDashboardService.getStudentDashboard().subscribe({
      next: (data) => {
        this.dashboard = data;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Lỗi load dashboard sinh viên', error);
        this.errorMessage = 'Không thể tải dữ liệu tổng quan.';
        this.isLoading = false;
      }
    });
  }

  // =========================== BIỂU ĐỒ CỘT ===========================

  /**
   * Danh sách lớp dùng để vẽ cột + nhãn.
   *  - Đảm bảo không quá 6 phần tử.
   *  - Backend đã TOP 6, nhưng vẫn slice lại cho an toàn.
   */
  get gradeBars() {
    if (!this.dashboard || !this.dashboard.grades) return [];
    return this.dashboard.grades.slice(0, 6);
  }

  /**
   * Chiều cao cột (%), 10 điểm -> 100%.
   */
  getBarHeight(grade: number | null | undefined): number {
    if (!grade || grade < 0) return 0;
    if (grade > 10) grade = 10;
    return grade * 10;
  }

  // =========================== BIỂU ĐỒ TRÒN ===========================

  get totalSessions(): number {
    if (!this.dashboard || !this.dashboard.attendanceSummary) return 0;
    const a = this.dashboard.attendanceSummary;
    return (a.presentCount || 0) + (a.absentCount || 0) + (a.lateCount || 0);
  }

  get presentPercent(): number {
    const total = this.totalSessions;
    if (!this.dashboard || total === 0) return 0;
    return (this.dashboard.attendanceSummary.presentCount / total) * 100;
  }

  get absentPercent(): number {
    const total = this.totalSessions;
    if (!this.dashboard || total === 0) return 0;
    return (this.dashboard.attendanceSummary.absentCount / total) * 100;
  }

  get latePercent(): number {
    const total = this.totalSessions;
    if (!this.dashboard || total === 0) return 0;
    return (this.dashboard.attendanceSummary.lateCount / total) * 100;
  }

  /**
   * Background conic-gradient cho biểu đồ tròn.
   *  - Dùng 3 biến CSS:
   *      --color-present
   *      --color-absent
   *      --color-late
   *    (định nghĩa trong SCSS).
   */
  get pieBackground(): string {
    const p = this.presentPercent;
    const a = this.absentPercent;
    const l = this.latePercent; // l hiện tại không dùng trong công thức nhưng giữ lại cho rõ ý nghĩa

    const degPresent = (p / 100) * 360;
    const degAbsent = ((p + a) / 100) * 360;

    return `conic-gradient(
      var(--color-present) 0deg ${degPresent}deg,
      var(--color-absent) ${degPresent}deg ${degAbsent}deg,
      var(--color-late) ${degAbsent}deg 360deg
    )`;
  }
}
