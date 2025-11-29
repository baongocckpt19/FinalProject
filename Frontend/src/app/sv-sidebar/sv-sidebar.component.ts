import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-sv-sidebar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sv-sidebar.component.html',
  styleUrl: './sv-sidebar.component.scss'
})
export class SvSidebarComponent {
  activeRoute: string = '';
  titleService: string = '';
currentFullName: string | null = null;
  constructor(private router: Router, private authService: AuthService) {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.activeRoute = event.urlAfterRedirects;

        if (event.url === '/sv-trangchu') {
          this.titleService = 'Trang chủ sinh viên';
        } else if (event.url === '/sv-lichhoc') {
          this.titleService = 'Lịch học';
        } else if (event.url === '/sv-diemso') {
          this.titleService = 'Kết quả học tập';
        } else {
          this.titleService = '';
        }
      }

     
    });
     this.authService.currentUser$.subscribe(account => {
      this.currentFullName = account?.fullName ?? null;
      console.log('sidebar - currentRoleName = ', account);
    });
  }

  navigateToStudentDashboard() {
    this.router.navigate(['/sv-trangchu']);
  }

  navigateToStudentSchedule() {
    this.router.navigate(['/sv-lichhoc']);
  }

  navigateToStudentResult() {
    this.router.navigate(['/sv-diemso']);
  }

  isActive(route: string): boolean {
    return this.activeRoute === route;
  }

  navigatetoPersonalPage() {
    this.router.navigate(['/trangcanhan']);
  }

  logout() {
    this.router.navigate(['/login']);
  }
}
