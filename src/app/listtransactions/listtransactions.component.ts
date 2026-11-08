import type { OnInit } from '@angular/core';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { ApiService } from '../core/api.service';
import { PagedList } from '../core/paged-list';
import type { Operation } from '../core/models';
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
  list = new PagedList<Operation>((page) => this.api.allOperations(page));
  ngOnInit() {
    void this.list.load();
  }
}
