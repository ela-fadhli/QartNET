import { Injectable } from '@angular/core';
import { Observable, of, delay } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor() {}

  login(credentials: any): Observable<any> {
    return of({ success: true, token: 'mock-jwt-token' }).pipe(delay(1000));
  }

  register(userData: any): Observable<any> {
    return of({ success: true, message: 'User registered successfully' }).pipe(delay(1500));
  }
}
