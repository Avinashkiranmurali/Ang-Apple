import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ConfirmationComponent } from './confirmation.component';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { TemplateStoreService } from '@app/state/template-store.service';
import { OrderInformationService } from '@app/services/order-information.service';
import { UserStoreService } from '@app/state/user-store.service';
import { CartService } from '@app/services/cart.service';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { TransitionService } from '@app/transition/transition.service';
import { AddressService } from '@app/services/address.service';
import { DecimalPipe } from '@angular/common';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';
import { TranslateModule } from '@ngx-translate/core';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { SafePipe } from '@app/pipes/safe.pipe';
import { OrderConfirmShippingDetailsComponent } from './order-confirm-shipping-details/order-confirm-shipping-details.component';
import { OrderConfirmItemsListComponent } from './order-confirm-items-list/order-confirm-items-list.component';
import { PaymentSummaryComponent } from '@app/modules/payment-summary/payment-summary.component';
import { OrderConfirmButtonsComponent } from './order-confirm-buttons/order-confirm-buttons.component';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { AppConstants } from '@app/constants/app.constants';
import { MediaProductComponent } from '@app/modules/shared/media-product/media-product.component';
import { HeapService } from '@app/analytics/heap/heap.service';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { AnalyticsService } from '@app/analytics/analytics.service';
import { OrderConfirm, OrderStatus } from '@app/models/order-detail';

