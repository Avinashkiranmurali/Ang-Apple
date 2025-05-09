import { ComponentFixture, fakeAsync, TestBed, tick, waitForAsync } from '@angular/core/testing';
import { CheckoutComponent } from './checkout.component';
import { TemplateStoreService } from '@app/state/template-store.service';
import { TemplateService } from '@app/services/template.service';
import { CartService } from '@app/services/cart.service';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { BehaviorSubject, of } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { UserStoreService } from '@app/state/user-store.service';
import { Program } from '@app/models/program';
import { HttpTestingController } from '@angular/common/http/testing';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { DecimalPipe, TitleCasePipe } from '@angular/common';
import { CheckoutSummaryComponent } from '@app/modules/checkout/checkout-summary/checkout-summary.component';
import { CheckoutItemsComponent } from '@app/modules/checkout/checkout-items/checkout-items.component';
import { PaymentSummaryComponent } from '@app/modules/payment-summary/payment-summary.component';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { CheckoutButtonsComponent } from '@app/modules/checkout/checkout-buttons/checkout-buttons.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CartTotalTempComponent } from '@app/modules/pricing/cart-total-temp/cart-total-temp.component';
import { PricingTempComponent } from '@app/modules/pricing/pricing-temp/pricing-temp.component';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { AppConstants } from '@app/constants/app.constants';
import { Cart } from '@app/models/cart';
import { DataMaskingModule } from '@bakkt/data-masking';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';
import { TransitionService } from '@app/transition/transition.service';
import { MediaProductComponent } from '@app/modules/shared/media-product/media-product.component';
import { AplImgSizePipe } from '@app/pipes/apl-img-size.pipe';

