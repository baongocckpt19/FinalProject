// src/app/model/account.ts
export type AccountRole = 'Học sinh' | 'Giảng viên' | 'Admin';

export interface Account {
  accountId: number;
  username: string;              // đổi thành username chữ thường
  roleId: number;                // không nên dùng Int16Array
  roleName: AccountRole;
  fullName?: string;

  // Các field thêm để dùng cho cả GV / SV
  studentId?: number | null;
  teacherId?: number | null;
  email?: string | null;
  phone?: string | null;
}
