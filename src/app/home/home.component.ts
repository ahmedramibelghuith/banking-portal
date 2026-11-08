import type { OnInit } from '@angular/core';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { firstValueFrom } from 'rxjs';
import { ApiService, errorMessage } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { PagedList } from '../core/paged-list';
import type { Account, Operation } from '../core/models';
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
  historyList: PagedList<Operation> | null = null;
  selectedId: string | null = null;
  page = 0;
  last = true;
  accountCount = 0;
  total = '0.00';
  loading = true;
  busy = false;
  error = '';
  message = '';
  isMenuOpened = false;
  ngOnInit() {
    void this.load();
  }
  async load(page = this.page) {
    this.loading = true;
    this.error = '';
    try {
      const [accounts, totals] = await Promise.all([
        firstValueFrom(this.api.accounts(page)),
        firstValueFrom(this.api.accountTotals()),
      ]);
      this.accounts = accounts.content;
      this.page = accounts.page;
      this.last = accounts.last;
      this.accountCount = totals.count;
      this.total = totals.total;
      if (this.historyList) await this.historyList.load();
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
      await this.load(0);
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
  async history(id: string) {
    this.selectedId = id;
    this.historyList = new PagedList((page) => this.api.history(id, page));
    await this.historyList.load(0);
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
