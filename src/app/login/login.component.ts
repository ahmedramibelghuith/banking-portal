
import {Component, inject, ChangeDetectionStrategy} from '@angular/core';

import { ActivatedRoute, Router } from '@angular/router';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css'],
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
export class LoginComponent {
  constructor(private router: Router,private activateRouter:ActivatedRoute) { }

  signIn() {
    const emailInput = document.getElementById('email') as HTMLInputElement;
    const email = emailInput.value;   
    this.router.navigate(['/home'], { queryParams: { email: email } });
    // Add more logic here as needed
  }
}