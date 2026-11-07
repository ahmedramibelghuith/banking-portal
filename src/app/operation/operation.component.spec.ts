import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { of } from 'rxjs';
import { OperationComponent } from './operation.component';
import { ApiService } from '../core/api.service';
describe('OperationComponent', () => {
  it('rejects nonpositive amounts and closes only after a valid operation', async () => {
    const action = jasmine.createSpy('deposit').and.returnValue(of({}));
    const dialog = { close: jasmine.createSpy('close'), disableClose: false };
    await TestBed.configureTestingModule({
      declarations: [OperationComponent],
      imports: [ReactiveFormsModule, MatDialogModule],
      providers: [
        { provide: ApiService, useValue: { deposit: action } },
        { provide: MatDialogRef, useValue: dialog },
        { provide: MAT_DIALOG_DATA, useValue: { account: { id: 1, solde: 100 } } },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(OperationComponent);
    fixture.detectChanges();

    fixture.componentInstance.form.controls.amount.setValue('-10');
    await fixture.componentInstance.deposit();
    expect(action).not.toHaveBeenCalled();
    expect(dialog.close).not.toHaveBeenCalled();
    fixture.componentInstance.form.controls.amount.setValue('10.25');
    await fixture.componentInstance.deposit();
    expect(action).toHaveBeenCalled();
    expect(dialog.close).toHaveBeenCalledWith(true);
  });
});
