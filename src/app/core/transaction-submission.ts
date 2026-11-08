import { minorUnits } from './money';
import type { AbstractControl } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import type { Observable } from 'rxjs';
import { firstValueFrom } from 'rxjs';
import { errorMessage } from './api.service';
interface TransactionDialog {
  disableClose: boolean | undefined;
  close(result?: boolean): void;
}
export class TransactionSubmission {
  busy = false;
  error = '';
  private attempt: { signature: string; key: string } | null = null;
  constructor(private dialog: TransactionDialog) {}
  async run(
    form: AbstractControl,
    payload: { amount: string; account?: string; receiver?: string },
    send: (key: string) => Observable<unknown>,
  ) {
    if (form.invalid || this.busy) {
      form.markAllAsTouched();
      return;
    }
    const signature = JSON.stringify({ ...payload, amount: minorUnits(payload.amount).toString() });
    if (this.attempt?.signature !== signature)
      this.attempt = { signature, key: crypto.randomUUID() };
    this.busy = true;
    this.error = '';
    this.dialog.disableClose = true;
    try {
      await firstValueFrom(send(this.attempt.key));
      this.dialog.close(true);
    } catch (error) {
      this.error =
        error instanceof HttpErrorResponse && error.status === 0
          ? 'The response was interrupted. Check history or retry unchanged values with the same request key.'
          : errorMessage(error);
    } finally {
      this.busy = false;
      this.dialog.disableClose = false;
    }
  }
}
