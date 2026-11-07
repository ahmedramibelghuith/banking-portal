import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { firstValueFrom } from 'rxjs';
import { ApiService, errorMessage } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { Summary } from '../core/models';
import { ListComponent } from '../list/list.component';
import { ListcomptesComponent } from '../listcomptes/listcomptes.component';
import { ListtransactionsComponent } from '../listtransactions/listtransactions.component';
@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css'],
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class AdminComponent implements OnInit {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  summary: Summary | null = null;
  error = '';
  loading = false;
  ngOnInit() {
    void this.load();
  }
  async load() {
    this.error = '';
    this.loading = true;
    try {
      this.summary = await firstValueFrom(this.api.summary());
    } catch (error) {
      this.error = errorMessage(error);
    } finally {
      this.loading = false;
    }
  }
  list() {
    this.dialog.open(ListComponent, { width: '900px', maxWidth: '95vw' });
  }
  Operationscomptes() {
    this.dialog.open(ListcomptesComponent, { width: '1000px', maxWidth: '95vw' });
  }
  Operationstransactions() {
    this.dialog.open(ListtransactionsComponent, { width: '1000px', maxWidth: '95vw' });
  }
  async logout() {
    try {
      await this.auth.logout();
      await this.router.navigate(['/login']);
    } catch (error) {
      this.error = errorMessage(error);
    }
  }
}
