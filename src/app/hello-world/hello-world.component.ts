import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-hello-world',
  templateUrl: './hello-world.component.html',
  styleUrl: './hello-world.component.css',
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false,
})
export class HelloWorldComponent {}
