import { TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { UserStoreService } from '@app/state/user-store.service';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { Offer } from '@app/models/offer';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { AuthenticationService } from '@app/auth/authentication.service';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

describe('PricingService', () => {
  let pricingService: PricingService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule, RouterTestingModule ],
      providers: [
        { provide: UserStoreService },
        { provide: HttpClient },
        { provide: AuthenticationService },
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate') }
        },
        { provide: CurrencyFormatPipe },
        { provide: CurrencyPipe },
        { provide: DecimalPipe }
      ]
    });

    // Inject the http test controller
    httpTestingController = TestBed.inject(HttpTestingController);
    pricingService = TestBed.inject(PricingService);
    pricingService.user = require('assets/mock/user.json');
    pricingService.user['program'] = require('assets/mock/program.json');
  });

  it('should be created', () => {
    expect(pricingService).toBeTruthy();
  });

  it('should call checkDiscounts method with no offers', () => {
    spyOn(pricingService, 'checkDiscounts').and.callThrough();
    pricingService.checkDiscounts(null);
    expect(pricingService.checkDiscounts).toHaveBeenCalled();
  });

  it('should call checkDiscounts with offer object', () => {
    spyOn(pricingService, 'checkDiscounts').and.callThrough();
    const offer: Offer = Object.assign({});
    offer['displayPrice'] =  {
      amount: 599,
      currencyCode: 'USD',
      points: 111000
    };
    offer['unpromotedDisplayPrice'] = {
      amount: 499,
      currencyCode: 'USD',
      points: 101000
    };
    pricingService.checkDiscounts(offer);
    expect(pricingService.checkDiscounts).toHaveBeenCalled();
  });

  it('should call getPricingOption method - bundled', () => {
    pricingService.user.program.bundledPricingOption = 'bundled';
    pricingService.getPricingOption();
    expect(pricingService.getPricingOption()).toBeTruthy();
  });

  it('should call getPricingOption method - UNBUNDLED_DETAIL_PAGE_BUNDLED_CHECKOUT', () => {
    pricingService.user.program.bundledPricingOption = 'UNBUNDLED_DETAIL_PAGE_BUNDLED_CHECKOUT';
    pricingService.getPricingOption();
    expect(pricingService.getPricingOption()).toBeTruthy();
  });

  it('should call getPricingOption method - BASE_PRICE_SHOPPING_UNBUNDLED_CHECKOUT', () => {
    pricingService.user.program.bundledPricingOption = 'BASE_PRICE_SHOPPING_UNBUNDLED_CHECKOUT';
    pricingService.getPricingOption();
    expect(pricingService.getPricingOption()).toBeTruthy();
  });

  it('should modify gift items', waitForAsync(() => {
    const placeOrder = require('assets/mock/orderConfirmation.json');
    const params = {isPromotionChecked: false};
    // Setup a request using the fakeResponse data
    pricingService.placeOrder(params).subscribe(
      (response) => expect(response).toBeTruthy(), fail
    );
    // Expect a call to this URL
    const req = httpTestingController.expectOne(pricingService.baseUrl + 'order/placeOrder');

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(placeOrder, { headers: {'xsrf-token': 'b82780ff-e016-4c82-8f7'}});
  }));

  it('should test for 404 error for giftItemModify', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    const params = {isPromotionChecked: false};
    pricingService.placeOrder(params).subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne(pricingService.baseUrl + 'order/placeOrder');
    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));
});
