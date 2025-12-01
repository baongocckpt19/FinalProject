import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { UserService } from '../services/user.service';
import { ClassService, ClassList } from '../services/class.service';
import { NotificationService } from '../services/notification.service';
import { DeviceService, Device } from '../services/device.service';
import { Router } from '@angular/router';


interface UserList {
  accountId: number;
  fullName: string;
  username: string;
  roleName: string;
  email: string | null;
  teacherId?: number | null;
  studentId?: number | null;

  teacherCode?: string | null;
  studentCode?: string | null;
  userCode?: string | null;

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
  studentCode: string;
  fullName: string;
  username: string;
  email: string;
  fingerCount: number;
}


// thêm interface cho SV trong modal
interface PendingStudent {
  studentId: number;
  studentCode?: string;
  fullName: string;
  username?: string;
}

/** LỊCH HỌC THEO NGÀY (ClassSchedule) */
interface ClassScheduleItem {
  scheduleId: number;
  classId: number;
  scheduleDate: string;   // '2025-11-27'
  startTime: string;      // '07:00' hoặc '07:00:00'
  endTime: string;        // '09:00' hoặc '09:00:00'
  room: string | null;
  isActive: boolean;      // true = hoạt động, false = tạm hoãn
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
    private classService: ClassService,
    private notify: NotificationService,
    private deviceService: DeviceService,
    private router: Router
  ) { }

  // ================== STATE CHUNG ==================

  showUserManagement = true;
  showClassManagement = false;
  // THÊM:
  showDeviceManagement = false;

  // ================== DEVICE ==================
  devices: Device[] = [];
  filteredDevices: Device[] = [];
  loadingDevices = false;
  deviceError: string | null = null;
  deviceSearchText: string = '';

  // Modal tạo / sửa thiết bị
  showCreateDeviceModal = false;
  showEditDeviceModal = false;
  editingDeviceId: number | null = null;

  // Form tạo mới
  createDeviceCode: string = '';
  createDeviceName: string = '';
  createDeviceRoom: string = '';
  createDeviceIsActive: boolean = true;

  // Form chỉnh sửa
  editDeviceCode: string = '';
  editDeviceName: string = '';
  editDeviceRoom: string = '';
  editDeviceIsActive: boolean = true;

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
  editClassNewTeacherCode = '';


  openUserManagement() {
    this.showUserManagement = true;
    this.showClassManagement = false;
    this.showDeviceManagement = false;

    if (this.users.length === 0) {
      this.fetchUsers();
    }
  }

  openClassManagement() {
    this.showClassManagement = true;
    this.showUserManagement = false;
    this.showDeviceManagement = false;

    if (this.classes.length === 0) {
      this.fetchClasses();
    }
  }

  // NEW: mở tab thiết bị
  openDeviceManagement() {
    this.showDeviceManagement = true;
    this.showUserManagement = false;
    this.showClassManagement = false;

    if (this.devices.length === 0) {
      this.fetchDevices();
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

    // ================== DEVICE LOGIC ==================

  fetchDevices() {
    this.loadingDevices = true;
    this.deviceService.getAllDevices().subscribe({
      next: (data: Device[]) => {
        this.devices = data;
        this.filteredDevices = data;
        this.loadingDevices = false;
      },
      error: (err) => {
        console.error(err);
        this.deviceError = 'Không tải được danh sách thiết bị';
        this.loadingDevices = false;
      }
    });
  }

  searchDevices() {
    const term = (this.deviceSearchText || '').trim().toLowerCase();
    if (!term) {
      this.filteredDevices = this.devices;
      return;
    }

    this.filteredDevices = this.devices.filter(d => {
      const code = (d.deviceCode || '').toLowerCase();
      const name = (d.deviceName || '').toLowerCase();
      const room = (d.room || '').toLowerCase();
      const statusText = d.isActive ? 'hoat dong active' : 'khong hoat dong inactive';
      return (
        code.includes(term) ||
        name.includes(term) ||
        room.includes(term) ||
        statusText.includes(term)
      );
    });
  }

  // Mở modal tạo thiết bị mới
  openCreateDeviceModal() {
    this.showCreateDeviceModal = true;
    this.createDeviceCode = '';
    this.createDeviceName = '';
    this.createDeviceRoom = '';
    this.createDeviceIsActive = true;
  }

  closeCreateDeviceModal() {
    this.showCreateDeviceModal = false;
  }

  submitCreateDevice() {
    if (!this.createDeviceCode) {
      this.notify.error('Vui lòng nhập mã thiết bị (DeviceCode)');
      return;
    }

    const payload = {
      deviceCode: this.createDeviceCode.trim(),
      deviceName: this.createDeviceName.trim() || null,
      room: this.createDeviceRoom.trim() || null,
      isActive: this.createDeviceIsActive
    };

    this.deviceService.createDevice(payload).subscribe({
      next: () => {
        this.notify.success('Thêm thiết bị điểm danh thành công');
        this.closeCreateDeviceModal();
        this.fetchDevices();
      },
      error: (err) => {
        console.error(err);
        this.notify.error('Thêm thiết bị thất bại (có thể trùng DeviceCode)');
      }
    });
  }

  // Mở modal sửa thiết bị
  openEditDeviceModal(device: Device) {
    this.editingDeviceId = device.deviceId;
    this.editDeviceCode = device.deviceCode;
    this.editDeviceName = device.deviceName || '';
    this.editDeviceRoom = device.room || '';
    this.editDeviceIsActive = device.isActive;

    this.showEditDeviceModal = true;
  }

  closeEditDeviceModal() {
    this.showEditDeviceModal = false;
    this.editingDeviceId = null;
  }

  saveDeviceChanges() {
    if (!this.editingDeviceId) return;

    if (!this.editDeviceCode) {
      this.notify.error('Vui lòng nhập mã thiết bị (DeviceCode)');
      return;
    }

    const payload = {
      deviceCode: this.editDeviceCode.trim(),
      deviceName: this.editDeviceName.trim() || null,
      room: this.editDeviceRoom.trim() || null,
      isActive: this.editDeviceIsActive
    };

    this.deviceService.updateDevice(this.editingDeviceId, payload).subscribe({
      next: () => {
        this.notify.success('Cập nhật thiết bị thành công');
        this.closeEditDeviceModal();
        this.fetchDevices(); // reload list
      },
      error: (err) => {
        console.error(err);
        this.notify.error('Cập nhật thiết bị thất bại');
      }
    });
  }

  deleteDevice(deviceId: number) {
    this.openConfirm('Bạn có chắc muốn xóa thiết bị điểm danh này không?', () => {
      this.deviceService.deleteDevice(deviceId).subscribe({
        next: () => {
          this.devices = this.devices.filter(d => d.deviceId !== deviceId);
          this.filteredDevices = this.filteredDevices.filter(d => d.deviceId !== deviceId);
          this.notify.success('Xóa thiết bị thành công');
        },
        error: (err) => {
          console.error(err);
          this.notify.error('Xóa thiết bị thất bại');
        }
      });
    });
  }

  toggleDeviceActive(device: Device, event?: Event) {
    if (event) {
      event.stopPropagation();
    }
    const newIsActive = !device.isActive;

    this.deviceService.updateActive(device.deviceId, newIsActive).subscribe({
      next: () => {
        device.isActive = newIsActive;
        this.notify.success('Cập nhật trạng thái thiết bị thành công');
      },
      error: (err) => {
        console.error(err);
        this.notify.error('Không thể cập nhật trạng thái thiết bị');
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
      const teacherCode = (u.teacherCode || '').toLowerCase();
      const studentCode = (u.studentCode || '').toLowerCase();
      const userCode = (u.userCode || '').toLowerCase();

      return (
        fullName.includes(term) ||
        username.includes(term) ||
        roleName.includes(term) ||
        email.includes(term) ||
        teacherId.includes(term) ||
        studentId.includes(term) ||
        accountId.includes(term) ||
        teacherCode.includes(term) ||
        studentCode.includes(term) ||
        userCode.includes(term)
      );
    });
  }

  deleteUser(accountId: number) {
    this.openConfirm('Bạn có chắc muốn xóa tài khoản này không?', () => {
      this.userService.deleteUser(accountId).subscribe({
        next: () => {
          this.users = this.users.filter(u => u.accountId !== accountId);
          this.filteredUsers = this.filteredUsers.filter(u => u.accountId !== accountId);
          this.notify.success('Xóa người dùng thành công');
        },
        error: (err) => {
          console.error(err);
          this.notify.error('Lỗi khi xóa người dùng');
        }
      });
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
    this.openConfirm('Bạn có chắc muốn xóa lớp này không?', () => {
      this.classService.deleteClass(classId).subscribe({
        next: () => {
          this.classes = this.classes.filter(c => c.classId !== classId);
          this.notify.success('Xóa lớp học thành công');
        },
        error: (err) => {
          console.error(err);
          this.notify.error('Lỗi khi xóa lớp');
        }
      });
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
        this.editClassNewTeacherCode = '';

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
    this.editClassNewTeacherCode = '';
  }

  /**
   * Khi người dùng gõ mã giảng viên mới
   */
  handleClassTeacherLookup(value: string) {
    const code = (value || '').trim();
    this.editClassNewTeacherCode = code;

    if (!code) {
      this.editClassNewTeacherId = null;
      this.editClassNewTeacherName = '';
      return;
    }

    this.classService.getTeacherByCode(code).subscribe({
      next: (t) => {
        this.editClassNewTeacherId = t.teacherId;   // gửi teacherId cho API updateClass
        this.editClassNewTeacherName = t.fullName;  // hiển thị tên
      },
      error: () => {
        this.editClassNewTeacherId = null;
        this.editClassNewTeacherName = 'Không tìm thấy giảng viên';
      }
    });
  }

  lookupTeacherForCreate(value: string) {
    const code = (value || '').trim();
    this.createClassTeacherCode = code;

    if (!code) {
      this.createClassTeacherId = null;
      this.createClassTeacherName = '';
      return;
    }

    this.classService.getTeacherByCode(code).subscribe({
      next: (t) => {
        this.createClassTeacherId = t.teacherId;   // lưu ID
        this.createClassTeacherName = t.fullName;  // hiển thị tên
      },
      error: () => {
        this.createClassTeacherId = null;
        this.createClassTeacherName = 'Không tìm thấy giảng viên';
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
        this.notify.success('Cập nhật lớp học thành công');
      },
      error: () => {
        this.notify.error('Cập nhật lớp học thất bại');
      }
    });
  }

  //================= TẠO LỚP MỚI ==================
  createClassName = '';
  createClassCode = '';
  createClassTeacherId: number | null = null;
  createClassTeacherName = '';
  createClassTeacherCode = '';
  showCreateClassModal = false;

  openCreateClassModal() {
    this.showCreateClassModal = true;

    // reset form mỗi lần mở
    this.createClassName = '';
    this.createClassCode = '';
    this.createClassTeacherId = null;
    this.createClassTeacherName = '';
    this.createClassTeacherCode = '';
  }
  closeCreateClassModal() {
    this.showCreateClassModal = false;
  }


  submitCreateClass() {
    if (!this.createClassName || !this.createClassCode) {
      this.notify.error('Vui lòng nhập tên lớp và mã lớp');
      return;
    }

    const payload: any = {
      className: this.createClassName,
      classCode: this.createClassCode,
      teacherId: this.createClassTeacherId
    };

    this.classService.createClass(payload).subscribe({
      next: () => {
        this.fetchClasses();
        this.closeCreateClassModal();
        this.notify.success('Tạo lớp học thành công');
      },
      error: () => {
        this.notify.error('Tạo lớp học thất bại (có thể mã lớp đã tồn tại)');
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
  manualStudentId: number | null = null;
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


  // khi gõ mã SV (có thể là studentId hoặc studentCode)
  onManualStudentCodeChange(value: string) {
    const code = (value || '').trim();
    this.manualStudentCode = code;

    // Reset khi xóa input
    if (!code) {
      this.manualStudentName = '';
      this.manualStudentId = null;
      this.canAddManualStudent = false;
      return;
    }

    // ✅ LUÔN gọi theo MSSV (studentCode)
    this.classService.getStudentByCode(code).subscribe({
      next: (stu: any) => {
        console.log('Tìm được SV theo MSSV:', stu);
        this.manualStudentName = stu.fullName;
        this.manualStudentId = stu.studentId;   // dùng để add vào lớp
        this.canAddManualStudent = true;
      },
      error: (err) => {
        console.error('Lỗi khi gọi API tìm SV theo MSSV:', err);
        this.manualStudentName = '';
        this.manualStudentId = null;
        this.canAddManualStudent = false;
      }
    });
  }




  // bấm nút +
  addStudentToPendingList() {
    if (!this.canAddManualStudent) return;
    if (this.manualStudentId == null) return;

    const id = this.manualStudentId;

    const exists = this.pendingStudentsToAdd.some(s => s.studentId === id);
    if (exists) return;

    this.pendingStudentsToAdd.push({
      studentId: id,
      studentCode: this.manualStudentCode,       // để hiển thị MSSV trong bảng
      fullName: this.manualStudentName,
      username: this.manualStudentUsername
    });

    // reset
    this.manualStudentCode = '';
    this.manualStudentName = '';
    this.manualStudentUsername = '';
    this.manualStudentId = null;
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
          this.notify.success('Đã thêm sinh viên vào lớp');
          this.closeAddStudentModal();
          this.fetchClasses();
        },
        error: () => {
          this.notify.error('Thêm sinh viên thủ công thất bại');
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
          this.notify.success(`Import CSV thành công: ${res?.imported ?? 0} SV, lỗi: ${res?.rejectedCount ?? 0}`);
          this.closeAddStudentModal();
          this.fetchClasses();
        },
        error: (err) => {
          console.error(err);
          this.notify.error('Import CSV thất bại');
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
      this.notify.error('Không xác định được lớp');
      return;
    }

    const classId = this.selectedClassForStudentModal.classId;

    this.openConfirm('Bạn có chắc muốn xóa sinh viên này khỏi lớp không?', () => {
      this.classService.removeStudentFromClass(classId, studentId).subscribe({
        next: () => {
          this.studentsOfSelectedClass = this.studentsOfSelectedClass
            .filter(s => s.studentId !== studentId);

          const cls = this.classes.find(c => c.classId === classId);
          if (cls && cls.studentCount > 0) {
            cls.studentCount = cls.studentCount - 1;
          }

          this.notify.success('Đã xóa sinh viên khỏi lớp');
        },
        error: (err) => {
          console.error(err);
          this.notify.error('Xóa sinh viên khỏi lớp thất bại');
        }
      });
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
  ngOnInit(): void {
    this.openUserManagement();
  }


  // ========== LỊCH HỌC CỦA LỚP ==========
  showClassScheduleModal = false;
  showCreateScheduleModal = false;
  showEditScheduleModal = false;

  selectedClassForSchedule: ClassList | null = null;
  classScheduleItems: ClassScheduleItem[] = [];

  editingScheduleId: number | null = null;

  // form thêm / sửa lịch học
  scheduleFormDate: string = '';       // '2025-11-27'
  scheduleFormStartTime: string = '';  // '07:00'
  scheduleFormEndTime: string = '';    // '09:00'
  scheduleFormRoom: string = '';
  scheduleFormIsActive: boolean = true;

  /** Mở modal lịch học khi bấm nút "Lịch học" ở bảng lớp */
  openClassScheduleModal(cls: ClassList) {
    this.selectedClassForSchedule = cls;
    this.showClassScheduleModal = true;

    this.loadSchedulesForClass(cls.classId);
  }

  /** Đóng modal lịch học */
  closeClassScheduleModal() {
    this.showClassScheduleModal = false;
    this.selectedClassForSchedule = null;
    this.classScheduleItems = [];
  }

  /** Gọi service lấy danh sách lịch học của 1 lớp */
  private loadSchedulesForClass(classId: number) {
    this.classService.getSchedulesByClassId(classId).subscribe({
      next: (data: ClassScheduleItem[]) => {
        this.classScheduleItems = data;
      },
      error: (err) => {
        console.error(err);
        alert('Không tải được lịch học của lớp');
      }
    });
  }

  /** Mở modal thêm lịch học */
  openCreateScheduleModal() {
    if (!this.selectedClassForSchedule) {
      alert('Không xác định được lớp để thêm lịch học');
      return;
    }

    this.showCreateScheduleModal = true;
    this.showEditScheduleModal = false;
    this.editingScheduleId = null;

    // reset form
    this.scheduleFormDate = '';
    this.scheduleFormStartTime = '';
    this.scheduleFormEndTime = '';
    this.scheduleFormRoom = '';
    this.scheduleFormIsActive = true;
  }

  /** Mở modal sửa lịch học */
  openEditScheduleModal(item: ClassScheduleItem) {
    this.editingScheduleId = item.scheduleId;
    this.showEditScheduleModal = true;
    this.showCreateScheduleModal = false;

    this.scheduleFormDate = item.scheduleDate;
    this.scheduleFormStartTime = item.startTime;
    this.scheduleFormEndTime = item.endTime;
    this.scheduleFormRoom = item.room || '';
    this.scheduleFormIsActive = item.isActive;
  }

  /** Đóng modal form thêm / sửa lịch học */
  closeScheduleFormModal() {
    this.showCreateScheduleModal = false;
    this.showEditScheduleModal = false;
    this.editingScheduleId = null;
  }

  /** Lưu form: nếu có editingScheduleId thì UPDATE, không thì CREATE */
  saveScheduleForm() {
    if (!this.selectedClassForSchedule) {
      alert('Không xác định được lớp');
      return;
    }

    if (!this.scheduleFormDate || !this.scheduleFormStartTime || !this.scheduleFormEndTime) {
      alert('Vui lòng nhập đầy đủ ngày và giờ học');
      return;
    }

    // kiểm tra giờ bắt đầu < giờ kết thúc (nếu parse được)
    const start = new Date(`${this.scheduleFormDate}T${this.scheduleFormStartTime}`);
    const end = new Date(`${this.scheduleFormDate}T${this.scheduleFormEndTime}`);
    if (!isNaN(start.getTime()) && !isNaN(end.getTime()) && end <= start) {
      alert('Giờ kết thúc phải sau giờ bắt đầu');
      return;
    }

    const payload = {
      classId: this.selectedClassForSchedule.classId,
      scheduleDate: this.scheduleFormDate,
      startTime: this.scheduleFormStartTime,
      endTime: this.scheduleFormEndTime,
      room: this.scheduleFormRoom,
      isActive: this.scheduleFormIsActive
    };

    // EDIT
    if (this.editingScheduleId) {
      this.classService.updateSchedule(this.editingScheduleId, payload).subscribe({
        next: () => {
          alert('Cập nhật lịch học thành công');
          this.loadSchedulesForClass(this.selectedClassForSchedule!.classId);
          this.closeScheduleFormModal();
        },
        error: (err) => {
          console.error(err);
          alert('Cập nhật lịch học thất bại');
        }
      });
      return;
    }

    // CREATE
    this.classService.createSchedule(payload).subscribe({
      next: () => {
        alert('Thêm lịch học thành công');
        this.loadSchedulesForClass(this.selectedClassForSchedule!.classId);
        this.closeScheduleFormModal();
      },
      error: (err) => {
        console.error(err);
        alert('Thêm lịch học thất bại');
      }
    });
  }


  /** Tick / bỏ tick tạm hoãn: cập nhật IsActive trong ClassSchedule */
  onToggleScheduleActive(item: ClassScheduleItem, event: Event) {
    const input = event.target as HTMLInputElement;
    const isChecked = input.checked;

    const newIsActive = !isChecked; // tick = tạm hoãn => isActive = false

    this.classService.updateScheduleActive(item.scheduleId, newIsActive).subscribe({
      next: () => {
        item.isActive = newIsActive;
      },
      error: (err) => {
        console.error(err);
        alert('Không thể cập nhật trạng thái tạm hoãn');
        // rollback checkbox
        input.checked = !isChecked;
      }
    });
  }
  /** Text trạng thái lớp theo thời gian + IsActive */
  getScheduleStatusLabel(item: ClassScheduleItem): string {
    if (!item.isActive) {
      return 'Tạm hoãn';
    }

    const now = new Date();
    const start = new Date(`${item.scheduleDate}T${item.startTime}`);
    const end = new Date(`${item.scheduleDate}T${item.endTime}`);

    if (isNaN(start.getTime()) || isNaN(end.getTime())) {
      return 'Không xác định';
    }

    if (now < start) {
      return 'Sắp diễn ra';
    }

    if (now > end) {
      return 'Đã kết thúc';
    }

    return 'Đang diễn ra';
  }

  /** CSS class để đổi màu badge */
  getScheduleStatusClass(item: ClassScheduleItem): string {
    if (!item.isActive) {
      return 'status-inactive';
    }

    const now = new Date();
    const start = new Date(`${item.scheduleDate}T${item.startTime}`);
    const end = new Date(`${item.scheduleDate}T${item.endTime}`);

    if (isNaN(start.getTime()) || isNaN(end.getTime())) {
      return '';
    }

    if (now < start) {
      return 'status-upcoming';
    }

    if (now > end) {
      return 'status-finished';
    }

    return 'status-running';
  }
  deleteSchedule(scheduleId: number) {
    if (!confirm('Bạn có chắc muốn xóa lịch học này không?')) return;

    this.classService.deleteSchedule(scheduleId).subscribe({
      next: () => {
        this.classScheduleItems = this.classScheduleItems
          .filter(s => s.scheduleId !== scheduleId);
      },
      error: (err) => {
        console.error(err);
        alert('Xóa lịch học thất bại');
      }
    });
  }
  // ================== CONFIRM DIALOG (XÁC NHẬN XÓA) ==================
  confirmVisible = false;
  confirmMessage = '';
  private confirmCallback: (() => void) | null = null;

  openConfirm(message: string, onConfirm: () => void) {
    this.confirmMessage = message;
    this.confirmCallback = onConfirm;
    this.confirmVisible = true;
  }

  closeConfirm() {
    this.confirmVisible = false;
    this.confirmMessage = '';
    this.confirmCallback = null;
  }

  confirmYes() {
    if (this.confirmCallback) {
      this.confirmCallback();
    }
    this.closeConfirm();
  }
  logout() {
    this.router.navigate(['/login']);
  }

}
