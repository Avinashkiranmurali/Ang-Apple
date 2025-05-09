import { Component, Input } from '@angular/core';
import { AddressFormBaseDirective } from '../address-form-base.directive';
import { SharedService } from '@app/modules/shared/shared.service';

@Component({
  selector: 'app-address-form-tw',
  templateUrl: './address-form-tw.component.html',
  styleUrls: ['./address-form-tw.component.scss']
})
export class AddressFormTwComponent extends AddressFormBaseDirective {
  @Input() shippingCities: {[key: string]: string};

  constructor(public sharedService: SharedService) {
    super();
  }
}
