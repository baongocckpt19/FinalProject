import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { UserService } from '../services/user.service';
import { ClassService, ClassList } from '../services/class.service';

interface UserList {
  accountId: number;
  fullName: string;
  username: string;
  roleName: string;
  email: string | null;
  teacherId?: number | null;
  studentId?: number | null;
    fingerCount?: number | null;
}

interface ClassDetail {
  classId: number;
  classCode: string;
  className: string;
  teacherId: number | null;
  teacherName: string | null;
  createdDate: string;
  status: boolean;

}
interface StudentOfClass {
  studentId: number;
  fullName: string;
  username: string;
  email: string;
  fingerCount: number;
}


// thêm interface cho SV trong modal
interface PendingStudent {
  studentId: number;
  fullName: string;
  username?: string;
}
@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.scss'
})
export class AdminComponent {

  constructor(
    private userService: UserService,
    private classService: ClassService
  ) { }

  // ================== STATE CHUNG ==================
  isShowDashboard = true;
  showAnalytics = false;
  showUserManagement = false;
  showClassManagement = false;

  // ================== USER ==================
  users: UserList[] = [];
  filteredUsers: UserList[] = [];
  loadingUsers = false;
  userError: string | null = null;
  searchText: string = '';

  // ================== CLASS ==================
  classes: ClassList[] = [];
  loadingClasses = false;
  classError: string | null = null;

  // ================== MODAL USER DETAIL ==================
  selectedUser: any = null;
  showUserDetailModal = false;

  // ================== MODAL EDIT CLASS ==================
  showEditClassModal = false;
  editingClassId: number | null = null;

  //================= MODAL DANH SÁCH SINH VIÊN CỦA LỚP ==================
  showStudentofClassModal = false;
  selectedClassForStudentModal: ClassList | null = null;
  studentsOfSelectedClass: StudentOfClass[] = [];
  loadingStudentsOfClass = false;




  // trường “hiện tại” (readonly)
  editClassCurrentName = '';
  editClassCurrentCode = '';
  editClassCurrentTeacher = '';

  // trường “mới” (để cập nhật)
  editClassNewName = '';
  editClassNewCode = '';
  editClassNewTeacherId: number | null = null;
  editClassNewTeacherName = '';

  // ================== MỞ CÁC TAB ==================
  openDashboard() {
    this.isShowDashboard = true;
    this.showAnalytics = false;
    this.showClassManagement = false;
    this.showUserManagement = false;
  }

  openAnalytics() {
    this.showAnalytics = true;
    this.isShowDashboard = false;
    this.showClassManagement = false;
    this.showUserManagement = false;
  }

  openUserManagement() {
    this.showUserManagement = true;
    this.isShowDashboard = false;
    this.showClassManagement = false;
    this.showAnalytics = false;

    if (this.users.length === 0) {
      this.fetchUsers();
    }
  }

  openClassManagement() {
    this.showClassManagement = true;
    this.isShowDashboard = false;
    this.showUserManagement = false;
    this.showAnalytics = false;

    if (this.classes.length === 0) {
      this.fetchClasses();
    }
  }

  // ================== USER LOGIC ==================
  fetchUsers() {
    this.loadingUsers = true;
    this.userService.getAllUsers().subscribe({
      next: (data: UserList[]) => {
        this.users = data;
        this.filteredUsers = data;
        this.loadingUsers = false;
      },
      error: (err) => {
        console.error(err);
        this.userError = 'Không tải được danh sách người dùng';
        this.loadingUsers = false;
      }
    });
  }

  searchUser() {
    const term = (this.searchText || '').trim().toLowerCase();

    if (!term) {
      this.filteredUsers = this.users;
      return;
    }

    this.filteredUsers = this.users.filter(u => {
      const fullName = (u.fullName || '').toLowerCase();
      const username = (u.username || '').toLowerCase();
      const roleName = (u.roleName || '').toLowerCase();
      const email = (u.email || '').toLowerCase();
      const teacherId = u.teacherId ? u.teacherId.toString() : '';
      const studentId = u.studentId ? u.studentId.toString() : '';
      const accountId = u.accountId ? u.accountId.toString() : '';

      return (
        fullName.includes(term) ||
        username.includes(term) ||
        roleName.includes(term) ||
        email.includes(term) ||
        teacherId.includes(term) ||
        studentId.includes(term) ||
        accountId.includes(term)
      );
    });
  }

  deleteUser(accountId: number) {
    if (!confirm('Bạn có chắc muốn xóa tài khoản này không?')) return;

    this.userService.deleteUser(accountId).subscribe({
      next: () => {
        this.users = this.users.filter(u => u.accountId !== accountId);
        this.filteredUsers = this.filteredUsers.filter(u => u.accountId !== accountId);
      },
      error: (err) => {
        console.error(err);
        alert('Lỗi khi xóa người dùng');
      }
    });
  }

