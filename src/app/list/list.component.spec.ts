import { PaginationComponent } from '../core/pagination.component';
import { TestBed } from '@angular/core/testing';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';
import { ListComponent } from './list.component';
import { ApiService } from '../core/api.service';
describe('ListComponent', () => {
  it('renders an empty result returned by the API', async () => {
    await TestBed.configureTestingModule({
      declarations: [ListComponent],
      imports: [PaginationComponent, MatDialogModule],
      providers: [
        {
          provide: ApiService,
          useValue: {
            clients: () => of({ content: [], page: 0, size: 25, totalElements: 0, last: true }),
          },
        },
        { provide: MatDialogRef, useValue: { close() {} } },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(ListComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No records yet.');
  });
});