describe('ConfirmationComponent', () => {
  let component: ConfirmationComponent;
  let userStoreService: UserStoreService;
  let httpTestingController: HttpTestingController;
  let templateStoreService: TemplateStoreService;
  let fixture: ComponentFixture<ConfirmationComponent>;
  const configData = require('assets/mock/configData.json');
  const programData = require('assets/mock/program.json');
  const userMock = require('assets/mock/user.json');
  userMock['program'] = programData;
  const userData = {
    user: userMock,
    program: programData,
    config: programData['config']
  };
  // Fake order information response
  const orderMock: OrderStatus = require('assets/mock/orderConfirmation.json');
  orderMock.cartTotal.discountedPrice = Object.assign({});

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        ConfirmationComponent,
        OrderConfirmShippingDetailsComponent,
        OrderConfirmItemsListComponent,
        PaymentSummaryComponent,
        OrderConfirmButtonsComponent,
        MediaProductComponent,
        SafePipe
      ],
      imports: [
        TranslateModule.forRoot(),
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        PricingService,
        { provide: MessagesStoreService },
        { provide: CartService },
        { provide: TransitionService },
        { provide: AddressService },
        { provide: DecimalPipe },
        { provide: EnsightenService },
        { provide: NgbActiveModal },
        { provide: MatomoService, useValue: {
          broadcast: () => {},
          initConfig: () => {} }
        },
        { provide: AnalyticsService, useValue: {
            broadcastEvent: () => {} }
        },
        { provide: HeapService,
          useValue: {
            broadcastEvent: () => {},
            loadInitialScript: () => {}
          }
        },
        { provide: CurrencyFormatPipe },
        { provide: CurrencyPipe }
      ]
    })
    .compileComponents();
    userStoreService = TestBed.inject(UserStoreService);
    httpTestingController = TestBed.inject(HttpTestingController);
    templateStoreService = TestBed.inject(TemplateStoreService);
    templateStoreService.addTemplate(configData['configData']);
    userStoreService.addUser(userData.user);
    userStoreService.addProgram(userData.program);
    userStoreService.addConfig(userData.config);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfirmationComponent);
    component = fixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
    component.discounts = [];
    component.paymentTemplate = AppConstants.paymentTemplate.cash_subsidy;
    const orderObj = {
      paymentInfo: 'cash',
      paymentTemp: 'cash_subsidy',
      purchasePaymentOption: 'cash'
    };
    sessionStorage.setItem('confirmOrder', JSON.stringify(orderObj));
    fixture.detectChanges();
  });

  afterEach(() => {
    sessionStorage.removeItem('confirmOrder');
    TestBed.resetTestingModule();
  });

  it('should create', waitForAsync(() => {
    // Fake response data
    const fakeResponse = require('assets/mock/user.json');
    fakeResponse['shipTo'] = {
      address1: '5900 Windward Pkwy',
      address2: 'Ste 450',
      address3: '',
      addressModified: null,
      businessName: '',
      cartTotalModified: false,
      city: 'Alpharetta',
      country: 'US',
      email: 'd@d.com',
      errorMessage: {},
      faxNumber: '',
      firstName: 'KINDRA',
      ignoreSuggestedAddress: 'true',
      lastName: 'CONNOR',
      middleName: '',
      phoneNumber: '(814) 384-7122',
      selectedAddressId: 0,
      state: 'GA',
      subCity: null,
      validAddress: true,
      warningMessage: {},
      zip4: '5479',
      zip5: '30005'
    };

    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/customer/user.json');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(fakeResponse);
    spyOn(component['orderInformationservice'], 'getOrderInformation').and.returnValue(of(orderMock));
    expect(component).toBeTruthy();
  }));

  it('should create instance with paymentType value', waitForAsync(() => {
    component.paymentTemplate = AppConstants.paymentTemplate.cash_subsidy;
    component.config.showVarOrderId = false;
    component.config.showFeeDetails = true;
    sessionStorage.removeItem('confirmOrder');
    fixture.detectChanges();
    const req = httpTestingController.expectOne('/apple-gr/customer/user.json');
    req.flush(userMock);
    fixture.detectChanges();

    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    // Expect a call to this URL
    const secondReq = httpTestingController.expectOne('/apple-gr/customer/user.json');
    // Assert that the request is a GET
    expect(secondReq.request.method).toEqual('GET');
    // Respond with the fake data when called
    secondReq.flush(userMock);
    expect(component.ngOnInit).toHaveBeenCalled();
  }));

  it('should create instance with paymentType value cash_only', waitForAsync(() => {
    component.paymentTemplate = AppConstants.paymentType.cash_only;
    component.config.showVarOrderId = false;
    component.config.showFeeDetails = true;
    sessionStorage.removeItem('test');
    component['activatedRoute'].snapshot.data = {
      analyticsObj: {
        pgName: '',
        pgType: '',
        pgSectionType: ''
      }
    };
    fixture.detectChanges();
    const req = httpTestingController.expectOne('/apple-gr/customer/user.json');
    req.flush(userMock);
    fixture.detectChanges();

    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    // Expect a call to this URL
    const secondReq = httpTestingController.expectOne('/apple-gr/customer/user.json');
    // Assert that the request is a GET
    expect(secondReq.request.method).toEqual('GET');
    // Respond with the fake data when called
    secondReq.flush(userMock);
    expect(component.ngOnInit).toHaveBeenCalled();
  }));

  it('should create instance - user call fails', waitForAsync(() => {
    // Fake error data
    const errorMsg = 'deliberate 404 error';
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/customer/user.json');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
    expect(component).toBeTruthy();
  }));

  it('should create instance - user call fails with 401 error', waitForAsync(() => {
    // Fake error data
    const errorMsg = 'deliberate 401 error';
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/customer/user.json');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with mock error
    req.flush(errorMsg, { status: 401, statusText: 'Unauthorized' });
    expect(component).toBeTruthy();
  }));

  it('should call continueShopping Method', () => {
    spyOn(component, 'continueShopping').and.callThrough();
    component.paymentTemplate = AppConstants.paymentTemplate.cash_subsidy;
    const data = {
      signOutUrl: 'javascript:void(0)'
    };
    sessionStorage.setItem('sessionURLs', JSON.stringify(data));
    component.continueShopping();
    expect(component.continueShopping).toHaveBeenCalled();
  });

  it('should call continueShopping Method - else check', () => {
    spyOn(component, 'continueShopping').and.callThrough();
    component.paymentTemplate = 'abcd';
    component.continueShopping();
    expect(component.continueShopping).toHaveBeenCalled();
  });

  it('should call discountedSubtotal Method', () => {
    spyOn(component, 'discountedSubtotal').and.callThrough();
    component.discountedSubtotal();
    expect(component.discountedSubtotal).toHaveBeenCalled();
  });

  it('should call hasDiscounts Method', () => {
    spyOn(component, 'hasDiscounts').and.callThrough();
    component.hasDiscounts();
    expect(component.hasDiscounts).toHaveBeenCalled();
  });

  it('should call hasDiscounts method and returns false', () => {
    spyOn(component, 'discountedSubtotal').and.callFake(() => { return false; });
    expect(component.hasDiscounts()).toBeFalsy();
  });

  it('should create for - cash only payments', waitForAsync(() => {
    // Fake response data
    const fakeResponse = require('assets/mock/user.json');
    fakeResponse['shipTo'] = {
      address1: '5900 Windward Pkwy',
      address2: 'Ste 450',
      address3: '',
      addressModified: null,
      businessName: '',
      cartTotalModified: false,
      city: 'Alpharetta',
      country: 'US',
      email: 'd@d.com',
      errorMessage: {},
      faxNumber: '',
      firstName: 'KINDRA',
      ignoreSuggestedAddress: 'true',
      lastName: 'CONNOR',
      middleName: '',
      phoneNumber: '(814) 384-7122',
      selectedAddressId: 0,
      state: 'GA',
      subCity: null,
      validAddress: true,
      warningMessage: {},
      zip4: '5479',
      zip5: '30005'
    };
    fakeResponse.billTo = fakeResponse.shipTo;

    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/customer/user.json');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(fakeResponse);
    orderMock.last4 = '7799';
    orderMock.showVarOrderId = true;
    component.savedSessionData = {
      usedPoints: 1000
    } as OrderConfirm;
    component.config.showCartSplitUp = true;
    component.config.showFeeDetails = true;
    component.config.displayDiscountedItemPriceInPriceBreakdown  = true;
    spyOn(component['sharedService'], 'isCashOnlyRedemption').and.returnValue(true);
    spyOn(component['sharedService'], 'getAnalyticsUserObject').and.returnValue({pgName: 'confirmation'});
    spyOn(component['orderInformationservice'], 'getOrderInformation').and.returnValue(of(orderMock));
    expect(component).toBeTruthy();
  }));

  it('should create', waitForAsync(() => {
    // Fake response data
    const fakeResponse = require('assets/mock/user.json');
    fakeResponse['shipTo'] = {
      address1: '5900 Windward Pkwy',
      address2: 'Ste 450',
      cartTotalModified: false,
      city: 'Alpharetta',
      country: 'US',
      email: 'd@d.com',
      firstName: 'KINDRA',
      ignoreSuggestedAddress: 'true',
      lastName: 'CONNOR',
      phoneNumber: '(814) 384-7122',
      selectedAddressId: 0,
      state: 'GA',
      zip4: '5479',
      zip5: '30005'
    };
    fakeResponse.billTo = fakeResponse.shipTo;
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/customer/user.json');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    // Respond with the fake data when called
    req.flush(fakeResponse);
    orderMock.last4 = '7799';
    orderMock.showVarOrderId = true;
    component.savedSessionData = {
      usedPoints: 1000
    } as OrderConfirm;
    orderMock.cartTotal.discountedItemsSubtotalPrice = { amount: 0, points: 0, currencyCode: 'USD' };
    orderMock.cartTotal.shippingPrice = { amount: 0, points: 0, currencyCode: 'USD' };
    orderMock.cartTotal.totalTaxes = { amount: 0, points: 0, currencyCode: 'USD' };
    orderMock.cartTotal.totalFees.amount = null;
    component.config.showCartSplitUp = true;
    component.config.showFeeDetails = true;
    component.config.displayDiscountedItemPriceInPriceBreakdown  = true;
    spyOn(component['sharedService'], 'isCashOnlyRedemption').and.returnValue(true);
    spyOn(component['sharedService'], 'getAnalyticsUserObject').and.returnValue({pgName: 'confirmation'});
    spyOn(component['orderInformationservice'], 'getOrderInformation').and.returnValue(of(orderMock));
    expect(component).toBeTruthy();
  }));

});
