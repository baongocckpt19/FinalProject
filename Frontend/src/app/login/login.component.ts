import { Component, Renderer2, ElementRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'; // Cần cho các directive cơ bản như ngIf, ngFor
import { FormsModule } from '@angular/forms'; // Cần cho ngModel

@Component({
  selector: 'app-login',
  // Thêm CommonModule và FormsModule vào imports
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  // Giữ nguyên styleUrl
  styleUrl: './login.component.scss',
  // Đảm bảo standalones: true nếu bạn đang dùng standalone component
  // standalone: true 
})
export class LoginComponent implements OnInit {
  // === Thuộc tính cho Data Binding (Binding Models) ===

  // Đăng nhập
  loginEmail = '';
  loginPassword = '';
  rememberMe = false;

  // Đăng ký
  userRole = 'student'; // Mặc định là 'student'
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
  
  // Tham chiếu DOM và Renderer vẫn cần cho việc chuyển đổi form và toggle password
  constructor(private renderer: Renderer2, private el: ElementRef) {}

  ngOnInit(): void {
    // Không cần gắn sự kiện submit bằng renderer/listener nữa
    // vì ta sẽ dùng (ngSubmit) trên tag <form> trong HTML
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
    this.isSignupMode = true; // Cập nhật biến trạng thái
    const container = this.el.nativeElement.querySelector('#formContainer');
    // Vẫn cần Renderer để thêm/xóa class cho hiệu ứng CSS
    this.renderer.addClass(container, 'show-signup');
    this.clearMessages();
  }

  switchToSignin(): void {
    this.isSignupMode = false; // Cập nhật biến trạng thái
    const container = this.el.nativeElement.querySelector('#formContainer');
    this.renderer.removeClass(container, 'show-signup');
    this.clearMessages();
  }

  // 🔹 Xử lý đăng nhập
  handleLogin(): void {
    this.clearMessages();

    // Dữ liệu được lấy trực tiếp từ thuộc tính class: this.loginEmail, this.loginPassword
    if (!this.loginEmail || !this.loginPassword) {
      this.showError('Vui lòng nhập đầy đủ thông tin!');
      return;
    }

    // Mô phỏng login (sau này bạn có thể gọi API thật ở đây)
    if (this.loginEmail === 'test@gmail.com' && this.loginPassword === '123456') {
      this.showSuccess('Đăng nhập thành công!');
      // console.log('Ghi nhớ đăng nhập:', this.rememberMe);
    } else {
      this.showError('Email hoặc mật khẩu không đúng!');
    }
  }

  // 🔹 Xử lý đăng ký
  handleRegister(): void {
    this.clearMessages();

    // Dữ liệu được lấy trực tiếp từ thuộc tính class
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

    // console.log('Đăng ký với vai trò:', this.userRole);
    this.showSuccess('Đăng ký thành công! Hãy đăng nhập để tiếp tục.');
    setTimeout(() => this.switchToSignin(), 1500);
  }

  // 🔹 Đăng nhập bằng mạng xã hội
  socialLogin(platform: string): void {
    this.clearMessages();
    this.showSuccess(`Đăng nhập bằng ${platform} thành công (demo)!`);
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