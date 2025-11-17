import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, map, Observable, tap } from 'rxjs';
import { AccountService } from './account.service';
import { Account } from '../model/account';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth'; // 👈 chỉnh đúng URL backend của m
  private userToken = new BehaviorSubject<string | null>(localStorage.getItem('token'));
  userToken$ = this.userToken.asObservable();

  private currentUserSubject = new BehaviorSubject<Account | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private accountService: AccountService) { }

  // Gọi API login
  login(username: string, password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, {
      username,
      passwordHash: password

    }).pipe(map((res: any) => {
     
      if(res && res.token){
        this.clearUser();
        this.setUser(res.token);
      }
      return res;
    }));
  }

  setUser(token: string) {
    if (!token) {
      this.clearUser();
      return;
    }

    localStorage.setItem('token', token);
    this.userToken.next(token);

    this.accountService.getCurrentAccount().subscribe(res => {
      if (res) {
        this.currentUserSubject.next(res.account);
      } else {
        this.clearUser();
      }
    });
  }

  clearUser() {
    localStorage.removeItem('token');
    this.userToken.next(null);
    this.currentUserSubject.next(null);
  }

  // Gọi API register
  register(data: {
    fullName: string;
    username: string;
    password: string;
    role: string;
  }): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, data);
  }

  getToken(): string | null {
    return localStorage.getItem('jwt_token');
  }

  logout(): void {
    localStorage.removeItem('jwt_token');
  }
}
