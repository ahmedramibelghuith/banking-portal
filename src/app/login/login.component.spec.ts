import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideRouter, RouterModule } from '@angular/router';
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
});
