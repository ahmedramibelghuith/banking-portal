import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { firstValueFrom } from 'rxjs';
import { ApiService, errorMessage } from '../core/api.service';
import { Account } from '../core/models';
@Component({
  selector: 'app-listcomptes',
  templateUrl: './listcomptes.component.html',
  styleUrls: ['./listcomptes.component.css'],
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class ListcomptesComponent implements OnInit {
  private api = inject(ApiService);
  dialog = inject<MatDialogRef<ListcomptesComponent>>(MatDialogRef);
  rows: Account[] = [];
  loading = true;
  error = '';
  async ngOnInit() {
    try {
      this.rows = await firstValueFrom(this.api.allAccounts());
    } catch (error) {
      this.error = errorMessage(error);
    } finally {
      this.loading = false;
    }
  }
}
