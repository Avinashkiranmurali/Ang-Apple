import { Injectable } from '@angular/core';
import { AddressFormEnComponent } from '@app/modules/shipping-address/address-form-en/address-form-en.component';
import { AddressFormSgComponent } from '@app/modules/shipping-address/address-form-sg/address-form-sg.component';
import { Address } from '@app/models/address';
import { AddressFormTwComponent } from '@app/modules/shipping-address/address-form-tw/address-form-tw.component';
import { AddressFormAuComponent } from '@app/modules/shipping-address/address-form-au/address-form-au.component';
import { AddressFormAeComponent } from '@app/modules/shipping-address/address-form-ae/address-form-ae.component';
import { AddressFormHkComponent } from '@app/modules/shipping-address/address-form-hk/address-form-hk.component';
import { AddressFormMyComponent } from '@app/modules/shipping-address/address-form-my/address-form-my.component';
import { AddressFormThComponent } from '@app/modules/shipping-address/address-form-th/address-form-th.component';
import { AddressFormMxComponent } from '@app/modules/shipping-address/address-form-mx/address-form-mx.component';
import { AddressFormPhComponent } from '@app/modules/shipping-address/address-form-ph/address-form-ph.component';

@Injectable({
  providedIn: 'root'
})
export class ShippingAddressService {

  addressFormTemplates: Array<object> = [
    { name: 'en', template: AddressFormEnComponent },
    { name: 'sg', template: AddressFormSgComponent },
    { name: 'tw', template: AddressFormTwComponent },
    { name: 'au', template: AddressFormAuComponent },
    { name: 'ae', template: AddressFormAeComponent },
    { name: 'hk', template: AddressFormHkComponent },
    { name: 'my', template: AddressFormMyComponent },
    { name: 'th', template: AddressFormThComponent },
    { name: 'mx', template: AddressFormMxComponent },
    { name: 'ph', template: AddressFormPhComponent }
  ];

  constructor() { }

  loadAddressFormComponent(name): object {
    return this.addressFormTemplates.find(object => object['name'] === name);
  }

  format5DigitZipCode(zipCode) {
    if(zipCode) {
      let formattedValue = zipCode.replace(/\D/g,'');
      if(!formattedValue || formattedValue?.length < 5) {
        return formattedValue;
      }

      formattedValue = formattedValue.trim();
      const zipWithoutDash = formattedValue.replace(/-/g, '');

      if(!zipWithoutDash || zipWithoutDash.length < 5) {
        return formattedValue;
      }

      const firstFiveDigits = zipWithoutDash.slice(0,5);
      return firstFiveDigits;
    }
  }
}
