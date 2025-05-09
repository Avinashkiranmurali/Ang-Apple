import { Directive, ElementRef, HostListener, OnInit } from '@angular/core';

@Directive({
  selector: '[appCustomSelect]'
})
export class CustomSelectDirective implements OnInit {

  private element: HTMLElement;

  constructor(
    private eleRef: ElementRef
  ) { }

  ngOnInit(): void {
    this.element = this.eleRef.nativeElement.querySelector('#addressContainer');
  }

  @HostListener('click') onClick() {
    if (this.element.classList.contains('open')) {
      this.hideSelect();
    } else {
      this.showSelect();
    }
  }

  @HostListener('focusout') onFocusout() {
    setTimeout(() => {
      if (this.element.classList.contains('open')) {
        this.hideSelect();
      }
    }, 300);
  }

  hideSelect() {
    this.element.querySelector('.select-selected').classList.remove('select-arrow-active');
    this.element.classList.remove('open');
    this.element.querySelector('.select-items').classList.add('select-hide');
  }

  showSelect() {
    this.element.querySelector('.select-selected').classList.add('select-arrow-active');
    this.element.classList.add('open');
    this.element.querySelector('.select-items').classList.remove('select-hide');
  }

}