  openUserDetail(user: any) {
    this.showUserDetailModal = true;
    this.selectedUser = user; // đổ tạm

    this.userService.getUserById(user.accountId).subscribe(detail => {
      this.selectedUser = detail;
    });
  }

  closeUserDetail() {
    this.showUserDetailModal = false;
    this.selectedUser = null;
  }

  onExportExcel() {
    this.userService.exportExcel().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'users.csv';
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => alert('Xuất file thất bại')
    });
  }

  // ================== CLASS LOGIC ==================
  fetchClasses() {
    this.loadingClasses = true;
    this.classService.getAllClasses().subscribe({
      next: (data) => {
        this.classes = data;
        this.loadingClasses = false;
      },
      error: (err) => {
        console.error(err);
        this.classError = 'Không tải được danh sách lớp học';
        this.loadingClasses = false;
      }
    });
  }

  searchClasses() {
    const term = (this.searchText || '').trim().toLowerCase();
    if (!term) {
      this.fetchClasses(); // bạn đang fetch lại, giữ nguyên đúng ý bạn
      return;
    }

    this.classes = this.classes.filter(c => {
      const classCode = (c.classCode || '').toLowerCase();
      const className = (c.className || '').toLowerCase();
      const teacherName = (c.teacherName || '').toLowerCase();
      return (
        classCode.includes(term) ||
        className.includes(term) ||
        teacherName.includes(term)
      );
    });
  }

  deleteClass(classId: number) {
    if (!confirm('Bạn có chắc muốn xóa lớp này không?')) return;

    this.classService.deleteClass(classId).subscribe({
      next: () => {
        this.classes = this.classes.filter(c => c.classId !== classId);
      },
      error: (err) => {
        console.error(err);
        alert('Lỗi khi xóa lớp');
      }
    });
  }

  // bật/tắt trạng thái lớp học
  toggleStatus(c: any) {
  const newStatus = !c.status;

  this.classService.toggleStatus(c.classId, newStatus).subscribe({
    next: () => {
      c.status = newStatus; // cập nhật trực tiếp giao diện
    },
    error: () => {
      alert("Không thể thay đổi trạng thái lớp");
    }
  });
}

  // ================== EDIT CLASS MODAL ==================

  /**
   * Mở modal và load thông tin lớp từ backend
   */
  openClassEditModal(classItem: ClassList) {
    if (!classItem || !classItem.classId) return;

    this.editingClassId = classItem.classId;

    this.classService.getClassById(classItem.classId).subscribe({
      next: (detail: ClassDetail) => {
        // current
        this.editClassCurrentName = detail.className;
        this.editClassCurrentCode = detail.classCode;
        this.editClassCurrentTeacher = detail.teacherName || '';

        // new (mặc định = current)
        this.editClassNewName = detail.className;
        this.editClassNewCode = detail.classCode;
        this.editClassNewTeacherId = detail.teacherId ?? null;
        this.editClassNewTeacherName = detail.teacherName || '';

        this.showEditClassModal = true;
      },
      error: () => {
        alert('Không tải được thông tin lớp');
      }
    });
  }

  /**
   * Đóng modal và reset state
   */
  closeClassEditModal() {
    this.showEditClassModal = false;
    this.editingClassId = null;

    this.editClassCurrentName = '';
    this.editClassCurrentCode = '';
    this.editClassCurrentTeacher = '';

    this.editClassNewName = '';
    this.editClassNewCode = '';
    this.editClassNewTeacherId = null;
    this.editClassNewTeacherName = '';
  }

  /**
   * Khi người dùng gõ mã giảng viên mới
   */
  handleClassTeacherLookup(value: string) {
    const id = Number(value);
    if (!id) {
      this.editClassNewTeacherId = null;
      this.editClassNewTeacherName = '';
      return;
    }
    this.classService.getTeacherById(id).subscribe({
      next: (t) => {
        this.editClassNewTeacherId = t.teacherId;
        this.editClassNewTeacherName = t.fullName;
      },
      error: () => {
        this.editClassNewTeacherName = 'Không tìm thấy giảng viên';
      }
    });
  }

  saveClassChanges() {
    if (!this.editingClassId) return;

    const payload = {
      classCode: this.editClassNewCode,
      className: this.editClassNewName,
      teacherId: this.editClassNewTeacherId
    };

    this.classService.updateClass(this.editingClassId, payload).subscribe({
      next: () => {
        // cập nhật lại trong danh sách hiển thị
        const idx = this.classes.findIndex(c => c.classId === this.editingClassId);
        if (idx !== -1) {
          this.classes[idx] = {
            ...this.classes[idx],
            className: this.editClassNewName,
            classCode: this.editClassNewCode,
            teacherName: this.editClassNewTeacherName || this.editClassCurrentTeacher
          };
        }
        this.closeClassEditModal();
      },
      error: () => {
        alert('Cập nhật lớp học thất bại');
      }
    });
  }
  //================= TẠO LỚP MỚI ==================
  createClassName = '';
  createClassCode = '';
  createClassTeacherId: number | null = null;
  createClassTeacherName = '';
  showCreateClassModal = false;

  openCreateClassModal() {
    this.showCreateClassModal = true;

    // reset form mỗi lần mở
    this.createClassName = '';
    this.createClassCode = '';
    this.createClassTeacherId = null;
    this.createClassTeacherName = '';
  }
  closeCreateClassModal() {
    this.showCreateClassModal = false;
  }

  lookupTeacherForCreate(value: string) {
    const id = Number(value);
    if (!id) {
      this.createClassTeacherId = null;
      this.createClassTeacherName = '';
      return;
    }

    this.classService.getTeacherById(id).subscribe({
      next: (t) => {
        this.createClassTeacherId = t.teacherId;
        this.createClassTeacherName = t.fullName;
      },
      error: () => {
        this.createClassTeacherName = 'Không tìm thấy giảng viên';
      }
    });
  }

  submitCreateClass() {
    if (!this.createClassName || !this.createClassCode) {
      alert('Vui lòng nhập tên lớp và mã lớp');
      return;
    }

    const payload: any = {
      className: this.createClassName,
      classCode: this.createClassCode,
      teacherId: this.createClassTeacherId
    };

    this.classService.createClass(payload).subscribe({
      next: () => {
        // sau khi tạo xong thì tải lại danh sách lớp
        this.fetchClasses();
        this.closeCreateClassModal();
      },
      error: () => {
        alert('Tạo lớp học thất bại (có thể mã lớp đã tồn tại)');
      }
    });
  }

  //====================== modal thêm sinh viên vào lớp ======================
  showAddStudentModal = false;
  activeAddStudentTab: 'manual' | 'excel' = 'manual';

  // class đang thêm SV
  classIdForAddingStudent: number | null = null;
  classNameForAddingStudent: string = '';

  // input cho tab manual
  manualStudentCode: string = '';
  manualStudentName: string = '';
  manualStudentUsername: string = '';
  canAddManualStudent = false;

  // danh sách SV sẽ thêm
  pendingStudentsToAdd: PendingStudent[] = [];

  // mở modal từ nút "Thêm SV" Ở BẢNG
  openAddStudentModal(cls: ClassList) {
    this.showAddStudentModal = true;
    this.activeAddStudentTab = 'manual';
    this.classIdForAddingStudent = cls.classId;
    this.classNameForAddingStudent = cls.className;

    // reset
    this.manualStudentCode = '';
    this.manualStudentName = '';
    this.manualStudentUsername = '';
    this.canAddManualStudent = false;
    this.pendingStudentsToAdd = [];
  }

  closeAddStudentModal() {
    this.showAddStudentModal = false;
  }

  // chuyển tab
  switchAddStudentTab(tab: 'manual' | 'excel') {
    this.activeAddStudentTab = tab;
  }

  // khi gõ mã SV
  onManualStudentCodeChange(value: string) {
    this.manualStudentCode = value;
    const id = Number(value);
    if (!id) {
      this.manualStudentName = '';
      this.manualStudentUsername = '';
      this.canAddManualStudent = false;
      return;
    }

    // gọi backend: /api/students/{id}
    this.classService.getStudentById(id).subscribe({
      next: (stu) => {
        this.manualStudentName = stu.fullName || '';
        this.manualStudentUsername = stu.username || '';
        this.canAddManualStudent = true;
      },
      error: () => {
        this.manualStudentName = 'Không tìm thấy sinh viên';
        this.manualStudentUsername = '';
        this.canAddManualStudent = false;
      }
    });
  }

  // bấm nút +
  addStudentToPendingList() {
    if (!this.canAddManualStudent) return;
    const id = Number(this.manualStudentCode);
    const exists = this.pendingStudentsToAdd.some(s => s.studentId === id);
    if (exists) return;

    this.pendingStudentsToAdd.push({
      studentId: id,
      fullName: this.manualStudentName,
      username: this.manualStudentUsername
    });

    // reset ô nhập
    this.manualStudentCode = '';
    this.manualStudentName = '';
    this.manualStudentUsername = '';
    this.canAddManualStudent = false;
  }

  // xóa 1 SV khỏi danh sách pending
  removePendingStudent(index: number) {
    this.pendingStudentsToAdd.splice(index, 1);
  }

  selectedExcelFile: File | null = null;

  // khi chọn file CSV
  onExcelFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files && input.files[0];
    if (!file) {
      this.selectedExcelFile = null;
      return;
    }
    this.selectedExcelFile = file;
  }

  // nút "Thêm sinh viên vào lớp" dùng chung cho 2 tab
  confirmAddStudentsToClass() {
    // nếu đang ở tab excel → upload CSV
    if (this.activeAddStudentTab === 'excel') {
      this.uploadCsvForClass();
      return;
    }

    // còn lại là tab thủ công
    if (!this.classIdForAddingStudent) {
      alert('Không xác định được lớp');
      return;
    }
    if (this.pendingStudentsToAdd.length === 0) {
      alert('Chưa có sinh viên nào để thêm');
      return;
    }

    const studentIds = this.pendingStudentsToAdd.map(s => s.studentId);

    this.classService.addStudentsToClass(this.classIdForAddingStudent, studentIds)
      .subscribe({
        next: () => {
          alert('Đã thêm sinh viên vào lớp');
          this.closeAddStudentModal();
          this.fetchClasses();
        },
        error: () => {
          alert('Thêm sinh viên thủ công thất bại');
        }
      });
  }

  uploadCsvForClass() {
    if (!this.classIdForAddingStudent) { alert('Không xác định được lớp để import'); return; }
    if (!this.selectedExcelFile) { alert('Vui lòng chọn file CSV'); return; }

    const formData = new FormData();
    formData.append('file', this.selectedExcelFile); // KEY phải là 'file'

    this.classService.importStudentsFromCsv(this.classIdForAddingStudent, formData)
      .subscribe({
        next: (res: any) => {
          alert(`Import CSV thành công: ${res?.imported ?? 0} SV. Lỗi: ${res?.rejectedCount ?? 0}`);
          // nếu cần xem chi tiết lỗi:
          // console.table(res?.rejectedRows || []);
          this.closeAddStudentModal();
          this.fetchClasses();
        },
        error: (err) => {
          console.error(err);
          alert('Import CSV thất bại');
        }
      });
  }

  //================= MODAL DANH SÁCH SINH VIÊN CỦA LỚP ==================
  openStudentofClassModal(cls: ClassList) {
    this.selectedClassForStudentModal = cls;
    this.showStudentofClassModal = true;
    this.loadingStudentsOfClass = true;

    this.classService.getStudentsOfClass(cls.classId).subscribe({
      next: (data: StudentOfClass[]) => {
        this.studentsOfSelectedClass = data;
        this.loadingStudentsOfClass = false;
      },
      error: (err) => {
        console.error(err);
        alert('Không tải được danh sách sinh viên của lớp');
        this.loadingStudentsOfClass = false;
      }
    });
  }

  closeStudentofClassModal() {
    this.showStudentofClassModal = false;
    this.selectedClassForStudentModal = null;
    this.studentsOfSelectedClass = [];
  }


  removeStudentFromClass(studentId: number) {
    if (!this.selectedClassForStudentModal) {
      alert('Không xác định được lớp');
      return;
    }
    if (!confirm('Bạn có chắc muốn xóa sinh viên này khỏi lớp không?')) return;

    const classId = this.selectedClassForStudentModal.classId;

    this.classService.removeStudentFromClass(classId, studentId).subscribe({
      next: () => {
        // remove trong mảng
        this.studentsOfSelectedClass = this.studentsOfSelectedClass
          .filter(s => s.studentId !== studentId);

        // đồng bộ lại bảng lớp (giảm số lượng sinh viên nếu cần)
        const cls = this.classes.find(c => c.classId === classId);
        if (cls && cls.studentCount > 0) {
          cls.studentCount = cls.studentCount - 1;
        }
      },
      error: (err) => {
        console.error(err);
        alert('Xóa sinh viên khỏi lớp thất bại');
      }
    });
  }


  // ================== EXPORT ==================

  // xuất danh sách lớp
  onExportClassExcel() {
    this.classService.exportExcel().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'classes.csv';
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => {
        alert('Xuất báo cáo lớp thất bại');
      }
    });
  }

  // xuất DS SV của 1 lớp
  onExportStudentsOfClass(classId: number) {
    this.classService.exportStudents(classId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `class_${classId}_students.csv`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => {
        alert('Xuất danh sách sinh viên thất bại');
      }
    });
  }
}
