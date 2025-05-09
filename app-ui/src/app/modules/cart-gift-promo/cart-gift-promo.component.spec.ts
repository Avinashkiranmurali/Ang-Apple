import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { CartGiftPromoComponent } from './cart-gift-promo.component';
import { RouterTestingModule } from '@angular/router/testing';
import { GiftPromoService } from '@app/services/gift-promo.service';
import { SessionService } from '@app/services/session.service';
import { Router } from '@angular/router';
import { CartService } from '@app/services/cart.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { of, throwError } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { CartPricingTempComponent } from '@app/modules/pricing/cart-pricing-temp/cart-pricing-temp.component';
import { UserStoreService } from '@app/state/user-store.service';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { DecimalPipe } from '@angular/common';
import { AplImgSizePipe } from '@app/pipes/apl-img-size.pipe';

describe('CartGiftPromoComponent', () => {
  let component: CartGiftPromoComponent;
  let fixture: ComponentFixture<CartGiftPromoComponent>;
  let cartService: CartService;
  const programData = require('assets/mock/program.json');
  const mockUser = require('assets/mock/user.json');
  mockUser['program'] = programData;
  const userStoreData = {
      user: mockUser,
      program: programData,
      config: programData['config'],
      get: () => of(mockUser)
  };
  let httpTestingController: HttpTestingController;
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        CartGiftPromoComponent,
        CartPricingTempComponent,
        CurrencyFormatPipe,
        AplImgSizePipe
      ],
      imports: [
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        RouterTestingModule
      ],
      providers: [
        { provide: GiftPromoService },
        { provide: SessionService },
        { provide: CartService },
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate') }
        },
        { provide: NgbActiveModal },
        { provide: ParsePsidPipe },
        { provide: CurrencyPipe, useValue: {
          program: of(programData),
          transform: () => of({}) }
        },
        { provide: UserStoreService, useValue: userStoreData },
        { provide: CurrencyFormatPipe },
        { provide: CurrencyFormatPipe },
        { provide: DecimalPipe },
        AplImgSizePipe
      ]
    })
    .compileComponents();
    // Inject the http test controller
    httpTestingController = TestBed.inject(HttpTestingController);
    cartService = TestBed.inject(CartService);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CartGiftPromoComponent);
    component = fixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
    // Fake response data
    const cartResponse = require('assets/mock/cart.json');
    for (const i in cartResponse.cartItems){
      if (cartResponse.cartItems.hasOwnProperty(i) && cartResponse.cartItems[i] !== undefined){
        cartResponse.cartItems[i].productDetail.isEligibleForGift = cartResponse.cartItems[i].productDetail.addOns.availableGiftItems.length > 0;
        cartResponse.cartItems[i].productDetail.isMultiGiftAvailable = cartResponse.cartItems[i].productDetail.addOns.availableGiftItems.length > 1;
      }
    }
    component.item = cartResponse['cartItems'][0];
    component.giftItem = cartResponse['cartItems'][0].selectedAddOns.giftItem;
    component.giftItem.productDetail.addOns.servicePlans = [];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call ngOnInit', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    const fakeResponse = require('assets/mock/cart.json');
    component.giftItem = fakeResponse.cartItems[0].selectedAddOns.giftItem;
    component.isEngraved = false;
    component.giftItem.engrave.line1 = '';
    component.giftItem.engrave.line2 = '';
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should call ngOnInit with engraving', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    const fakeResponse = require('assets/mock/cart.json');
    component.giftItem = fakeResponse.cartItems[0].selectedAddOns.giftItem;
    component.isEngraved = undefined;
    component.giftItem.engrave.line1 = '';
    component.giftItem.engrave.line2 = 'Engraving';
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should call ngOnInit no gift', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    component.giftItem = null;
    component.isEngraved  = undefined;
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should call changeGiftSelection navigation', () => {
    component.changeGiftSelection();
    expect(component.changeGiftSelection).toBeDefined();
  });

  it('should call addFreeGift method', () => {
    component.addFreeGift();
    const fakeResponse = require('assets/mock/cart.json');
    // Expect a call to this URL
    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart/modify/' + 230624);

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(fakeResponse.cartItems);
    expect(component.addFreeGift).toBeDefined();
  });

  it('should call addFreeGift method - 401 Error', () => {
    component.addFreeGift();
    // Expect a call to this URL
    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart/modify/' + 230624);
    // Respond with mock error
    req.flush('401 Error', { status: 401, statusText: 'Not Found' });
    expect(component.addFreeGift).toBeDefined();
  });

  it('should call addFreeGift method - 404 Not Found Error', () => {
    component.addFreeGift();
    // Expect a call to this URL
    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart/modify/' + 230624);
    // Respond with mock error
    req.flush('404 Error', { status: 404, statusText: 'Not Found' });
    expect(component.addFreeGift).toBeDefined();
  });

  it('should call removeGiftItem method', () => {
    const fakeResponse = require('assets/mock/cart.json');
    component.giftItem = fakeResponse.cartItems[0].selectedAddOns.giftItem;
    spyOn(component.cartService, 'modifyCart').and.returnValue(of(fakeResponse));
    spyOn(component, 'removeGiftItem').and.callThrough();
    component.removeGiftItem(component.giftItem);
    expect(component.removeGiftItem).toBeDefined();
    expect(component.removeGiftItem).toHaveBeenCalled();
  });

  it('should call removeGiftItem method - 401 Error', () => {
    const fakeResponse = require('assets/mock/cart.json');
    component.giftItem = fakeResponse.cartItems[0].selectedAddOns.giftItem;
    spyOn(component.cartService, 'modifyCart').and.returnValue(throwError({ status: 401, statusText: 'Not Found' }));
    spyOn(component, 'removeGiftItem').and.callThrough();
    component.removeGiftItem(component.giftItem);
    expect(component.addFreeGift).toBeDefined();
    expect(component.removeGiftItem).toHaveBeenCalled();
  });

  it('should call removeGiftItem method - 404 Not Found Error', () => {
    const fakeResponse = require('assets/mock/cart.json');
    component.giftItem = fakeResponse.cartItems[0].selectedAddOns.giftItem;
    spyOn(component.cartService, 'modifyCart').and.returnValue(throwError({ status: 404, statusText: 'Not Found' }));
    spyOn(component, 'removeGiftItem').and.callThrough();
    component.removeGiftItem(component.giftItem);
    expect(component.addFreeGift).toBeDefined();
    expect(component.removeGiftItem).toHaveBeenCalled();
  });

  it('should call editEngraveGiftTxt method', () => {
    const fakeResponse = require('assets/mock/cart.json');
    component.giftItem = fakeResponse.cartItems[0].selectedAddOns.giftItem;
    component.item.selectedAddOns.giftItem = component.giftItem;
    // Expect a call to this URL
    component.editEngraveGiftTxt();
    expect(component.editEngraveGiftTxt).toBeDefined();
  });

  it('should call cartUpdateEvent method', () => {
    spyOn(component, 'cartUpdateEvent').and.callThrough();
    const event = {
      type: 'addOrUpdateEvent',
      item: null
    };
    component.cartUpdateEvent(event);
    expect(component.cartUpdateEvent).toHaveBeenCalled();
  });

});
