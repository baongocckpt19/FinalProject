// src/app/chatbot/chatbot.component.ts

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';

// 👇 Import cả Service và Interface từ attendance.service.ts
import { AttendanceService, TeacherClass } from '../services/attendance.service';

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, HttpClientModule],
  templateUrl: './chatbot.component.html',
  styleUrl: './chatbot.component.scss'
})
export class ChatbotComponent implements OnInit {
  loading = false;
  error: string | null = null;

  // Sử dụng interface TeacherClass được import từ attendance.service
  teacherClasses: TeacherClass[] = [];
  
  selectedClassId: number | null = null;
  currentClass: TeacherClass | null = null; 

  analysis: any = null;

  constructor(private attendanceService: AttendanceService) {}

  ngOnInit() {
    this.loadTeacherClasses();
  }

  // 1. Lấy danh sách lớp
  loadTeacherClasses() {
    this.attendanceService.getTeacherClasses().subscribe({
      next: (data) => {
        this.teacherClasses = data;

        // Mặc định chọn lớp đầu tiên nếu danh sách không rỗng
        if (this.teacherClasses.length > 0) {
          this.selectClass(this.teacherClasses[0].classId);
        }
      },
      error: (err) => {
        console.error('Lỗi tải danh sách lớp:', err);
        this.error = 'Không thể tải danh sách lớp học của bạn.';
      }
    });
  }

  // 2. Sự kiện khi user chọn lớp khác
  onClassChange(event: any) {
    const classId = Number(event.target.value);
    this.selectClass(classId);
  }

  // 3. Xử lý logic chọn lớp và gọi API phân tích
  selectClass(classId: number) {
    this.selectedClassId = classId;
    
    // Tìm object lớp để hiển thị tên ra UI
    this.currentClass = this.teacherClasses.find(c => c.classId === classId) || null;
    
    this.fetchAnalysis();
  }

  fetchAnalysis() {
    if (!this.selectedClassId) return;

    this.loading = true;
    this.error = null;
    this.analysis = null; // Reset dữ liệu cũ khi đang tải mới

    this.attendanceService.analyticsAttendance(this.selectedClassId).subscribe({
      next: (data) => {
        this.analysis = data;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.error = 'Lỗi khi lấy dữ liệu phân tích: ' + (err.message || 'Unknown error');
        this.loading = false;
      }
    });
  }
}