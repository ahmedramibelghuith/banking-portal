import type { OnInit } from '@angular/core';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { ApiService } from '../core/api.service';
import { PagedList } from '../core/paged-list';
import type { User } from '../core/models';
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
  list = new PagedList<User>((page) => this.api.clients(page));
  ngOnInit() {
    void this.list.load();
  }
}
