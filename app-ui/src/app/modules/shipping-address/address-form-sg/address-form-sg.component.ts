import { Component } from '@angular/core';
import { AddressFormBaseDirective } from '../address-form-base.directive';

@Component({
  selector: 'app-address-form-sg',
  templateUrl: './address-form-sg.component.html',
  styleUrls: ['./address-form-sg.component.scss']
})
export class AddressFormSgComponent extends AddressFormBaseDirective {

  constructor() {
    super();
  }
}
