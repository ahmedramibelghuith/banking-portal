import { DOCUMENT, Directive, ElementRef, EventEmitter, Output, inject } from '@angular/core';
import type { AfterViewInit, OnDestroy } from '@angular/core';
import { filter, fromEvent } from 'rxjs';
import type { Subscription } from 'rxjs';
@Directive({ selector: '[clickOutside]', standalone: false })
export class ClickOutsideDirective implements AfterViewInit, OnDestroy {
  @Output() clickOutside = new EventEmitter<void>();
  private element = inject<ElementRef<HTMLElement>>(ElementRef);
  private document = inject(DOCUMENT);
  private subscription: Subscription | undefined;
  ngAfterViewInit(): void {
    this.subscription = fromEvent(this.document, 'click')
      .pipe(filter((event) => !this.element.nativeElement.contains(event.target as Node | null)))
      .subscribe(() => this.clickOutside.emit());
  }
  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }
}
