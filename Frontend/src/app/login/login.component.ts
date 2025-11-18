import { Component, Renderer2, ElementRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule, HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true, // 👈 thêm để component tự hoạt động được
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {

  // === Thuộc tính cho Data Binding (Binding Models) ===
  // Đăng nhập
  username = '';
  password = '';
  rememberMe = false;
  // Đăng ký
  userRole = 'student';
  fullName = '';
  registerEmail = '';
  registerPassword = '';
  confirmPassword = '';
  agreeTerms = false;

  // Trạng thái Form
  isSignupMode = false;

  // Thông báo
  successMessage: string | null = null;
  errorMessage: string | null = null;


  constructor(
    private router: Router,
    private renderer: Renderer2,
    private el: ElementRef,
    private authService: AuthService // 👈 inject AuthService
  ) { }

  ngOnInit(): void {
    // Không cần listener — sử dụng (ngSubmit) trong template
  }

  // 🔹 Ẩn/hiện mật khẩu
  togglePassword(inputId: string): void {
    const input = this.el.nativeElement.querySelector(`#${inputId}`) as HTMLInputElement;
    if (input) {
      input.type = input.type === 'password' ? 'text' : 'password';
    }
  }

  // 🔹 Chuyển giữa đăng nhập và đăng ký
  switchToSignup(): void {
    this.isSignupMode = true;
    const container = this.el.nativeElement.querySelector('#formContainer');
    this.renderer.addClass(container, 'show-signup');
    this.clearMessages();
  }

  switchToSignin(): void {
    this.isSignupMode = false;
    const container = this.el.nativeElement.querySelector('#formContainer');
    this.renderer.removeClass(container, 'show-signup');
    this.clearMessages();
  }

  // 🔹 Xử lý đăng nhập 
  handleLogin(): void {
    this.clearMessages();

    if (!this.username || !this.password) {
      this.showError('Vui lòng nhập đầy đủ thông tin!');
      return;
    }

    // 👇 gọi API qua AuthService
    this.authService.login(this.username, this.password).subscribe({
      next: (res) => {
        if (res) {
          this.showSuccess('Đăng nhập thành công!');
          this.authService.currentUser$.subscribe((account) => {
            this.showSuccess('Đăng nhập thành công!');
            if (account?.roleName == "Admin") {
              this.router.navigate(['/admin']);
            } else if (account?.roleName == "Giảng viên") {
              this.router.navigate(['/gv_trangchu']);
            } else {
              this.router.navigate(['/trangcanhan']);
            }
          })
        } else {
          this.showError('Phản hồi không hợp lệ từ máy chủ!');
        }
      },
      error: (err: HttpErrorResponse) => {
        this.showError(err.error?.message || 'Sai tài khoản hoặc mật khẩu!');
      }
    });
  }

  // 🔹 Xử lý đăng ký (có thể gọi API thật)
  handleRegister(): void {
    this.clearMessages();

    if (!this.fullName || !this.registerEmail || !this.registerPassword || !this.confirmPassword) {
      this.showError('Vui lòng điền đầy đủ thông tin!');
      return;
    }

    if (this.registerPassword !== this.confirmPassword) {
      this.showError('Mật khẩu xác nhận không khớp!');
      return;
    }

    if (!this.agreeTerms) {
      this.showError('Bạn phải đồng ý với điều khoản sử dụng!');
      return;
    }

    // 👇 Gọi API đăng ký thật (nếu backend có)
    this.authService.register({
      fullName: this.fullName,
      username: this.registerEmail,
      password: this.registerPassword,
      role: this.userRole
    }).subscribe({
      next: (res) => {
        this.showSuccess('Đăng ký thành công! Hãy đăng nhập để tiếp tục.');
        setTimeout(() => this.switchToSignin(), 1500);
      },
      error: (err: HttpErrorResponse) => {
        this.showError(err.error?.message || 'Đăng ký thất bại!');
      }
    });
  }


  // 🔹 Hiển thị thông báo
  showSuccess(message: string): void {
    this.errorMessage = null;
    this.successMessage = message;
    setTimeout(() => this.successMessage = null, 3000);
  }

  showError(message: string): void {
    this.successMessage = null;
    this.errorMessage = message;
    setTimeout(() => this.errorMessage = null, 3000);
  }

  clearMessages(): void {
    this.successMessage = null;
    this.errorMessage = null;
  }

}
