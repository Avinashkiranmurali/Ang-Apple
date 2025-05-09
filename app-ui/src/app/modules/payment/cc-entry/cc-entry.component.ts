import { Component, Injector, OnInit } from '@angular/core';
import { User } from '@app/models/user';
import { Config } from '@app/models/config';
import { PaymentStoreService } from '@app/state/payment-store.service';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateService } from '@ngx-translate/core';
import { TitleCasePipe } from '@angular/common';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { Router } from '@angular/router';
import { BreakPoint } from '@app/components/utils/break-point';
import { Program } from '@app/models/program';
import { PaymentService } from '@app/services/payment.service';
import { Address } from '@app/models/address';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { isEmpty } from 'lodash';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { TransitionService } from '@app/transition/transition.service';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';

@Component({
  selector: 'app-cc-entry',
  templateUrl: './cc-entry.component.html',
  styleUrls: ['./cc-entry.component.scss']
})

export class CcEntryComponent extends BreakPoint implements OnInit {
  displayOrderSummOnMobile: boolean;
  isUnbundled;
  state;
  payFrequency;
  paymentRequired;
  pointsUsed: number;
  pointLabel;
  user: User;
  config: Config;
  purchasePaymentOption;
  program: Program;
  translateParams: { [key: string]: string };
  selectPaymentsForm: FormGroup;
  useSameShippingAddress: boolean;
  shippingAddress: Address;
  postCardDetailsError: Array<{[key: string]: string}> = [];
  cardType: string;
  formFields = [];
  constructor(
    public injector: Injector,
    private stateService: PaymentStoreService,
    private pricingService: PricingService,
    private userStore: UserStoreService,
    private translateService: TranslateService,
    private titleCasePipe: TitleCasePipe,
    private currencyPipe: CurrencyPipe,
    private router: Router,
    private fb: FormBuilder,
    private paymentService: PaymentService,
    private currencyFormatPipe: CurrencyFormatPipe,
    private ensightenService: EnsightenService,
    private transitionService: TransitionService
  ) {
    super(injector);
    this.displayOrderSummOnMobile = false;
    this.user = this.userStore.user;
    this.program = this.userStore.program;
    this.config = this.userStore.config;

    if (this.program.formatPointName !== '') {
      this.pointLabel = this.translateService.instant(this.program.formatPointName);
      this.translateParams = {
        pointLabel: this.titleCasePipe.transform(this.pointLabel)
      };
    }

    this.stateService.get().subscribe(data => {
      this.state =  data;
      const params = { ...this.translateParams };

      if (this.state.cart) {
        if (this.state.cart.cartTotal) {
          params.points = this.currencyFormatPipe.transform(this.state.cart.cartTotal.price.points, this.userStore.config.pricingTemplate , this.user.locale);
          if (this.config.showDecimal) {
            params.amount = this.currencyPipe.transform(this.state.cart.cartTotal.price.amount);
          } else {
            params.amount = this.currencyPipe.transform(this.state.cart.cartTotal.price.amount, '', 'symbol', '1.0-0');
          }
        }
      }
      this.translateParams = params;
    });
    const pricingOptions = this.pricingService.getPricingOption();
    this.isUnbundled = pricingOptions['isUnbundled'];
    this.selectPaymentsFormInit();
  }

  ngOnInit(): void {
    this.payFrequency = this.config['payFrequency'] || '';

    this.stateService.get().subscribe(data => {
      this.state = data;
    });
    this.paymentService.getPaymentTransactionApi().subscribe((response) =>  {
      if (response) {
        sessionStorage.setItem('paymentApiDet', JSON.stringify(response));
        // TODO
        // $rootScope.closeTransition();
        // return true;
      } else {
        // return false;
        // TODO
        // $rootScope.closeTransition();
        // $state.go('store.cart');
      }
    });

    // Analytics object
    const userAnalyticsObj = {
      pgName: '',
      pgType: '',
      pgSectionType: ''
    };
    this.ensightenService.broadcastEvent(userAnalyticsObj, []);
  }

  selectPaymentsFormInit() {
    const expirationDatePattern = '(0[1-9]|1[0-2])\/([[0-9]{2})';
    const securityCodePattern = new RegExp('^[0-9]{3,4}$');
    this.selectPaymentsForm = this.fb.group({
      cardForm: this.fb.group({
        cardNumber: ['', [Validators.required, this.creditCardValidation()]],
        expiration: ['', [Validators.required, Validators.pattern(expirationDatePattern)]],
        securityCode: ['', [Validators.required, Validators.pattern(securityCodePattern)]],
        billingAddress: this.fb.group({
          useSameShippingAddress: [true],
          address1: [''],
          address2: [''],
          address3: [''],
          city: [''],
          state: [''],
          input_state: [''],
          zip5: ['']
        })
      })
    });

    const billingAddressForm = this.selectPaymentsForm.get('cardForm').get('billingAddress');
    billingAddressForm.get('useSameShippingAddress').valueChanges.subscribe(value => {
      if (value) {
        billingAddressForm.get('address1').clearValidators();
        billingAddressForm.get('city').clearValidators();
        billingAddressForm.get('state').clearValidators();
        billingAddressForm.get('input_state').clearValidators();
        billingAddressForm.get('zip5').clearValidators();
      } else {
        billingAddressForm.get('address1').setValidators(Validators.required);
        if (this.user.country !== 'SG') {
          billingAddressForm.get('city').setValidators(Validators.required);
        } else {
          billingAddressForm.get('city').clearValidators();
        }
        if (['US', 'CA', 'MX', 'AU'].indexOf(this.user.country) > -1) {
          billingAddressForm.get('state').setValidators(Validators.required);
        } else {
          billingAddressForm.get('state').clearValidators();
        }
        if (['PH', 'MY'].indexOf(this.user.country) > -1) {
          billingAddressForm.get('input_state').setValidators(Validators.required);
        } else {
          billingAddressForm.get('input_state').clearValidators();
        }
        if (['HK', 'AE'].indexOf(this.user.country) === -1) {
          billingAddressForm.get('zip5').setValidators(Validators.required);
        } else {
          billingAddressForm.get('zip5').clearValidators();
        }
      }
      billingAddressForm.get('address1').updateValueAndValidity();
      billingAddressForm.get('city').updateValueAndValidity();
      billingAddressForm.get('state').updateValueAndValidity();
      billingAddressForm.get('input_state').updateValueAndValidity();
      billingAddressForm.get('zip5').updateValueAndValidity();
    });
  }

