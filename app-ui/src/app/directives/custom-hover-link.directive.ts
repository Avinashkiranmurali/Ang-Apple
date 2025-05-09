import { Directive, Input, ElementRef, HostListener, NgZone } from '@angular/core';

@Directive({
  selector: '[appCustomHoverLink]'
})
export class CustomHoverLinkDirective {

  @Input() navBarColors;
  constructor(
    private eleRef: ElementRef,
    private ngZone: NgZone
  ) { }

  @HostListener('mouseover') onMouseOver() {
    this.ngZone.runOutsideAngular(() => {
    this.eleRef.nativeElement.style.color = this.navBarColors['hoverColor'];
    });
  }

  @HostListener('mouseleave') onMouseLeave() {
    this.ngZone.runOutsideAngular(() => {
    if (this.eleRef.nativeElement.classList.contains('active')) {
    this.eleRef.nativeElement.style.color = this.navBarColors['activeTextColor'];
    } else {
      this.eleRef.nativeElement.style.color = this.navBarColors['textColor'];
    }
    });
  }

  @HostListener('click') onClick() {
    this.ngZone.runOutsideAngular(() => {
    Array.from(document.querySelectorAll('.main.nav-animation'))
      .forEach((item: HTMLElement) => {
        if (item !== this.eleRef.nativeElement) {
          item.style.color = this.navBarColors['textColor'];
        }
      });
    });
  }

}
