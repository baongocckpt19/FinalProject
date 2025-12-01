// src/app/services/user.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

interface UserList {
  accountId: number;
  fullName: string;
  username: string;
  roleName: string;
  email: string;
  userCode?: string;    
  fingerCount?: number | null;
}
@Injectable({
  providedIn: 'root'
})
export class UserService {

  private apiUrl = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient) { }

  getAllUsers(): Observable<UserList[]> {
    return this.http.get<UserList[]>(this.apiUrl);
  }

  getUserById(id: number) {
    return this.http.get<any>(`http://localhost:8080/api/users/${id}`);
  }


  deleteUser(id: number) {
    return this.http.delete(`http://localhost:8080/api/account/${id}`);
  }
  exportExcel() {
    return this.http.get('http://localhost:8080/api/users/export/excel', {
      responseType: 'blob'
    });
  }

}
