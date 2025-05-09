import { Component } from '@angular/core';
import { AddressFormBaseDirective } from '../address-form-base.directive';

@Component({
  selector: 'app-address-form-ae',
  templateUrl: './address-form-ae.component.html',
  styleUrls: ['./address-form-ae.component.scss']
})
export class AddressFormAeComponent extends AddressFormBaseDirective {

  constructor(
  ) {
    super();
  }
}
