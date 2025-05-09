import { Component, Input } from '@angular/core';
import { AddressFormBaseDirective } from '../address-form-base.directive';
import { SharedService } from '@app/modules/shared/shared.service';

@Component({
  selector: 'app-address-form-ph',
  templateUrl: './address-form-ph.component.html',
  styleUrls: ['./address-form-ph.component.scss']
})
export class AddressFormPhComponent extends AddressFormBaseDirective {
  @Input() shippingStates: Array<{[key: string]: string}>;

  constructor(public sharedService: SharedService
  ) {
    super();
  }
}
