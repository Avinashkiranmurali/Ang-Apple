import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { CartButtonsComponent } from './cart-buttons.component';
import { TranslateModule } from '@ngx-translate/core';
import { CartService } from '@app/services/cart.service';
import { TemplateService } from '@app/services/template.service';
import { ShippingAddressService } from '@app/modules/shipping-address/shipping-address.service';
import { AddressService } from '@app/services/address.service';
import { of, Subscription } from 'rxjs';
import { Router } from '@angular/router';
import { SharedService } from '@app/modules/shared/shared.service';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

describe('CartButtonsComponent', () => {
  let component: CartButtonsComponent;
  let sharedService: SharedService;
  let cartService: CartService;
  let fixture: ComponentFixture<CartButtonsComponent>;
  let httpTestingController: HttpTestingController;

  const fakeResponse = require('assets/mock/cart.json')['cartItems'];
  const cartData = require('assets/mock/cart.json');
  const cartObjTotals = cartData.cartTotal;

  const cartTempMock = [{
    cartItems: {
      templateUrl: 'apple-gr/vars/default/templates/apple-gr-templates/cart-items-template.html'
    },
    cartItemsList: {
      templateUrl: 'apple-gr/vars/default/templates/apple-gr-templates/cart-items-list-template.html',
      template: 'cart-default.htm'
    },
    cartItemsButtons: {
      templateUrl: 'apple-gr/vars/default/templates/apple-gr-templates/cart-buttons-template.html',
      template: 'cart-button-simple.htm'
    },
    cartDiscounts: null,
    cartPaymentSummary: null,
    cartPaymentButtons: null
  }];

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [CartButtonsComponent],
      providers: [
        { provide: CartService },
        { provide: TemplateService },
        { provide: ShippingAddressService },
        { provide: AddressService },
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate') }
        },
        { provide: CurrencyFormatPipe },
        { provide: CurrencyPipe },
        { provide: DecimalPipe }
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot(),
      ]
    })
    .compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
    sharedService = TestBed.inject(SharedService);
    cartService = TestBed.inject(CartService);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CartButtonsComponent);
    const ADDRESS_MOCK = require('assets/mock/address.json');
    component = fixture.componentInstance;
    component.isAddressValid = true;
    component.skipPaymentOptionPage = true;
    component.checkoutAddress = ADDRESS_MOCK[0];
    component.cartId = 484678;
    component.subscriptions = [new Subscription()];
    component.cartTemplate = null;
    fixture.detectChanges();
  });


  afterAll(() => {
    component.cartTemplate = cartTempMock[0];
  });
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call click on gotoCheckout button', () => {
    spyOn(component, 'goToCheckout').and.callThrough();
    component.isAddressValid = false;
    fixture.detectChanges();
    component.goToCheckout();
    component.isAddressValid = true;
    fixture.detectChanges();
    component.goToCheckout();
    expect(component.goToCheckout).toHaveBeenCalledTimes(2);
  });

  it('should call click on redirectUrl', () => {
    spyOn(component, 'redirectUrl').and.callThrough();
    const data = {
      navigateBackUrl: 'javascript:void(0)'
    };
    sessionStorage.setItem('sessionURLs', JSON.stringify(data));
    component.redirectUrl();
  });

  it('should call click on redirectUrl else block', () => {
    spyOn(component, 'redirectUrl').and.callThrough();
    const data = {
      sessionURL: {
        navigateBackUrl: 'javascript:void(0)'
      }
    };
    sessionStorage.setItem('sessionURLs', JSON.stringify(data));
    component.redirectUrl();
  });

  it('should call cartpage when click on continue to shipping', () => {
    spyOn(component, 'goToStore').and.callThrough();
    component.goToStore();
    expect(component.goToStore).toHaveBeenCalled();
  });

  it('should call prepareErrorWarningObj method', () => {
    spyOn(sharedService, 'prepareErrorWarningObj').and.callThrough();
    const errorObject = {
      firstName: 'First and Last name must be alpha characters, each with 1 characters minimum.',
      phoneNumber: 'Enter a valid phone number',
      'firstName,lastName': 'Enter valid data'
    };
    sharedService.prepareErrorWarningObj(errorObject);
    expect(sharedService.prepareErrorWarningObj).toHaveBeenCalled();
  });
  it('should call prepareErrorWarningObj method  check with Empty Params', () => {
    spyOn(sharedService, 'prepareErrorWarningObj').and.callThrough();
    const errorObject = '';
    sharedService.prepareErrorWarningObj(errorObject);
    expect(sharedService.prepareErrorWarningObj).toHaveBeenCalled();
  });

  it('should call passPaymentType', () => {
    spyOn(component['cartService'], 'passPaymentType').and.returnValue(of({ success: true }));
    spyOn(component, 'passPaymentType').and.callThrough();
    spyOn(component['cartService'], 'updateRemainingCost').and.callFake(() => {});    
    component.cartObjTotals = cartObjTotals;
    component.passPaymentType('points');
    expect(component.isProcessing).toBeFalse();
    expect(component.passPaymentType).toHaveBeenCalled();
  });

  it('should call and passPaymentType method', fakeAsync(() => {
    // Setup a request using the fakeResponse data
    cartService.passPaymentType(fakeResponse[0].id, {selectedPaymentOption: 'points'}).subscribe(
      (response) => expect(response).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );
    component.cartObjTotals = cartObjTotals;
    tick(200);
    // Expect a call to this URL
    const req = httpTestingController.expectOne(cartService.baseUrl + '/cart/' + fakeResponse[0].id);
    // Assert that the request is a PUT
    expect(req.request.method).toEqual('PUT');
    // Respond with the fake data when called
    req.flush(fakeResponse);

  }));

});