  creditCardValidation(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;
      const cardFormatPattern = new RegExp('^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11}|(?:(62|88)\\d{14,17}))$', 'mg');
      if (!value) {
        return null;
      }
      if (cardFormatPattern.test(value)) {
        return null;
      } else {
        return {creditCardPattern: true};
      }
    };
  }

  toggleOrderSummary(){
    this.displayOrderSummOnMobile = !this.displayOrderSummOnMobile;
  }

  backToPaymentPage() {
    delete this.state.selections.payment.splitPayOption;
    this.state.selections.payment.paySummaryTemplate = '';
    delete this.state.selections.payment.pointsToUse;
    delete this.state.selections.payment.cashToUse;
    this.stateService.set(this.state);
    this.router.navigate(['/store', 'payment']);
  }

  getCardType(cardType: string) {
    this.cardType = cardType;
  }

  postCardDetails() {
    // TODO Form Submission Handling
    //
    //   /**
    //    * Hook into angularjs form validation with
    //    * $scope.selectPaymentsForm
    //    */
    //
    this.useSameShippingAddress = this.selectPaymentsForm.value.cardForm.billingAddress.useSameShippingAddress;
    const expiration = this.selectPaymentsForm.get('cardForm').get('expiration').value;
    // we will get the value expiration month and year '01/20' to ['01','20']
    const MonthYear = expiration.split('/');
    let addressDetails = {};
    this.shippingAddress = this.state.cart.shippingAddress;
    let subCity;
    if (this.useSameShippingAddress) {
      addressDetails = this.shippingAddress;
      subCity = (this.shippingAddress.subCity) ? this.shippingAddress.subCity : undefined;
    } else {
      addressDetails = this.selectPaymentsForm.value.cardForm.billingAddress;
      addressDetails['country'] = this.shippingAddress?.country;

    }
    let streetAddress = (addressDetails['address3']) ? addressDetails['address2'].concat((addressDetails['address2']) ? ',' : '', addressDetails['address3']).trim() : addressDetails['address2'];
    streetAddress = subCity ? streetAddress.concat(',', subCity).trim() : streetAddress;
    const CCEntryDetails = {
      firstName: this.shippingAddress.firstName,
      lastName: this.shippingAddress.lastName,
      ccUsername: this.shippingAddress.firstName + ' ' + this.shippingAddress.lastName,
      addr1: addressDetails['address1'],
      addr2: streetAddress,
      addr3: '',
      city: addressDetails['city'],
      state: addressDetails['state'],
      zip: addressDetails['zip5'],
      country: addressDetails['country'],
      phoneNumber: null,
      ccType: this.cardType,
      ccNum: this.selectPaymentsForm.get('cardForm').get('cardNumber').value,
      ccMon: MonthYear[0],
      ccYear: MonthYear[1],
      ccName: null,
      ccCCV: this.selectPaymentsForm.get('cardForm').get('securityCode').value,
      last4: this.selectPaymentsForm.get('cardForm').get('cardNumber').value.slice(this.selectPaymentsForm.get('cardForm').get('cardNumber').value.length - 4)
    };
    //   /!**
    //    * This is the data call to submit card and address details and gets the errors
    //    *!/
    this.paymentService.postPaymentDetails(CCEntryDetails).subscribe((response) => {
      this.transitionService.openTransition('credit-form-success', '', false);
      this.postCardDetailsError = [];
      delete CCEntryDetails.ccNum;
      delete CCEntryDetails.ccCCV;
      delete CCEntryDetails.ccMon;
      delete CCEntryDetails.ccYear;
      this.paymentService.postCardDetails(CCEntryDetails).subscribe((result) => {
      }, error => {
      });
      setTimeout(() => { this.transitionService.closeTransition(); }, 200);
      setTimeout(() => {
        this.router.navigate(['/store', 'checkout']);
      }, 300);
    }, error => {
      this.transitionService.closeTransition();
      if (error.status === 401 || error.status === 0) {
        // this.sessionService.showTimeout();
        this.postCardDetailsError = [{error: 'Error'}];
      } else {
        this.formFields = [CCEntryDetails].map(field => field);
        this.formErrorProcessing(error.error);
      }
    });
    // Stops progress in this case
    return false;
  }

  formErrorProcessing(errorResponse) {
      const intersection = [errorResponse].filter(x => this.formFields.includes(x));
      if ( intersection.length === 0 ) {
        // displaying shipping address form with errors
        this.useSameShippingAddress = false; // TODO pass the useSameShippingAddress state [from/to] billing-address component
      }
      this.postCardDetailsError = [];
      if (Object.prototype.toString.call( errorResponse ) === '[object Array]' && errorResponse.length > 0) {
        this.postCardDetailsError = errorResponse;
      } else {
        if (!isEmpty(errorResponse)) {
          this.postCardDetailsError.push(errorResponse);
        }
      }
    }
}
