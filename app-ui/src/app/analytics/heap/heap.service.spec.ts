import { DecimalPipe } from '@angular/common';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Injectable } from '@angular/core';
import { fakeAsync, inject, TestBed, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { SharedService } from '@app/modules/shared/shared.service';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateService } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { HeapService } from './heap.service';
import { AppConstants } from '@app/constants/app.constants';
import { Program } from '@app/models/program';

@Injectable()
export class TranslateServiceStub {
  public get<T>(key: T): Observable<T> {
    return of(key);
  }

  public instant(key: string): any {
    return '';
  }
}

describe('HeapService', () => {
  let heapService: HeapService;
  const programData = require('assets/mock/program.json');
  const confirmOrder = require('assets/mock/products-confirm-order.json');
  const cartData = require('assets/mock/cart.json');
  const cartObjTotals = cartData.cartTotal;
  const mediaProducts = [{
    itemId: 'amp-music',
    addedToCart: true,
  }, {
    itemId: 'amp-news-plus',
    addedToCart: true,
  }];
  programData['config']['heapEndPoint'] = '3691461206';
  const productDetails = require('assets/mock/product-detail.json');
  const mockUser = require('assets/mock/user.json');
  mockUser['program'] = programData;
  const userData = {
    user: mockUser,
    program: programData,
    config: programData['config'],
    get: () => of(mockUser),
    addProgram: () => {}
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule
      ],
      providers: [
        {provide: TranslateService, useClass: TranslateServiceStub},
        {provide: UserStoreService, useValue: userData},
        {
          provide: Router, useValue: {
            navigate: jasmine.createSpy('navigate'),
            url: 'imac/mackbook'
          }
        },
        SharedService,
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe
      ]
    });
    heapService = TestBed.inject(HeapService);
  });

  it('should be created', () => {
    expect(heapService).toBeTruthy();
  });

  it('should call loadInitialScript', () => {
    spyOn(heapService, 'loadInitialScript').and.callThrough();
    heapService.loadInitialScript();
    expect(heapService.loadInitialScript).toHaveBeenCalled();
  });

  it('should call broadcastEvent for Product View', () => {
    window.heap.userId = '8247225847470545';
    spyOn(heapService, 'broadcastEvent').and.callThrough();
    heapService.broadcastEvent('Product View', {
      name: '13-inch MacBook Air - Gold',
      sku: 'MGQP3LL/A',
      brand: 'Apple®',
      manufacturer: 'Apple',
      categories: [{
        parents: [{
          name: 'Mac'
        }],
        name: 'MacBook Air'
      }]
    });
    expect(heapService.broadcastEvent).toHaveBeenCalled();
  });

  it('should call broadcastEvent for Order Placed', () => {
    spyOn(heapService, 'broadcastEvent').and.callThrough();
    heapService.broadcastEvent('Order Placed', {
      payload: {
        products: confirmOrder,
        productTotals: cartObjTotals
      }
    });
    expect(heapService.broadcastEvent).toHaveBeenCalled();
  });

  it('should call broadcastEvent for Item Viewed', () => {
    spyOn(heapService, 'broadcastEvent').and.callThrough();
    heapService.broadcastEvent('Item Viewed', productDetails);
    expect(heapService.broadcastEvent).toHaveBeenCalled();
  });

  it('should call broadcastEvent for Item Added to Cart', () => {
    spyOn(heapService, 'broadcastEvent').and.callThrough();
    heapService.broadcastEvent('Item Added to Cart', productDetails);
    expect(heapService.broadcastEvent).toHaveBeenCalled();
  });

  it('should call broadcastEvent for Item Removed from Cart', () => {
    spyOn(heapService, 'broadcastEvent').and.callThrough();
    heapService.broadcastEvent('Item Removed from Cart', productDetails);
    expect(heapService.broadcastEvent).toHaveBeenCalled();
  });

  it('should call broadcastEvent for Session Started', () => {
    spyOn(heapService, 'broadcastEvent').and.callThrough();
    heapService.broadcastEvent('Session Started');
    expect(heapService.broadcastEvent).toHaveBeenCalled();
  });

  it('should call addUserProperties', fakeAsync(() => {
    const userStore = TestBed.inject(UserStoreService);
    (userStore as any).activePropertyChanged = of();
    heapService.user.initialUserBalance = 999;
    tick(1);
    spyOn(heapService, 'addUserProperties').and.callThrough();
    heapService.addUserProperties();
    expect(heapService.addUserProperties).toHaveBeenCalled();
  }));

  it('should call trackOrderLinePlaced', () => {
     const items = [
       {
        'VAR ID': 'Delta',
        'VAR Name': 'Delta',
        'Program ID': 'b2s_qa_only',
        'Program Name': 'NotLogged',
        'Platform': 'Apple',
        'Storefront Type': 'Merchandise',
        'Storefront Name': 'Apple',
        'On Behalf Of': 'false',
        'Event Source': 'storefront',
        'Order ID': '2100466084',
        'Order Line Quantity': 1,
        'Order Line Item Price': 958.11,
        'Order Line Total': 958.11,
        'Order Line Item Points': 383244,
        'Order Line Total Points': 383244,
        'Item SKU': 'MGHH3LL/A',
        'Item Name': 'iPhone 12 256GB Black',
        'Item Category': 'iPhone',
        'Item Subcategory': 'iPhone 12',
        'Item Brand': 'Apple®',
        'Item Supplier': 'Apple',
        'orderLineNumber': 1
      },
       {
       'VAR ID': 'Delta',
        'VAR Name': 'Delta',
        'Program ID': 'b2s_qa_only',
        'Program Name': 'NotLogged',
        'Platform': 'Apple',
        'Storefront Type': 'Merchandise',
        'Storefront Name': 'Apple',
        'On Behalf Of': 'false',
        'Event Source': 'storefront',
        'Order ID': '2100466084',
        'Order Line Quantity': 1,
        'Order Line Item Price': 958.11,
        'Order Line Total': 958.11,
        'Order Line Item Points': 383244,
        'Order Line Total Points': 383244,
        'Item SKU': 'MGHH3LL/A',
        'Item Name': 'iPhone 12 256GB Black',
        'Item Category': 'iPhone',
        'Item Subcategory': 'iPhone 12',
        'Item Brand': 'Apple®',
        'Item Supplier': 'Apple',
        'orderLineNumber': 1
       }
     ];

    const orderLineNumber = 0;
    spyOn(heapService, 'trackOrderLinePlaced').withArgs(items, orderLineNumber).and.callThrough();

    heapService.trackOrderLinePlaced(items, orderLineNumber);
    expect(heapService.trackOrderLinePlaced).toBeDefined();
    expect(heapService.trackOrderLinePlaced).toHaveBeenCalled();
  // }));
  });

  it('should get products when getSubscribedMediaProducts is called', () => {
    const products = heapService.getSubscribedMediaProducts(mediaProducts, '2100118073');
    expect(products.length).toBeGreaterThan(0);
  });

  it('should call trackOrderLinePlaced when broadcastEvent is called', () => {
    const payload = {
      payload: {
        products: {...confirmOrder, subscribedMediaProducts: mediaProducts},
        productTotals: cartObjTotals
      }
    };
    window.heap.userId = '8247225847470545';
    spyOn(heapService, 'trackOrderLinePlaced').and.callThrough();
    heapService.broadcastEvent(AppConstants.analyticServices.HEAP_EVENTS.ORDER_LINE_PLACED, payload);
    expect(heapService.trackOrderLinePlaced).toHaveBeenCalled();
  });

  it('should call window.heap.addUserProperties with user.program object as empty', fakeAsync(inject([UserStoreService], (userStoreService) => {
    (userStoreService as any).activePropertyChanged = of();
    userStoreService.user.program = {};
    heapService.user.initialUserBalance = 999;
    tick(1);
    spyOn(window.heap, 'addUserProperties').and.callThrough();
    heapService.addUserProperties();
    expect(window.heap.addUserProperties).toHaveBeenCalled();
  })));

  it('should call broadcastEvent when broadcastSessionEvent is called', inject([UserStoreService], (userStoreService) => {
    spyOn(heapService, 'broadcastEvent');
    userStoreService.config.loginRequired = false;
    heapService.broadcastSessionEvent();
    expect(heapService.broadcastEvent).toHaveBeenCalled();
  }));

  it('should call broadcastEvent when broadcastSessionEvent is called', inject([UserStoreService], (userStoreService) => {
    spyOn(heapService, 'broadcastEvent');
    userStoreService.config.loginRequired = true;
    heapService.broadcastSessionEvent();
    expect(heapService.broadcastEvent).not.toHaveBeenCalled();
  }));

  it('should call getGiftItems method', () => {
    spyOn(heapService, 'getGiftItems').and.callThrough();
    productDetails.giftItem = {};
    expect(heapService.getGiftItems([], productDetails, '123456')).toBeDefined();
    expect(heapService.getGiftItems).toHaveBeenCalled();
  });

  it('should call getCategory method', () => {
    spyOn(heapService, 'getCategory').and.callThrough();
    const data = require('assets/mock/gift-products.json');
    expect(heapService.getCategory(data[0])).toBeDefined();
    expect(heapService.getCategory).toHaveBeenCalled();
  });

  it('should call addUserProperties method if program entries doesnot exists', () => {
    spyOn(heapService, 'addUserProperties').and.callThrough();
    heapService['userStoreService'].program = Object.assign({} as Program);
    heapService.user.balance = null;
    heapService.addUserProperties();
    expect(heapService.addUserProperties).toHaveBeenCalled();
    heapService['userStoreService'].program = programData;
    heapService.user.balance = userData.user.balance;
  });

  it('should call getOrderItem method', () => {
    spyOn(heapService, 'getOrderItem').and.callThrough();
    heapService['userStoreService'].addProgram(Object.assign({} as Program));
    heapService.user.program = Object.assign({} as Program);
    heapService.user.proxyUserId = 123;
    expect(heapService.getOrderItem(productDetails, '123456')).toBeDefined();
    expect(heapService.getOrderItem).toHaveBeenCalled();
    heapService.user.program = programData;
  });

  it('should call broadcastEvent method if program call entries doesnot exists', () => {
    spyOn(heapService, 'broadcastEvent').and.callThrough();
    heapService['userStoreService'].addProgram(Object.assign({} as Program));
    heapService.user.program = Object.assign({} as Program);
    heapService.user.proxyUserId = 123;
    heapService.broadcastEvent('Item Viewed', productDetails);
    expect(heapService.broadcastEvent).toHaveBeenCalled();
    heapService.user.program = programData;
  });

});
