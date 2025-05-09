import { Directive, ViewContainerRef } from '@angular/core';

@Directive({
  selector: '[appFooter]'
})
export class FooterDirective {

  constructor(
    public viewContainerRef: ViewContainerRef
  ) { }

}
