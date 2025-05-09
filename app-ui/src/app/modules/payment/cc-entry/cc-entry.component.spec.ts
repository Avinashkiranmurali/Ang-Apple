import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { CcEntryComponent } from './cc-entry.component';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Component, Input } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { TitleCasePipe, DecimalPipe } from '@angular/common';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { RouterTestingModule } from '@angular/router/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { UserStoreService } from '@app/state/user-store.service';
import { Program } from '@app/models/program';
import { PaymentStoreService } from '@app/state/payment-store.service';
import { CardComponent } from './card/card.component';
import { PaymentSummaryComponent } from '../payment-summary/payment-summary.component';
import { DataMaskingModule } from '@bakkt/data-masking';
import { Address } from '@app/models/address';
import { Router } from '@angular/router';
import { PaymentService } from '@app/services/payment.service';
import { OrderSummaryComponent } from '../order-summary/order-summary.component';
import { PricingTempComponent } from '@app/modules/pricing/pricing-temp/pricing-temp.component';
import { CartTotalTempComponent } from '@app/modules/pricing/cart-total-temp/cart-total-temp.component';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { of } from 'rxjs';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';

@Component({
  selector: 'app-billing-address',
  template: ''
})
class MockBillingAddressComponent {
  @Input() postCardDetailsError;
  @Input() state;
  @Input() addressFields;
  @Input() shippingAddress: Address;
  @Input() billingAddress: Address;
}

