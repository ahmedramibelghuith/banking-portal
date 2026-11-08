import { FormControl, FormGroup } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Subject } from 'rxjs';
import { TransactionSubmission } from './transaction-submission';
describe('TransactionSubmission', () => {
  it('holds the dialog open while pending and reuses the key after an ambiguous failure', async () => {
    const dialog = { disableClose: false, close: jasmine.createSpy('close') };
    const submission = new TransactionSubmission(dialog);
    const form = new FormGroup({ amount: new FormControl('10.25') });
    const first = new Subject<unknown>();
    const send = jasmine.createSpy('send').and.returnValue(first);
    const pending = submission.run(form, { amount: '10.25' }, send);
    expect(submission.busy).toBeTrue();
    expect(dialog.disableClose).toBeTrue();
    expect(dialog.close).not.toHaveBeenCalled();
    const key = send.calls.mostRecent().args[0];
    first.error(new HttpErrorResponse({ status: 0 }));
    await pending;
    expect(dialog.close).not.toHaveBeenCalled();
    expect(submission.error).toContain('same request key');
    expect(dialog.disableClose).toBeFalse();
    const retry = new Subject<unknown>();
    send.and.returnValue(retry);
    const repeated = submission.run(form, { amount: '10.25' }, send);
    expect(send.calls.mostRecent().args[0]).toBe(key);
    retry.next({});
    retry.complete();
    await repeated;
    expect(dialog.close).toHaveBeenCalledWith(true);
  });
  it('changes the key when the payload changes and leaves the dialog open on rejection', async () => {
    const dialog = { disableClose: false, close: jasmine.createSpy('close') };
    const submission = new TransactionSubmission(dialog);
    const form = new FormGroup({ amount: new FormControl('10') });
    const keys: string[] = [];
    const send = (key: string) => {
      keys.push(key);
      const response = new Subject<unknown>();
      queueMicrotask(() =>
        response.error(
          new HttpErrorResponse({ status: 409, error: { message: 'Insufficient funds' } }),
        ),
      );
      return response;
    };
    await submission.run(form, { amount: '10' }, send);
    await submission.run(form, { amount: '20' }, send);
    expect(keys[0]).not.toBe(keys[1]);
    expect(submission.error).toBe('Insufficient funds');
    expect(dialog.close).not.toHaveBeenCalled();
  });
});
