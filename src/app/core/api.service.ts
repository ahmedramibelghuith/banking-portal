import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Account, Operation, Summary, User } from './models';
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
  accounts() {
    return this.http.get<Account[]>('/api/comptes');
  }
  createAccount(type: Account['type']) {
    return this.http.post<Account>('/api/comptes', { type });
  }
  closeAccount(id: number) {
    return this.http.delete<void>(`/api/comptes/${id}`);
  }
  history(id: number) {
    return this.http.get<Operation[]>(`/api/comptes/${id}/operations`);
  }
  deposit(compteId: number, montant: string) {
    return this.http.post<Operation>('/api/operations/versement', { compteId, montant });
  }
  withdraw(compteId: number, montant: string) {
    return this.http.post<Operation>('/api/operations/retrait', { compteId, montant });
  }
  transfer(compteSourceId: number, compteDestinataireId: number, montant: string) {
    return this.http.post<void>('/api/operations/virement', {
      compteSourceId,
      compteDestinataireId,
      montant,
    });
  }
  summary() {
    return this.http.get<Summary>('/api/admin/summary');
  }
  clients() {
    return this.http.get<User[]>('/api/admin/clients');
  }
  allAccounts() {
    return this.http.get<Account[]>('/api/admin/comptes');
  }
  allOperations() {
    return this.http.get<Operation[]>('/api/admin/operations');
  }
}
