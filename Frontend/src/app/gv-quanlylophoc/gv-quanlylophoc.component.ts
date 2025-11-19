import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TeacherClassService } from '../services/teacher-class.service';

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
    private teacherClassService: TeacherClassService
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
      alert('Vui lòng nhập đủ thông tin bắt buộc!');
      return;
    }

    // TODO: gọi API tạo lớp (nếu bạn muốn)
    this.openNotificationModal();
    this.closeAddClassModal();

    this.newClass = {
      className: '',
      classCode: '',
      description: '',
      maxStudents: null
    };
  }

  /*modal chi tiết lớp học*/
  showClassDetailModal = false;
  selectedClass: any = null;
  openClassDeatilModal(lop: any) {
    this.selectedClass = { ...lop }; // clone nhỏ cho chắc
    this.showClassDetailModal = true;
  }
  closeClassDetailModal() {
    this.showClassDetailModal = false;
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
        alert('Không tải được danh sách lớp học');
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
      soSvCoVanTay: dto.fingerprintedCount
    };
  }

  toggleClassStatus(lop: any) {
  const newStatus = !lop.status; // đảo trạng thái boolean

  this.teacherClassService.updateClassStatus(lop.id, newStatus).subscribe({
    next: () => {
      // cập nhật UI
      lop.status = newStatus;
      lop.trangThai = newStatus ? 'Tạm dừng' : 'Hoạt động';
    },
    error: (err) => {
      console.error('Update class status error', err);
      alert('Không thể thay đổi trạng thái lớp');
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
      },
      error: (err) => {
        console.error('Error exportMyClasses', err);
        alert('Xuất dữ liệu thất bại');
      }
    });
  }
}
