import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { errorMessage } from '../core/api.service';
@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  form = inject(FormBuilder).nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });
  busy = false;
  error = this.route.snapshot.queryParamMap.has('unavailable')
    ? 'The sign-in service is unavailable. Please try again.'
    : '';
  notice = this.route.snapshot.queryParamMap.has('registered')
    ? 'Account created. Sign in to continue.'
    : '';
  async signIn() {
    if (this.form.invalid || this.busy) {
      this.form.markAllAsTouched();
      return;
    }
    this.busy = true;
    this.error = '';
    try {
      const { email, password } = this.form.getRawValue();
      const user = await this.auth.login(email, password);
      await this.router.navigate([user.role === 'ADMIN' ? '/admin' : '/home']);
    } catch (error) {
      this.error = errorMessage(error);
    } finally {
      this.busy = false;
    }
  }
}
