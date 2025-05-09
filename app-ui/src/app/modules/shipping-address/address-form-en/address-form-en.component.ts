import { Component, Input } from '@angular/core';
import { AddressFormBaseDirective } from '../address-form-base.directive';
import { SharedService } from '@app/modules/shared/shared.service';

@Component({
  selector: 'app-address-form-en',
  templateUrl: './address-form-en.component.html',
  styleUrls: ['./address-form-en.component.scss']
})
export class AddressFormEnComponent extends AddressFormBaseDirective {
  @Input() shippingStates: Array<{[key: string]: string}>;

  constructor( public sharedService: SharedService
  ) {
    super();
  }
}

