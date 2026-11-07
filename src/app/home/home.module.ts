import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MatDialogModule } from '@angular/material/dialog';
import { HomeComponent } from './home.component';
import { ClickOutsideDirective } from './clickOutside.directive';
import { OperationComponent } from '../operation/operation.component';
import { WithdrawComponent } from '../withdraw/withdraw.component';
import { TransferComponent } from '../transfer/transfer.component';
@NgModule({
  declarations: [
    HomeComponent,
    ClickOutsideDirective,
    OperationComponent,
    WithdrawComponent,
    TransferComponent,
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    RouterModule.forChild([{ path: '', component: HomeComponent }]),
  ],
})
export class HomeModule {}