describe('CheckoutComponent', () => {
  let component: CheckoutComponent;
  let templateStoreService: TemplateStoreService;
  let templateService: TemplateService;
  let fixture: ComponentFixture<CheckoutComponent>;
  let httpTestingController: HttpTestingController;
  const messageData = require('assets/mock/messages.json');
  const programData: Program = require('assets/mock/program.json');
  const userMock = require('assets/mock/user.json');
  userMock['program'] = programData;
  const userData = {
    user: userMock,
    program: programData,
    config: programData['config'],
    get: () => of(userMock)
  };
  const configData = require('assets/mock/configData.json');
  const cartData = require('assets/mock/cart.json');
  cartData.cartTotal.discountedPrice = 10;
  cartData.discountData = {
    discountCode: 'ABC10',
    shortDescription: 'testing discount',
    longDescription: 'testing discount for karma unit testing',
    discountType: 'discount',
    discountAmount: '10',
  };
  const routerEvent$ = new BehaviorSubject<NavigationEnd>(null);
  let router: Router;
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        CheckoutComponent,
        CheckoutItemsComponent,
        PaymentSummaryComponent,
        CheckoutSummaryComponent,
        CheckoutButtonsComponent,
        CartTotalTempComponent,
        PricingTempComponent,
        MediaProductComponent,
        CurrencyFormatPipe,
        OrderByPipe,
        AplImgSizePipe
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot(),
        BrowserAnimationsModule,
        DataMaskingModule
      ],
      providers: [
        { provide: CartService },
        { provide: TitleCasePipe },
        { provide: DecimalPipe },
        { provide: CurrencyPipe },
        { provide: TemplateService },
        { provide: CurrencyFormatPipe },
        { provide: MessagesStoreService, useValue: {messages: messageData } },
        { provide: NgbActiveModal },
        { provide: TransitionService },
        { provide: UserStoreService, useValue : userData },
        { provide: ActivatedRoute, useValue: {
            params: of({category: undefined}),
            queryParams: of({}),
            snapshot: {
              data: of({})
            }
          }
        },
        { provide: MatomoService, useValue: {
          broadcast: () => {} }
        },
        { provide: EnsightenService, useValue: {
            broadcastEvent: () => {}
        }},
      ],
    })
    .compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
    templateService = TestBed.inject(TemplateService);
    templateService.template = configData['configData'];
    templateStoreService = TestBed.inject(TemplateStoreService);
    templateStoreService.addTemplate(configData['configData']);
    router = TestBed.inject(Router);
    router.navigate = jasmine.createSpy('navigate');
    (router as any).events = routerEvent$.asObservable();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CheckoutComponent);
    component = fixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
    component.program = programData;
    component.eppStatus = false;
    component.config = userData.config;
    component.config.showFeeDetails = false;
    component.config.paymentType = AppConstants.paymentType.cc_variable;
    fixture.detectChanges();
  });

  it('should create', fakeAsync(() => {
    component['userStore'].config.epp = null;
    fixture.detectChanges();
    tick(500);
    expect(component).toBeTruthy();
  }));

  it('should create if epp data available', fakeAsync(() => {
    component['userStore'].config.epp = true;
    fixture.detectChanges();
    tick(500);
    expect(component).toBeTruthy();
  }));

  it('should call hasDiscounts', () => {
    spyOn(component, 'hasDiscounts').and.callThrough();
    component.cartData = cartData;
    fixture.detectChanges();
    component.hasDiscounts();
    expect(component.hasDiscounts).toHaveBeenCalled();
  });

  it('should call getCart - valid address', waitForAsync(() => {
    component.eppStatus = true;
    component.config.showFeeDetails = true;
    component.config.fullCatalog = false;
    sessionStorage.setItem('confirmOrder', null);
    fixture.detectChanges();
    spyOn(component, 'getCart').and.callThrough();
    component.pricingOption = userData.program.bundledPricingOption;
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/cart');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    cartData.shippingAddress.validAddress = true;
    cartData.discounts = [{
      discountCode: 'Testing',
      shortDescription: 'Testing',
      longDescription: 'Testing',
      discountType: 'Testing',
      discountAmount: 'Testing'
    }];
    cartData.paymentLimit = null;
    cartData.subscriptions = [{itemId: 'amp-music', addedToCart: true}];
    cartData.cartItems[0].productDetail.additionalInfo.PricingModel = { paymentValue: 200, repaymentTerm: 3 };
    req.flush(cartData);
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  }));

  it('should call getCart - valid address without shipment payment option', waitForAsync(() => {
    component.eppStatus = false;
    component.config.showFeeDetails = false;
    component.config.fullCatalog = false;
    sessionStorage.setItem('confirmOrder', null);
    fixture.detectChanges();
    spyOn(component, 'getCart').and.callThrough();
    component.pricingOption = userData.program.bundledPricingOption;
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/cart');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    cartData.shippingAddress.validAddress = true;
    cartData.cartItems[0].productDetail.additionalInfo.PricingModel = null;
    cartData.cartTotal.actual = null;
    cartData.paymentLimit = {};
    req.flush(cartData);
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  }));

  it('should call getCart - when its unbundled payment structure', waitForAsync(() => {
    component.eppStatus = false;
    component.config.showFeeDetails = false;
    component.config.fullCatalog = false;
    sessionStorage.setItem('confirmOrder', null);
    fixture.detectChanges();
    spyOn(component, 'getCart').and.callThrough();
    component.pricingOption = userData.program.bundledPricingOption;
    cartData.shippingAddress.validAddress = true;
    spyOn(component['pricingService'], 'getPricingOption').and.returnValue({option: 'unbundledCheckout', isUnbundled: true});
    spyOn(component['cartService'], 'getCart').and.returnValue(of(cartData));
    component.showTaxDisclaimer = true;
    fixture.detectChanges();
    component.getCart();
    expect(component.isUnbundled).toBeTruthy();
    expect(component.showTaxDisclaimer).toBeTruthy();
    component.showTaxDisclaimer = false;
    fixture.detectChanges();
    component.getCart();
    expect(component.showTaxDisclaimer).toBeFalsy();
    expect(component.setCartTaxFeeTitle).toBe('');
    expect(component.getCart).toHaveBeenCalled();
  }));

  it('should call getCart - valid address with else check', () => {
    component.eppStatus = true;
    component.config.showFeeDetails = false;
    component.config.fullCatalog = false;
    fixture.detectChanges();
    spyOn(component, 'getCart').and.callThrough();
    component.pricingOption = userData.program.bundledPricingOption;
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/cart');
    const cartDataValue = require('assets/mock/cart.json');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    cartDataValue.shippingAddress.validAddress = true;
    cartDataValue.discounts = [];
    cartDataValue.selectedPaymentOption = '';
    req.flush(cartDataValue);
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  });

  it('should call getCart - invalid address with errorMessage', waitForAsync(() => {
    const address = require('assets/mock/address.json')[1];
    address.businessName = 'test';
    address.address3 = 'test street';
    address.errorMessage = {
      firstName: 'First and Last name must be alpha characters, each with 1 characters minimum.',
      phoneNumber: 'Enter a valid phone number'
    };
    component.checkoutAddress = address;
    fixture.detectChanges();
    spyOn(component, 'getCart').and.callThrough();
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/cart');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    const response: Cart = require('assets/mock/cart.json');
    response.shippingAddress = component.checkoutAddress;
    response.shippingAddress.validAddress = false;
    req.flush(response);
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  }));

  it('should call getCart - invalid address without errorMessage', waitForAsync(() => {
    spyOn(component, 'getCart').and.callThrough();
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/cart');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    const cartResponse: Cart = require('assets/mock/cart.json');
    cartResponse.shippingAddress.validAddress = false;
    req.flush(cartResponse);
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  }));

  it('should call getCart - else check scenario', waitForAsync(() => {
    component.config.fullCatalog = true;
    sessionStorage.removeItem('confirmOrder');
    fixture.detectChanges();
    spyOn(component, 'getCart').and.callThrough();
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/cart');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    const mockResponse: Cart = Object.assign({});
    mockResponse.shippingAddress = Object.assign({});
    mockResponse.promotionalSubscription = {
      displayCheckbox: false,
      isChecked: false
    };
    mockResponse.cartItems = [];
    req.flush(mockResponse);
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  }));

  it('should test for 401 error', waitForAsync(() => {
    const errorMsg = 'deliberate 401 error';
    spyOn(component, 'getCart').and.callThrough();
    const errreq = httpTestingController.expectOne('/apple-gr/service/cart');
    // Respond with mock error
    errreq.flush(errorMsg, { status: 401, statusText: 'Not Found' });
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  }));

  it('should test for 0 error - getCart', waitForAsync(() => {
    const errorMsg = 'deliberate 0 error';
    spyOn(component, 'getCart').and.callThrough();
    const errreq = httpTestingController.expectOne('/apple-gr/service/cart');
    // Respond with mock error
    errreq.flush(errorMsg, { status: 0, statusText: 'Not Found' });
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  }));

  it('should test for 404 error - getCart', () => {
    const errorMsg = 'deliberate 404 error';
    spyOn(component, 'getCart').and.callThrough();
    const errreq = httpTestingController.expectOne('/apple-gr/service/cart');
    // Respond with mock error
    errreq.flush(errorMsg, { status: 404, statusText: 'Not Found' });
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  });

  it('should call discountedSubtotal', () => {
    spyOn(component, 'discountedSubtotal').and.callThrough();
    component.discountedSubtotal();
    expect(component.discountedSubtotal).toHaveBeenCalled();
  });

  it('should call msgWithArgs', () => {
    component.msgWithArgs('test', 'two');
    expect(component.msgWithArgs).toBeTruthy();
  });

  it('should unsubscribe on destroy', () => {
    component.ngOnDestroy();
    expect(component.ngOnDestroy).toBeTruthy();
  });

  it('should call setCartSubtotal method with items discounted total', () => {
    const data = require('assets/mock/cart.json');
    component.showCartSplitUp = true;
    component.config.displayDiscountedItemPriceInPriceBreakdown = true;
    spyOn(component, 'setCartSubtotal').and.callThrough();
    const cartTotal = data['cartTotal'];
    cartTotal['discountedItemsSubtotalPrice'] = {
      amount: 2000,
      currencyCode: 'USD',
      points: 410000
    };
    component.setCartSubtotal(cartTotal);
    expect(component.setCartSubtotal).toHaveBeenCalled();
  });

  it('should call setCartSubtotal method - with item subtotal', () => {
    const data = require('assets/mock/cart-with-noSufficientPoints.json');
    component.config.displayDiscountedItemPriceInPriceBreakdown = false;
    component.config.paymentType = AppConstants.paymentType.cc_fixed;
    component.pointsAvailable = 445000;
    component.showCartSplitUp = true;
    component.suppPaymentPctMaxLimit = Object.assign({});
    component.suppRewardsPctMinLimit = Object.assign({});
    spyOn(component, 'setCartSubtotal').and.callThrough();
    const cart = data['cartTotal'];
    cart['shippingPrice'] = {
      amount: 5,
      currencyCode: 'USD',
      points: 200
    };
    component.setCartSubtotal(cart);
    expect(component.setCartSubtotal).toHaveBeenCalled();
  });

  it('should call setCartSubtotal method - paymentType points_only else check', () => {
    const mockData = require('assets/mock/cart.json');
    component.config.paymentType = AppConstants.paymentType.points_only;
    component.pointsAvailable = 450000;
    component.cartUserAdds = 500;
    component.paymentRequired = false;
    spyOn(component, 'setCartSubtotal').and.callThrough();
    const data = mockData['cartTotal'];
    data['discountedPrice'] = {
      amount: 5,
      currencyCode: 'USD',
      points: 200000
    };
    component.setCartSubtotal(data);
    expect(component.setCartSubtotal).toHaveBeenCalled();
  });

  it('should call setCartSubtotal method - paymentType points_only', () => {
    const mockValue = require('assets/mock/cart.json');
    component.config.paymentType = AppConstants.paymentType.points_only;
    component.pointsAvailable = 445000;
    component.cartUserAdds = 0;
    component.paymentRequired = false;
    spyOn(component['sharedService'], 'isPointsOnlyRewards').and.returnValue(true);
    spyOn(component, 'setCartSubtotal').and.callThrough();
    const data = mockValue['cartTotal'];
    data['discountedPrice'] = {
      amount: 5,
      currencyCode: 'USD',
      points: 445000
    };
    component.setCartSubtotal(data);
    expect(component.setCartSubtotal).toHaveBeenCalled();
  });

  it('should call setCartSubtotal method - paymentType points_only and payment is required', () => {
    const data = require('assets/mock/cart.json');
    component.config.paymentType = AppConstants.paymentType.points_only;
    component.pointsAvailable = 450000;
    component.cartUserAdds = 0;
    component.paymentRequired = true;
    spyOn(component, 'setCartSubtotal').and.callThrough();
    const cartTotal = data['cartTotal'];
    component.setCartSubtotal(cartTotal);
    expect(component.paymentRequired).toBeTruthy();
    expect(component.setCartSubtotal).toHaveBeenCalled();
  });

  it('should call setCartSubtotal method - paymentType cash_only', () => {
    const data = require('assets/mock/cart.json');
    component.config.paymentType = AppConstants.paymentType.cash_only;
    component.pointsAvailable = 445000;
    component.cost = 3240;
    component.displayCartTotal = data['displayCartTotal'];
    spyOn(component, 'setCartSubtotal').and.callThrough();
    const cartTotal = data['cartTotal'];
    spyOn(component['sharedService'], 'isCashOnlyRedemption').and.returnValue(true);
    component.setCartSubtotal(cartTotal);
    expect(component.paymentRequired).toBeTruthy();
    expect(component.setCartSubtotal).toHaveBeenCalled();
  });

  it('should call setCartSubtotal method - paymentType cash_subsidy', () => {
    const data = require('assets/mock/cart.json');
    component.config.paymentType = AppConstants.paymentType.points_fixed;
    component.config.paymentTemplate = AppConstants.paymentTemplate.cash_subsidy;
    component.pointsAvailable = 445000;
    component.cost = 3240;
    component.displayCartTotal = data['displayCartTotal'];
    spyOn(component, 'setCartSubtotal').and.callThrough();
    const cartTotal = data['cartTotal'];
    spyOn(component['sharedService'], 'isCashOnlyRedemption').and.returnValue(true);
    component.setCartSubtotal(cartTotal);
    expect(component.setCartSubtotal).toHaveBeenCalled();
  });

  it('should call ngOnInit - when updatePointsBalance is not available', () => {
    const sessionResponse = require('assets/mock/validSession.json');
    spyOn(component['keyStoneSyncService'], 'isKeyStoneSync').and.returnValue(true);
    spyOn(component['sessionService'], 'getSessionURLs').and.returnValue(of(sessionResponse));
    component.ngOnInit();
    expect(component.paymentTemplate).toBeDefined();
    expect(component.analyticsUserObject).toBeDefined();
  });

  it('should call ngOnInit - when updatePointsBalance is available', () => {
    const sessionResponse = {
      signOutUrl: 'https://wfbk-uat-mn.epsilon.com/home/LogOutRedir',
      keepAliveUrl: 'https://wfbk-uat-mn.epsilon.com/home/keepalive',
      navigateBackUrl: 'https://wfbk-uat-mn.epsilon.com/#/deeplink/B2S/Merchandise',
      timeOutUrl: 'https://wfbk-uat-mn.epsilon.com/home/LogOutRedir',
      updatedPointsBalance: true
    };
    spyOn(component['keyStoneSyncService'], 'isKeyStoneSync').and.returnValue(true);
    spyOn(component['sessionService'], 'getSessionURLs').and.returnValue(of(sessionResponse));
    component.ngOnInit();
    expect(component.paymentTemplate).toBeDefined();
    expect(component.analyticsUserObject).toBeDefined();
  });

  it('should call hasDiscounts', () => {
    component.cartData = cartData;
    fixture.detectChanges();
    spyOn(component, 'discountedSubtotal').and.callFake(() => { return false });
    expect(component.hasDiscounts()).toBeFalsy();
  });

  it('should call ngOnInit - when updatePointsBalance is not available', () => {
    const sessionResponse = require('assets/mock/validSession.json');
    spyOn(component['keyStoneSyncService'], 'isKeyStoneSync').and.returnValue(true);
    spyOn(component['sessionService'], 'getSessionURLs').and.returnValue(of(sessionResponse));
    component.ngOnInit();
    expect(component.paymentTemplate).toBeDefined();
    expect(component.analyticsUserObject).toBeDefined();
  });

  it('should call ngOnInit - when updatePointsBalance is available', () => {
    const sessionResponse = {
      signOutUrl: 'https://wfbk-uat-mn.epsilon.com/home/LogOutRedir',
      keepAliveUrl: 'https://wfbk-uat-mn.epsilon.com/home/keepalive',
      navigateBackUrl: 'https://wfbk-uat-mn.epsilon.com/#/deeplink/B2S/Merchandise',
      timeOutUrl: 'https://wfbk-uat-mn.epsilon.com/home/LogOutRedir',
      updatedPointsBalance: true
    };
    spyOn(component['keyStoneSyncService'], 'isKeyStoneSync').and.returnValue(true);
    spyOn(component['sessionService'], 'getSessionURLs').and.returnValue(of(sessionResponse));
    component.ngOnInit();
    expect(component.paymentTemplate).toBeDefined();
    expect(component.analyticsUserObject).toBeDefined();
  });

  it('should call hasDiscounts', () => {
    component.cartData = cartData;
    fixture.detectChanges();
    spyOn(component, 'discountedSubtotal').and.callFake(() => { return false });
    expect(component.hasDiscounts()).toBeFalsy();
  });

});