describe('CcEntryComponent', () => {
  let component: CcEntryComponent;
  let fixture: ComponentFixture<CcEntryComponent>;
  let userStore: UserStoreService;
  let stateService: PaymentStoreService;
  let stateData = null;
  let httpTestingController: HttpTestingController;
  const cartData = require('assets/mock/cart.json');
  const programData: Program = require('assets/mock/program.json');
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };
  const transactionApiResponse = {
    url: 'https://pay-vip.aplqa2.bridge2solutions.net/paymentserver/api/payment/test',
    transactionId: 'VUF8YjJzX3FhX29ubHl8ZXJpY3w4RTlCRjcyNkZGRjg5NjIyQjVFRkY3QzRDODRFMz'
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        CcEntryComponent,
        CardComponent,
        PaymentSummaryComponent,
        MockBillingAddressComponent,
        OrderSummaryComponent,
        PricingTempComponent,
        CartTotalTempComponent,
        CurrencyFormatPipe
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        FormsModule,
        ReactiveFormsModule,
        TranslateModule.forRoot(),
        DataMaskingModule
      ],
      providers: [
        TitleCasePipe,
        DecimalPipe,
        CurrencyPipe,
        CurrencyFormatPipe,
        NgbActiveModal,
        PricingService,
        PaymentService,
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate') }
        },
        { provide: EnsightenService, useValue: {
          broadcastEvent: () => {}
        }}
      ]
    })
    .compileComponents();
    userStore = TestBed.inject(UserStoreService);
    stateService = TestBed.inject(PaymentStoreService);
    httpTestingController = TestBed.inject(HttpTestingController);
    userStore.addUser(userData.user);
    userStore.addProgram(userData.program);
    userStore.addConfig(userData.config);
    stateData = stateService.getInitial();
    stateData['cart'] = cartData;
    stateData['selections'] = {
      payment: {
        splitPayOption: {
          cashToUse: 200.00
        }
      }
    };
    spyOn(stateService, 'get').and.returnValue(of(stateData));
    spyOn(stateService, 'set').and.callFake(() => {});
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CcEntryComponent);
    component = fixture.componentInstance;
    component.state = stateData;
    component.shippingAddress = cartData['shippingAddress'];
    component.displayOrderSummOnMobile = false;
    fixture.detectChanges();
  });

  it('should create', () => {
    // Expect a call to this URL - Return true for every call to the match predicate since it has nocache parameter
    const req = httpTestingController.expectOne(() => true);
    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');
    // Respond with the fake/null data when called
    req.flush(transactionApiResponse);
    expect(component).toBeTruthy();
  });

  it('should create instance with payFrequency', () => {
    component.user['program']['config']['payFrequency'] = 'TEST';
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.payFrequency).toEqual('TEST');
  });

  it('should create instance without payFrequency value', () => {
    component.user['program']['config']['payFrequency'] = '';
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.payFrequency).toEqual('');
  });

  it('should create toggleOrderSummary', () => {
    component.toggleOrderSummary();
    expect(component.displayOrderSummOnMobile).toBeTruthy();
  });

  it('should call backToPaymentPage', () => {
    spyOn(component, 'backToPaymentPage').and.callThrough();
    component.backToPaymentPage();
    expect(component.backToPaymentPage).toHaveBeenCalled();
  });

  it('should call getCardType', () => {
    spyOn(component, 'getCardType').and.callThrough();
    component.getCardType('VISA');
    expect(component.getCardType).toHaveBeenCalled();
  });

  it('should call postCardDetails', waitForAsync(() => {
    component.selectPaymentsForm.patchValue(
      {
        cardForm: {
          cardNumber: '3734567890123457',
          expiration: '02/23',
          securityCode: '747',
          billingAddress: {
            useSameShippingAddress: true,
            address1: '5900 Windward Pkwy',
            address2: 'Ste 450',
            address3: 'street',
            city: 'Alpharetta',
            state: 'GA',
            input_state: '',
            zip5: '30005'
          }
        }
      });
    sessionStorage.setItem('paymentApiDet', JSON.stringify(transactionApiResponse));
    spyOn(component, 'postCardDetails').and.callThrough();
    component.postCardDetails();
    const fakeResponse = { code: 1, message: 'Success'};
    // Expect a call to this URL - Return true for every call to the match predicate since it has nocache parameter
    const req = httpTestingController.expectOne(transactionApiResponse.url);
    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');
    // Respond with the fake data when called
    req.flush(fakeResponse);
    // Expect a call to this URL - Return true for every call to the match predicate since it has nocache parameter
    const paymentReq = httpTestingController.expectOne('/apple-gr/service/ccEntry/init');
    // Assert that the request is a POST
    expect(paymentReq.request.method).toEqual('POST');
    // Respond with the fake data when called
    paymentReq.flush(fakeResponse);
    expect(component.postCardDetails).toHaveBeenCalled();
  }));

  it('should call postCardDetails - post card failure', waitForAsync(() => {
    component.selectPaymentsForm.patchValue(
      {
        cardForm: {
          cardNumber: '3734567890123457',
          expiration: '02/23',
          securityCode: '747',
          billingAddress: {
            useSameShippingAddress: true,
            address1: '5900 Windward Pkwy',
            address2: 'Ste 450',
            address3: 'street',
            city: 'Alpharetta',
            state: 'GA',
            input_state: '',
            zip5: '30005'
          }
        }
      });
    sessionStorage.setItem('paymentApiDet', JSON.stringify(transactionApiResponse));
    spyOn(component, 'postCardDetails').and.callThrough();
    component.postCardDetails();
    const fakeResponse = { code: 1, message: 'Success'};
    // Expect a call to this URL - Return true for every call to the match predicate since it has nocache parameter
    const req = httpTestingController.expectOne(transactionApiResponse.url);
    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');
    // Respond with the fake data when called
    req.flush(fakeResponse);
    // Expect a call to this URL - Return true for every call to the match predicate since it has nocache parameter
    const paymentReq = httpTestingController.expectOne('/apple-gr/service/ccEntry/init');
    // Assert that the request is a POST
    expect(paymentReq.request.method).toEqual('POST');
    // Respond with the fake data when called
    paymentReq.flush('Failure', { status: 404, statusText: 'Not Found' });
    expect(component.postCardDetails).toHaveBeenCalled();
  }));

  it('should call postCardDetails - cc entry failure', waitForAsync(() => {
    component.selectPaymentsForm.patchValue(
      {
        cardForm: {
          cardNumber: '3734567890123457',
          expiration: '02/23',
          securityCode: '747',
          billingAddress: {
            useSameShippingAddress: true,
            address1: '5900 Windward Pkwy',
            address2: 'Ste 450',
            address3: 'street',
            city: 'Alpharetta',
            state: 'GA',
            input_state: '',
            zip5: '30005'
          }
        }
      });
    sessionStorage.setItem('paymentApiDet', JSON.stringify(transactionApiResponse));
    spyOn(component, 'postCardDetails').and.callThrough();
    component.postCardDetails();
    const error = {
      creditCard: 'Please enter creditcard number'
    };
    // Expect a call to this URL - Return true for every call to the match predicate since it has nocache parameter
    const req = httpTestingController.expectOne(transactionApiResponse.url);
    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');
    // Respond with the fake data when called
    req.flush(error, { status: 404, statusText: 'Not Found' });
    expect(component.postCardDetails).toHaveBeenCalled();
  }));

  it('should call postCardDetails - cc entry failure else check', waitForAsync(() => {
    component.selectPaymentsForm.patchValue(
      {
        cardForm: {
          cardNumber: '3734567890123457',
          expiration: '02/23',
          securityCode: '747',
          billingAddress: {
            useSameShippingAddress: false,
            address1: '5900 Windward Pkwy',
            address2: 'Ste 450',
            address3: 'street',
            city: 'Alpharetta',
            state: 'GA',
            input_state: '',
            zip5: '30005'
          }
        }
      });
    sessionStorage.setItem('paymentApiDet', JSON.stringify(transactionApiResponse));
    spyOn(component, 'postCardDetails').and.callThrough();
    component.postCardDetails();
    const error = [{ccNMumber: 'Please enter creditcard number' }];
    // Expect a call to this URL - Return true for every call to the match predicate since it has nocache parameter
    const req = httpTestingController.expectOne(transactionApiResponse.url);
    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');
    // Respond with the fake data when called
    req.flush(error, { status: 404, statusText: 'Not Found' });
    expect(component.postCardDetails).toHaveBeenCalled();
  }));

  it('should call postCardDetails - cc entry failure if check', waitForAsync(() => {
    component.selectPaymentsForm.patchValue(
      {
        cardForm: {
          cardNumber: '3734567890123457',
          expiration: '02/23',
          securityCode: '747',
          billingAddress: {
            useSameShippingAddress: false,
            address1: '5900 Windward Pkwy',
            address2: 'Ste 450',
            address3: 'street',
            city: 'Alpharetta',
            state: 'GA',
            input_state: '',
            zip5: '30005'
          }
        }
      });
    sessionStorage.setItem('paymentApiDet', JSON.stringify(transactionApiResponse));
    spyOn(component, 'postCardDetails').and.callThrough();
    component.postCardDetails();
    const error = [{ccNMumber: 'Please enter creditcard number' }];
    // Expect a call to this URL - Return true for every call to the match predicate since it has nocache parameter
    const req = httpTestingController.expectOne(transactionApiResponse.url);
    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');
    // Respond with the fake data when called
    req.flush(error, { status: 401, statusText: 'Not Found' });
    expect(component.postCardDetails).toHaveBeenCalled();
  }));

  it('should call selectPaymentsFormInit whenever useSameShippingAddress changes - SG Country', () => {
    component.selectPaymentsForm.patchValue(
      {
        cardForm: {
          billingAddress: {
            useSameShippingAddress: true,
          }
        }
      });
    component.user.country = 'SG';
    fixture.detectChanges();
    component.selectPaymentsForm.patchValue(
      {
        cardForm: {
          billingAddress: {
            useSameShippingAddress: false,
          }
        }
      });
    component.user.country = 'SG';
    fixture.detectChanges();
    expect(component.user.country).toEqual('SG');
  });

  it('should call selectPaymentsFormInit whenever useSameShippingAddress changes - PH Country', () => {
    component.selectPaymentsForm.patchValue(
      {
        cardForm: {
          billingAddress: {
            useSameShippingAddress: true,
          }
        }
      });
    component.user.country = 'PH';
    fixture.detectChanges();
    component.selectPaymentsForm.patchValue(
      {
        cardForm: {
          billingAddress: {
            useSameShippingAddress: false,
          }
        }
      });
    component.user.country = 'PH';
    fixture.detectChanges();
    expect(component.user.country).toEqual('PH');
  });

  it('should call selectPaymentsFormInit whenever useSameShippingAddress changes - CA Country', () => {
    component.selectPaymentsForm.patchValue(
      {
        cardForm: {
          billingAddress: {
            useSameShippingAddress: true,
          }
        }
      });
    component.user.country = 'CA';
    fixture.detectChanges();
    component.selectPaymentsForm.patchValue(
      {
        cardForm: {
          billingAddress: {
            useSameShippingAddress: false,
          }
        }
      });
    component.user.country = 'CA';
    fixture.detectChanges();
    expect(component.user.country).toEqual('CA');
  });

  it('should call selectPaymentsFormInit whenever useSameShippingAddress changes - HK Country', () => {
    component.selectPaymentsForm.patchValue(
      {
        cardForm: {
          billingAddress: {
            useSameShippingAddress: true,
          }
        }
      });
    component.user.country = 'HK';
    fixture.detectChanges();
    component.selectPaymentsForm.patchValue(
      {
        cardForm: {
          billingAddress: {
            useSameShippingAddress: false,
          }
        }
      });
    component.user.country = 'HK';
    fixture.detectChanges();
    expect(component.user.country).toEqual('HK');
  });

});
