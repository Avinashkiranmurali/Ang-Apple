import { Directive, ViewContainerRef } from '@angular/core';

@Directive({
  selector: '[appShippingAddressForm]'
})
export class ShippingAddressFormDirective {

  constructor(public viewContainerRef: ViewContainerRef) {

  }

}
