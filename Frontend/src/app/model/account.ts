export interface Account {
    userName: string;
    roleId: Int16Array;
    roleName: AccountRole;
}

export type AccountRole = 'Học sinh' | 'Giảng viên' | 'Admin';