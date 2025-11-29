import { Component } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { SidebarComponent } from "./sidebar/sidebar.component";
import { CommonModule } from '@angular/common';
import { ToastComponent } from "./toast/toast.component";
import { SvSidebarComponent } from "./sv-sidebar/sv-sidebar.component";
import { AuthService } from './services/auth.service';
import { filter } from 'rxjs';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, SidebarComponent, SvSidebarComponent, CommonModule, ToastComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'Frontend';

  isLogInPage = false;
  isSlideshow = false;
  isAdminPage = false;

  // lấy roleName từ Account: "Admin" | "Học sinh" | "Giảng viên"
  currentRoleName: string | null = null;

  constructor(
    private router: Router,
    private authService: AuthService
  ) {

    // Lắng nghe router để ẩn sidebar ở login / slideshow / admin
    this.router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe((event: any) => {
        const url = event.url as string;
        this.isLogInPage = url === '/login';
        this.isSlideshow = url === '/slideshow';
        this.isAdminPage = url === '/admin';
      });

    // Lắng nghe thông tin user hiện tại từ AuthService
    this.authService.currentUser$.subscribe(account => {
      this.currentRoleName = account?.roleName ?? null;
      // console.log('currentRoleName = ', this.currentRoleName);
    });
  }
}
