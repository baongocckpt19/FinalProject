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

  }
}
