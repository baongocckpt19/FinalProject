// auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, map, Observable } from 'rxjs';
import { AccountService } from './account.service';
import { Account } from '../model/account';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';

  // Dùng 1 key CỐ ĐỊNH cho token
  private readonly TOKEN_KEY = 'token';

  private userToken = new BehaviorSubject<string | null>(
    localStorage.getItem(this.TOKEN_KEY)
  );
  userToken$ = this.userToken.asObservable();

  private currentUserSubject = new BehaviorSubject<Account | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private accountService: AccountService
  ) {
    // Khi reload trang, nếu trong localStorage còn token thì load lại currentAccount
    const token = this.getToken();
    if (token) {
      this.setUser(token);
    }
  }

  // Gọi API login
  login(username: string, password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, {
      username,
      passwordHash: password
    }).pipe(
      map((res: any) => {
        if (res && res.token) {
          // Không xoá lung tung, chỉ reset state internal
          this.clearUser(false);
          this.setUser(res.token); // lưu token + gọi /api/account
        }
        return res;
      })
    );
  }

  setUser(token: string) {
    if (!token) {
      this.clearUser();
      return;
    }

    // Lưu token
    localStorage.setItem(this.TOKEN_KEY, token);
    this.userToken.next(token);

    // Gọi /api/account để lấy thông tin account hiện tại

    this.accountService.getCurrentAccount().subscribe({
      next: (res) => {
        if (res && res.account) {
          const raw = res.account;

          const mapped: Account = {
            accountId: raw.accountId,
            username: raw.username ?? raw.userName,  // hỗ trợ cả 2 kiểu
            roleId: raw.roleId,
            roleName: raw.roleName as any,
            fullName: raw.fullName,

            studentId: raw.studentId ?? raw.studentID ?? raw.student?.studentId ?? null,
            teacherId: raw.teacherId ?? raw.teacherID ?? raw.teacher?.teacherId ?? null,
            email: raw.email ?? null,
            phone: raw.phone ?? null
          };

          console.log('Mapped account in AuthService:', mapped);
          this.currentUserSubject.next(mapped);
        } else {
          this.clearUser();
        }
      },
      error: () => {
        this.clearUser();
      }
    });

  }

  clearUser(removeToken: boolean = true) {
    if (removeToken) {
      localStorage.removeItem(this.TOKEN_KEY);
    }
    this.userToken.next(null);
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  logout(): void {
    this.clearUser(true);
  }

  // Đăng ký (nếu backend có)
  register(data: {
    fullName: string;
    username: string;
    password: string;
    role: string;
  }): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, data);
  }
}
