import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ApiService } from '../core/api.service';
import { moneyValidator } from '../core/form-validators';
import { TransactionSubmission } from '../core/transaction-submission';
import type { Account } from '../core/models';
@Component({
  selector: 'app-operation',
  templateUrl: './operation.component.html',
  styleUrls: ['./operation.component.css'],
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class OperationComponent {
  data = inject<{ account: Account }>(MAT_DIALOG_DATA);
  dialog = inject<MatDialogRef<OperationComponent, boolean>>(MatDialogRef);
  private api = inject(ApiService);
  submission = new TransactionSubmission(this.dialog);
  form = inject(FormBuilder).nonNullable.group({ amount: ['', moneyValidator] });
  async deposit() {
    const { amount } = this.form.getRawValue();
    await this.submission.run(this.form, { account: this.data.account.id, amount }, (key) =>
      this.api.deposit(this.data.account.id, amount, key),
    );
  }
}
