import { ViewContainerRef } from '@angular/core';
import { ShippingAddressFormDirective } from './shipping-address-form.directive';

describe('ShippingAddressFormDirective', () => {
  // eslint-disable-next-line prefer-const
  let viewContainerRef: ViewContainerRef;

  it('should create an instance', () => {
    const directive = new ShippingAddressFormDirective(viewContainerRef);
    expect(directive).toBeTruthy();

  });
});
