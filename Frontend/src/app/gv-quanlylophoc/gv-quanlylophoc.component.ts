import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule, NgModel } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { Router } from '@angular/router';

@Component({
  selector: 'app-gv-quanlylophoc',
  imports: [CommonModule, FormsModule],
  templateUrl: './gv-quanlylophoc.component.html',
  styleUrl: './gv-quanlylophoc.component.scss'
})
export class GvQuanlylophocComponent {

  constructor(private router: Router) { }

  /*modal thêm lớp học*/
  showAddClassModal = false;
  openAddClassModal() {
    this.showAddClassModal = true;
  }
  closeAddClassModal() {
    this.showAddClassModal = false;
  }

  /*modal chi tiết lớp học*/
  showClassDetailModal = false;
  selectedClass: any = null;
  openClassDeatilModal(lop: any) {
    this.selectedClass = lop;
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
  }

  /*modal xóa lớp học*/
  showDeleteModal = false;
  openDeleteModal() {
    this.showDeleteModal = true;
  }
  closeDeleteModal() {
    this.showDeleteModal = false;
  }


  lophoc = [
    { id: 1, tenLop: 'Lập trình Web', classCode: 'IT301', soHocSinh: 45, ngayTao: '15/08/2024', trangThai: 'Hoạt động' },
    { id: 2, tenLop: 'Cơ sở dữ liệu', classCode: 'IT302', soHocSinh: 38, ngayTao: '20/08/2024', trangThai: 'Hoạt động' },
    { id: 3, tenLop: 'Thuật toán', classCode: 'IT303', soHocSinh: 42, ngayTao: '25/08/2024', trangThai: 'Tạm dừng' },
    { id: 4, tenLop: 'Hệ thống thông tin', classCode: 'IT304', soHocSinh: 35, ngayTao: '01/09/2024', trangThai: 'Hoạt động' },
    { id: 5, tenLop: 'Trí tuệ nhân tạo', classCode: 'IT305', soHocSinh: 28, ngayTao: '05/09/2024', trangThai: 'Hoạt động' },
    { id: 6, tenLop: 'Mạng máy tính', classCode: 'IT306', soHocSinh: 40, ngayTao: '10/09/2024', trangThai: 'Hoạt động' },
    { id: 7, tenLop: 'Phân tích dữ liệu', classCode: 'IT307', soHocSinh: 33, ngayTao: '12/09/2024', trangThai: 'Tạm dừng' },
    { id: 8, tenLop: 'Nguyên lý hệ điều hành', classCode: 'IT308', soHocSinh: 47, ngayTao: '15/09/2024', trangThai: 'Hoạt động' },
    { id: 9, tenLop: 'Kỹ thuật lập trình', classCode: 'IT309', soHocSinh: 50, ngayTao: '18/09/2024', trangThai: 'Hoạt động' },
    { id: 10, tenLop: 'Nhập môn Công nghệ thông tin', classCode: 'IT310', soHocSinh: 60, ngayTao: '20/09/2024', trangThai: 'Hoạt động' },
    { id: 11, tenLop: 'An toàn thông tin', classCode: 'IT311', soHocSinh: 27, ngayTao: '25/09/2024', trangThai: 'Tạm dừng' },
    { id: 12, tenLop: 'Thiết kế giao diện', classCode: 'IT312', soHocSinh: 32, ngayTao: '28/09/2024', trangThai: 'Hoạt động' },
    { id: 13, tenLop: 'Cấu trúc dữ liệu', classCode: 'IT313', soHocSinh: 41, ngayTao: '01/10/2024', trangThai: 'Hoạt động' },
    { id: 14, tenLop: 'Phát triển phần mềm', classCode: 'IT314', soHocSinh: 37, ngayTao: '03/10/2024', trangThai: 'Hoạt động' },
    { id: 15, tenLop: 'Lập trình di động', classCode: 'IT315', soHocSinh: 36, ngayTao: '05/10/2024', trangThai: 'Tạm dừng' },
    { id: 16, tenLop: 'Lập trình Java', classCode: 'IT316', soHocSinh: 44, ngayTao: '07/10/2024', trangThai: 'Hoạt động' },
    { id: 17, tenLop: 'Công nghệ phần mềm', classCode: 'IT317', soHocSinh: 39, ngayTao: '10/10/2024', trangThai: 'Hoạt động' },
    { id: 18, tenLop: 'Kiểm thử phần mềm', classCode: 'IT318', soHocSinh: 31, ngayTao: '12/10/2024', trangThai: 'Tạm dừng' },
    { id: 19, tenLop: 'Phân tích yêu cầu hệ thống', classCode: 'IT319', soHocSinh: 30, ngayTao: '14/10/2024', trangThai: 'Hoạt động' },
    { id: 20, tenLop: 'Học máy cơ bản', classCode: 'IT320', soHocSinh: 29, ngayTao: '16/10/2024', trangThai: 'Hoạt động' },
    { id: 21, tenLop: 'Trí tuệ nhân tạo nâng cao', classCode: 'IT321', soHocSinh: 22, ngayTao: '18/10/2024', trangThai: 'Tạm dừng' },
    { id: 22, tenLop: 'Quản trị dự án CNTT', classCode: 'IT322', soHocSinh: 26, ngayTao: '20/10/2024', trangThai: 'Hoạt động' },
    { id: 23, tenLop: 'Khai phá dữ liệu', classCode: 'IT323', soHocSinh: 34, ngayTao: '22/10/2024', trangThai: 'Hoạt động' },
    { id: 24, tenLop: 'Đồ án tốt nghiệp', classCode: 'IT324', soHocSinh: 18, ngayTao: '25/10/2024', trangThai: 'Hoạt động' },
    { id: 25, tenLop: 'Nhập môn Python', classCode: 'IT325', soHocSinh: 55, ngayTao: '28/10/2024', trangThai: 'Hoạt động' }
  ];

  /*thay dổi trang thái lớp học trong modal chi tiết*/
  toggleClassStatusInModal() {
    if (this.selectedClass.trangThai === 'Hoạt động') {
      this.selectedClass.trangThai = 'Tạm dừng';
    } else {
      this.selectedClass.trangThai = 'Hoạt động';
    }
  }


  /*sắp xếp tên lớp học*/
  sortAscending: boolean = true; // Biến để lưu trạng thái sắp xếp
  sortByName() {
    this.lophoc.sort((a, b) => {
      const nameA = a.tenLop.toLowerCase();
      const nameB = b.tenLop.toLowerCase();

      if (nameA < nameB) return this.sortAscending ? -1 : 1;
      if (nameA > nameB) return this.sortAscending ? 1 : -1;
      return 0;
    });
    // Đổi hướng sắp xếp (tăng ↔ giảm)
    this.sortAscending = !this.sortAscending;
  }

  /*chức năng tìm kiếm*/
  filteredClassList = [...this.lophoc];
  searchText: string = '';
  searchClasses() {
    const search = this.searchText.trim().toLowerCase();

    if (!search) {
      this.filteredClassList = [...this.lophoc]; // nếu rỗng → hiển thị toàn bộ
      return;
    }

    this.filteredClassList = this.lophoc.filter(lop =>
      lop.tenLop.toLowerCase().includes(search) ||
      lop.classCode.toLowerCase().includes(search)
    );
  }

}
