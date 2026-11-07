import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideRouter, RouterModule } from '@angular/router';
import { RegisterComponent } from './register.component';
import { AuthService } from '../core/auth.service';
describe('RegisterComponent', () => {
  it('rejects mismatched passwords before making an API request', async () => {
    const auth = { register: jasmine.createSpy('register') };
    await TestBed.configureTestingModule({
      declarations: [RegisterComponent],
      imports: [ReactiveFormsModule, RouterModule],
      providers: [provideRouter([]), { provide: AuthService, useValue: auth }],
    }).compileComponents();
    const fixture = TestBed.createComponent(RegisterComponent);
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({
      prenom: 'First',
      nom: 'Last',
      email: 'test@example.com',
      password: 'Valid-password-123',
      confirmation: 'different-password',
    });
    await fixture.componentInstance.register();
    expect(fixture.componentInstance.form.hasError('passwordMismatch')).toBeTrue();
    expect(auth.register).not.toHaveBeenCalled();
  });
});
