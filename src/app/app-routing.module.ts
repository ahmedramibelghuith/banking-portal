import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { authGuard, adminGuard } from './core/auth.guard';
const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', title: 'Sign in | Banking Portal', component: LoginComponent },
  {
    path: 'register',
    title: 'Register | Banking Portal',
    loadChildren: () => import('./register/register.module').then((m) => m.RegisterModule),
  },
  {
    path: 'home',
    title: 'Accounts | Banking Portal',
    canActivate: [authGuard],
    loadChildren: () => import('./home/home.module').then((m) => m.HomeModule),
  },
  {
    path: 'admin',
    title: 'Administration | Banking Portal',
    canActivate: [adminGuard],
    loadChildren: () => import('./admin/admin.module').then((m) => m.AdminModule),
  },
  { path: '**', redirectTo: 'login' },
];
@NgModule({ imports: [RouterModule.forRoot(routes)], exports: [RouterModule] })
export class AppRoutingModule {}
