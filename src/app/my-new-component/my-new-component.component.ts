import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-my-new-component',
  templateUrl: './my-new-component.component.html',
  styleUrl: './my-new-component.component.css',
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false,
})
export class MyNewComponentComponent {}
