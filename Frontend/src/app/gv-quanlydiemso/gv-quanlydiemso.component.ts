import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule, NgModel } from '@angular/forms';

@Component({
  selector: 'app-gv-quanlydiemso',
  imports: [CommonModule, FormsModule],
  templateUrl: './gv-quanlydiemso.component.html',
  styleUrl: './gv-quanlydiemso.component.scss'
})
export class GvQuanlydiemsoComponent {

  ///modal nhập điểm từ file
  showFileImportModal = false;
  openFileImportModal() {
    this.showFileImportModal = true;
  }
  closeFileImportModal() {
    this.showFileImportModal = false;
  }
  //thêm file điểm từ modal
  newFile = {
    gradeFile: null
  };
  addFile() {
    const { gradeFile } = this.newFile;
    if (gradeFile == null) {
      alert('Vui lòng chọn file điểm!');
      return;
    }
    // Xử lý logic thêm file điểm
    this.openNotificationModal();
    this.closeFileImportModal();
    this.newFile.gradeFile = null; // Reset form nếu cần
  }




  ///modal nhập điểm hàng loạt
  showBulkGradeInputModal = false;
  openBulkGradeInputModal() {
    this.showBulkGradeInputModal = true;
  }
  closeBulkGradeInputModal() {
    this.showBulkGradeInputModal = false;
  }
  //hàm xử lý nhập điểm hàng loạt
  newBulkGrades = {
    grades: ''
  };
  processBulkGrades() {
    const { grades } = this.newBulkGrades;
    if (!grades) {
      alert('Vui lòng nhập điểm!');
      return;
    }
    // Xử lý logic nhập điểm hàng loạt
    this.openNotificationModal();
    this.closeBulkGradeInputModal();
  }




  ///modal thông báo
  showNotificationModal = false;
  openNotificationModal() {
    this.showNotificationModal = true;
  }
  closeNotificationModal() {
    this.showNotificationModal = false;
  }


 sv = [
  { id: 1, ho: "Nguyễn Văn", ten: "A", mssv: "2021001", diemChuyenCan: 8.2, diemGiuaKy: 8.5, diemCuoiKy: 7.8, diemTrungbinh: 0, xepLoai: "" },
  { id: 2, ho: "Trần Thị", ten: "B", mssv: "2021002", diemChuyenCan: 8.9, diemGiuaKy: 9.0, diemCuoiKy: 8.6, diemTrungbinh: 0, xepLoai: "" },
 
];

  //tính điểm trung bình và xêp loại
  diemTB() {
    this.sv.forEach(sinhVien => {
      sinhVien.diemTrungbinh = (sinhVien.diemChuyenCan * 0.2) + (sinhVien.diemGiuaKy * 0.3) + (sinhVien.diemCuoiKy * 0.5);
      if (sinhVien.diemTrungbinh >= 9) sinhVien.xepLoai = "Xuất sắc";
      else if (sinhVien.diemTrungbinh >= 8) sinhVien.xepLoai = "Giỏi";
      else if (sinhVien.diemTrungbinh >= 7) sinhVien.xepLoai = "Khá";
      else if (sinhVien.diemTrungbinh >= 5) sinhVien.xepLoai = "Trung bình";
      else sinhVien.xepLoai = "Yếu";
    });
    this.updateGradeDistribution(); // Cập nhật biểu đồ phân bố điểm sau khi tính điểm trung bình
    this.updateAttentionList(); // Cập nhật danh sách sinh viên cần chú ý sau khi tính điểm trung bình

  }
  ngOnInit() {
    this.diemTB();
    this.sortByName(); // Sắp xếp danh sách sinh viên theo tên khi khởi tạo
  }
  //lưu điểm sinh viên
  saveStudentGrade(sinhVien: any) {
    this.openNotificationModal();
    this.diemTB(); // Cập nhật lại điểm trung bình và xếp loại sau khi lưu
  }


  //modal chi tiết sinh viên
  showStudentDetailModal = false;
  selectedStudent: any = null;
  openStudentDetailModal(sv: any) {
    this.selectedStudent = sv;
    this.showStudentDetailModal = true;
  }
  closeStudentDetailModal() {
    this.showStudentDetailModal = false;
  }

