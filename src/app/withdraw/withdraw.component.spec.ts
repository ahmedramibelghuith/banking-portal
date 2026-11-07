import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { of } from 'rxjs';
import { WithdrawComponent } from './withdraw.component';
import { ApiService } from '../core/api.service';
describe('WithdrawComponent', () => {
  it('rejects nonpositive amounts and closes only after a valid operation', async () => {
    const action = jasmine.createSpy('withdraw').and.returnValue(of({}));
    const dialog = { close: jasmine.createSpy('close'), disableClose: false };
    await TestBed.configureTestingModule({
      declarations: [WithdrawComponent],
      imports: [ReactiveFormsModule, MatDialogModule],
      providers: [
        { provide: ApiService, useValue: { withdraw: action } },
        { provide: MatDialogRef, useValue: dialog },
        { provide: MAT_DIALOG_DATA, useValue: { account: { id: 1, solde: 100 } } },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(WithdrawComponent);
    fixture.detectChanges();

    fixture.componentInstance.form.controls.amount.setValue('-10');
    await fixture.componentInstance.withdraw();
    expect(action).not.toHaveBeenCalled();
    expect(dialog.close).not.toHaveBeenCalled();
    fixture.componentInstance.form.controls.amount.setValue('10.25');
    await fixture.componentInstance.withdraw();
    expect(action).toHaveBeenCalled();
    expect(dialog.close).toHaveBeenCalledWith(true);
  });
});
