import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { AttendanceService } from '../services/attendance.service';

@Component({
  selector: 'app-chatbot',
  imports: [CommonModule, HttpClientModule],
  templateUrl: './chatbot.component.html',
  styleUrl: './chatbot.component.scss'
})
export class ChatbotComponent {
  loading = false;
  error: string | null = null;
  classId = 1; // TODO: bind from class selector if needed

  // Data structure aligned with backend payload
  analysis: any = null;

  constructor(private http: HttpClient, private attendanceService: AttendanceService) {}

  ngOnInit() {
    this.fetchAnalysis();
  }

  fetchAnalysis() {
    this.loading = true;
    this.error = null;
    this.attendanceService.analyticsAttendance(this.classId).subscribe({
      next: (data) => {
        this.analysis = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error fetching analysis: ' + err.message;
        this.loading = false;
      }
    });
  }
}
