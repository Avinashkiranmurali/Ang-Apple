import { Component, Input } from '@angular/core';
import { AddressFormBaseDirective } from '../address-form-base.directive';
import { SharedService } from '@app/modules/shared/shared.service';


@Component({
  selector: 'app-address-form-au',
  templateUrl: './address-form-au.component.html',
  styleUrls: ['./address-form-au.component.scss']
})
export class AddressFormAuComponent extends AddressFormBaseDirective {
  @Input() shippingStates: Array<{[key: string]: string}>;

  constructor(public sharedService: SharedService) {
    super();
  }
}
