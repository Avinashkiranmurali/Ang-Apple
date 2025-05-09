import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse, HttpRequest, HTTP_INTERCEPTORS } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed, waitForAsync } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Cart } from '@app/models/cart';
import { Messages } from '@app/models/messages';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { CartService } from '@app/services/cart.service';
import { TranslateModule } from '@ngx-translate/core';
import { LoaderInterceptorService } from './loader-interceptor.service';
import { MessagesService } from '@app/services/messages.service';
import { SharedService } from '@app/modules/shared/shared.service';

describe('LoaderInterceptorService', () => {
  let service: LoaderInterceptorService;
  let httpMock: HttpTestingController;
  let cartService: CartService;
  let sharedService: SharedService;
  let messageService: MessagesService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: LoaderInterceptorService },
        { provide: Router, useValue: {
          url: '/store/cart',
          navigate: jasmine.createSpy('navigate') }
        },
        {
          provide: HTTP_INTERCEPTORS,
          useClass: LoaderInterceptorService,
          multi: true
        },
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe
      ]
    });
    httpMock = TestBed.inject(HttpTestingController);
    cartService = TestBed.inject(CartService);
    httpTestingController = TestBed.inject(HttpTestingController);
    service = TestBed.inject(LoaderInterceptorService);
    messageService = TestBed.inject(MessagesService);
    sharedService = TestBed.inject(SharedService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should intercept requested api service - succeeded', () => {
    // Fake response data
    const fakeResponse = require('assets/mock/cart.json')['cartItems'];
    const req: HttpRequest<any> = Object.assign({});
    req['url' as any] = '/apple-gr/service/cart';
    req['method' as any] = 'GET';
    service.intercept(req, null);
    cartService.getCart().subscribe(
      (cart: Cart) => expect(cart).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );
    // Expect a call to this URL
    const cartReq = httpTestingController.expectOne(cartService.baseUrl + 'cart');
    // Assert that the request is a GET
    expect(cartReq.request.method).toEqual('GET');
    // Respond with the fake data when called
    cartReq.flush(fakeResponse);
  });

  it('should intercept requested api service - for engrave api response', () => {
    // Fake response data
    const fakeResponse = require('assets/mock/product-detail.json');
    const req: HttpRequest<any> = Object.assign({});
    req['url' as any] = '/apple-gr/service/products/30001MME23LL/A?withVariations=false&withEngraveConfig=true';
    req['method' as any] = 'GET';
    service.intercept(req, null);
    sharedService.getProducts('/30001MME23LL/A?withVariations=false&withEngraveConfig=true').subscribe(
      (msg: Messages) => expect(msg).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );
    // Expect a call to this URL
    const msgReq = httpTestingController.expectOne(cartService.baseUrl + 'products/30001MME23LL/A?withVariations=false&withEngraveConfig=true');
    // Assert that the request is a GET
    expect(msgReq.request.method).toEqual('GET');
    // Respond with the fake data when called
    msgReq.flush(fakeResponse);
  });

  it('should intercept requested api service - messages', () => {
    // Fake response data
    const fakeResponse = require('assets/mock/messages.json');
    const req: HttpRequest<any> = Object.assign({});
    req['url' as any] = '/apple-gr/service/messages';
    req['method' as any] = 'GET';
    service.intercept(req, null);
    messageService.getMessages().subscribe(
      (data: Messages) => expect(data).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );
    // Expect a call to this URL
    const cartReq = httpTestingController.expectOne('/apple-gr/service/messages');
    // Assert that the request is a GET
    expect(cartReq.request.method).toEqual('GET');
    // Respond with the fake data when called
    cartReq.flush(fakeResponse);
  });

  it('should test for 404 error - removeRequest if failed', waitForAsync(() => {
    const req: HttpRequest<any> = Object.assign({});
    req['url' as any] = '/apple-gr/service/cart';
    req['method' as any] = 'GET';
    service.intercept(req, null);
    const errorMsg = 'deliberate 404 error';
    cartService.getCart().subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );
    const cartReq = httpTestingController.expectOne(cartService.baseUrl + 'cart');
    // Respond with mock error
    cartReq.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

  it('should call for else', () => {
    const req: HttpRequest<any> = Object.assign({});
    req['url' as any] = '/apple-gr/service/cart';
    req['method' as any] = 'GET';
    service.intercept(req, null);
    expect(req.url.indexOf('cart') < 0);
  });

  it('should call for removeReq', () => {
    spyOn(service, 'removeRequest').and.callThrough();
    const fakeResponse = require('assets/mock/cart.json');
    const req: HttpRequest<any> = Object.assign({});
    req['url' as any] = '/apple-gr/service/cart';
    req['method' as any] = 'GET';
    service['requests'] = [req];
    service.removeRequest(req);
    expect(service.removeRequest).toHaveBeenCalled();
  });

});
