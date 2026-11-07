import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { User } from './models';
@Injectable({ providedIn: 'root' })
export class CsrfState {
  header = '';
  token = '';
}
export const csrfInterceptor: HttpInterceptorFn = (request, next) => {
  const csrf = inject(CsrfState);
  if (
    request.url.startsWith('/api/') &&
    !['GET', 'HEAD', 'OPTIONS'].includes(request.method) &&
    csrf.token
  ) {
    request = request.clone({ setHeaders: { [csrf.header]: csrf.token } });
  }
  return next(request);
};
@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private csrf = inject(CsrfState);
  user = signal<User | null>(null);
  async refreshCsrf() {
    const csrf = await firstValueFrom(
      this.http.get<{ headerName: string; token: string }>('/api/auth/csrf'),
    );
    this.csrf.header = csrf.headerName;
    this.csrf.token = csrf.token;
  }
  async restore(): Promise<User | null> {
    try {
      const user = await firstValueFrom(this.http.get<User>('/api/auth/me'));
      await this.refreshCsrf();
      this.user.set(user);
      return user;
    } catch (error) {
      this.user.set(null);
      if (error instanceof HttpErrorResponse && error.status === 401) return null;
      throw error;
    }
  }
  async login(email: string, password: string) {
    await this.refreshCsrf();
    const form = new URLSearchParams({ username: email.trim().toLowerCase(), password });
    await firstValueFrom(
      this.http.post('/api/auth/login', form.toString(), {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      }),
    );
    return this.restore();
  }
  async register(data: { nom: string; prenom: string; email: string; password: string }) {
    await this.refreshCsrf();
    await firstValueFrom(this.http.post<User>('/api/auth/register', data));
    return this.login(data.email, data.password);
  }
  async logout() {
    await this.refreshCsrf();
    await firstValueFrom(this.http.post('/api/auth/logout', {}));
    this.user.set(null);
    this.csrf.token = '';
  }
}
