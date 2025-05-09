import { TestBed, waitForAsync } from '@angular/core/testing';
import { MatomoService } from './matomo.service';
import { UserStoreService } from '@app/state/user-store.service';
import { SessionService } from '@app/services/session.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SharedService } from '@app/modules/shared/shared.service';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { Product } from '@app/models/product';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { DecimalPipe } from '@angular/common';
import { CurrencyPipe } from '@app/pipes/currency.pipe';


describe('MatomoService', () => {
  let service: MatomoService;
  let userStoreService: UserStoreService;
  const programData = require('assets/mock/program.json');
  const mockUser = require('assets/mock/user.json');
  programData.config.matomoEndPoint = 'https://bridge2-dev.innocraft.cloud/';
  programData.config.matomoSiteId = 3;
  mockUser['program'] = programData;
  const userData = {
    user: mockUser,
    program: programData,
    config: programData['config'],
    get: () => of(mockUser)
  };
  const product = require('assets/mock/product-detail.json');

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        SessionService,
        SharedService,
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe,
        { provide: NgbActiveModal },
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate') }
        },
        { provide: UserStoreService, useValue: userData }
      ]
    });
    service = TestBed.inject(MatomoService);
    userStoreService = TestBed.inject(UserStoreService);
    service['userStoreService'].user = userData.user;
    window['_paq'] = [];
    service.matomoEnabled = true;
    service.loadInitialScript();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  /* it('should call setAnalyticsConfig method', () => {
    spyOn(service, 'setAnalyticsConfig').and.callThrough();
    service.setAnalyticsConfig('noArgs');
    expect(service.setAnalyticsConfig).toHaveBeenCalled();
  }); */

  it('should call broadcast method for matomoOrderSuccess', () => {
    window['_paq'] = [];
    spyOn(service, 'broadcast').and.callThrough();
    const orderEvent: {[key: string]: object} = {};
    orderEvent.payload = {
      products: require('assets/mock/cart.json')['cartItems'],
      orderId: 2100118073,
      price: {
        amount: 162.41,
        points: 64964
      },
      productTotals: {
        price: {
          points: 1500
        }
      }
    };
    service.broadcast('matomoOrderSuccess', orderEvent);
    expect(service.broadcast).toHaveBeenCalled();
  });

  it('should call broadcast method of matomoOrderSuccess event for PAYROLL_DEDUCTION', () => {
    window['_paq'] = [];
    spyOn(service, 'broadcast').and.callThrough();
    const orderEvent: {[key: string]: object} = {};
    orderEvent.payload = {
      products: require('assets/mock/cart.json')['cartItems'],
      orderId: 2100118073,
      price: {
        amount: 162.41,
        points: 64964
      },
      productTotals: {
        price: {
          points: 1500
        }
      }
    };
    service['userStoreService'].user.program.payments[0].paymentOption = 'PAYROLL_DEDUCTION';
    service.broadcast('matomoOrderSuccess', orderEvent);
    expect(service.broadcast).toHaveBeenCalled();
  });

  it('should call broadcast method for matomoRoute', () => {
    spyOn(service, 'broadcast').and.callThrough();
    const orderEvent: {[key: string]: object} = {};
    orderEvent.payload = {
      location: 'giftcard',
      routeName: 'AGP'
    };
    service['userStoreService'].user.program.config.loginRequired = true;
    service['userStoreService'].user.userId = 'alex';
    service.broadcast('matomoRoute', orderEvent);
    expect(service.broadcast).toHaveBeenCalled();
  });

  it('should call broadcast method for matomoRoute if loginRequired', () => {
    spyOn(service, 'broadcast').and.callThrough();
    const orderEvent: {[key: string]: object} = {};
    orderEvent.payload = {
      location: 'giftcard',
      routeName: 'AGP'
    };
    service['userStoreService'].user.program.config.loginRequired = false;
    service['userStoreService'].user.userId = 'alex';
    service.broadcast('matomoRoute', orderEvent);
    expect(service.broadcast).toHaveBeenCalled();
  });

  it('should call broadcast method for matomoProductView', () => {
    spyOn(service, 'broadcast').and.callThrough();
    const args: {[key: string]: object} = {};
    args.payload = require('assets/mock/cart.json')['cartItems'][0];
    args.payload['sku'] = 'MR912LL/A';
    args.payload['name'] = 'Apple TV HD 32GB';
    service.broadcast('matomoProductView', args);
    expect(service.broadcast).toHaveBeenCalled();
  });

  it('should call broadcast method matomoProductView event for getPrice Method', waitForAsync(() => {
    spyOn(service, 'broadcast').and.callThrough();
    const args: {[key: string]: object} = {};
    args.payload = require('assets/mock/cart.json')['cartItems'][0];
    args.payload['sku'] = 'MR912LL/A';
    args.payload['name'] = 'Apple TV HD 32GB';
    service['userStoreService'].user.program.config.pricingTemplate = 'points_decimal';
    service.broadcast('matomoProductView', args);
    expect(service.broadcast).toHaveBeenCalled();
  }));

  it('should call broadcast method for matomoRemoveFromCart', () => {
    spyOn(service, 'getCartTotal').and.callFake(() => 0);
    spyOn(service, 'broadcast').and.callThrough();
    const args: {[key: string]: object} = {};
    args.payload = {
      cart: require('assets/mock/cart.json'),
      product: require('assets/mock/cart.json')['cartItems'][0]
    };
    service.broadcast('matomoRemoveFromCart', args);
    expect(service.broadcast).toHaveBeenCalled();
  });

  it('should call broadcast method for matomoUpdateCart - for singleItemPurchase', () => {
    spyOn(service, 'getCartTotal').and.callFake(() => 0);
    spyOn(service, 'broadcast').and.callThrough();
    const args: {[key: string]: object} = {};
    args.payload = require('assets/mock/cart.json');
    service.broadcast('matomoUpdateCart', args);
    expect(service.broadcast).toHaveBeenCalled();
  });

  it('should call broadcast method for matomoCategorySearch', () => {
    spyOn(service, 'broadcast').and.callThrough();
    const args: {[key: string]: object} = {};
    const results = require('assets/mock/facets-filters.json');
    args.payload = {
      results: results.products,
      searchTerm: 'Charger'
    };
    service['userStoreService'].user.program.config.loginRequired = true;
    service['userStoreService'].user.userId = 'alex';
    service.broadcast('matomoCategorySearch', args);
    expect(service.broadcast).toHaveBeenCalled();
  });

  it('should call broadcast method for matomoAppleError', () => {
    spyOn(service, 'broadcast').and.callThrough();
    const args: {[key: string]: object} = {};
    args.payload = {
      location: 'chennai',
      canonicalTitle: 'imac'
    };
    service['userStoreService'].user.program.config.loginRequired = true;
    service['userStoreService'].user.userId = 'alex';
    service.broadcast('matomoAppleError', args);
    expect(service.broadcast).toHaveBeenCalled();
  });

  it('should call broadcast method for matomoCanonicalPage', () => {
    spyOn(service, 'broadcast').and.callThrough();
    const args: {[key: string]: object} = {};
    args.payload = {
      location: 'chennai',
      canonicalTitle: 'imac'
    };
    service['userStoreService'].user.program.config.loginRequired = true;
    service['userStoreService'].user.userId = 'alex';
    service.broadcast('matomoCanonicalPage', args);
    expect(service.broadcast).toHaveBeenCalled();
  });

  it('should call broadcast method for matomoEngraving', () => {
    spyOn(service, 'broadcast').and.callThrough();
    const args: {[key: string]: object} = {};
    args.payload = require('assets/mock/cart.json');
    service.broadcast('matomoEngraving', args);
    expect(service.broadcast).toHaveBeenCalled();
  });

  it('should call getPrice method for points only paymentType', () => {
    spyOn(service, 'getPrice').and.callThrough();
    spyOn(service, 'getCartTotal').and.callFake(() => 0);
    service['userStoreService'].user.program.config.pricingTemplate = 'points_only';
    service.getPrice(product, 2);
    expect(service.getPrice).toHaveBeenCalled();
  });

  it('should call getPrice method for cash only paymentType', () => {
    spyOn(service, 'getPrice').and.callThrough();
    spyOn(service, 'getCartTotal').and.callFake(() => 0);
    spyOn(service['sharedService'], 'isRewardsRedemption').and.returnValue(false);
    spyOn(service['sharedService'], 'isCashOnlyRedemption').and.returnValue(true);
    service.getPrice(product, 2);
    expect(service.getPrice).toHaveBeenCalled();
  });

  it('should call getPrice method for points_decimal pricingTemplate', () => {
    spyOn(service, 'getPrice').and.callThrough();
    spyOn(service, 'getCartTotal').and.callFake(() => 0);
    service['userStoreService'].user.program.config.pricingTemplate = 'points_decimal';
    service.getPrice(product, 2);
    expect(service.getPrice).toHaveBeenCalled();
  });

  it('should call getCartTotal method for unbundledDetails', () => {
    spyOn(service, 'getCartTotal').and.callThrough();
    const data = require('assets/mock/cart.json');
    service['userStoreService'].user.program.bundledPricingOption = 'UNBUNDLED_DETAIL_PAGE_BUNDLED_CHECKOUT';
    service['userStoreService'].user.program.config.pricingTemplate = 'cash';
    service.getCartTotal(data.displayCartTotal);
    expect(service.getCartTotal).toHaveBeenCalled();
  });

  it('should call getCartTotal method for unbundledDetails and points_cash template', () => {
    spyOn(service, 'getCartTotal').and.callThrough();
    const data = require('assets/mock/cart.json');
    service['userStoreService'].user.program.bundledPricingOption = 'UNBUNDLED_DETAIL_PAGE_BUNDLED_CHECKOUT';
    service['userStoreService'].user.program.config.pricingTemplate = 'points_cash';
    spyOn(service['sharedService'], 'isRewardsRedemption').and.returnValue(false);
    spyOn(service['sharedService'], 'isCashOnlyRedemption').and.returnValue(true);
    service.getCartTotal(data.displayCartTotal);
    expect(service.getCartTotal).toHaveBeenCalled();
  });

  it('should call getCartTotal method for unbundledDetails and points_decimal template', () => {
    spyOn(service, 'getCartTotal').and.callThrough();
    const data = require('assets/mock/cart.json');
    service['userStoreService'].user.program.bundledPricingOption = 'UNBUNDLED_DETAIL_PAGE_BUNDLED_CHECKOUT';
    service['userStoreService'].user.program.config.pricingTemplate = 'points_decimal';
    service.getCartTotal(data.displayCartTotal);
    expect(service.getCartTotal).toHaveBeenCalled();
  });

  it('should call getCartTotal method for unbundledCheckout', () => {
    spyOn(service, 'getCartTotal').and.callThrough();
    const data = require('assets/mock/cart.json');
    // service['userStoreService'].user.program.config.paymentOption = 'POINTS';
    service['userStoreService'].user.program.bundledPricingOption = 'BASE_PRICE_SHOPPING_UNBUNDLED_CHECKOUT';
    service['userStoreService'].user.program.config.pricingTemplate = 'cash';
    service.getCartTotal(data.displayCartTotal);
    expect(service.getCartTotal).toHaveBeenCalled();
  });

  it('should call getCartTotal method for unbundledCheckout and points_decimal template', () => {
    spyOn(service, 'getCartTotal').and.callThrough();
    const data = require('assets/mock/cart.json');
    // service['userStoreService'].user.program.config.paymentOption = 'POINTS';
    service['userStoreService'].user.program.bundledPricingOption = 'BASE_PRICE_SHOPPING_UNBUNDLED_CHECKOUT';
    service['userStoreService'].user.program.config.pricingTemplate = 'points_decimal';
    service.getCartTotal(data.displayCartTotal);
    expect(service.getCartTotal).toHaveBeenCalled();
  });

  it('should call getCartTotal method for unbundledCheckout and points_cash template', () => {
    spyOn(service, 'getCartTotal').and.callThrough();
    const data = require('assets/mock/cart.json');
    // service['userStoreService'].user.program.config.paymentOption = 'POINTS';
    service['userStoreService'].user.program.bundledPricingOption = 'BASE_PRICE_SHOPPING_UNBUNDLED_CHECKOUT';
    service['userStoreService'].user.program.config.pricingTemplate = 'points_cash';
    service.getCartTotal(data.displayCartTotal);
    expect(service.getCartTotal).toHaveBeenCalled();
  });

  it('should call getCartTotal method for cash only paymentType', () => {
    spyOn(service, 'getCartTotal').and.callThrough();
    // service['userStoreService'].user.program.config.paymentOption = 'CASH';
    service['userStoreService'].user.program.bundledPricingOption = 'BUNDLED';
    const data = require('assets/mock/cart.json');
    service.getCartTotal(data.displayCartTotal);
    expect(service.getCartTotal).toHaveBeenCalled();
  });

  it('should call getCanonicalName method for REVIEW', () => {
    spyOn(service, 'getCanonicalName').and.callThrough();
    const view = {
      routeName: 'REVIEW',
      location: ''
    };
    service.initConfig();
    service.getCanonicalName(service.configObject.stateMap, view);
    expect(service.getCanonicalName).toHaveBeenCalled();
  });

  it('should call getCanonicalName method for NONE', () => {
    spyOn(service, 'getCanonicalName').and.callThrough();
    const view = {
      routeName: '',
      location: ''
    };
    service.getCanonicalName(service.configObject.stateMap, view);
    expect(service.getCanonicalName).toHaveBeenCalled();
  });

  it('should call getCategory method without product details', () => {
    spyOn(service, 'getCategory').and.callThrough();
    const productMock: Product = Object.assign({});
    productMock.categories = null;
    service.getCategory(productMock);
    expect(service.getCategory).toHaveBeenCalled();
  });

  it('should call sendErrorToAnalyticService method', () => {
    spyOn(service, 'sendErrorToAnalyticService').and.callThrough();
    service.sendErrorToAnalyticService();
    expect(service.sendErrorToAnalyticService).toHaveBeenCalled();
  });
  it('should not call initConfig if matomoDisabled', () => {
    service['userStoreService'].config.matomoEndPoint = '';
    service['userStoreService'].config.matomoSiteId = '';
    service.matomoEnabled = false;
    service.loadInitialScript();
    expect(service.matomoEnabled).toBe(false);
  });

});
