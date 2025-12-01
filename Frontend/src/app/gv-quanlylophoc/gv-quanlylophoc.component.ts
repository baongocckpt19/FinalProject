// src/app/gv-quanlylophoc/gv-quanlylophoc.component.ts
import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TeacherClassService } from '../services/teacher-class.service';
import { ClassService, StudentOfClass } from '../services/class.service';
import { forkJoin } from 'rxjs';
import { NotificationService } from '../services/notification.service';

@Component({
  selector: 'app-gv-quanlylophoc',
  imports: [CommonModule, FormsModule],
  standalone: true,
  templateUrl: './gv-quanlylophoc.component.html',
  styleUrl: './gv-quanlylophoc.component.scss'
})
export class GvQuanlylophocComponent implements OnInit {

  constructor(
    private router: Router,
    private teacherClassService: TeacherClassService,
    private classService: ClassService,
    private notify: NotificationService
  ) { }

  /*modal thêm lớp học*/
  showAddClassModal = false;
  openAddClassModal() {
    this.showAddClassModal = true;
  }
  closeAddClassModal() {
    this.showAddClassModal = false;
  }
  newClass = {
    className: '',
    classCode: '',
    description: '',
    maxStudents: null as number | null
  };

  addClass() {
    const { className, classCode, maxStudents } = this.newClass;

    if (!className || !classCode || !maxStudents) {
      this.notify.error('Vui lòng nhập đủ thông tin bắt buộc!');
      return;
    }

    // TODO: gọi API tạo lớp (nếu bạn muốn)
    this.openNotificationModal();
    this.closeAddClassModal();

    this.notify.success('Thêm lớp học (demo) thành công!');

    this.newClass = {
      className: '',
      classCode: '',
      description: '',
      maxStudents: null
    };
  }

  /*modal thực hiện thành công*/
  showNotificationModal = false;
  closeNotificationModal() {
    this.showNotificationModal = false;
  }
  openNotificationModal() {
    this.showNotificationModal = true;
    this.closeAddClassModal();
    this.closeClassDetailModal();
  }

  /*modal xóa lớp học*/
  showDeleteModal = false;
  openDeleteModal() {
    this.showDeleteModal = true;
  }
  closeDeleteModal() {
    this.showDeleteModal = false;
  }

  // ====== DỮ LIỆU LỚP HỌC ======
  lophoc: any[] = [];
  filteredClassList: any[] = [];

  // tìm kiếm
  searchText: string = '';

  // sắp xếp
  sortAscending: boolean = true;
  sortByNameActive: boolean = false;

  ngOnInit() {
    this.loadMyClasses();
  }

  // gọi API backend để lấy lớp của giảng viên đang đăng nhập
  loadMyClasses() {
    this.teacherClassService.getMyClasses().subscribe({
      next: (data) => {
        // data là List<ClassListDto> từ backend
        this.lophoc = data.map(dto => this.mapDtoToViewModel(dto));
        this.filteredClassList = [...this.lophoc];
        this.sortBystatus();
      },
      error: (err) => {
        console.error('Error loadMyClasses', err);
        this.notify.error('Không tải được danh sách lớp học');
      }
    });
  }

  private mapDtoToViewModel(dto: any) {
    // Status: ở backend bạn dùng 0 = hoạt động, 1 = hoàn thành/tạm dừng
    const statusText = dto.status ? 'Tạm dừng' : 'Hoạt động';

    return {
      id: dto.classId,
      tenLop: dto.className,
      classCode: dto.classCode,
      soHocSinh: dto.studentCount,
      ngayTao: dto.createdDate, // có thể format lại nếu muốn
      trangThai: statusText,
      soSvCoVanTay: dto.fingerprintedCount,
      teacherName: dto.teacherName,
      // Nếu cần toggleStatus dùng boolean:
      status: dto.status
    };
  }

  toggleClassStatus(lop: any) {
    const newStatus = !lop.status; // đảo trạng thái boolean

    this.teacherClassService.updateClassStatus(lop.id, newStatus).subscribe({
      next: () => {
        // cập nhật UI
        lop.status = newStatus;
        lop.trangThai = newStatus ? 'Tạm dừng' : 'Hoạt động';
        this.notify.success('Cập nhật trạng thái lớp thành công');
      },
      error: (err) => {
        console.error('Update class status error', err);
        this.notify.error('Không thể thay đổi trạng thái lớp');
      }
    });
  }

  // sắp xếp tên lớp học
  sortByName() {
    this.sortByNameActive = true;

    this.filteredClassList.sort((a, b) =>
      a.tenLop.localeCompare(b.tenLop, 'vi', { sensitivity: 'base' })
    );
  }

