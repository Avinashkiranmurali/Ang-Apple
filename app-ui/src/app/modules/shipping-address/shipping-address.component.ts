import { AfterViewInit, AfterContentChecked, ChangeDetectionStrategy, ChangeDetectorRef, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { Messages } from '@app/models/messages';
import { Location } from '@angular/common';
import { ShippingAddressFormDirective } from '@app/modules/shipping-address/shipping-address-form.directive';
import { ShippingAddressService } from '@app/modules/shipping-address/shipping-address.service';
import { UserStoreService } from '@app/state/user-store.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CartService } from '@app/services/cart.service';
import { Cart } from '@app/models/cart';
import { Config } from '@app/models/config';
import { User } from '@app/models/user';
import { Address } from '@app/models/address';
import { Subscription } from 'rxjs';
import { Router } from '@angular/router';
import { ModalsService } from '@app/components/modals/modals.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { TemplateService } from '@app/services/template.service';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { TranslateService } from '@ngx-translate/core';
import { TransitionService } from '@app/transition/transition.service';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';
import { AddressService } from '@app/services/address.service';

@Component({
  selector: 'app-shipping-address',
  templateUrl: './shipping-address.component.html',
  styleUrls: ['./shipping-address.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class ShippingAddressComponent implements OnInit, AfterViewInit, AfterContentChecked, OnDestroy {
  // General Declarations
  messages: Messages;
  config: Config;
  user: User;
  buttonColor: string;
  addressFormCountry: string;
  isPostalCode: boolean;
  zipMaxLength: number;
  phoneMaxLength: number;
  changeShipAddress: FormGroup;
  shippingStates: Array<{[key: string]: string}>;
  shippingCities: {[key: string]: string};
  cartData: Cart;
  reverseNameOrder: boolean;
  errorMessage: {[key: string]: string} = {};
  isMultiAddress: boolean;
  shipAddress: Address;
  extras: {[key: string]: boolean} = {};
  overrideShipping: boolean;
  ctnButtonEnabled = false;
  private contactInfoLockOverrides: Array<string>;
  private mercAddressLockOverrides: Array<string>;
  private hasNoAddress: boolean;
  // form fields subscription
  private nameStatusSub: {[key: string]: Subscription} = {};
  errorCount = 0;
  errorCountLabel = '';
  @ViewChild(ShippingAddressFormDirective) appShippingAddressForm;
  constructor(
    private messageStore: MessagesStoreService,
    private shippingAddressService: ShippingAddressService,
    private fb: FormBuilder,
    private userStore: UserStoreService,
    private cdref: ChangeDetectorRef,
    private cartService: CartService,
    private ele: ElementRef,
    private router: Router,
    private modalService: ModalsService,
    private location: Location,
    private sharedService: SharedService,
    private templateService: TemplateService,
    private ensightenService: EnsightenService,
    private matomoService: MatomoService,
    private translateService: TranslateService,
    private transitionService: TransitionService,
    private addressService: AddressService
  ) {
    this.messages = this.messageStore.messages;
    this.buttonColor = this.templateService.getTemplatesProperty('buttonColor') || '';
    this.config = this.userStore.config;
    this.user = this.userStore.user;
    if (this.user.locale.split('_')[1] === 'US' || this.user.locale.split('_')[1] === 'CA') {
      this.addressFormCountry = 'en';
    } else {
      this.addressFormCountry = this.user.locale.split('_')[1].toLowerCase();
    }
    this.overrideShipping = ((this.user.overrideShipping !== undefined) ? this.user.overrideShipping : (this.config.overrideShipping !== undefined) ? this.config.overrideShipping : true);
    if (this.config.ContactInfoLockOverrides) {
      this.contactInfoLockOverrides = this.config.ContactInfoLockOverrides.toString().replace(/[\s,]+/g, ',').split(',');
    } else {
      this.contactInfoLockOverrides = [];
    }
    if (this.config.MercAddressLockOverrides) {
      this.mercAddressLockOverrides = this.config.MercAddressLockOverrides.toString().replace(/[\s,]+/g, ',').split(',');
    } else {
      this.mercAddressLockOverrides = [];
    }
  }

  ngOnInit(): void {
    this.reverseNameOrder = (['zh_tw', 'en_tw'].indexOf(this.user.locale.toLowerCase()) >= 0);
    this.isMultiAddress = (this.user.addresses && this.user.addresses.length > 1);
    this.extras.hideAddressFields = this.isMultiAddress;
    // this.hideAddressFields = this.isMultiAddress;
    this.hasNoAddress = (this.user.addresses && this.user.addresses.length === 0);
    switch (this.user.locale.toLowerCase()) {
      case 'en_us':
      case 'fr_fr':
        this.isPostalCode = false;
        this.zipMaxLength = 5;
        this.phoneMaxLength = 15;
        break;
      case 'ru_ru':
        this.isPostalCode = true;
        this.zipMaxLength = 6;
        this.phoneMaxLength = 15;
        break;
      default:
        this.isPostalCode = true;
        this.zipMaxLength = 7;
        this.phoneMaxLength = 13;
    }
    this.changeShipAddressFormInit();
  }

  changeShipAddressFormInit() {
    this.changeShipAddress = this.fb.group({
      name: [{value: '', disabled: this.config.ShipToNameLocked}, Validators.required],
      businessName: [{value: '', disabled: this.config.businessNameLocked}],
      email: [{
        value: '',
        disabled: (this.isContactInfoEditable('email') || (this.user.addresses !== null && this.user.addresses !== undefined && (Object.keys(this.user.addresses).length > 0)))
      }, [Validators.required, Validators.email]],
      phoneNumber: [{
        value: '',
        disabled: this.isContactInfoEditable('phoneNumber') || !this.overrideShipping
      }, Validators.required],
      address1: [{
        value: '',
        disabled: this.isAddressInfoEditable('address1') || !this.overrideShipping
      }, Validators.required],
      address2: [{
        value: '',
        disabled: this.isAddressInfoEditable('address2') || !this.overrideShipping
      }],
      address3: [{
        value: '',
        disabled: this.isAddressInfoEditable('address3') || !this.overrideShipping
      }],
      city: [{
        value: '',
        disabled: this.isAddressInfoEditable('city') || !this.overrideShipping
      }, Validators.required],
      subCity: [{
        value: '',
        disabled: this.isAddressInfoEditable('subCity') || !this.overrideShipping
      }, Validators.required],
      district: [{
        value: '',
        disabled: this.isAddressInfoEditable('district') || !this.overrideShipping
      }, Validators.required],
      state: [{
        value: '',
        disabled: this.isAddressInfoEditable('state') || !this.overrideShipping
      }, Validators.required],
      zip5: [{
        value: '',
        disabled: this.isAddressInfoEditable('zip5') || !this.overrideShipping
      }, Validators.required]
    });
  }

  ngAfterViewInit() {
    const viewContainerRef = this.appShippingAddressForm.viewContainerRef;
    viewContainerRef.clear();
    const componentRef = this.appShippingAddressForm.viewContainerRef.createComponent(this.shippingAddressService.loadAddressFormComponent(this.addressFormCountry)['template']);
    (componentRef.instance).changeShipAddress = this.changeShipAddress;
    (componentRef.instance).errorMessage = this.errorMessage;
    (componentRef.instance).user = this.user;
    (componentRef.instance).config = this.config;
    (componentRef.instance).extras = this.extras;
    (componentRef.instance).isMultiAddress = this.isMultiAddress;
    (componentRef.instance).overrideShipping = this.overrideShipping;
    (componentRef.instance).isPostalCode = this.isPostalCode;
    (componentRef.instance).zipMaxLength = this.zipMaxLength;
    (componentRef.instance).phoneMaxLength = this.phoneMaxLength;
    (componentRef.instance).parentMultiAddressSelect.subscribe(value => {
      this.multiAddressSelect(value);
    });
    // Remove form Controls if not present in form template
    setTimeout(() => {
      for (const control of Object.keys(this.changeShipAddress.controls)) {
        if (!(this.ele.nativeElement.querySelector('[formcontrolname="' + control + '"]'))) {
          this.changeShipAddress.removeControl(control);
        }
      }
    }, 100);
    this.getCart(componentRef);
    // this.cdref.detectChanges();
  }

  ngAfterContentChecked(): void {
    this.cdref.detectChanges();
  }

  displayAddressCTA(): boolean{
    if (this.user.addresses) {
      return !(this.hasNoAddress && !this.overrideShipping);
    } else {
      return !(this.config.MercAddressLocked && this.config.ContactInfoLocked && this.config.ShipToNameLocked && this.config.businessNameLocked && this.contactInfoLockOverrides.length && this.mercAddressLockOverrides.length);
    }
  }

  disableButton(): boolean{
    if (this.isMultiAddress && this.ctnButtonEnabled) {
      return false;
    } else {
      return this.changeShipAddress.pristine;
    }
  }

  submitAddressChange(): void {
  const formControls = this.changeShipAddress.controls;
  this.setShippingAddressName(this.changeShipAddress.getRawValue());
  this.errorCount = 0;
  this.errorCountLabel = '';
  if (this.changeShipAddress.invalid) {
     for (const controlsKey in formControls) {
       if (formControls[controlsKey].errors) {
         switch (controlsKey) {
           case 'name':
             if (!formControls.name.hasError('invalid')) {
               this.errorMessage[controlsKey] = formControls[controlsKey].errors.required ? this.messages.requiredNameError : this.messages.maximum35Characters;
             }
             this.errorCount++;
             break;
           case 'city':
             this.errorMessage[controlsKey] = formControls[controlsKey].errors.required ? this.messages.requiredCityError : this.messages.maximum35Characters;
             this.errorCount++;
             break;
           case 'state':
             this.errorMessage[controlsKey] = formControls[controlsKey].errors.required ? this.messages.requiredStateError : this.messages.maximum35Characters;
             this.errorCount++;
             break;
           case 'subCity':
             this.errorMessage[controlsKey] = formControls[controlsKey].errors.required ? this.messages.requiredDistrictError : this.messages.maximum35Characters;
             this.errorCount++;
             break;
           case 'zip5':
             this.errorMessage[controlsKey] = formControls[controlsKey].errors.required ? this.messages.requiredZipError : this.messages.maximumZipCharacters;
             this.errorCount++;
             break;
           case 'phoneNumber':
             this.errorMessage[controlsKey] = formControls[controlsKey].errors.required ? this.messages.requiredPhoneError : this.messages.maximum21Characters;
             this.errorCount++;
             break;
           case 'email':
             this.errorMessage[controlsKey] = formControls[controlsKey].errors.required ? this.messages.requiredEmailError : this.messages.patternEmailError;
             this.errorCount++;
             break;
           default:
             this.errorMessage[controlsKey] = formControls[controlsKey].errors.required ? this.messages.requiredAddressError : this.messages.maximum35Characters;
             this.errorCount++;
             break;
         }
       }
     }
     this.updateMultiAddressError();
     this.updateErrorMessage();
     setTimeout (() => {
       for (const key of Object.keys(formControls)) {
         if (formControls[key].invalid) {
           const invalidControl = this.ele.nativeElement.querySelector('[formcontrolname="' + key + '"]');
           invalidControl.focus();
           break;
         }
       }
      }, 200);
   } else {
      // TODO implement the success of API
      this.shipAddress.ignoreSuggestedAddress = 'false';
      this.addressService.modifyAddress(this.shipAddress).subscribe(data => {
        this.transitionService.openTransition('address-form-success', '', false);
        const returnedData: Address = data;
        const hasErrorMsgs = Object.keys(returnedData.errorMessage).length > 0;
        const hasWarningMsgs = Object.keys(returnedData.warningMessage).length > 0;
        returnedData.firstName = returnedData.firstName ? decodeURIComponent(returnedData.firstName) : '';
        returnedData.lastName = returnedData.lastName ? decodeURIComponent(returnedData.lastName) : '';
        returnedData.address1 = decodeURIComponent(returnedData.address1);
        returnedData.address2 = returnedData.address2 ? decodeURIComponent(returnedData.address2) : '';
        returnedData.address3 = returnedData.address3 ? decodeURIComponent(returnedData.address3) : '';
        returnedData.city = decodeURIComponent(returnedData.city);
        returnedData.businessName = returnedData.businessName ? decodeURIComponent(returnedData.businessName) : '';
        if (hasErrorMsgs || hasWarningMsgs) {
          const isAddressChanged = (returnedData.addressModified === 'Y');
          const message = (isAddressChanged && !hasWarningMsgs) ? returnedData.errorMessage : returnedData.warningMessage;
          this.modalService.openSuggestAddressModalComponent(isAddressChanged, this.shipAddress, returnedData, message);
        } else {
          this.toNextPage();
        }
        setTimeout(() => { this.transitionService.closeTransition(); }, 100);
      }, error => {
        this.transitionService.closeTransition();
        if (error.status === 401 || error.status === 0) {
          // TODO handle the session Mgmt
        } else if (error.status < 500) {
          this.errorCount++;
          Object.assign(this.errorMessage, error.error.errorMessage);
          this.updateAddressErrors();
          this.updateErrorMessage();
          setTimeout(this.timeoutCallback, 300);
        } else {
          this.modalService.openOopsModalComponent('shippingAddressError');
        }
        this.matomoService.sendErrorToAnalyticService();
      });
    }
  }

  timeoutCallback = () => {
    const inputElement = this.ele.nativeElement.querySelector('.input-error');
    if (inputElement) {
      inputElement.focus();
    }
  };

  updateErrorMessage() {
    if (this.errorCount > 0) {
      if (this.errorCount === 1) {
        this.errorCountLabel = this.translateService.instant('singleErrorCountLabel', { errorCount: this.errorCount });
      } else {
        this.errorCountLabel = this.translateService.instant('multiErrorCountLabel', { errorCount: this.errorCount });
      }
    } else {
      this.errorCountLabel = '';
    }
  }

  toNextPage() {
    const skipPaymentOptionPage = this.sharedService.verifySkipPaymentOption(this.cartData) || false;
    if (skipPaymentOptionPage || !this.config.fullCatalog) {
      this.router.navigate(['/store', 'checkout']);
    } else {
      this.router.navigate(['/store', 'payment']);
    }
  }

  cancel(): void{
    this.location.back();
  }

  isContactInfoEditable(key: string): boolean {
    const override = (this.contactInfoLockOverrides.indexOf(key) >= 0);
    return (this.config.ContactInfoLocked && !override);
  }

  isAddressInfoEditable(key: string): boolean {
    const override = (this.mercAddressLockOverrides.indexOf(key) >= 0);
    return (this.config.MercAddressLocked && !override);
  }

  getCart(componentRef): void {
    this.transitionService.openTransition();
    this.cartService.getCart().subscribe(data => {
      this.transitionService.closeTransition();
      this.cartData = data;
      this.shipAddress = data.shippingAddress;
      (componentRef.instance).shipAddress = this.shipAddress;
      const shippingAddress = this.cartData.shippingAddress;
      this.setShippingAddressName(shippingAddress, 'concat');
      this.setShipAddressForm(shippingAddress);
      // Setting error messages, if available on page form load
      if (Object.keys(shippingAddress.errorMessage).length > 0) {
        if (this.user.locale.toLowerCase() === 'en_us' && shippingAddress.errorMessage['zip5']) {
          delete shippingAddress.errorMessage['zip5']; ///as this will be formatted in the call above this.setShipAddressForm
        }
        Object.assign(this.errorMessage, shippingAddress.errorMessage);
        this.updateAddressErrors();
        setTimeout(this.timeoutCallback, 300);
      }
      // Multi Address Scenario
      if (this.user.addresses) {
        if (!this.isMultiAddress && !this.hasNoAddress) {
          if (this.shipAddress.selectedAddressId) {
            this.extras.hideAddressFields = false;
          } else {
            this.shipAddress.selectedAddressId = this.user.addresses[0].addressId;
            this.shipAddress.selectedAddressName = this.user.addresses[0].addressName;
            this.setAddressData(this.user.addresses[0]);
          }
        } else if (this.isMultiAddress){
          if (this.shipAddress.selectedAddressId >= -1) {
            this.extras.hideAddressFields = !this.overrideShipping;
            if (this.shipAddress.selectedAddressId === 0) {
              this.extras.hideAddressFields = true;
            }
            this.user.addresses.forEach(value => {
              if (this.shipAddress.selectedAddressId === value.addressId) {
                this.setAddressData(value);
                this.shipAddress.selectedAddressName = value.addressName || '';
              }
            });
          }
        }
      }
      this.setState(shippingAddress, componentRef);
      // Get City data list for loacle
      if (this.user.locale.toLowerCase() === 'zh_tw') {
        this.setCity(shippingAddress, componentRef);
      }
      this.clearErrors();

      // SHIPPING_ADDRESS analytics object
      const userAnalyticsObj = {
        pgName: '',
        pgType: '',
        pgSectionType: ''
      };
      this.ensightenService.broadcastEvent(userAnalyticsObj, this.cartData.cartItems);
    },
    error => {
      this.transitionService.closeTransition();
    });
  }

  setState(shippingAddress, componentRef): void {
    if (['en_us', 'en_ca', 'fr_ca', 'en_au', 'es_mx', 'ru_ru'].indexOf(this.user.locale.toLowerCase()) > -1) {
      this.addressService.getStates().subscribe(data => {
        this.shippingStates = data;
        (componentRef.instance).shippingStates = this.shippingStates;
        for (const state of Object.keys(this.shippingStates)) {
          if (this.shippingStates[state] === shippingAddress.state) {
            this.changeShipAddress.patchValue({
              state: this.shippingStates[state]
            });
            break;
          }
        }
      });
    }
  }

  setCity(shippingAddress, componentRef): void {
    this.addressService.getCities().subscribe(data => {
      this.shippingCities = data;
      (componentRef.instance).shippingCities = this.shippingCities;
      if (this.shippingCities) {
        for (const city of Object.keys(this.shippingCities)) {
          if (city === shippingAddress.city || this.shippingCities[city] === shippingAddress.city) {
            this.changeShipAddress.patchValue({
              city: this.shippingCities[city]
            });
            break;
          }
        }
      }
    });
  }

  setShipAddressForm(shippingAddress): void {
    this.changeShipAddress.patchValue({
      businessName: shippingAddress.businessName,
      email: shippingAddress.email,
      phoneNumber: shippingAddress.phoneNumber,
      address1: shippingAddress.address1,
      address2: shippingAddress.address2,
      address3: shippingAddress.address3,
      city: shippingAddress.city,
      subCity: shippingAddress.subCity,
      state: shippingAddress.state,
      zip5: this.user.locale && this.user.locale.toLowerCase() === 'en_us' ? this.shippingAddressService.format5DigitZipCode(shippingAddress.zip5) : shippingAddress.zip5
    });
  }

  setAddressData(selectedAddress): void {
    if (Object.keys(selectedAddress).length > 0) {
      this.changeShipAddress.patchValue({
        phoneNumber: selectedAddress.phoneNumber,
        address1: selectedAddress.address1,
        address2: selectedAddress.address2,
        address3: selectedAddress.address3,
        city: selectedAddress.city,
        subCity: selectedAddress.subCity,
        state: selectedAddress.state,
        zip5: this.user.locale && this.user.locale.toLowerCase() === 'en_us' ? this.shippingAddressService.format5DigitZipCode(selectedAddress.zip5) : selectedAddress.zip5
      });
      this.shipAddress.phoneNumber = selectedAddress.phoneNumber;
      this.shipAddress.address1 = selectedAddress.address1;
      this.shipAddress.address2 = selectedAddress.address2;
      this.shipAddress.address3 = selectedAddress.address3;
      this.shipAddress.city = selectedAddress.city;
      this.shipAddress.subCity = selectedAddress.subCity;
      this.shipAddress.state = selectedAddress.state;
      this.shipAddress.zip5 = this.user.locale && this.user.locale.toLowerCase() === 'en_us' ? this.shippingAddressService.format5DigitZipCode(selectedAddress.zip5) : selectedAddress.zip5;
    } else {
      this.changeShipAddress.patchValue({
        phoneNumber: '',
        address1: '',
        address2: '',
        address3: '',
        city: '',
        subCity: '',
        state: '',
        zip5: ''
      });
      this.shipAddress.phoneNumber = '';
      this.shipAddress.address1 = '';
      this.shipAddress.address2 = '';
      this.shipAddress.address3 = '';
      this.shipAddress.city = '';
      this.shipAddress.subCity = '';
      this.shipAddress.state = '';
      this.shipAddress.zip5 = null;
      this.ctnButtonEnabled = true;
    }
  }

  setShippingAddressName(shippingAddress, type?: string): void {
    if (type === 'concat') {
      let receipentName;
      if (shippingAddress.firstName !== '' && shippingAddress.lastName !== '') {
        if (this.reverseNameOrder) {
          receipentName = shippingAddress.lastName.concat(' ', shippingAddress.firstName).trim();
        } else {
          receipentName = shippingAddress.firstName.concat(' ', shippingAddress.lastName).trim();
        }
        this.changeShipAddress.patchValue({
          name: receipentName
        });
      }
    } else {
      if (!shippingAddress.name) {
        this.changeShipAddress.patchValue({
          name: ''
        });
      }
      let name = shippingAddress.name.trim();
      const spaceExist = name.indexOf(' ');
      const nameLength = name.length;
      if (nameLength > 35) {
        name = name.substring(0, 35);
      }
      if (spaceExist > 0) {
        name = name.replace(/  +/g, ' ');
        // Reverse the order of fullName for zh_TW locale
        if (this.reverseNameOrder) {
          const nameArr = name.split(' ');
          name = nameArr.pop() + ' ' + nameArr.join(' ');
        }
        const lastNameTemp = name.substr(name.indexOf(' ') + 1);
        if (lastNameTemp !== '' && lastNameTemp.length >= 1) {
          this.shipAddress.firstName = name.substr(0, name.indexOf(' '));
          this.shipAddress.lastName = lastNameTemp;
          Object.assign(this.shipAddress, this.changeShipAddress.value);
          delete this.shipAddress['name'];
        } else {
          this.changeShipAddress.get('name').setErrors({invalid: true});
          this.errorMessage.name = this.messages.invalidName;
        }
      } else if (name !== '') {
        this.changeShipAddress.get('name').setErrors({invalid: true});
        this.errorMessage.name = this.messages.invalidName;
      }
    }
  }

  multiAddressSelect(addressId: number): void {
    let selectedAddress: {[key: string]: any} = {};
    if (addressId !== undefined) {
      this.user.addresses.forEach(address => {
        if (addressId === address.addressId) {
          selectedAddress = address;
        }
      });
    }
    this.setAddressData(selectedAddress);
    this.shipAddress.selectedAddressId = selectedAddress.addressId ? selectedAddress.addressId : -1;
    this.shipAddress.selectedAddressName = selectedAddress.addressName ? selectedAddress.addressName : '';
    this.extras.hideAddressFields = !this.overrideShipping;
    if (addressId !== undefined) {
      this.changeShipAddress.markAsDirty();
      this.changeShipAddress.updateValueAndValidity();
    }
    this.checkForUSZip();
  }

  updateMultiAddressError(): void {
    if (this.isMultiAddress && this.extras.hideAddressFields ){
      const excludeError = ['businessName', 'name', 'phoneNumber', 'email'];
      for (const errorKey of Object.keys(this.errorMessage)) {
        if (excludeError.indexOf(errorKey) === -1) {
          this.errorMessage.selectedAddressName = this.errorMessage[errorKey];
          break;
        }
      }
    }
  }

  updateAddressErrors(): void {
    if (this.errorMessage.firstName && this.errorMessage.lastName) {
      this.errorMessage.name = this.errorMessage.firstName;
      delete this.errorMessage.firstName;
      delete this.errorMessage.lastName;
    } else if (this.errorMessage.firstName) {
      this.errorMessage.name = this.errorMessage.firstName;
      delete this.errorMessage.firstName;
    } else if (this.errorMessage.lastName) {
      this.errorMessage.name = this.errorMessage.lastName;
      delete this.errorMessage.lastName;
    }
    this.updateMultiAddressError();
  }

  clearErrors(): void {
    for (const key of Object.keys(this.changeShipAddress.controls)) {
      if (this.changeShipAddress.controls[key]) {
        this.nameStatusSub[key] = this.changeShipAddress.controls[key].statusChanges.subscribe(value => {
          if (this.errorMessage[key]) {
            this.errorMessage[key] = '';
          }
        });
      }
    }
  }

  checkForUSZip(): void {
    if (this.user.locale.toLowerCase() === 'en_us') {
      if (this.changeShipAddress.get('zip5') && this.changeShipAddress.get('zip5').value !== '') {
        this.changeShipAddress.get('zip5').setValue(this.shippingAddressService.format5DigitZipCode(this.changeShipAddress.get('zip5').value));
        if (this.errorMessage.selectedAddressName && this.errorMessage.selectedAddressName === this.messages.maximumZipCharacters) {
          delete this.errorMessage.selectedAddressName;
        }
      }
    }
  }

  ngOnDestroy() {
    for (const key of Object.keys(this.nameStatusSub)) {
        this.nameStatusSub[key].unsubscribe();
    }
  }

}

