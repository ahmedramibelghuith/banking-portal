import { MoneyPipe } from '../core/money';
import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { of } from 'rxjs';
import { TransferComponent } from './transfer.component';
import { ApiService } from '../core/api.service';
describe('TransferComponent', () => {
  it('rejects nonpositive amounts and closes only after a valid operation', async () => {
    const action = jasmine.createSpy('transfer').and.returnValue(of({}));
    const dialog = { close: jasmine.createSpy('close'), disableClose: false };
    await TestBed.configureTestingModule({
      declarations: [TransferComponent],
      imports: [MoneyPipe, ReactiveFormsModule, MatDialogModule],
      providers: [
        { provide: ApiService, useValue: { transfer: action } },
        { provide: MatDialogRef, useValue: dialog },
        { provide: MAT_DIALOG_DATA, useValue: { account: { id: '1', solde: '100.00' } } },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(TransferComponent);
    fixture.detectChanges();
    fixture.componentInstance.form.controls.receiver.setValue('2');
    fixture.componentInstance.form.controls.amount.setValue('-10');
    await fixture.componentInstance.transfer();
    expect(action).not.toHaveBeenCalled();
    expect(dialog.close).not.toHaveBeenCalled();
    fixture.componentInstance.form.controls.amount.setValue('10.25');
    await fixture.componentInstance.transfer();
    expect(action).toHaveBeenCalledWith(
      '1',
      '2',
      '10.25',
      jasmine.stringMatching(/^[0-9a-f-]{36}$/),
    );
    expect(dialog.close).toHaveBeenCalledWith(true);
  });
});