  //ẩn hiện điểm
  isGradeVisible = true;
  toggleGradeVisibility() {
    this.isGradeVisible = !this.isGradeVisible;
  }

  //lưu tất cả điểm
  saveAllGrades() {
    this.openNotificationModal();
    this.diemTB(); // Cập nhật lại điểm trung bình và xếp loại sau khi lưu
  }

  
  //tìm kiếm sinh viên
  searchText: string = '';
  sinhvienFiltered=[...this.sv];  // clone mảng gốc để lọc vì nếu không sẽ mất dữ liệu ở mang gốc khi tìm kiếm
  searchStudent() {
    const searchInput = this.searchText.toLowerCase();
    // Lọc danh sách sinh viên dựa trên tên hoặc MSSV
    this.sv = this.sinhvienFiltered.filter(sinhVien =>
      sinhVien.ho.toLowerCase().includes(searchInput) ||
      sinhVien.mssv.toLowerCase().includes(searchInput) ||
      sinhVien.ten.toLowerCase().includes(searchInput)
    );
    if (!searchInput) {
      this.ngOnInit(); // Nếu ô tìm kiếm trống, hiển thị lại danh sách đầy đủ
    } 
    this.sortByName(); // Sắp xếp lại danh sách sau khi tìm kiếm
  }

  //sắp xếp theo tên
  sortByName() {
    this.sv.sort((a, b) =>  a.ten.localeCompare(b.ten, 'vi', { sensitivity: 'base' }) );
  }

  //sắp xếp các cột trong bảng
  // Biến lưu trạng thái sắp xếp
currentSortColumn: string = '';
isAscending: boolean = true;

// Hàm sắp xếp chung
sortBy(column: string) {
  if (this.currentSortColumn === column) {
    // Nếu nhấn lại cùng cột thì đảo ngược thứ tự
    this.isAscending = !this.isAscending;
  } else {
    // Nếu là cột khác thì mặc định tăng dần
    this.currentSortColumn = column;
    this.isAscending = true;
  }

  this.sv.sort((a: any, b: any) => {
    let valA = a[column];
    let valB = b[column];

    // Với cột "ten" hoặc "xepLoai" thì sắp xếp theo chữ
    if (typeof valA === 'string') {
      return this.isAscending
        ? valA.localeCompare(valB, 'vi', { sensitivity: 'base' })
        : valB.localeCompare(valA, 'vi', { sensitivity: 'base' });
    }

    // Với cột điểm thì sắp xếp theo số
    if (typeof valA === 'number') {
      return this.isAscending ? valA - valB : valB - valA;
    }

    return 0;
  });
}
 
//CẬP NHẬT CHART
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

    return {
      range: `${r.min}-${r.max}`,
      count
    };
  });
}


//DANH SÁCH SINH VIÊN CẦN CHÚ Ý
attentionGradeType: string = 'average'; // Loại điểm đang chọn
attentionStudents: any[] = [];          // Danh sách sinh viên cần chú ý

// Lấy danh sách sinh viên cần chú ý theo loại điểm
updateAttentionList() {
  const type = this.attentionGradeType;

  this.attentionStudents = this.sv
    .filter(sv => {
      if (type === 'attendance') return sv.diemChuyenCan < 4;
      if (type === 'midterm') return sv.diemGiuaKy < 4;
      if (type === 'final') return sv.diemCuoiKy < 4;
      return sv.diemTrungbinh < 4; // average
    })
    .map(sv => ({
      id: sv.id,
      name: `${sv.ho} ${sv.ten}`,
      mssv: sv.mssv,
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

// Hàm lấy tên hiển thị của loại điểm
getGradeTypeName(type: string): string {
  switch (type) {
    case 'attendance': return 'điểm chuyên cần';
    case 'midterm': return 'điểm giữa kỳ';
    case 'final': return 'điểm cuối kỳ';
    default: return 'điểm trung bình';
  }
}

// Gọi khi thay đổi select
onGradeTypeChange(event: any) {
  this.attentionGradeType = event.target.value;
  this.updateAttentionList();
}

// Mở chi tiết sinh viên (dùng modal có sẵn)
viewStudentDetail(id: number) {
  const sv = this.sv.find(s => s.id === id);
  if (sv) this.openStudentDetailModal(sv);
}

}
