import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
@Component({
  selector: 'app-pagination',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `<nav class="actions pagination" aria-label="Pagination">
    <button class="secondary" [disabled]="busy || page === 0" (click)="pageChange.emit(page - 1)">
      Previous</button
    ><span>Page {{ page + 1 }}</span
    ><button class="secondary" [disabled]="busy || last" (click)="pageChange.emit(page + 1)">
      Next
    </button>
  </nav>`,
})
export class PaginationComponent {
  @Input() page = 0;
  @Input() last = true;
  @Input() busy = false;
  @Output() pageChange = new EventEmitter<number>();
}
