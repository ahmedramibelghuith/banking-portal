import { MoneyPipe } from '../core/money';
import { PaginationComponent } from '../core/pagination.component';
import { TestBed } from '@angular/core/testing';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';
import { ListtransactionsComponent } from './listtransactions.component';
import { ApiService } from '../core/api.service';
describe('ListtransactionsComponent', () => {
  it('renders an empty result returned by the API', async () => {
    await TestBed.configureTestingModule({
      declarations: [ListtransactionsComponent],
      imports: [MoneyPipe, PaginationComponent, MatDialogModule],
      providers: [
        {
          provide: ApiService,
          useValue: {
            allOperations: () =>
              of({ content: [], page: 0, size: 25, totalElements: 0, last: true }),
          },
        },
        { provide: MatDialogRef, useValue: { close() {} } },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(ListtransactionsComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No records yet.');
  });
});