  // tìm kiếm
  searchClasses() {
    const search = this.searchText.trim().toLowerCase();
    this.sortByNameActive = false;

    if (!search) {
      this.filteredClassList = [...this.lophoc];
      this.sortBystatus();
      return;
    }

    this.filteredClassList = this.lophoc.filter(lop =>
      lop.tenLop.toLowerCase().includes(search) ||
      lop.classCode.toLowerCase().includes(search)
    );
    this.sortBystatus();
  }

  // mặc định sắp xếp theo trạng thái
  sortBystatus() {
    this.filteredClassList.sort((a, b) => {
      // "Hoạt động" lên trên
      if (a.trangThai === 'Hoạt động' && b.trangThai !== 'Hoạt động') return -1;
      if (a.trangThai !== 'Hoạt động' && b.trangThai === 'Hoạt động') return 1;
      return 0;
    });
  }

  // ====== EXPORT CÁC LỚP CỦA GIẢNG VIÊN ======
  exportMyClasses() {
    this.teacherClassService.exportMyClasses().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'my-classes.csv';
        a.click();
        window.URL.revokeObjectURL(url);

        this.notify.success('Xuất danh sách lớp thành công');
      },
      error: (err) => {
        console.error('Error exportMyClasses', err);
        this.notify.error('Xuất dữ liệu thất bại');
      }
    });
  }

  // ====== QUẢN LÝ SINH VIÊN TRONG MODAL LỚP ======
  studentsInClass: StudentOfClass[] = [];      // DS hiện có trong DB
  pendingAddStudents: StudentOfClass[] = [];   // SV mới thêm (chưa lưu DB)
  removedStudentIds: number[] = [];            // ID SV sẽ bị xóa khỏi lớp khi Save

  // Form thêm SV
  newStudentIdInput: string = '';
  searchedStudent: any = null;      // dữ liệu trả về từ /api/students/{id}
  searchStudentError: string = '';

  // Thanh tìm kiếm sinh viên
  studentSearchText: string = '';

  // Danh sách sau khi filter (tự động cập nhật UI)
  filteredStudents: StudentOfClass[] = [];

  /*modal chi tiết lớp học*/
  showClassDetailModal = false;
  selectedClass: any = null;

  openClassDeatilModal(lop: any) {
    this.selectedClass = { ...lop }; // clone nhỏ

    // reset state thêm/xóa
    this.studentsInClass = [];
    this.pendingAddStudents = [];
    this.removedStudentIds = [];
    this.newStudentIdInput = '';
    this.searchedStudent = null;
    this.searchStudentError = '';

    this.showClassDetailModal = true;

    // gọi API lấy DS SV của lớp
    this.classService.getStudentsOfClass(lop.id).subscribe({
      next: (data) => {
        this.studentsInClass = data;
        this.updateFilteredStudents();
      },
      error: (err) => {
        console.error('Error getStudentsOfClass', err);
        this.notify.error('Không tải được danh sách sinh viên của lớp');
      }
    });
  }

  closeClassDetailModal() {
    this.showClassDetailModal = false;
  }

  // Cập nhật danh sách lọc khi có thay đổi thêm/xóa
  private updateFilteredStudents() {
    const all = this.getDisplayedStudents();

    if (!this.studentSearchText.trim()) {
      this.filteredStudents = [...all];
      return;
    }

    const search = this.studentSearchText.trim().toLowerCase();

    this.filteredStudents = all.filter(st =>
      st.fullName.toLowerCase().includes(search) ||
    
      st.username.toLowerCase().includes(search) ||
      st.studentId.toString().includes(search)
    );
  }

  // Trigger từ sự kiện nhập
  filterDisplayedStudents() {
    this.updateFilteredStudents();
  }

  // Tìm SV theo MSSV (StudentId)
  searchStudent() {
    this.searchStudentError = '';
    this.searchedStudent = null;

    const code = String(this.newStudentIdInput).trim();
    if (!code) {
      this.searchStudentError = 'Vui lòng nhập MSSV hợp lệ';
      this.notify.error('Vui lòng nhập MSSV hợp lệ');
      return;
    }

    this.classService.getStudentByCode(code).subscribe({
      next: (data) => {
        this.searchedStudent = data;
        this.searchStudentError = '';
      },
      error: (err) => {
        console.error('Error getStudentById', err);
        this.searchStudentError = 'Không tìm thấy sinh viên với MSSV này';
        this.notify.error('Không tìm thấy sinh viên với MSSV này');
      }
    });
  }

  // Thêm SV vừa tìm được vào danh sách tạm (chưa lưu DB)
  addPendingStudent() {
    if (!this.searchedStudent) return;
    const sid = this.searchedStudent.studentId;

    // 1) đã có trong lớp & chưa bị đánh dấu xóa
    const existed = this.studentsInClass.some(s => s.studentId === sid)
      && !this.removedStudentIds.includes(sid);
    if (existed) {
      this.notify.error('Sinh viên này đã nằm trong lớp');
      return;
    }

    // 2) đã nằm trong pendingAdd
    const existedInPending = this.pendingAddStudents.some(s => s.studentId === sid);
    if (existedInPending) {
      this.notify.error('Sinh viên này đã được thêm tạm');
      return;
    }

    const newSt: StudentOfClass = {
      studentId: this.searchedStudent.studentId,
      studentCode: this.searchedStudent.userCode,
      fullName: this.searchedStudent.fullName,
      username: this.searchedStudent.username,
      email: this.searchedStudent.email,
      fingerCount: 0   // chưa biết số vân tay => để 0
    };

    this.pendingAddStudents.push(newSt);

    // reset input
    this.newStudentIdInput = '';
    this.searchedStudent = null;
    this.updateFilteredStudents();

    this.notify.success('Đã thêm sinh viên vào danh sách tạm');
  }

  // Danh sách SV hiển thị (DS hiện có - đã bị đánh dấu xóa + DS pendingAdd)
  getDisplayedStudents(): StudentOfClass[] {
    const removed = new Set(this.removedStudentIds);

    const existing = this.studentsInClass.filter(s => !removed.has(s.studentId));
    const added = this.pendingAddStudents;

    return [...existing, ...added];
  }

  // Kiểm tra SV có thuộc danh sách pendingAdd không
  isPendingAdd(studentId: number): boolean {
    return this.pendingAddStudents.some(s => s.studentId === studentId);
  }

  // Khi bấm nút "Xóa" trên từng dòng
  markRemoveStudent(st: StudentOfClass) {
    const sid = st.studentId;

    if (this.isPendingAdd(sid)) {
      // Nếu là SV mới thêm tạm -> chỉ cần bỏ khỏi pendingAdd
      this.pendingAddStudents = this.pendingAddStudents.filter(s => s.studentId !== sid);
    } else {
      // SV đang tồn tại trong lớp -> đánh dấu xóa
      if (!this.removedStudentIds.includes(sid)) {
        this.removedStudentIds.push(sid);
      }
    }
    this.updateFilteredStudents();

    this.notify.success('Đã đánh dấu xóa sinh viên khỏi lớp (chưa lưu)');
  }

  saveClassChanges() {
    if (!this.selectedClass) return;

    const classId = this.selectedClass.id;
    const addIds = this.pendingAddStudents.map(s => s.studentId);
    const removeIds = [...this.removedStudentIds];

    if (addIds.length === 0 && removeIds.length === 0) {
      // không có thay đổi
      this.notify.error('Không có thay đổi nào để lưu');
      this.openNotificationModal();
      return;
    }

    const requests = [];

    if (addIds.length > 0) {
      requests.push(this.classService.addStudentsToClass(classId, addIds));
    }

    if (removeIds.length > 0) {
      for (const sid of removeIds) {
        requests.push(this.classService.removeStudentFromClass(classId, sid));
      }
    }

    forkJoin(requests).subscribe({
      next: () => {
        // Sau khi lưu thành công:
        // - reload bảng lớp để cập nhật số SV
        // - đóng modal + mở thông báo
        this.loadMyClasses();
        this.openNotificationModal();
        this.notify.success('Lưu thay đổi lớp học thành công');
      },
      error: (err) => {
        console.error('Save class changes error', err);
        this.notify.error('Không thể lưu thay đổi. Vui lòng thử lại.');
      }
    });
  }

  exportClassStudents(lop: any) {
    const classId = lop.id;   // vì mapDtoToViewModel đã set id: dto.classId

    if (!classId) {
      this.notify.error('Không xác định được ID lớp để xuất dữ liệu');
      return;
    }

    this.classService.exportStudents(classId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;

        // tên file gợi ý: mã lớp + tên lớp
        const safeCode = lop.classCode || 'class';
        const safeName = (lop.tenLop || '').replace(/[/\\?%*:|"<>]/g, '_');
        a.download = `${safeCode}_${safeName}_students.csv`;

        a.click();
        window.URL.revokeObjectURL(url);

        this.notify.success('Xuất danh sách sinh viên lớp thành công');
      },
      error: (err) => {
        console.error('Error exportClassStudents', err);
        this.notify.error('Xuất dữ liệu lớp thất bại');
      }
    });
  }
}
