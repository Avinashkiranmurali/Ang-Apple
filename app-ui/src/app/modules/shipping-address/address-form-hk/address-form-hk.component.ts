import { Component } from '@angular/core';
import { AddressFormBaseDirective } from '../address-form-base.directive';

@Component({
  selector: 'app-address-form-hk',
  templateUrl: './address-form-hk.component.html',
  styleUrls: ['./address-form-hk.component.scss']
})
export class AddressFormHkComponent extends AddressFormBaseDirective {

  constructor(
  ) {
    super();
  }
}
