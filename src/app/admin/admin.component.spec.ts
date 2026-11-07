import { TestBed } from '@angular/core/testing';
import { RouterModule, provideRouter } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';
import { AdminComponent } from './admin.component';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
describe('AdminComponent', () => {
  it('renders the server summary without interchanging accounts and transactions', async () => {
    await TestBed.configureTestingModule({
      declarations: [AdminComponent],
      imports: [RouterModule],
      providers: [
        provideRouter([]),
        {
          provide: ApiService,
          useValue: { summary: () => of({ clients: 2, accounts: 3, transactions: 7 }) },
        },
        { provide: AuthService, useValue: {} },
        { provide: MatDialog, useValue: {} },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(AdminComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    const stats = fixture.nativeElement.querySelectorAll('.stat-card strong');
    expect(
      Array.from(stats).map((node: unknown) => (node as HTMLElement).textContent?.trim()),
    ).toEqual(['2', '3', '7']);
  });
});
