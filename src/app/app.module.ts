import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule } from '@angular/forms';
import { provideHttpClient, withXhr, withInterceptors } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import { csrfInterceptor } from './core/auth.service';
@NgModule({
  declarations: [AppComponent, LoginComponent],
  imports: [BrowserModule, AppRoutingModule, ReactiveFormsModule],
  providers: [provideHttpClient(withXhr(), withInterceptors([csrfInterceptor]))],
  bootstrap: [AppComponent],
})
export class AppModule {}
