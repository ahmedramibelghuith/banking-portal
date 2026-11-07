import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AbstractControl, FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { firstValueFrom } from 'rxjs';
import { ApiService, errorMessage } from '../core/api.service';
import { Account } from '../core/models';
@Component({
  selector: 'app-operation',
  templateUrl: './operation.component.html',
  styleUrls: ['./operation.component.css'],
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class OperationComponent {
  data = inject<{ account: Account }>(MAT_DIALOG_DATA);
  dialog = inject<MatDialogRef<OperationComponent>>(MatDialogRef);
  private api = inject(ApiService);
  form = inject(FormBuilder).nonNullable.group({
    amount: [
      '',
      [
        Validators.required,
        Validators.pattern(/^(?:0|[1-9]\d{0,11})(?:\.\d{1,2})?$/),
        (c: AbstractControl) => (Number(c.value) > 0 ? null : { positive: true }),
      ],
    ],
  });
  busy = false;
  error = '';
  async deposit() {
    if (this.form.invalid || this.busy) {
      this.form.markAllAsTouched();
      return;
    }
    this.busy = true;
    this.error = '';
    this.dialog.disableClose = true;
    try {
      const amount = this.form.getRawValue().amount;
      await firstValueFrom(this.api.deposit(this.data.account.id, amount));
      this.dialog.close(true);
    } catch (error) {
      this.error = errorMessage(error);
    } finally {
      this.busy = false;
      this.dialog.disableClose = false;
    }
  }
}
