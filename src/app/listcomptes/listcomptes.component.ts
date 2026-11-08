import type { OnInit } from '@angular/core';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { ApiService } from '../core/api.service';
import { PagedList } from '../core/paged-list';
import type { Account } from '../core/models';
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
  list = new PagedList<Account>((page) => this.api.allAccounts(page));
  ngOnInit() {
    void this.list.load();
  }
}
