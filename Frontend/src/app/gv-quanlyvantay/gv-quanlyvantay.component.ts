// ====================== FILE: gv-quanlyvantay.component.ts ======================
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  DeviceFingerprintInfo,
  FingerprintService,
  StudentFingerprintInfo
} from '../services/fingerprint.service';

// Trạng thái phiên đăng ký vân tay trên UI
type EnrollState =
  | 'idle'
  | 'waitingDevice'
  | 'receivedFromDevice'
  | 'saving'
  | 'done'
  | 'error';

@Component({
  selector: 'app-gv-quanlyvantay',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gv-quanlyvantay.component.html',
  styleUrl: './gv-quanlyvantay.component.scss'
})
export class GvQuanlyvantayComponent {
  // =========================================================
  // 1. STATE CHO TÌM KIẾM SINH VIÊN
  // =========================================================

  /** MSSV nhập từ ô tìm kiếm */
  studentCodeInput: string = '';

  /** Thông tin sinh viên sau khi tìm */
  selectedStudent: StudentFingerprintInfo | null = null;

  /** Cờ loading khi gọi API tìm sinh viên */
  searching = false;

  /** Lỗi hiển thị bên dưới ô tìm kiếm */
  searchStudentError = '';

  // =========================================================
  // 2. STATE CHO PHIÊN ĐĂNG KÝ VÂN TAY
  // =========================================================

  /** Trạng thái UI của phiên đăng ký */
  enrollState: EnrollState = 'idle';

  /** Mã phiên do server trả về */
  currentSessionCode: string | null = null;

  /** Slot vân tay (index trên module) */
  currentSensorSlot: number | null = null;

  /** Cờ loading khi đang tạo phiên */
  creatingSession = false;

  /** Message hiển thị bên dưới nhóm nút */
  lastEnrollMessage = '';

  /** Timer để poll trạng thái từ server */
  private pollTimer: any;

  // =========================================================
  // 3. DANH SÁCH THIẾT BỊ ĐIỂM DANH
  // =========================================================

  /** Danh sách thiết bị đang active */
  devices: DeviceFingerprintInfo[] = [];

  /** Thiết bị đang được chọn trong select */
  selectedDeviceCode: string = '';

  // =========================================================
  // 4. CONSTRUCTOR + LIFECYCLE
  // =========================================================

  constructor(private fingerprintService: FingerprintService) { }

  /** Load danh sách thiết bị khi component khởi tạo */
  ngOnInit(): void {
    this.loadDevices();
  }

  // =========================================================
  // 5. HÀM TÌM KIẾM SINH VIÊN THEO MSSV
  // =========================================================

