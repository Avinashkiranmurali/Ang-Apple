import { Component } from '@angular/core';
import { AddressFormBaseDirective } from '../address-form-base.directive';

@Component({
  selector: 'app-address-form-th',
  templateUrl: './address-form-th.component.html',
  styleUrls: ['./address-form-th.component.scss']
})
export class AddressFormThComponent extends AddressFormBaseDirective {

  constructor(
  ) {
    super();
  }
}
