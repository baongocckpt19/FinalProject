// ====================== FILE: gv-quanlyvantay.component.ts ======================
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  FingerprintService,
  StudentFingerprintInfo
} from '../services/fingerprint.service';

type EnrollState = 'idle' | 'waitingDevice' | 'receivedFromDevice' | 'saving' | 'done' | 'error';

@Component({
  selector: 'app-gv-quanlyvantay',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gv-quanlyvantay.component.html',
  styleUrl: './gv-quanlyvantay.component.scss'
})
export class GvQuanlyvantayComponent {
  // nhập MSSV
  studentIdInput: number | null = null;

  // thông tin sinh viên được tìm thấy (từ API)
  selectedStudent: StudentFingerprintInfo | null = null;

  // trạng thái tìm sinh viên
  searching = false;
  searchStudentError = '';

  // trạng thái phiên đăng ký vân tay
  enrollState: EnrollState = 'idle';
  currentSessionCode: string | null = null;
  currentSensorSlot: number | null = null;
  creatingSession = false;
  lastEnrollMessage = '';

  constructor(
    private fingerprintService: FingerprintService
  ) { }

  // Gọi API backend để tìm theo studentId
  searchStudent(): void {
    this.searchStudentError = '';
    this.selectedStudent = null;
    this.enrollState = 'idle';
    this.currentSessionCode = null;
    this.currentSensorSlot = null;
    this.lastEnrollMessage = '';

    if (!this.studentIdInput) {
      this.searchStudentError = 'Vui lòng nhập mã sinh viên.';
      return;
    }

    this.searching = true;

    this.fingerprintService.getStudentFingerprintInfo(this.studentIdInput).subscribe({
      next: (info) => {
        this.selectedStudent = info;
        this.searching = false;
        this.enrollState = 'idle';
      },
      error: (err) => {
        this.searching = false;
        this.selectedStudent = null;
        console.error('searchStudent error', err);
        if (err.status === 404) {
          this.searchStudentError = `Không tìm thấy sinh viên với MSSV: ${this.studentIdInput}.`;
        } else {
          this.searchStudentError = 'Có lỗi xảy ra khi tìm sinh viên. Vui lòng thử lại.';
        }
      }
    });
  }

  // tạo session enroll
  createEnrollSession(): void {
    if (!this.selectedStudent) {
      this.lastEnrollMessage = 'Vui lòng chọn sinh viên trước.';
      return;
    }

    this.creatingSession = true;
    this.lastEnrollMessage = '';
    this.enrollState = 'idle';
    this.currentSessionCode = null;
    this.currentSensorSlot = null;

    this.fingerprintService.createEnrollSession(this.selectedStudent.studentId).subscribe({
      next: (res) => {
        this.currentSessionCode = res.sessionCode;
        this.enrollState = 'waitingDevice';
        this.creatingSession = false;
        this.lastEnrollMessage = 'Đã tạo phiên. Hãy nhập sessionCode này vào ESP32 để bắt đầu quét.';
        // BẮT ĐẦU POLL 2S/LẦN
        this.startPollingSession();
      },
      error: (err) => {
        console.error('createEnrollSession error', err);
        this.creatingSession = false;
        this.enrollState = 'error';
        this.lastEnrollMessage = 'Không thể tạo phiên đăng ký. Vui lòng thử lại.';
      }
    });
  }
  private pollTimer: any;



  startPollingSession(): void {
    if (this.pollTimer) {
      clearInterval(this.pollTimer);
    }
    this.pollTimer = setInterval(() => {
      if (this.enrollState === 'waitingDevice' && this.currentSessionCode) {
        this.checkSessionFromServer();
      } else {
        clearInterval(this.pollTimer);
      }
    }, 2000);
  }

  // hủy session enroll (ở UI, nếu backend có API hủy thì nối thêm)
  cancelEnrollSession(): void {
    this.currentSessionCode = null;
    this.currentSensorSlot = null;
    this.enrollState = 'idle';
    this.lastEnrollMessage = 'Đã hủy phiên đăng ký hiện tại.';
    if (this.pollTimer) {
      clearInterval(this.pollTimer);
    }

    // nếu có API hủy session trên backend, bạn gọi ở đây
  }

  // (tuỳ chọn) Gọi API check trạng thái session / sensorSlot
  checkSessionFromServer(): void {
    if (!this.currentSessionCode) {
      return;
    }
    this.fingerprintService.checkEnrollTemp(this.currentSessionCode).subscribe({
      next: (res) => {
        if (res.found && typeof res.sensorSlot === 'number') {
          this.currentSensorSlot = res.sensorSlot;
          this.enrollState = 'receivedFromDevice';
          this.lastEnrollMessage = `Đã nhận template từ thiết bị (slot ${res.sensorSlot}). Bạn có thể lưu cho sinh viên.`;
        } else {
          this.lastEnrollMessage = 'Chưa nhận được template từ thiết bị. Vui lòng kiểm tra lại ESP/LCD.';
        }
      },
      error: (err) => {
        console.error('checkSessionFromServer error', err);
        this.lastEnrollMessage = 'Không thể kiểm tra trạng thái phiên. Vui lòng thử lại.';
      }
    });
  }

  // confirm lưu template cho sinh viên
  confirmEnroll(): void {
    if (!this.selectedStudent) {
      this.lastEnrollMessage = 'Vui lòng chọn sinh viên trước.';
      return;
    }
    if (!this.currentSessionCode) {
      this.lastEnrollMessage = 'Không có sessionCode. Hãy tạo lại phiên đăng ký.';
      return;
    }
    if (this.enrollState !== 'receivedFromDevice' && this.enrollState !== 'waitingDevice') {
      // tuỳ bạn muốn chặt chẽ thế nào, ở đây cho phép cả khi waitingDevice (backend sẽ kiểm tra)
      // nhưng tốt nhất là bấm "Kiểm tra" trước -> chuyển sang receivedFromDevice.
    }

    this.enrollState = 'saving';
    this.lastEnrollMessage = 'Đang lưu vân tay cho sinh viên...';

    this.fingerprintService.confirmEnroll(this.selectedStudent.studentId, this.currentSessionCode).subscribe({
      next: () => {
        this.enrollState = 'done';
        this.lastEnrollMessage = 'Đã lưu vân tay cho sinh viên.';
        this.refreshStudentInfo();
      },
      error: (err) => {
        console.error('confirmEnroll error', err);
        this.enrollState = 'error';
        if (err.error && typeof err.error === 'string') {
          this.lastEnrollMessage = 'Lỗi: ' + err.error;
        } else {
          this.lastEnrollMessage = 'Không thể lưu vân tay. Vui lòng kiểm tra lại session hoặc thiết bị.';
        }
      }
    });
  }

  // sau khi confirm thành công, lấy lại info để cập nhật hasFingerprint + devices
  private refreshStudentInfo(): void {
    if (!this.selectedStudent) return;
    const studentId = this.selectedStudent.studentId;

    this.fingerprintService.getStudentFingerprintInfo(studentId).subscribe({
      next: (info) => {
        this.selectedStudent = info;
      },
      error: (err) => {
        console.error('refreshStudentInfo error', err);
      }
    });
  }



}
