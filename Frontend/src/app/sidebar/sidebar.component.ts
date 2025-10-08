import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  imports: [CommonModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  activeRoute: string = ''; // lưu trữ route đang hiển thị
  titleService: string = '';
  constructor(private router: Router) {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.activeRoute = event.urlAfterRedirects; // cập nhật route đang hiển thị
      }
      if (event instanceof NavigationEnd) {
        if (event.url === '/gv_trangchu') {
          this.titleService = "Trang chủ";
        } else if (event.url === '/gv_quanlylophoc') {
          this.titleService = "Quản lý lớp học";
        } else if (event.url === '/gv_quanlydiemso') {
          this.titleService = "Quản lý điểm số";
        } else if (event.url === '/gv_quanlydiemdanh') {
          this.titleService = "Quản lý điểm danh";
        } else if (event.url === '/chatbot') {
          this.titleService = "Chat Bot";
        } else {
          this.titleService = "";    
        }
      }
    });

  }


  navigateToDashboard() {
    this.router.navigate(['/gv_trangchu']);
  }
  navigateToClassManagement() {
    this.router.navigate(['/gv_quanlylophoc']);
  }
  navigateToScoreManagement() {
    this.router.navigate(['/gv_quanlydiemso']);
  }
  navigateToAttendance() {
    this.router.navigate(['/gv_quanlydiemdanh']);
  }
  navigateToChatBot() {
    this.router.navigate(['/chatbot']);
  }
  logout() {
    this.router.navigate(['/login']);
  }
  isActive(route: string): boolean {
    return this.activeRoute === route;
  }
}