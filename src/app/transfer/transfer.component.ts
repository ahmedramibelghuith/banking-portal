import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ApiService } from '../core/api.service';
import { moneyValidator, accountIdValidator } from '../core/form-validators';
import { TransactionSubmission } from '../core/transaction-submission';
import type { Account } from '../core/models';
@Component({
  selector: 'app-transfer',
  templateUrl: './transfer.component.html',
  styleUrls: ['./transfer.component.css'],
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class TransferComponent {
  data = inject<{ account: Account }>(MAT_DIALOG_DATA);
  dialog = inject<MatDialogRef<TransferComponent, boolean>>(MatDialogRef);
  private api = inject(ApiService);
  submission = new TransactionSubmission(this.dialog);
  form = inject(FormBuilder).nonNullable.group({
    receiver: ['', accountIdValidator],
    amount: ['', moneyValidator],
  });
  async transfer() {
    const { amount, receiver } = this.form.getRawValue();
    await this.submission.run(
      this.form,
      { account: this.data.account.id, receiver, amount },
      (key) => this.api.transfer(this.data.account.id, receiver, amount, key),
    );
  }
}
