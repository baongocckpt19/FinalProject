import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-sidebar',
  imports: [CommonModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  activeRoute: string = ''; // lưu trữ route đang hiển thị
  titleService: string = '';
  currentFullName: string | null = null;
  constructor(private router: Router, private authService: AuthService) {
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
        } else if (event.url === '/gv_lichday') {
          this.titleService = "Lịch giảng dạy";
        } else if (event.url === '/chatbot') {
          this.titleService = "Chat Bot";
        }  
        else if (event.url === '/gv-quanlyvantay') {
          this.titleService = "Thêm vân tay";
        }
         else {
          this.titleService = "";    
        }
      }
    });
    this.authService.currentUser$.subscribe(account => {
      this.currentFullName = account?.fullName ?? null;
      console.log('sidebar - currentRoleName = ', account);
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
  navigateToSchedule() {
    this.router.navigate(['/gv_lichday']);
  }
  navigateToChatBot() {
    this.router.navigate(['/chatbot']);
  }
  logout() {
    this.router.navigate(['/login']);
  }
  
  navigateToFingerprint() {
    this.router.navigate(['/gv-quanlyvantay']);
  }
  isActive(route: string): boolean {
    return this.activeRoute === route;
  }
  navigatetoPersonalPage() {
    this.router.navigate(['/trangcanhan']);
  }
  
}