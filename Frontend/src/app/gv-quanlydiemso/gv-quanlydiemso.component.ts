import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  GradeService,
  TeacherClass,
  StudentGradeApi,
  StudentGradeSaveDto
} from '../services/grade.service';
import { NotificationService } from '../services/notification.service';

interface StudentGrade {
  studentId: number;
  fullName: string;
  username: string;
  mssv: string;
  diemChuyenCan: number;
  diemGiuaKy: number;
  diemCuoiKy: number;
  diemTrungbinh: number;
  xepLoai: string;
}

@Component({
  selector: 'app-gv-quanlydiemso',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gv-quanlydiemso.component.html',
  styleUrl: './gv-quanlydiemso.component.scss'
})
export class GvQuanlydiemsoComponent {

  constructor(
    private gradeService: GradeService,
    private notify: NotificationService
  ) { }

  // ================== MODAL NHẬP FILE ĐIỂM ==================
  showFileImportModal = false;

  // file CSV được chọn
  newFile: { gradeFile: File | null } = { gradeFile: null };

  // loại điểm đang chọn trong select (attendance | midterm | final | all)
  fileImportGradeType: string = 'attendance';

  openFileImportModal() {
    this.showFileImportModal = true;
  }

  closeFileImportModal() {
    this.showFileImportModal = false;
    this.newFile.gradeFile = null;
  }

