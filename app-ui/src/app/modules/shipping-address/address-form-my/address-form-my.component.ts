import { Component } from '@angular/core';
import { AddressFormBaseDirective } from '../address-form-base.directive';

@Component({
  selector: 'app-address-form-my',
  templateUrl: './address-form-my.component.html',
  styleUrls: ['./address-form-my.component.scss']
})
export class AddressFormMyComponent extends AddressFormBaseDirective {

  constructor(
  ) {
    super();
  }
}
