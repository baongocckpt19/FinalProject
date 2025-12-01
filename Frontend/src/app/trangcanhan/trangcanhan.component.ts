// src/app/trangcanhan/trangcanhan.component.ts
import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Subscription } from 'rxjs';

import {
  UserProfile,
  UserProfileService,
  UpdateProfileRequest,
  ChangePasswordRequest,
  Gender
} from '../services/user-profile.service';

import { NotificationService } from '../services/notification.service';

interface EditProfileModel {
  fullName: string;
  roleName: string;
  email: string;
  phone: string;
  address: string;
  birthDate: string; // yyyy-MM-dd
  gender: Gender;

  // ⭐ NEW
  userCode: string;
}

interface PasswordModel {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

@Component({
  selector: 'app-trangcanhan',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './trangcanhan.component.html',
  styleUrl: './trangcanhan.component.scss'
})
export class TrangcanhanComponent implements OnInit, OnDestroy {
  // ================== STATE CHUNG ==================
  profile: UserProfile | null = null;
  private subs = new Subscription();

  isEditing = false;
  isSaving = false;

  editModel: EditProfileModel | null = null;

  // ✅ Cho phép sửa MÃ SỐ hay không:
  // - true  nếu studentId == null VÀ teacherId == null
  // - false nếu đã có studentId hoặc teacherId
  canEditUserCode = false;

  // ================== PASSWORD STATE ==================
  passwordModel: PasswordModel = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  showCurrentPassword = false;
  showNewPassword = false;
  showConfirmPassword = false;

  currentPasswordValid: boolean | null = null;
  currentPasswordChecking = false;
  isChangingPassword = false;

  constructor(
    private userProfileService: UserProfileService,
    private notify: NotificationService
  ) { }

