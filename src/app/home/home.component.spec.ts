import { TestBed } from '@angular/core/testing';
import { provideRouter, RouterModule } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { HomeComponent } from './home.component';
import { ClickOutsideDirective } from './clickOutside.directive';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { Account } from '../core/models';
describe('HomeComponent', () => {
  it('loads server balances and keeps existing accounts when creation fails', async () => {
    const accounts: Account[] = [
      {
        id: 1,
        dateCreation: '2026-01-01',
        solde: 125.5,
        etat: 'ACTIVE',
        type: 'CURRENT',
        clientId: 1,
        clientEmail: 'test@example.com',
      },
    ];
    const api = {
      accounts: () => of(accounts),
      createAccount: () => throwError(() => new Error('offline')),
    };
    await TestBed.configureTestingModule({
      declarations: [HomeComponent, ClickOutsideDirective],
      imports: [RouterModule],
      providers: [
        provideRouter([]),
        { provide: ApiService, useValue: api },
        { provide: AuthService, useValue: { user: () => ({ prenom: 'Test' }) } },
        { provide: MatDialog, useValue: {} },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    expect(fixture.componentInstance.total).toBe(125.5);
    await fixture.componentInstance.create('SAVINGS');
    fixture.detectChanges();
    expect(fixture.componentInstance.accounts).toEqual(accounts);
    expect(fixture.nativeElement.querySelector('[role="alert"]')).not.toBeNull();
  });
});
