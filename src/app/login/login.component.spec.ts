import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, convertToParamMap, provideRouter, RouterModule } from '@angular/router';
import { LoginComponent } from './login.component';
import { AuthService } from '../core/auth.service';
describe('LoginComponent', () => {
  it('does not submit an invalid form and surfaces authentication failures', async () => {
    const auth = { login: jasmine.createSpy('login').and.rejectWith({}) };
    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      imports: [ReactiveFormsModule, RouterModule],
      providers: [provideRouter([]), { provide: AuthService, useValue: auth }],
    }).compileComponents();
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
    await fixture.componentInstance.signIn();
    expect(auth.login).not.toHaveBeenCalled();
    fixture.componentInstance.form.setValue({ email: 'test@example.com', password: 'password' });
    await fixture.componentInstance.signIn();
    fixture.detectChanges();
    expect(auth.login).toHaveBeenCalledWith('test@example.com', 'password');
    expect(fixture.nativeElement.querySelector('[role="alert"]')).not.toBeNull();
    expect(fixture.componentInstance.busy).toBeFalse();
  });
  it('shows an infrastructure outage as an error rather than a registration success notice', async () => {
    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      imports: [ReactiveFormsModule, RouterModule],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: {} },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: convertToParamMap({ unavailable: 'true' }) } },
        },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain(
      'unavailable',
    );
    expect(fixture.nativeElement.querySelector('.success')).toBeNull();
  });
});