  /** Gọi API backend để tìm theo studentCode (MSSV) */
  searchStudent(): void {
    // reset state liên quan
    this.searchStudentError = '';
    this.selectedStudent = null;
    this.enrollState = 'idle';
    this.currentSessionCode = null;
    this.currentSensorSlot = null;
    this.lastEnrollMessage = '';

    const code = this.studentCodeInput?.trim();
    if (!code) {
      this.searchStudentError = 'Vui lòng nhập mã sinh viên.';
      return;
    }

    this.searching = true;

    this.fingerprintService.getStudentFingerprintInfoByCode(code).subscribe({
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
          this.searchStudentError = `Không tìm thấy sinh viên với MSSV: ${code}.`;
        } else {
          this.searchStudentError =
            'Có lỗi xảy ra khi tìm sinh viên. Vui lòng thử lại.';
        }
      }
    });
  }

  // =========================================================
  // 6. HÀM LOAD DANH SÁCH THIẾT BỊ
  // =========================================================

  /** Lấy danh sách thiết bị active từ backend */
  loadDevices(): void {
    this.fingerprintService.getActiveDevices().subscribe({
      next: (list) => {
        this.devices = list;
        if (list.length > 0) {
          // Chọn thiết bị đầu tiên làm mặc định
          this.selectedDeviceCode = list[0].deviceCode;
        }
      },
      error: () => {
        console.error('Không lấy được danh sách thiết bị');
      }
    });
  }

  // =========================================================
  // 7. TẠO PHIÊN ĐĂNG KÝ VÂN TAY
  // =========================================================

  createEnrollSession(): void {
    if (!this.selectedStudent) {
      this.lastEnrollMessage = 'Vui lòng chọn sinh viên trước.';
      return;
    }

    if (!this.selectedDeviceCode) {
      this.lastEnrollMessage = 'Vui lòng chọn thiết bị.';
      return;
    }

    this.creatingSession = true;
    this.lastEnrollMessage = '';
    this.enrollState = 'idle';
    this.currentSessionCode = null;
    this.currentSensorSlot = null;

    this.fingerprintService
      .createEnrollSession(this.selectedStudent.studentId, this.selectedDeviceCode)
      .subscribe({
        next: (res) => {
          this.currentSessionCode = res.sessionCode;
          this.enrollState = 'waitingDevice';
          this.creatingSession = false;

          this.lastEnrollMessage =
            `Đã tạo phiên cho ${this.selectedDeviceCode}. Session: ` +
            res.sessionCode;

          this.startPollingSession();
        },
        error: (err) => {
          console.error('createEnrollSession error', err);
          this.creatingSession = false;
          this.enrollState = 'error';
          this.lastEnrollMessage = 'Không thể tạo phiên. Kiểm tra thiết bị!';
        }
      });
  }

  // =========================================================
  // 8. POLLING CHECK SESSION TỪ SERVER
  // =========================================================

  /** Bắt đầu poll 2s/lần để kiểm tra xem ESP đã gửi template lên chưa */
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

  /** Hủy session enroll (UI, nếu backend có API hủy thì nối thêm) */
  cancelEnrollSession(): void {
    this.currentSessionCode = null;
    this.currentSensorSlot = null;
    this.enrollState = 'idle';
    this.lastEnrollMessage = 'Đã hủy phiên đăng ký hiện tại.';

    if (this.pollTimer) {
      clearInterval(this.pollTimer);
    }
    // Nếu có API hủy session trên backend => gọi ở đây
  }

  /** (Tùy chọn) Gọi API check trạng thái session / sensorSlot */
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
          this.lastEnrollMessage =
            'Chưa nhận được template từ thiết bị. Vui lòng kiểm tra lại ESP/LCD.';
        }
      },
      error: (err) => {
        console.error('checkSessionFromServer error', err);
        this.lastEnrollMessage =
          'Không thể kiểm tra trạng thái phiên. Vui lòng thử lại.';
      }
    });
  }

  // =========================================================
  // 9. CONFIRM LƯU TEMPLATE CHO SINH VIÊN
  // =========================================================

  confirmEnroll(): void {
    if (!this.selectedStudent) {
      this.lastEnrollMessage = 'Vui lòng chọn sinh viên trước.';
      return;
    }
    if (!this.currentSessionCode) {
      this.lastEnrollMessage = 'Không có sessionCode. Hãy tạo lại phiên đăng ký.';
      return;
    }

    // Nếu muốn chặt hơn có thể bắt buộc enrollState === 'receivedFromDevice'
    this.enrollState = 'saving';
    this.lastEnrollMessage = 'Đang lưu vân tay cho sinh viên...';

    this.fingerprintService
      .confirmEnroll(this.selectedStudent.studentId, this.currentSessionCode)
      .subscribe({
        next: (res) => {
          this.enrollState = 'done';
          this.lastEnrollMessage =
            res.message || 'Đã lưu vân tay cho sinh viên.';

          if (typeof res.sensorSlot === 'number') {
            this.currentSensorSlot = res.sensorSlot;
          }

          if (this.selectedStudent && res.studentId === this.selectedStudent.studentId) {
            this.selectedStudent.hasFingerprint = true;
          }

          // Lấy lại info để cập nhật danh sách devices, slot,... từ backend
          this.refreshStudentInfo();
        },
        error: (err) => {
          console.error('confirmEnroll error', err);
          this.enrollState = 'error';

          if (err.error && typeof err.error === 'string') {
            this.lastEnrollMessage = 'Lỗi: ' + err.error;
          } else if (err.error && err.error.message) {
            this.lastEnrollMessage = 'Lỗi: ' + err.error.message;
          } else {
            this.lastEnrollMessage =
              'Không thể lưu vân tay. Vui lòng kiểm tra lại session hoặc thiết bị.';
          }
        }
      });
  }

  // =========================================================
  // 10. HÀM PHỤ: REFRESH LẠI THÔNG TIN SINH VIÊN SAU KHI LƯU
  // =========================================================

  /** Sau khi confirm thành công, lấy lại info để cập nhật hasFingerprint + devices */
  private refreshStudentInfo(): void {
    if (!this.selectedStudent) return;

    const studentId = this.selectedStudent.studentId;

    this.fingerprintService.getStudentFingerprintInfoById(studentId).subscribe({
      next: (info) => {
        this.selectedStudent = info;
      },
      error: (err) => {
        console.error('refreshStudentInfo error', err);
      }
    });
  }

  // 11. hàm đồng bộ vân tay sang các thiết bị khác
  // [1] Thêm biến vào trong class GvQuanlyvantayComponent
syncing = false;
syncMessage = '';

// [2] Thêm hàm xử lý vào trong class
syncFingerprintToAll(): void {
  if (!this.selectedStudent || !this.selectedStudent.hasFingerprint) return;

  if (!confirm(`Đồng bộ vân tay của ${this.selectedStudent.fullName} sang các thiết bị khác?`)) return;

  this.syncing = true;
  this.syncMessage = 'Đang gửi lệnh...';

  // Gọi service (cần update service ở bước 3)
  this.fingerprintService.distributeFingerprintToAllDevices(this.selectedStudent.studentId)
    .subscribe({
      next: (res) => {
        this.syncing = false;
        this.syncMessage = 'Thành công! Lệnh đồng bộ đã được tạo.';
      },
      error: (err) => {
        this.syncing = false;
        this.syncMessage = 'Lỗi: Không thể gọi server.';
        console.error(err);
      }
    });
}
}
