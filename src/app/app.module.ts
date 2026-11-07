import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule } from '@angular/forms';
import { provideHttpClient, withXhr, withInterceptors } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import { MyNewComponentComponent } from './my-new-component/my-new-component.component';
import { HelloWorldComponent } from './hello-world/hello-world.component';
import { csrfInterceptor } from './core/auth.service';
@NgModule({
  declarations: [AppComponent, LoginComponent, MyNewComponentComponent, HelloWorldComponent],
  imports: [BrowserModule, AppRoutingModule, ReactiveFormsModule],
  providers: [provideHttpClient(withXhr(), withInterceptors([csrfInterceptor]))],
  bootstrap: [AppComponent],
})
export class AppModule {}
