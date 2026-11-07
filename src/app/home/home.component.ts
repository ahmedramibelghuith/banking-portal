import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { firstValueFrom } from 'rxjs';
import { ApiService, errorMessage } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { Account, Operation } from '../core/models';
import { OperationComponent } from '../operation/operation.component';
import { WithdrawComponent } from '../withdraw/withdraw.component';
import { TransferComponent } from '../transfer/transfer.component';
@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class HomeComponent implements OnInit {
  private api = inject(ApiService);
  private dialog = inject(MatDialog);
  private router = inject(Router);
  auth = inject(AuthService);
  accounts: Account[] = [];
  operations: Operation[] = [];
  selectedId: number | null = null;
  loading = true;
  busy = false;
  historyLoading = false;
  error = '';
  historyError = '';
  message = '';
  isMenuOpened = false;
  get total() {
    return this.accounts
      .filter((a) => a.etat === 'ACTIVE')
      .reduce((total, a) => total + Number(a.solde), 0);
  }
  ngOnInit() {
    void this.load();
  }
  async load() {
    this.loading = true;
    this.error = '';
    try {
      this.accounts = await firstValueFrom(this.api.accounts());
      if (this.selectedId !== null) await this.history(this.selectedId);
    } catch (error) {
      this.error = errorMessage(error);
    } finally {
      this.loading = false;
    }
  }
  async create(type: Account['type']) {
    if (this.busy) return;
    this.busy = true;
    this.error = '';
    this.message = '';
    try {
      await firstValueFrom(this.api.createAccount(type));
      this.message = 'Account opened.';
      await this.load();
    } catch (error) {
      this.error = errorMessage(error);
    } finally {
      this.busy = false;
    }
  }
  async close(account: Account) {
    if (
      this.busy ||
      !confirm(`Close account #${account.id}? Its transaction history will be retained.`)
    )
      return;
    this.busy = true;
    this.error = '';
    this.message = '';
    try {
      await firstValueFrom(this.api.closeAccount(account.id));
      this.message = 'Account closed.';
      await this.load();
    } catch (error) {
      this.error = errorMessage(error);
    } finally {
      this.busy = false;
    }
  }
  async history(id: number) {
    this.selectedId = id;
    this.historyLoading = true;
    this.historyError = '';
    this.operations = [];
    try {
      const operations = await firstValueFrom(this.api.history(id));
      if (this.selectedId === id) this.operations = operations;
    } catch (error) {
      if (this.selectedId === id) this.historyError = errorMessage(error);
    } finally {
      if (this.selectedId === id) this.historyLoading = false;
    }
  }
  async transact(account: Account, kind: 'deposit' | 'withdraw' | 'transfer') {
    const component =
      kind === 'deposit'
        ? OperationComponent
        : kind === 'withdraw'
          ? WithdrawComponent
          : TransferComponent;
    const changed = await firstValueFrom(
      this.dialog
        .open<
          OperationComponent | WithdrawComponent | TransferComponent,
          { account: Account },
          boolean
        >(component, {
          data: { account },
          width: '440px',
          maxWidth: '95vw',
          autoFocus: 'first-tabbable',
        })
        .afterClosed(),
    );
    if (changed) {
      this.message = 'Transaction completed.';
      await this.load();
    }
  }
  toggleMenu() {
    this.isMenuOpened = !this.isMenuOpened;
  }
  clickedOutside() {
    this.isMenuOpened = false;
  }
  async logout() {
    this.error = '';
    try {
      await this.auth.logout();
      await this.router.navigate(['/login']);
    } catch (error) {
      this.error = errorMessage(error);
    }
  }
}
