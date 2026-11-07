import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AbstractControl, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { errorMessage } from '../core/api.service';
@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class RegisterComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  form = inject(FormBuilder).nonNullable.group(
    {
      prenom: ['', [Validators.required, Validators.maxLength(100), Validators.pattern(/\S/)]],
      nom: ['', [Validators.required, Validators.maxLength(100), Validators.pattern(/\S/)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
      password: ['', [Validators.required, Validators.minLength(12), Validators.maxLength(64)]],
      confirmation: ['', Validators.required],
    },
    {
      validators: (c: AbstractControl) =>
        c.get('password')?.value === c.get('confirmation')?.value
          ? null
          : { passwordMismatch: true },
    },
  );
  busy = false;
  error = '';
  async register() {
    if (this.form.invalid || this.busy) {
      this.form.markAllAsTouched();
      return;
    }
    this.busy = true;
    this.error = '';
    try {
      const { nom, prenom, email, password } = this.form.getRawValue();
      await this.auth.register({
        nom: nom.trim(),
        prenom: prenom.trim(),
        email: email.trim(),
        password,
      });
      await this.router.navigate(['/home']);
    } catch (error) {
      this.error = errorMessage(error);
    } finally {
      this.busy = false;
    }
  }
}
