import { Component, Input } from '@angular/core';
import { AddressFormBaseDirective } from '../address-form-base.directive';
import { SharedService } from '@app/modules/shared/shared.service';

@Component({
  selector: 'app-address-form-mx',
  templateUrl: './address-form-mx.component.html',
  styleUrls: ['./address-form-mx.component.scss']
})
export class AddressFormMxComponent extends AddressFormBaseDirective {
  @Input() shippingStates: Array<{[key: string]: string}>;

  constructor(public sharedService: SharedService
  ) {
    super();
  }

  get address3Field() {
    return this.changeShipAddress.get('address3');
  }

}
