import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import type { Account, Operation, Summary, User, PageView, AccountTotals } from './models';
export function errorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0 || error.status === 502 || error.status === 504)
      return 'Cannot reach the banking API. Please try again.';
    if (error.status === 401)
      return error.error?.message || 'Your session has expired. Please sign in again.';
    return error.error?.message || 'The request could not be completed. Please try again.';
  }
  return 'An unexpected error occurred. Please try again.';
}
@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  accounts(page = 0) {
    return this.http.get<PageView<Account>>(`/api/comptes?page=${page}`);
  }
  accountTotals() {
    return this.http.get<AccountTotals>('/api/comptes/summary');
  }
  createAccount(type: Account['type']) {
    return this.http.post<Account>('/api/comptes', { type });
  }
  closeAccount(id: string) {
    return this.http.delete<void>(`/api/comptes/${id}`);
  }
  history(id: string, page = 0) {
    return this.http.get<PageView<Operation>>(`/api/comptes/${id}/operations?page=${page}`);
  }
  deposit(compteId: string, montant: string, key: string) {
    return this.http.post<Operation>(
      '/api/operations/versement',
      { compteId, montant },
      { headers: { 'Idempotency-Key': key } },
    );
  }
  withdraw(compteId: string, montant: string, key: string) {
    return this.http.post<Operation>(
      '/api/operations/retrait',
      { compteId, montant },
      { headers: { 'Idempotency-Key': key } },
    );
  }
  transfer(compteSourceId: string, compteDestinataireId: string, montant: string, key: string) {
    return this.http.post<void>(
      '/api/operations/virement',
      {
        compteSourceId,
        compteDestinataireId,
        montant,
      },
      { headers: { 'Idempotency-Key': key } },
    );
  }
  summary() {
    return this.http.get<Summary>('/api/admin/summary');
  }
  clients(page = 0) {
    return this.http.get<PageView<User>>(`/api/admin/clients?page=${page}`);
  }
  allAccounts(page = 0) {
    return this.http.get<PageView<Account>>(`/api/admin/comptes?page=${page}`);
  }
  allOperations(page = 0) {
    return this.http.get<PageView<Operation>>(`/api/admin/operations?page=${page}`);
  }
}
