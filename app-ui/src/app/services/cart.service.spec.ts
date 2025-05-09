import { TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { CartService } from './cart.service';
import { Cart } from '@app/models/cart';
import { Observable, of, throwError } from 'rxjs';
import { UserStoreService } from '@app/state/user-store.service';
import { TransitionService } from '@app/transition/transition.service';
import { RouterTestingModule } from '@angular/router/testing';
import { SharedService } from '@app/modules/shared/shared.service';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { DecimalPipe } from '@angular/common';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { ModalsService } from '@app/components/modals/modals.service';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Router } from '@angular/router';
import { TemplateStoreService } from '@app/state/template-store.service';
import { HeapService } from '@app/analytics/heap/heap.service';
import { TranslateModule } from '@ngx-translate/core';


describe('CartService', () => {
  let cartService: CartService;
  let httpTestingController: HttpTestingController;
  // Fake response data
  const fakeResponse = require('assets/mock/cart.json')['cartItems'];
  const programData = require('assets/mock/program.json');
  const productDetails = require('assets/mock/product-detail.json');
  const mockAddCartResponse = require('assets/mock/addToCart.json');
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };
  userData.user['program'] = programData;
  userData.user['browseOnly'] = false;
  userData.config['loginRequired'] = false;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule, RouterTestingModule, TranslateModule.forRoot() ],
      providers: [
        { provide: UserStoreService, useValue: userData },
        TransitionService,
        SharedService,
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe,
        { provide: MatomoService, useValue: {
            broadcast: () => {}
          }},
        { provide: HeapService,
          useValue: {
            broadcastEvent: () => {},
            loadInitialScript: () => {},
            loadHeapScript: () => {}
          }
        },
        { provide: ModalsService, useValue: {
            openAnonModalComponent: () => of({}),
            openSuggestAddressModalComponent: () => of({}),
            openEngraveModalComponent: () => {},
            openBrowseOnlyComponent: () => ({}) }
        },
        { provide: ParsePsidPipe },
        { provide: CurrencyPipe, useValue: {
            program: of(programData),
            transform: () => of({}) }
        },
        { provide: NgbActiveModal },
        { provide: Router, useValue: {
            url: 'store/cart',
            navigate: jasmine.createSpy('navigate') }
        },
        { provide: TemplateStoreService, useValue: {
            buttonColor: () => of({}) }
        }
      ]
    });

    // Inject the http test controller
    httpTestingController = TestBed.inject(HttpTestingController);
    cartService = TestBed.inject(CartService);
    cartService.messages = require('assets/mock/messages.json');
    cartService.user = cartService.user;
    cartService.config = programData['config'];
    cartService.config['loginRequired'] = false;
    cartService.user['browseOnly'] = false;
  });

  afterEach(() => {
    // After every test, assert that there are no more pending requests.
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(cartService).toBeTruthy();
  });

  it('should return Cart', waitForAsync(() => {
    // Setup a request using the fakeResponse data
    const cartResponse = { cartItems: fakeResponse };
    spyOn(cartService['sharedService'], 'giftAttributeUpdates').and.callFake(() => cartResponse.cartItems);
    cartService.getCart().subscribe(
      (cart: Cart) => expect(cart).toEqual(cartResponse as any, 'should return fakeResponse'), fail
    );

    // Expect a call to this URL
    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(cartResponse);
    expect(cartService.getCart).toBeDefined();
  }));

  it('should return Cart items', waitForAsync(() => {
    // Setup a request using the fakeResponse data
    const data = {
      cartItems: []
    };
    cartService.getCart().subscribe(
      (cart: Cart) => expect(cart).toEqual(data as any, 'should return fakeResponse'), fail
    );

    // Expect a call to this URL
    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(data);
    expect(cartService.getCart).toBeDefined();
  }));

  it('should test for 404 error', waitForAsync(() => {
  const errorMsg = 'deliberate 404 error';

  cartService.getCart().subscribe(
    data => fail('should have failed with the 404 error'),
    (error: HttpErrorResponse) => {
      expect(error.status).toEqual(404, 'status');
      expect(error.error).toEqual(errorMsg, 'message');
    }
  );

  const req = httpTestingController.expectOne(cartService.baseUrl + 'cart');

    // Respond with mock error
  req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

  it('should add to cart', waitForAsync(() => {
    const iD = '230624';
    // Setup a request using the fakeResponse data
    cartService.addItemToCart( iD, '' ).subscribe(
      (response) => expect(response).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );
    // Expect a call to this URL
    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart/add');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(fakeResponse);

    // Run some expectations
    expect(fakeResponse.length).toBe(4);
    expect(fakeResponse[0].id).toBe(230624);
    expect(fakeResponse[0].productName).toBe('Apple TV HD 32GB');
    expect(fakeResponse[0].productDetail.categories[0].slug).toBe('apple-tv-apple-tv');
    expect(fakeResponse[1].id).toBe(230623);
  }));


  it('should test for 404 error', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';

    cartService.addItemToCart('230624', '').subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart/add');

    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

  it('should modify the cart', waitForAsync(() => {
    const id = 230624;
    // Setup a request using the fakeResponse data
    cartService.modifyCart( id, '' ).subscribe(
      (response) => expect(response).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );
    // Expect a call to this URL
    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart/modify/' + id);

    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(fakeResponse);

    // Run some expectations
    expect(fakeResponse.length).toBe(4);
    expect(fakeResponse[0].id).toBe(230624);
    expect(fakeResponse[0].productName).toBe('Apple TV HD 32GB');
    expect(fakeResponse[0].productDetail.categories[0].slug).toBe('apple-tv-apple-tv');
    expect(fakeResponse[1].id).toBe(230623);
  }));

  it('should modify the cart with cartItems', waitForAsync(() => {
    const id = 230624;
    const cartData = { cartItems: fakeResponse } as any;
    spyOn(cartService['sharedService'], 'giftAttributeUpdates').and.callFake(() => cartData.cartItems);

    // Setup a request using the fakeResponse data
    cartService.modifyCart( id, '' ).subscribe(
      (response) => expect(response).toEqual(cartData, 'should return fakeResponse'), fail
    );
    // Expect a call to this URL
    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart/modify/' + id);

    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(cartData);
    expect(cartService.modifyCart).toBeDefined();
  }));

  it('should modify the cart without cart items', waitForAsync(() => {
    const id = 230624;
    const fakeData = { cartItems: [] } as any;
    // Setup a request using the fakeResponse data
    cartService.modifyCart( id, '' ).subscribe(
      (response) => expect(response).toEqual(fakeData, 'should return fakeResponse'), fail
    );
    // Expect a call to this URL
    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart/modify/' + id);

    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(fakeData);
    expect(cartService.modifyCart).toBeDefined();
  }));

  it('should test for 404 error', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    const id = 230624;
    cartService.modifyCart(230624, '').subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart/modify/' + id);

    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

  it('should add purchase points', waitForAsync(() => {
    // Setup a request using the fakeResponse data
    cartService.addPurchasePoints(0).subscribe(
      (response) => expect(response).toEqual(response, 'should return fakeResponse'), fail
    );
    // Expect a call to this URL - Return true for every call to the match predicate since it has nocache parameter
    const req = httpTestingController.expectOne(() => true);

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(fakeResponse);
  }));

  it('should test for 404 error - addPurchasePoints', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    cartService.addPurchasePoints(0).subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );
    // Return true for every call to the match predicate since it has nocache parameter
    const req = httpTestingController.expectOne(() => true);
    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

  it('should get purchase points', waitForAsync(() => {
    // Setup a request using the fakeResponse data
    cartService.getPurchasePoints(0).subscribe(
      (response) => expect(response).toEqual(response, 'should return fakeResponse'), fail
    );
    // Expect a call to this URL - Return true for every call to the match predicate since it has nocache parameter
    const req = httpTestingController.expectOne(() => true);

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(fakeResponse);
  }));

  it('should test for 404 error - addPurchasePoints', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    cartService.getPurchasePoints(0).subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );
    // Return true for every call to the match predicate since it has nocache parameter
    const req = httpTestingController.expectOne(() => true);
    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

  it('should call updateRemainingCost method', () => {
    spyOn(cartService, 'updateRemainingCost').and.callThrough();
    const cartTotal = require('assets/mock/cart.json')['cartTotal'];
    cartService.updateRemainingCost(cartTotal);

    cartTotal.discountApplied = true;
    cartTotal.discountedPrice = {
      amount: 2405.36,
      currencyCode: 'USD',
      points: 445600
    };
    cartService.updateRemainingCost(cartTotal);
    spyOn(cartService, 'isCashOnlyRedemption').and.callFake(() => true);
    cartService.updateRemainingCost(cartTotal);
    expect(cartService.updateRemainingCost).toHaveBeenCalled();
  });

  it('should call and passPaymentType method', waitForAsync(() => {
    // Setup a request using the fakeResponse data
    cartService.passPaymentType(fakeResponse[0].id, {selectedPaymentOption: 'points'}).subscribe(
      (response) => expect(response).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );
    // Expect a call to this URL
    const req = httpTestingController.expectOne(cartService.baseUrl + '/cart/' + fakeResponse[0].id);

    // Assert that the request is a PUT
    expect(req.request.method).toEqual('PUT');

    // Respond with the fake data when called
    req.flush(fakeResponse);

  }));

  it('should test for 404 error - passPaymentType', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    cartService.passPaymentType(fakeResponse[0].id, {selectedPaymentOption: 'points'}).subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne(cartService.baseUrl + '/cart/' + fakeResponse[0].id);
    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

  it('should implement getter and setter for disable quantity', () => {
    spyOn(cartService, 'getDisableQty').and.callThrough();
    cartService.setDisableQty(0, false);
    expect(cartService.getDisableQty(0)).toBeFalsy();
    cartService.disableQty = null;
    expect(cartService.getDisableQty(1)).toBeUndefined();
  });

  it('should implement getter and setter for engrave', () => {
    spyOn(cartService, 'getHasEngraving').and.callThrough();
    cartService.setHasEngraving(0, false);
    expect(cartService.getHasEngraving(0)).toBeFalsy();
    cartService.hasEngraving = null;
    expect(cartService.getHasEngraving(1)).toBeUndefined();
  });

  it('should implement getter and setter for disable checkout button', () => {
    spyOn(cartService, 'getDisableCheckoutBtn').and.callThrough();
    cartService.setDisableCheckoutBtn(true);
    expect(cartService.getDisableCheckoutBtn()).toBeTruthy();
  });

  it('should implement getter and setter for update cartObj', () => {
    spyOn(cartService, 'getUpdateCartObj').and.callThrough();
    const cartData = require('assets/mock/cart.json');
    cartService.setUpdateCartObj(cartData);
    cartService.getUpdateCartObj();
    expect(cartService.getUpdateCartObj).toHaveBeenCalled();
  });

  it('should call getObservable', () => {
    cartService.getObservable();
    expect(cartService.getObservable()).toBeInstanceOf(Observable);
  });

  it('should call updateGiftItemModifyCart methods - success response', () => {
    spyOn(cartService, 'updateGiftItemModifyCart').and.callThrough();
    // Fake response data
    const fakeResponse = require('assets/mock/cart.json');
    spyOn(cartService, 'modifyCart').and.returnValue(of(fakeResponse));
    cartService.updateGiftItemModifyCart(230624, '');
    expect(cartService.updateGiftItemModifyCart).toHaveBeenCalled();
  });

  it('should call updateGiftItemModifyCart methods- 401 failure response', () => {
    spyOn(cartService, 'updateGiftItemModifyCart').and.callThrough();
    // Fake response data
    const errorResponse = {
      status: 401,
      statusText: 'Not Found'
    };
    spyOn(cartService, 'modifyCart').and.returnValue(throwError(errorResponse));
    cartService.updateGiftItemModifyCart(230624, '');
    expect(cartService.updateGiftItemModifyCart).toHaveBeenCalled();
  });

  it('should call updateGiftItemModifyCart methods- 404 failure response', () => {
    spyOn(cartService, 'updateGiftItemModifyCart').and.callThrough();
    // Fake response data
    const errorResponse = {
      status: 404,
      statusText: 'Not Found'
    };
    spyOn(cartService, 'modifyCart').and.returnValue(throwError(errorResponse));
    cartService.updateGiftItemModifyCart(230624, '');
    expect(cartService.updateGiftItemModifyCart).toHaveBeenCalled();
  });
  it('should not call addToCart api if browse only option is true', () => {
    spyOn(cartService, 'addToCart').and.callThrough();
    const psid = '30001MXG22LL/A';
    cartService.user['browseOnly'] = true;
    cartService.addToCart(psid, productDetails);
    expect(cartService.addToCart).toHaveBeenCalled();
  });


  it('should call addToCart for anonymous user login', () => {
    spyOn(cartService, 'addToCart').and.callThrough();
    const psid = '30001MXG22LL/A';
    cartService.config['loginRequired'] = true;
    productDetails.hasRelatedProduct = false;
    productDetails.relatedProducts = [];
    cartService.addToCart(psid, productDetails);
    expect(cartService.addToCart).toHaveBeenCalled();
  });

  it('should call addToCart method', waitForAsync(() => {
    const psid = '30001MXG22LL/A';
    productDetails['isEngravable'] = false;
    cartService.config['fullCatalog'] = false;
    cartService.addToCart(psid, productDetails);
    const mockCartResponse = require('assets/mock/cart.json');
    const mockModifyCartAddress = require('assets/mock/address.json')[1];

    // Expect a call to this URL for addToCart
    const req = httpTestingController.expectOne('/apple-gr/service/' + 'cart/add');

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(mockAddCartResponse);

    // Expect a call to this URL for get cart items
    const cartReq = httpTestingController.expectOne('/apple-gr/service/cart');

    // Assert that the request is a GET
    expect(cartReq.request.method).toEqual('GET');

    // Respond with the fake data when called
    cartReq.flush(mockCartResponse);

    // Expect a call to this URL for get cart items
    const modifyCartAddressReq = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');

    // Assert that the request is a POST
    expect(modifyCartAddressReq.request.method).toEqual('POST');

    // Respond with the fake data when called
    modifyCartAddressReq.flush(mockModifyCartAddress);
  }));

  it('should call addToCart method for isEligibleForGift=true with more gift items', waitForAsync(() => {
    const psid = '30001MXG22LL/A';
    productDetails['isEngravable'] = false;
    cartService.config['fullCatalog'] = true;
    productDetails.addOns['availableGiftItems'] = [
      {
        psid: '30001MK0C2AM/A',
        name: 'Apple Pencil (1st generation)',
        isEngravable: false
      },
      {
        psid: '30001MKQ42AM/A',
        name: 'USB-C to Lightning Cable (2 m)',
        isEngravable: false
      },
      {
        psid: '30001MU8F2AM/A',
        name: 'Apple Pencil (2nd generation)',
        isEngravable: true
      }
    ];
    cartService.addToCart(psid, productDetails);
    // Expect a call to this URL for addToCart
    const req = httpTestingController.expectOne('/apple-gr/service/' + 'cart/add');

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(mockAddCartResponse);
  }));

  it('should call addToCart method for isEligibleForGift=true', waitForAsync(() => {
    const psid = '30001MXG22LL/A';
    cartService.config['fullCatalog'] = true;
    productDetails.addOns['availableGiftItems'] = [
      {
        psid: '30001MK0C2AM/A',
        name: 'Apple Pencil (1st generation)',
        isEngravable: false
      }
    ];
    cartService.addToCart(psid, productDetails);
    // Expect a call to this URL for addToCart
    const req = httpTestingController.expectOne('/apple-gr/service/' + 'cart/add');

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(mockAddCartResponse);
  }));

  it('should test for 404 error - add to cart response', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    const psid = '30001MXG22LL/A';
    cartService.addToCart(psid, productDetails);
    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart/add');

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

  it('should test for 400 error - add to cart response', waitForAsync(() => {
    const psid = '30001MXG22LL/A';
    cartService.addToCart(psid, productDetails);
    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart/add');

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with mock error
    req.flush( '', { status: 400, statusText: 'Not Found' });
  }));

  it('should test for 403 error - add to cart response', waitForAsync(() => {
    const psid = '30001MXG22LL/A';
    cartService.addToCart(psid, productDetails);
    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart/add');

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with mock error
    req.flush( '', { status: 403, statusText: 'Not Found' });
  }));

  it('should test for 400 error(pricingFull) - add to cart response', waitForAsync(() => {
    const psid = '30001MXG22LL/A';
    cartService.addToCart(psid, productDetails);
    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart/add');
    const error = {
      pricingFull: true
    };

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with mock error
    req.flush(error, { status: 400, statusText: 'Not Found' });
  }));

  it('should test for 500 error - add to cart response', waitForAsync(() => {
    const errorMsg = 'deliberate 500 error';
    const psid = '30001MXG22LL/A';
    cartService.addToCart(psid, productDetails);
    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart/add');

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with mock error
    req.flush(errorMsg, { status: 500, statusText: 'Not Found' });
  }));

  it('should call addToCart method for with default router: store/cart', waitForAsync(() => {
    const psid = '30001MXG22LL/A';
    productDetails['isEngravable'] = false;
    cartService.config['fullCatalog'] = true;
    cartService.addToCart(psid, productDetails);
    // Expect a call to this URL for addToCart
    const req = httpTestingController.expectOne('/apple-gr/service/' + 'cart/add');

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(mockAddCartResponse);
  }));

  it('should call addToCart method for isEligibleForGift=true but no gift items available', waitForAsync(() => {
    const psid = '30001MXG22LL/A';
    productDetails['isEngravable'] = false;
    cartService.config['fullCatalog'] = true;
    cartService.addToCart(psid, productDetails);
    // Expect a call to this URL for addToCart
    const req = httpTestingController.expectOne('/apple-gr/service/' + 'cart/add');

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(mockAddCartResponse);
  }));

  it('should call addToCart method for isEngravable=true but no gift items available', waitForAsync(() => {
    const psid = '30001MXG22LL/A';
    productDetails['isEngravable'] = true;
    productDetails['availableGiftItems'] = null;
    cartService.addToCart(psid, productDetails);
    // Expect a call to this URL for addToCart
    const req = httpTestingController.expectOne('/apple-gr/service/' + 'cart/add');

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(mockAddCartResponse);
  }));

  it('should call initError method', () => {
    spyOn(cartService, 'initError').and.callThrough();
    cartService.initError();
    expect(cartService.initError).toHaveBeenCalled();
    expect(cartService.addCartError).toBeFalsy();
  });

  it('should call isCashOnlyRedemption', () => {
    spyOn(cartService, 'isCashOnlyRedemption').and.callThrough();
    const redemptionOptions = {
      splitpay: [
        {
          id: 966,
          varId: 'Delta',
          programId: 'b2s_qa_only',
          paymentOption: 'splitpay',
          limitType: 'percentage',
          paymentMinLimit: 0,
          paymentMaxLimit: 50,
          orderBy: 2,
          paymentProvider: null,
          lastUpdatedBy: 'Appl_user',
          lastUpdatedDate: 1527177099217,
          active: true
        }
      ],
      pointsonly: [
        {
          id: 965,
          varId: 'Delta',
          programId: 'b2s_qa_only',
          paymentOption: 'pointsonly',
          limitType: 'percentage',
          paymentMinLimit: 50,
          paymentMaxLimit: 0,
          orderBy: 1,
          paymentProvider: null,
          lastUpdatedBy: 'Appl_user',
          lastUpdatedDate: 1527177099217,
          active: true
        }
      ]
    };
    cartService['userStore']['program']['redemptionOptions'] = redemptionOptions as any;
    cartService.isCashOnlyRedemption();
    expect(cartService.isCashOnlyRedemption()).toBeFalsy();
    expect(cartService.isCashOnlyRedemption).toHaveBeenCalled();
  });

  it('should call setAmounts method', () => {
    spyOn(cartService, 'setAmounts').and.callThrough();
    expect(cartService.setAmounts(100, 100)).toBe(0);
  });

  it('should call addToCart method - leads to shipping address page', waitForAsync(() => {
    spyOn(cartService, 'addToCart').and.callThrough();
    const psid = '30001MXG22LL/A';
    const mockCartResponse = require('assets/mock/cart.json');
    mockCartResponse.shippingAddress.validAddress = false;
    productDetails['isEngravable'] = false;
    cartService['userStore']['config']['fullCatalog'] = false;
    cartService['userStore']['config']['skipBagPage'] = true;
    spyOn(cartService, 'addItemToCart').and.returnValue(of(mockAddCartResponse));
    spyOn(cartService, 'getCart').and.returnValue(of(mockCartResponse));
    cartService.addToCart(psid, productDetails);
    expect(cartService.addToCart).toHaveBeenCalled();
  }));

  it('should call addToCart method - leads to error page response', waitForAsync(() => {
    spyOn(cartService, 'addToCart').and.callThrough();
    const psid = '30001MXG22LL/A';
    const mockCartResponse = require('assets/mock/cart.json');
    productDetails['isEngravable'] = false;
    cartService['userStore']['config']['fullCatalog'] = false;
    cartService['userStore']['config']['skipBagPage'] = true;
    spyOn(cartService, 'addItemToCart').and.returnValue(of(mockAddCartResponse));
    spyOn(cartService, 'getCart').and.returnValue(throwError({statusCode: 404, statusText: 'Not Found'}));
    cartService.addToCart(psid, productDetails);
    expect(cartService.addToCart).toHaveBeenCalled();
  }));

  it('should call addToCart method - leads to related products page', waitForAsync(() => {
    spyOn(cartService, 'addToCart').and.callThrough();
    const psid = '30001MXG22LL/A';
    const mockCartResponse = require('assets/mock/cart.json');
    productDetails['isEngravable'] = false;
    productDetails.addOns['availableGiftItems'] = [
      {
        psid: '30001MK0C2AM/A',
        name: 'Apple Pencil (1st generation)',
        isEngravable: false
      }
    ];
    productDetails.hasRelatedProduct = true;
    cartService['userStore']['config']['fullCatalog'] = true;
    cartService['userStore']['config']['skipBagPage'] = false;
    spyOn(cartService, 'addItemToCart').and.returnValue(of(mockAddCartResponse));
    spyOn(cartService, 'getCart').and.returnValue(of(mockCartResponse));
    cartService.addToCart(psid, productDetails);
    expect(cartService.addToCart).toHaveBeenCalled();
  }));

  it('should call addToCart method - leads to else check for updateCart flag', waitForAsync(() => {
    spyOn(cartService, 'addToCart').and.callThrough();
    const psid = '30001MXG22LL/A';
    const mockCartResponse = require('assets/mock/cart.json');
    productDetails['isEngravable'] = false;
    productDetails.addOns['availableGiftItems'] = [];
    productDetails.hasRelatedProduct = false;
    cartService['userStore']['config']['fullCatalog'] = true;
    cartService['userStore']['config']['skipBagPage'] = false;
    spyOn(cartService, 'addItemToCart').and.returnValue(of(mockAddCartResponse));
    spyOn(cartService, 'getCart').and.returnValue(of(mockCartResponse));
    cartService.addToCart(psid, productDetails, true);
    expect(cartService.addToCart).toHaveBeenCalled();
  }));

  it('should call addToCart method - leads to else check for related products page flow', waitForAsync(() => {
    spyOn(cartService, 'addToCart').and.callThrough();
    const psid = '30001MXG22LL/A';
    const mockCartResponse = require('assets/mock/cart.json');
    productDetails['isEngravable'] = false;
    productDetails.addOns['availableGiftItems'] = [];
    productDetails.hasRelatedProduct = true;
    cartService['userStore']['config']['fullCatalog'] = true;
    cartService['userStore']['config']['skipBagPage'] = false;
    spyOn(cartService, 'addItemToCart').and.returnValue(of(mockAddCartResponse));
    spyOn(cartService, 'getCart').and.returnValue(of(mockCartResponse));
    cartService.addToCart(psid, productDetails, false);
    expect(cartService.addToCart).toHaveBeenCalled();
  }));

  it('should call addToCart method - leads to else check to cart page flow', waitForAsync(() => {
    spyOn(cartService, 'addToCart').and.callThrough();
    const psid = '30001MXG22LL/A';
    const mockCartResponse = require('assets/mock/cart.json');
    productDetails['isEngravable'] = false;
    productDetails.addOns['availableGiftItems'] = [];
    productDetails.hasRelatedProduct = false;
    cartService['userStore']['config']['fullCatalog'] = true;
    cartService['userStore']['config']['skipBagPage'] = false;
    spyOn(cartService, 'addItemToCart').and.returnValue(of(mockAddCartResponse));
    spyOn(cartService, 'getCart').and.returnValue(of(mockCartResponse));
    cartService.addToCart(psid, productDetails, false);
    productDetails.addOns['availableGiftItems'] = [
      {
        psid: '30001MK0C2AM/A',
        name: 'Apple Pencil (1st generation)',
        isEngravable: false
      },
      {
        psid: '30001MKQ42AM/A',
        name: 'USB-C to Lightning Cable (2 m)',
        isEngravable: false
      },
      {
        psid: '30001MU8F2AM/A',
        name: 'Apple Pencil (2nd generation)',
        isEngravable: true
      }
    ];
    expect(cartService.addToCart).toHaveBeenCalled();
  }));

});
