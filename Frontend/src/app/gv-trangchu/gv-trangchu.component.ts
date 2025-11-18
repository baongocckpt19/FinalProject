import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

import { AuthService } from '../services/auth.service';
import { Account } from '../model/account';
import {
  TeacherDashboardService,
  TeacherDashboardStats,
  ScoreDistribution,
  TopScoreClass,
  TopAttendanceClass
} from '../services/teacher-dashboard.service';

@Component({
  selector: 'app-gv-trangchu',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './gv-trangchu.component.html',
  styleUrl: './gv-trangchu.component.scss'
})
export class GvTrangchuComponent implements OnInit {

  currentAccount: Account | null = null;
  stats: TeacherDashboardStats | null = null;

  topScoreClasses: TopScoreClass[] = [];
  topAttendanceClasses: TopAttendanceClass[] = [];
  scoreDistribution: ScoreDistribution | null = null;

  constructor(
    private router: Router,
    private authService: AuthService,
    private dashboardService: TeacherDashboardService
  ) {
    this.authService.currentUser$.subscribe(acc => {
      this.currentAccount = acc;
    });
  }

  ngOnInit(): void {
    // Stats tổng
    this.dashboardService.getStats().subscribe({
      next: (res) => {
        this.stats = res;
      },
      error: (err) => {
        console.error('Lỗi load thống kê', err);
        this.stats = {
          totalStudents: 0,
          activeClasses: 0,
          averageScore: 0,
          attendanceRate: 0
        };
      }
    });

    // Top 5 lớp điểm cao nhất
    this.dashboardService.getTopScoreClasses().subscribe({
      next: (res) => this.topScoreClasses = res || [],
      error: (err) => {
        console.error('Lỗi load top lớp điểm', err);
        this.topScoreClasses = [];
      }
    });

    // Top 5 lớp điểm danh cao nhất
    this.dashboardService.getTopAttendanceClasses().subscribe({
      next: (res) => this.topAttendanceClasses = res || [],
      error: (err) => {
        console.error('Lỗi load top lớp điểm danh', err);
        this.topAttendanceClasses = [];
      }
    });

    // Phân bố điểm
    this.dashboardService.getScoreDistribution().subscribe({
      next: (res) => this.scoreDistribution = res,
      error: (err) => {
        console.error('Lỗi load phân bố điểm', err);
        this.scoreDistribution = { gioi: 0, kha: 0, trungBinh: 0, yeu: 0 };
      }
    });
  }

  navigateToDashboard() {
    this.router.navigate(['/gv_trangchu']);
  }

  // chiều rộng cột điểm (0-10) → 0-100%
  getScoreBarWidth(score: number): number {
    const max = 10;
    if (!score || score < 0) return 0;
    if (score > max) score = max;
    return (score / max) * 100;
  }

  // chiều rộng cột tỉ lệ điểm danh (0-100)
  getAttendanceBarWidth(rate: number): number {
    if (!rate || rate < 0) return 0;
    if (rate > 100) return 100;
    return rate;
  }

  // % cho từng mức điểm
  getScorePercent(key: 'gioi' | 'kha' | 'trungBinh' | 'yeu'): number {
    if (!this.scoreDistribution) return 0;
    const d = this.scoreDistribution;
    const total = d.gioi + d.kha + d.trungBinh + d.yeu;
    if (!total) return 0;
    const value = d[key];
    return Math.round((value / total) * 1000) / 10; // làm tròn 1 chữ số thập phân
  }

  // background cho pie chart
  getPieBackground(): string {
    if (!this.scoreDistribution) return '#e5e7eb';

    const g = this.getScorePercent('gioi');
    const k = this.getScorePercent('kha');
    const t = this.getScorePercent('trungBinh');
    const y = this.getScorePercent('yeu');

    if (g + k + t + y === 0) return '#e5e7eb';

    const gDeg = g * 3.6;
    const kDeg = k * 3.6;
    const tDeg = t * 3.6;
    const yDeg = 360 - gDeg - kDeg - tDeg;

    const gEnd = gDeg;
    const kEnd = gEnd + kDeg;
    const tEnd = kEnd + tDeg;
    const yEnd = tEnd + yDeg;

    return `conic-gradient(
      #22c55e 0deg ${gEnd}deg,
      #3b82f6 ${gEnd}deg ${kEnd}deg,
      #facc15 ${kEnd}deg ${tEnd}deg,
      #ef4444 ${tEnd}deg ${yEnd}deg
    )`;
  }
}
