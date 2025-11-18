export interface Account {
    userName: string;
    roleId: Int16Array;
    roleName: AccountRole;
    fullName?: string;
}

export type AccountRole = 'Học sinh' | 'Giảng viên' | 'Admin';