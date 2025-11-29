// src/app/sv-diemso/sv-diemso.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GradeService, StudentClassGrade } from '../services/grade.service';

@Component({
  selector: 'app-sv-diemso',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './sv-diemso.component.html',
  styleUrl: './sv-diemso.component.scss'
})
export class SvDiemsoComponent implements OnInit {

  allClasses: StudentClassGrade[] = [];
  filteredClasses: StudentClassGrade[] = [];

  currentFilter: 'all' | 'in-progress' | 'completed' = 'all';
  searchKeyword: string = '';

  // phân trang
  pageSize = 6;          // tuỳ bạn, 6 card / trang
  currentPage = 1;
  totalPages = 1;

  loading = false;
  error: string | null = null;

  constructor(private gradeService: GradeService) { }

  ngOnInit(): void {
    this.loadData();
  }

  loadData() {
    this.loading = true;
    this.error = null;

    this.gradeService.getStudentClassesWithGrades().subscribe({
      next: (res) => {
        this.allClasses = res || [];
        this.applyFilterAndSearch();
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.error = 'Không tải được dữ liệu điểm.';
        this.loading = false;
      }
    });
  }

  // lọc theo trạng thái
  setFilter(filter: 'all' | 'in-progress' | 'completed') {
    this.currentFilter = filter;
    this.currentPage = 1;
    this.applyFilterAndSearch();
  }

  onSearchChange() {
    this.currentPage = 1;
    this.applyFilterAndSearch();
  }

  private applyFilterAndSearch() {
    let data = [...this.allClasses];

    // trạng thái: DB: 0 = đang hoạt động → status=false
    if (this.currentFilter === 'in-progress') {
      data = data.filter(c => c.status === false);
    } else if (this.currentFilter === 'completed') {
      data = data.filter(c => c.status === true);
    }

    if (this.searchKeyword.trim()) {
      const kw = this.searchKeyword.trim().toLowerCase();
      data = data.filter(c =>
        c.className.toLowerCase().includes(kw) ||
        c.classCode.toLowerCase().includes(kw)
      );
    }

    this.filteredClasses = data;
    this.totalPages = Math.max(1, Math.ceil(this.filteredClasses.length / this.pageSize));
    if (this.currentPage > this.totalPages) {
      this.currentPage = this.totalPages;
    }
  }

  get pagedClasses(): StudentClassGrade[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredClasses.slice(start, start + this.pageSize);
  }

  changePage(offset: number) {
    const newPage = this.currentPage + offset;
    if (newPage < 1 || newPage > this.totalPages) return;
    this.currentPage = newPage;
  }

  // hiển thị điểm với format "--" nếu null
  displayGrade(g: number | null | undefined): string {
    if (g === null || g === undefined) return '--';
    return g.toFixed(1);
  }

  // text trạng thái lớp
  getStatusText(c: StudentClassGrade): string {
    return c.status ? 'Đã hoàn thành' : 'Đang học';
  }

  // class css cho badge
  getStatusCss(c: StudentClassGrade): string {
    return c.status ? 'completed' : 'in-progress';
  }
}
