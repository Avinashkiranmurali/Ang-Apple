import { Component, OnInit, Input } from '@angular/core';
import { Config } from '@app/models/config';
import { User } from '@app/models/user';
import { UserStoreService } from '@app/state/user-store.service';
import { Address } from '@app/models/address';
import { ControlContainer } from '@angular/forms';
import { SharedService } from '@app/modules/shared/shared.service';
import { AddressService } from '@app/services/address.service';

@Component({
  selector: 'app-billing-address',
  templateUrl: './billing-address.component.html',
  styleUrls: ['./billing-address.component.scss']
})
export class BillingAddressComponent implements OnInit {
  errorMessage: {[key: string]: string};
  config: Config;
  user: User;
  country;
  shippingStates;
  shippingCities;
  isPostalCode;
  locale;
  billingAddressForm;
  @Input() shippingAddress: Address;
  @Input() postCardDetailsError;
  @Input() state;
  constructor(
    private userStore: UserStoreService,
    public sharedService: SharedService,
    public controlContainer: ControlContainer,
    private addressService: AddressService
  ) {
    this.config = this.userStore.config;
    this.user = this.userStore.user;
  }

  ngOnInit(): void {
    const reverseNameOrder = (['zh_tw', 'en_tw'].indexOf(this.user.locale.toLocaleLowerCase()) >= 0);
    const shippingAddress = this.state.cart.shippingAddress;
    if (reverseNameOrder) {
      // unique case for tw locale where they reverse the order or the first and last name
      this.shippingAddress.recipientName = shippingAddress.lastName.concat(' ', shippingAddress.firstName).trim();
    } else {
      // every other
      this.shippingAddress.recipientName = shippingAddress.firstName.concat(' ', shippingAddress.lastName).trim();
    }
    this.country = this.user.country;

    // Get state/province list data for locale
    if (['en_us', 'en_ca', 'fr_ca', 'en_au', 'es_mx', 'ru_ru'].indexOf(this.user.locale.toLocaleLowerCase()) >= 0) {
      this.addressService.getStateProvince().subscribe((data) => {
        this.shippingStates = data;
      });
    }
    // Get city list data for locale
    if (this.user.locale.toLocaleLowerCase() === 'zh_tw') {
      this.addressService.getCities().subscribe((data) =>  {
        this.shippingCities = data.data;
      });
    }
    switch (this.user.locale.toLocaleLowerCase()) {
      case 'en_us':
      case 'fr_fr':
        this.isPostalCode = false;
        break;
      case 'ru_ru':
        this.isPostalCode = true;
        break;
      default:
        this.isPostalCode = true;
    }
    this.billingAddressForm = this.controlContainer.control;
    // TODO remove the below temporary object once handled, its not handled in JS Application
    this.errorMessage = {};
  }

}
