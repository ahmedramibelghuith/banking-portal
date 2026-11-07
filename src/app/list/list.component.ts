import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { firstValueFrom } from 'rxjs';
import { ApiService, errorMessage } from '../core/api.service';
import { User } from '../core/models';
@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.css'],
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class ListComponent implements OnInit {
  private api = inject(ApiService);
  dialog = inject<MatDialogRef<ListComponent>>(MatDialogRef);
  rows: User[] = [];
  loading = true;
  error = '';
  async ngOnInit() {
    try {
      this.rows = await firstValueFrom(this.api.clients());
    } catch (error) {
      this.error = errorMessage(error);
    } finally {
      this.loading = false;
    }
  }
}