  // =====================================================
  // LIFECYCLE
  // =====================================================
  ngOnInit(): void {
    this.loadProfile();
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  private loadProfile(): void {
    const sub = this.userProfileService.getMyProfile().subscribe({
      next: (profile) => {
        this.profile = profile;

        // ✅ Rule: chỉ cho sửa "Mã số" khi chưa được gán Student/Teacher
        this.canEditUserCode = !profile.studentId && !profile.teacherId;

        if (this.isEditing) {
          this.prepareEditModel();
        }
      },
      error: (err) => {
        console.error('Load profile error', err);
        this.notify.error('Không tải được thông tin hồ sơ.');
      }
    });
    this.subs.add(sub);
  }

  // Chuẩn hoá model edit từ profile
  private prepareEditModel(): void {
    if (!this.profile) {
      this.editModel = null;
      return;
    }

    this.editModel = {
      fullName: this.profile.fullName || '',
      roleName: this.profile.roleName,
      email: this.profile.email || '',
      phone: this.profile.phone || '',
      address: this.profile.address || '',
      birthDate: this.profile.birthDate || '',
      gender: this.profile.gender,
      userCode: this.profile.userCode || ''
    };
  }

  // =====================================================
  // GETTERS CHO TEMPLATE
  // =====================================================

  // Avatar = chữ cái đầu
  get avatarLabel(): string {
    if (this.profile?.fullName && this.profile.fullName.trim().length > 0) {
      return this.profile.fullName.trim().charAt(0).toUpperCase();
    }
    if (this.profile?.username && this.profile.username.trim().length > 0) {
      return this.profile.username.trim().charAt(0).toUpperCase();
    }
    return '?';
  }

  // Text hiển thị vai trò
  get roleBadgeText(): string {
    const role = this.profile?.roleName;
    if (!role) return 'Người dùng';

    if (role === 'Học sinh') return '🎓 Sinh viên';
    if (role === 'Giảng viên') return '👨‍🏫 Giảng viên';
    if (role === 'Admin') return '🛡️ Admin';

    return role;
  }

  // Mã số hiển thị: ưu tiên userCode, fallback accountId
  get userCode(): string {
    if (!this.profile) return '-';
    if (this.profile.userCode && this.profile.userCode.trim().length > 0) {
      return this.profile.userCode;
    }
    return String(this.profile.accountId);
  }

  formatGender(gender: Gender): string {
    if (!gender) return 'Chưa cập nhật';
    return gender;
  }

  // =====================================================
  // PROFILE / EDIT VIEW
  // =====================================================

  onEditClick(): void {
    if (!this.profile) {
      this.notify.error('Không xác định được tài khoản hiện tại.');
      return;
    }
    this.isEditing = true;
    this.prepareEditModel();
  }

  cancelEdit(): void {
    this.isEditing = false;
    this.prepareEditModel();
  }

  saveChanges(): void {
    if (!this.editModel) return;

    if (!this.editModel.fullName.trim()) {
      this.notify.error('Vui lòng nhập họ và tên.');
      return;
    }

    // Xử lý userCode gửi lên backend:
    let userCodeToSend: string | null = null;

    if (this.canEditUserCode) {
      // Chỉ khi chưa có Student/Teacher thì mới lấy từ input
      const code = this.editModel.userCode?.trim();
      userCodeToSend = code && code.length > 0 ? code : null;
    } else {
      // Nếu không cho sửa thì giữ nguyên mã số hiện tại (nếu có)
      const code = this.profile?.userCode?.trim();
      userCodeToSend = code && code.length > 0 ? code : null;
    }

    const payload: UpdateProfileRequest = {
      fullName: this.editModel.fullName.trim(),
      roleName: this.editModel.roleName,
      email: this.editModel.email?.trim() || null,
      phone: this.editModel.phone?.trim() || null,
      address: this.editModel.address?.trim() || null,
      birthDate: this.editModel.birthDate || null,
      gender: this.editModel.gender,
      userCode: userCodeToSend
    };

    this.isSaving = true;

    this.userProfileService.updateMyProfile(payload).subscribe({
      next: () => {
        this.isSaving = false;
        this.notify.success('Cập nhật thông tin cá nhân thành công.');

        if (this.profile) {
          this.profile = {
            ...this.profile,
            fullName: payload.fullName,
            email: payload.email,
            phone: payload.phone,
            address: payload.address,
            birthDate: payload.birthDate,
            gender: payload.gender,
            userCode: userCodeToSend
          };
        }

        // Sau lần đầu tạo Student/Teacher, lần load sau có studentId/teacherId
        // -> canEditUserCode sẽ false
        this.isEditing = false;
      },
      error: (err) => {
        console.error('Update profile error', err);
        this.isSaving = false;
        this.notify.error('Cập nhật thông tin thất bại.');
      }
    });
  }

  // =====================================================
  // PASSWORD VIEW
  // =====================================================

  togglePasswordVisibility(field: 'current' | 'new' | 'confirm'): void {
    if (field === 'current') {
      this.showCurrentPassword = !this.showCurrentPassword;
    } else if (field === 'new') {
      this.showNewPassword = !this.showNewPassword;
    } else {
      this.showConfirmPassword = !this.showConfirmPassword;
    }
  }

  // Blur khỏi ô mật khẩu hiện tại -> check luôn
  onCurrentPasswordBlur(): void {
    const pwd = this.passwordModel.currentPassword?.trim();
    if (!pwd) {
      this.currentPasswordValid = null;
      return;
    }

    this.currentPasswordChecking = true;

    this.userProfileService.checkCurrentPassword(pwd).subscribe({
      next: (res) => {
        this.currentPasswordValid = !!res.valid;
        this.currentPasswordChecking = false;
      },
      error: (err) => {
        console.error('Check current password error', err);
        this.currentPasswordValid = false;
        this.currentPasswordChecking = false;
      }
    });
  }

  changePassword(): void {
    if (!this.passwordModel.currentPassword || !this.passwordModel.newPassword) {
      this.notify.error('Vui lòng nhập đầy đủ mật khẩu.');
      return;
    }

    if (this.passwordModel.newPassword !== this.passwordModel.confirmPassword) {
      this.notify.error('Mật khẩu xác nhận không khớp.');
      return;
    }

    if (this.currentPasswordValid === false) {
      this.notify.error('Mật khẩu hiện tại không đúng.');
      return;
    }

    const payload: ChangePasswordRequest = {
      currentPassword: this.passwordModel.currentPassword,
      newPassword: this.passwordModel.newPassword
    };

    this.isChangingPassword = true;

    this.userProfileService.changePassword(payload).subscribe({
      next: () => {
        this.isChangingPassword = false;
        this.notify.success('Đổi mật khẩu thành công.');

        this.passwordModel = {
          currentPassword: '',
          newPassword: '',
          confirmPassword: ''
        };
        this.currentPasswordValid = null;
      },
      error: (err) => {
        console.error('Change password error', err);
        this.isChangingPassword = false;
        this.notify.error('Đổi mật khẩu thất bại.');
      }
    });
  }

  resetPasswordForm(form: NgForm): void {
    form.resetForm();
    this.passwordModel = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    };
    this.currentPasswordValid = null;
  }
}
