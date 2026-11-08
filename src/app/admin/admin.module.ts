import { MoneyPipe } from '../core/money';
import { PaginationComponent } from '../core/pagination.component';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatDialogModule } from '@angular/material/dialog';
import { AdminComponent } from './admin.component';
import { ListComponent } from '../list/list.component';
import { ListcomptesComponent } from '../listcomptes/listcomptes.component';
import { ListtransactionsComponent } from '../listtransactions/listtransactions.component';
@NgModule({
  declarations: [AdminComponent, ListComponent, ListcomptesComponent, ListtransactionsComponent],
  imports: [
    CommonModule,
    MoneyPipe,
    PaginationComponent,
    MatDialogModule,
    RouterModule.forChild([{ path: '', component: AdminComponent }]),
  ],
})
export class AdminModule {}