  // Bắt sự kiện chọn file từ input type="file"
  // <input type="file" (change)="onFileSelected($event)" ...>
  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.newFile.gradeFile = input.files[0];
    } else {
      this.newFile.gradeFile = null;
    }
  }

  // Nhấn nút "Nhập điểm" trong modal
  addFile() {
    if (!this.selectedClassId) {
      this.notify.error('Chưa chọn lớp.');
      return;
    }

    const file = this.newFile.gradeFile;
    if (!file) {
      this.notify.error('Vui lòng chọn file CSV!');
      return;
    }

    const gradeType = this.fileImportGradeType; // attendance / midterm / final / all

    this.gradeService.importGradesFromCsv(this.selectedClassId, gradeType, file)
      .subscribe({
        next: (res) => {
          console.log('Kết quả import:', res);

          const total = res?.total ?? 0;
          const imported = res?.imported ?? 0;
          const rejected = res?.rejectedCount ?? 0;
          console.log(
            `Import CSV hoàn tất. Tổng dòng: ${total}, Thành công: ${imported}, Bỏ qua: ${rejected}`
          );

          if (imported > 0) {
            this.notify.success('Thực hiện thành công');
            this.loadGrades();
          } else {
            this.notify.error('Thực hiện thất bại');
          }

          this.closeFileImportModal();
        },
        error: (err) => {
          console.error('Lỗi import điểm từ file:', err);
          this.notify.error('Thực hiện thất bại');
        }
      });
  }

  // ================== MODAL NHẬP ĐIỂM HÀNG LOẠT (TEXT) ==================
  showBulkGradeInputModal = false;
  newBulkGrades = { grades: '' };

  // loại điểm đang chọn trong modal (attendance | midterm | final | all)
  bulkGradeType: string = 'attendance';

  private parseGrade(s: string): number | null {
    if (!s) return null;
    const val = parseFloat(s.replace(',', '.'));
    if (isNaN(val)) return null;
    if (val < 0 || val > 10) return null;
    return val;
  }

  openBulkGradeInputModal() {
    this.showBulkGradeInputModal = true;
  }

  closeBulkGradeInputModal() {
    this.showBulkGradeInputModal = false;
  }

  processBulkGrades() {
    if (!this.selectedClassId) {
      this.notify.error('Chưa chọn lớp.');
      return;
    }

    const text = (this.newBulkGrades.grades || '').trim();
    if (!text) {
      this.notify.error('Vui lòng nhập dữ liệu điểm.');
      return;
    }

    const gradeType = this.bulkGradeType; // attendance | midterm | final | all

    const lines = text.split(/\r?\n/);
    let applied = 0;
    let notFoundCount = 0;
    let invalidCount = 0;

    for (const rawLine of lines) {
      const line = rawLine.trim();
      if (!line) continue; // bỏ dòng trống

      const parts = line.split(',').map(p => p.trim());
      if (!parts[0]) {
        invalidCount++;
        continue;
      }

      const mssvStr = parts[0];

      // Tìm sinh viên trong danh sách hiện tại:
      //  - so với studentId (số)
      //  - hoặc so với mssv (string)
      const sv = this.sv.find(s =>
        String(s.studentId) === mssvStr || (s.mssv && s.mssv === mssvStr)
      );

      if (!sv) {
        notFoundCount++;
        continue;
      }

      try {
        if (gradeType === 'attendance') {
          if (parts.length < 2) {
            invalidCount++;
            continue;
          }
          const g = this.parseGrade(parts[1]);
          if (g == null) {
            invalidCount++;
            continue;
          }
          sv.diemChuyenCan = g;
        } else if (gradeType === 'midterm') {
          if (parts.length < 2) {
            invalidCount++;
            continue;
          }
          const g = this.parseGrade(parts[1]);
          if (g == null) {
            invalidCount++;
            continue;
          }
          sv.diemGiuaKy = g;
        } else if (gradeType === 'final') {
          if (parts.length < 2) {
            invalidCount++;
            continue;
          }
          const g = this.parseGrade(parts[1]);
          if (g == null) {
            invalidCount++;
            continue;
          }
          sv.diemCuoiKy = g;
        } else if (gradeType === 'all') {
          if (parts.length < 4) {
            invalidCount++;
            continue;
          }
          const att = this.parseGrade(parts[1]);
          const mid = this.parseGrade(parts[2]);
          const fin = this.parseGrade(parts[3]);

          if (att == null || mid == null || fin == null) {
            invalidCount++;
            continue;
          }

          sv.diemChuyenCan = att;
          sv.diemGiuaKy = mid;
          sv.diemCuoiKy = fin;
        } else {
          invalidCount++;
          continue;
        }

        applied++;
      } catch {
        invalidCount++;
      }
    }

    if (applied === 0) {
      console.warn(
        'Áp dụng điểm hàng loạt thất bại. NotFound:',
        notFoundCount,
        'Invalid:',
        invalidCount
      );
      this.notify.error('Thực hiện thất bại');
      return;
    }

    // Cập nhật lại điểm TB, xếp loại trên UI
    this.diemTB();

    // Gọi API lưu TẤT CẢ điểm của lớp
    this.saveAllGrades();

    console.log(
      `Áp dụng điểm hàng loạt xong. Hợp lệ: ${applied}, Không tìm thấy MSSV: ${notFoundCount}, Sai định dạng/ngoài 0–10: ${invalidCount}`
    );

    // Đóng modal + reset text
    this.closeBulkGradeInputModal();
    this.newBulkGrades.grades = '';
  }

  // ================== MODAL THÔNG BÁO (KHÔNG DÙNG NỮA, GIỮ LẠI NẾU TEMPLATE CÒN THAM CHIẾU) ==================
  showNotificationModal = false;

  openNotificationModal() {
    this.showNotificationModal = true;
  }

  closeNotificationModal() {
    this.showNotificationModal = false;
  }

  // ================== DỮ LIỆU LỚP & ĐIỂM ==================
  classes: TeacherClass[] = [];
  selectedClassId: number | null = null;
  selectedClassDisplayName: string = '';

  sv: StudentGrade[] = [];
  sinhvienFiltered: StudentGrade[] = [];

  // ================== STATS HEADER ==================
  totalStudents: number = 0;      // Tổng sinh viên
  averageClassScore: number = 0;  // Điểm TB lớp
  passRate: number = 0;           // Tỉ lệ qua môn (%)
  needImproveCount: number = 0;   // Số SV cần cải thiện (DTB < 7)

  // ================== ẨN / HIỆN ĐIỂM ==================
  isGradeVisible = true;
  toggleGradeVisibility() {
    this.isGradeVisible = !this.isGradeVisible;
  }

  // ================== TÌM KIẾM & SẮP XẾP ==================
  searchText: string = '';
  currentSortColumn: string = '';
  isAscending: boolean = true;

  searchStudent() {
    const searchInput = this.searchText.toLowerCase().trim();

    if (!searchInput) {
      this.sv = [...this.sinhvienFiltered];
      this.sortByName();
      return;
    }

    this.sv = this.sinhvienFiltered.filter(sinhVien =>
      (sinhVien.fullName || '').toLowerCase().includes(searchInput) ||
      (sinhVien.mssv || '').toLowerCase().includes(searchInput) ||
      (sinhVien.username || '').toLowerCase().includes(searchInput)
    );
    this.sortByName();
  }

  sortByName() {
    this.sv.sort((a, b) =>
      a.fullName.localeCompare(b.fullName, 'vi', { sensitivity: 'base' })
    );
  }

  sortBy(column: string) {
    if (this.currentSortColumn === column) {
      this.isAscending = !this.isAscending;
    } else {
      this.currentSortColumn = column;
      this.isAscending = true;
    }

    this.sv.sort((a: any, b: any) => {
      const valA = a[column];
      const valB = b[column];

      if (typeof valA === 'string') {
        return this.isAscending
          ? valA.localeCompare(valB, 'vi', { sensitivity: 'base' })
          : valB.localeCompare(valA, 'vi', { sensitivity: 'base' });
      }

      if (typeof valA === 'number') {
        return this.isAscending ? valA - valB : valB - valA;
      }

      return 0;
    });
  }

  // ================== CHART PHÂN BỐ ĐIỂM ==================
  gradeDistribution: { range: string, count: number }[] = [];

  updateGradeDistribution() {
    const ranges = [
      { min: 0, max: 1 },
      { min: 1, max: 2 },
      { min: 2, max: 3 },
      { min: 3, max: 4 },
      { min: 4, max: 5 },
      { min: 5, max: 6 },
      { min: 6, max: 7 },
      { min: 7, max: 8 },
      { min: 8, max: 9 },
      { min: 9, max: 10 }
    ];

    this.gradeDistribution = ranges.map(r => {
      const count = this.sv.filter(
        s => s.diemTrungbinh >= r.min && s.diemTrungbinh < r.max
      ).length;
      return { range: `${r.min}-${r.max}`, count };
    });
  }

  // ================== DS SINH VIÊN CẦN CHÚ Ý ==================
  attentionGradeType: string = 'average';
  attentionStudents: any[] = [];

  updateAttentionList() {
    const type = this.attentionGradeType;

    this.attentionStudents = this.sv
      .filter(sv => {
        if (type === 'attendance') return sv.diemChuyenCan < 4;
        if (type === 'midterm') return sv.diemGiuaKy < 4;
        if (type === 'final') return sv.diemCuoiKy < 4;
        return sv.diemTrungbinh < 4;
      })
      .map(sv => ({
        id: sv.studentId,
        name: sv.fullName,
        mssv: sv.studentId,
        gradeLabel:
          type === 'attendance' ? 'Chuyên cần' :
          type === 'midterm' ? 'Giữa kỳ' :
          type === 'final' ? 'Cuối kỳ' : 'Trung bình',
        grade:
          type === 'attendance' ? sv.diemChuyenCan :
          type === 'midterm' ? sv.diemGiuaKy :
          type === 'final' ? sv.diemCuoiKy : sv.diemTrungbinh
      }));
  }

  getGradeTypeName(type: string): string {
    switch (type) {
      case 'attendance': return 'điểm chuyên cần';
      case 'midterm': return 'điểm giữa kỳ';
      case 'final': return 'điểm cuối kỳ';
      default: return 'điểm trung bình';
    }
  }

  onGradeTypeChange(event: any) {
    this.attentionGradeType = event.target.value;
    this.updateAttentionList();
  }

  // ================== STATS TỪ ĐIỂM ==================
  updateStatsFromGrades() {
    const n = this.sv.length;
    this.totalStudents = n;

    if (n === 0) {
      this.averageClassScore = 0;
      this.passRate = 0;
      this.needImproveCount = 0;
      return;
    }

    const sum = this.sv.reduce((acc, sv) => acc + (sv.diemTrungbinh ?? 0), 0);
    this.averageClassScore = sum / n;

    const passed = this.sv.filter(sv => sv.diemTrungbinh >= 4).length;
    this.passRate = (passed / n) * 100;

    this.needImproveCount = this.sv.filter(sv => sv.diemTrungbinh < 7).length;
  }

  // ================== TÍNH ĐIỂM TB & XẾP LOẠI ==================
  diemTB() {
    this.sv.forEach(sinhVien => {
      const at = sinhVien.diemChuyenCan ?? 0;
      const mid = sinhVien.diemGiuaKy ?? 0;
      const fin = sinhVien.diemCuoiKy ?? 0;

      sinhVien.diemTrungbinh = (at * 0.25) + (mid * 0.25) + (fin * 0.5);

      if (sinhVien.diemTrungbinh >= 9)      sinhVien.xepLoai = 'Xuất sắc';
      else if (sinhVien.diemTrungbinh >= 8) sinhVien.xepLoai = 'Giỏi';
      else if (sinhVien.diemTrungbinh >= 7) sinhVien.xepLoai = 'Khá';
      else if (sinhVien.diemTrungbinh >= 5) sinhVien.xepLoai = 'Trung bình';
      else                                  sinhVien.xepLoai = 'Yếu';
    });

    this.updateGradeDistribution();
    this.updateAttentionList();
    this.updateStatsFromGrades();
  }

  // ================== LIFECYCLE ==================
  ngOnInit() {
    this.loadTeacherClasses();
  }

  // ================== LOAD DỮ LIỆU TỪ BACKEND ==================
  private loadTeacherClasses() {
    this.gradeService.getTeacherClasses().subscribe({
      next: (classes) => {
        this.classes = classes;
        if (classes.length > 0) {
          this.selectedClassId = classes[0].classId;
          this.updateSelectedClassDisplayName();
          this.loadGrades();
        }
      },
      error: (err) => {
        console.error('Lỗi tải danh sách lớp:', err);
        this.notify.error('Thực hiện thất bại');
      }
    });
  }

  private updateSelectedClassDisplayName() {
    if (!this.selectedClassId) {
      this.selectedClassDisplayName = '';
      return;
    }
    const c = this.classes.find(x => x.classId === this.selectedClassId);
    if (c) {
      this.selectedClassDisplayName = `${c.className} (${c.classCode})`;
    } else {
      this.selectedClassDisplayName = '';
    }
  }

  onClassChange() {
    this.updateSelectedClassDisplayName();
    this.loadGrades();
  }

  private loadGrades() {
    if (!this.selectedClassId) return;

    this.gradeService.getClassGrades(this.selectedClassId).subscribe({
      next: (data: StudentGradeApi[]) => {
        this.sv = data.map(item => ({
          studentId: item.studentId,
          fullName: item.fullName,
          username: item.username,
          mssv: item.studentId.toString(), // MSSV hiển thị = StudentId
          diemChuyenCan: item.attendanceGrade?? 0,
          diemGiuaKy: item.midtermGrade ?? 0,
          diemCuoiKy: item.finalGrade ?? 0,
          diemTrungbinh: item.averageGrade ?? 0,
          xepLoai: ''
        }));

        this.sinhvienFiltered = [...this.sv];
        this.diemTB();
        this.sortByName();
      },
      error: (err) => {
        console.error('Lỗi load điểm:', err);
        this.notify.error('Thực hiện thất bại');
      }
    });
  }

  // ================== LƯU ĐIỂM ==================
  saveStudentGrade(sinhVien: StudentGrade) {
    if (!this.selectedClassId) {
      this.notify.error('Chưa chọn lớp.');
      return;
    }

    const dto: StudentGradeSaveDto = {
      studentId: sinhVien.studentId,
      attendanceGrade: sinhVien.diemChuyenCan ?? 0,
      midtermGrade: sinhVien.diemGiuaKy ?? 0,
      finalGrade: sinhVien.diemCuoiKy ?? 0
    };

    this.gradeService.updateStudentGrade(this.selectedClassId, dto).subscribe({
      next: () => {
        this.diemTB();
        this.notify.success('Thực hiện thành công');
      },
      error: (err) => {
        console.error('Lỗi lưu điểm sinh viên:', err);
        this.notify.error('Thực hiện thất bại');
      }
    });
  }

  saveAllGrades() {
    if (!this.selectedClassId) {
      this.notify.error('Chưa chọn lớp.');
      return;
    }

    const list: StudentGradeSaveDto[] = this.sv.map(s => ({
      studentId: s.studentId,
      attendanceGrade: s.diemChuyenCan ?? 0,
      midtermGrade: s.diemGiuaKy ?? 0,
      finalGrade: s.diemCuoiKy ?? 0
    }));

    this.gradeService.updateAllGrades(this.selectedClassId, list).subscribe({
      next: () => {
        this.diemTB();
        this.notify.success('Thực hiện thành công');
      },
      error: (err) => {
        console.error('Lỗi lưu tất cả điểm:', err);
        this.notify.error('Thực hiện thất bại');
      }
    });
  }

  // ================== EXPORT BÁO CÁO CSV ==================
  exportGrades() {
    if (!this.selectedClassId) {
      this.notify.error('Chưa chọn lớp.');
      return;
    }

    this.gradeService.exportClassGrades(this.selectedClassId).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;

        const cls = this.classes.find(c => c.classId === this.selectedClassId);
        const code = cls?.classCode || `class_${this.selectedClassId}`;
        const safeCode = code.replace(/[^a-zA-Z0-9_-]/g, '');
        a.download = `bao_cao_diem_${safeCode}.csv`;

        a.click();
        window.URL.revokeObjectURL(url);
        this.notify.success('Thực hiện thành công');
      },
      error: (err) => {
        console.error('Lỗi xuất báo cáo điểm:', err);
        this.notify.error('Thực hiện thất bại');
      }
    });
  }

}
