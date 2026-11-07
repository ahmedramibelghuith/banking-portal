import { TestBed } from '@angular/core/testing';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';
import { ListcomptesComponent } from './listcomptes.component';
import { ApiService } from '../core/api.service';
describe('ListcomptesComponent', () => {
  it('renders an empty result returned by the API', async () => {
    await TestBed.configureTestingModule({
      declarations: [ListcomptesComponent],
      imports: [MatDialogModule],
      providers: [
        { provide: ApiService, useValue: { allAccounts: () => of([]) } },
        { provide: MatDialogRef, useValue: { close() {} } },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(ListcomptesComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No records yet.');
  });
});
