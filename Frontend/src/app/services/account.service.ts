// account.service.ts
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  private apiUrl = 'http://localhost:8080/api/account';

  constructor(private http: HttpClient) { }

  getCurrentAccount(): Observable<any>{
    const token = localStorage.getItem('token');
    let headers: HttpHeaders | undefined = undefined;

    if (token) {
      headers = new HttpHeaders({
        Authorization: `Bearer ${token}`
      });
    }

    return this.http.get(this.apiUrl, { headers });
  }
}
