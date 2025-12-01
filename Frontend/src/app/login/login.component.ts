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
  registerUsername = '';
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
            } if (account?.roleName == "Học sinh") {
              this.router.navigate(['/sv-trangchu']);
            } else if (account?.roleName == "Giảng viên") {
              this.router.navigate(['/gv_trangchu']);
            }
            // } else {
            //   this.router.navigate(['/trangcanhan']);
            // }
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
  // 🔹 Xử lý đăng ký (gọi API thật)
  handleRegister(): void {
    this.clearMessages();

    if (!this.registerUsername || !this.registerPassword || !this.confirmPassword) {
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

    this.authService.register({
      username: this.registerUsername,
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

  // ==== Quên mật khẩu ====
  isForgotMode = false;     // bật/tắt popup
  forgotStep = 1;           // 1: nhập username, 2: nhập code + mật khẩu mới
  forgotUsername = '';
  resetCode = '';
  newPassword = '';
  confirmNewPassword = '';

    openForgotPassword(): void {
    this.clearMessages();
    this.isForgotMode = true;
    this.forgotStep = 1;
    this.forgotUsername = '';
    this.resetCode = '';
    this.newPassword = '';
    this.confirmNewPassword = '';
  }

  closeForgotPassword(): void {
    this.isForgotMode = false;
    // không cần reset hết, tuỳ bạn
  }

    submitForgotRequest(): void {
    this.clearMessages();

    if (!this.forgotUsername) {
      this.showError('Vui lòng nhập tài khoản!');
      return;
    }

    this.authService.forgotPassword(this.forgotUsername).subscribe({
      next: (res) => {
        this.showSuccess(res?.message || 'Đã gửi mã xác nhận. Vui lòng kiểm tra email.');
        this.forgotStep = 2;
      },
      error: (err: HttpErrorResponse) => {
        this.showError(err.error?.message || 'Không thể gửi mã xác nhận. Vui lòng thử lại.');
      }
    });
  }
    submitResetPassword(): void {
    this.clearMessages();

    if (!this.resetCode) {
      this.showError('Vui lòng nhập mã xác nhận!');
      return;
    }

    if (!this.newPassword || !this.confirmNewPassword) {
      this.showError('Vui lòng nhập đầy đủ mật khẩu mới!');
      return;
    }

    if (this.newPassword !== this.confirmNewPassword) {
      this.showError('Mật khẩu xác nhận không khớp!');
      return;
    }

    this.authService.resetPassword({
      username: this.forgotUsername,
      code: this.resetCode,
      newPassword: this.newPassword
    }).subscribe({
      next: (res) => {
        this.showSuccess(res?.message || 'Đổi mật khẩu thành công, hãy đăng nhập lại.');
        this.isForgotMode = false;
      },
      error: (err: HttpErrorResponse) => {
        this.showError(err.error?.message || 'Không thể đổi mật khẩu. Vui lòng thử lại.');
      }
    });
  }


}
