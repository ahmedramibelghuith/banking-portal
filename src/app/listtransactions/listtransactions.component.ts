import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { firstValueFrom } from 'rxjs';
import { ApiService, errorMessage } from '../core/api.service';
import { Operation } from '../core/models';
@Component({
  selector: 'app-listtransactions',
  templateUrl: './listtransactions.component.html',
  styleUrls: ['./listtransactions.component.css'],
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class ListtransactionsComponent implements OnInit {
  private api = inject(ApiService);
  dialog = inject<MatDialogRef<ListtransactionsComponent>>(MatDialogRef);
  rows: Operation[] = [];
  loading = true;
  error = '';
  async ngOnInit() {
    try {
      this.rows = await firstValueFrom(this.api.allOperations());
    } catch (error) {
      this.error = errorMessage(error);
    } finally {
      this.loading = false;
    }
  }
}
