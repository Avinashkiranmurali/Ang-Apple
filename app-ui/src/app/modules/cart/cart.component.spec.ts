import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { CartComponent } from './cart.component';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { CartService } from '@app/services/cart.service';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { TemplateStoreService } from '@app/state/template-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { BehaviorSubject, of, throwError } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { TranslateModule } from '@ngx-translate/core';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CartItemsComponent } from './cart-items/cart-items.component';
import { CartTotalTempComponent } from '@app/modules/pricing/cart-total-temp/cart-total-temp.component';
import { CartButtonsComponent } from './cart-buttons/cart-buttons.component';
import { Subscription } from 'rxjs';
import { AppConstants } from '@app/constants/app.constants';
import { TemplateService } from '@app/services/template.service';
import { CartPricingTempComponent } from '@app/modules/pricing/cart-pricing-temp/cart-pricing-temp.component';
import { ItemEngraveComponent } from './item-engrave/item-engrave.component';
import { CartGiftPromoComponent } from '@app/modules/cart-gift-promo/cart-gift-promo.component';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';
import { FormsModule } from '@angular/forms';
import { AddressService } from '@app/services/address.service';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';
import { Cart, CartItem, CartTotal } from '@app/models/cart';
import { TransitionService } from '@app/transition/transition.service';
import { MediaProductComponent } from '@app/modules/shared/media-product/media-product.component';
import { CarouselComponent } from '@app/modules/carousel/carousel.component';
import { DecimalPipe } from '@angular/common';
import { AplImgSizePipe } from '@app/pipes/apl-img-size.pipe';

describe('CartComponent', () => {
  let component: CartComponent;
  let fixture: ComponentFixture<CartComponent>;
  let templateStoreService: TemplateStoreService;
  let userStoreService: UserStoreService;
  let cartService: CartService;
  let httpTestingController: HttpTestingController;
  let templateService: TemplateService;
  const programData = require('assets/mock/program.json');
  const cartData = require('assets/mock/cart.json');
  const configData = require('assets/mock/configData.json');
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config'],
    enableNotificationBanner: () => of({}),
  };
  const routerEvent$ = new BehaviorSubject<NavigationEnd>(null);
  let router: Router;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        CartComponent,
        CartItemsComponent,
        CartTotalTempComponent,
        CartPricingTempComponent,
        CartButtonsComponent,
        ItemEngraveComponent,
        CartGiftPromoComponent,
        MediaProductComponent,
        CarouselComponent,
        OrderByPipe,
        CurrencyFormatPipe,
        AplImgSizePipe
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        FormsModule,
        TranslateModule.forRoot(),
      ],
      providers: [
        PricingService,
        ParsePsidPipe,
        OrderByPipe,
        CurrencyPipe,
        CurrencyFormatPipe,
        NgbActiveModal,
        TransitionService,
        DecimalPipe,
        AplImgSizePipe,
        { provide: MessagesStoreService },
        { provide: TemplateStoreService },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({ category: undefined }),
            queryParams: of({}),
            snapshot: {data: {pageName: 'BAG'}}
          }
        },
        { provide: AddressService, useValue: {
            updateRedemptionOption: () => {},
            decodeAddress: () => {}
        }},
        { provide: MatomoService, useValue: {
          broadcast: () => {}
        }},
        { provide: EnsightenService, useValue: {
            broadcastEvent: () => {}
        }}
      ]
    }).compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
    templateStoreService = TestBed.inject(TemplateStoreService);
    userStoreService = TestBed.inject(UserStoreService);
    cartService = TestBed.inject(CartService);
    templateService = TestBed.inject(TemplateService);
    templateStoreService.addTemplate(configData['configData']);
    userStoreService.addUser(userData.user);
    userStoreService.addProgram(userData.program);
    userStoreService.addConfig(userData.config);
    templateService.template = configData['configData'];
    router = TestBed.inject(Router);
    router.navigate = jasmine.createSpy('navigate');
    (router as any).events = routerEvent$.asObservable();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CartComponent);
    component = fixture.componentInstance;
    component.user = userData.user;
    component.program = userData.program;
    component.config = userData.config;
    component.subscriptions = [new Subscription()];
    component.cartData = cartData;
    component.cartObjItems = component['orderByPipe'].transform(cartData.cartItems, 'desc', 'id');
    for (const i in cartData.cartItems.cartItems){
      if (cartData.cartItems.cartItems.hasOwnProperty(i)){
        cartData.cartItems.cartItems[i].productDetail.isEligibleForGift = cartData.cartItems.cartItems[i].productDetail.addOns.availableGiftItems.length > 0;
        cartData.cartItems.cartItems[i].productDetail.isMultiGiftAvailable = cartData.cartItems.cartItems[i].productDetail.addOns.availableGiftItems.length > 1;
      }
    }
    component.currentItemQty = [];
    component.getCurrentItemQuantities(component.cartObjItems);
    component.displayCartTotal = cartData.displayCartTotal;
    fixture.detectChanges();
  });

  afterAll(() => {
    component['userStore'].program = userData.program;
    component.program = userData.program;
  });

  
  it('should create', () => {
    spyOn(component, 'setCartSubtotal').and.callFake(() => {});
    cartService.setUpdateCartObj(cartData);
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should call ngOnInit', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    component['activateRoute'].snapshot.data = {
      brcrumb: 'Cart',
      theme: 'main-cart',
      pageName: 'BAG',
      analyticsObj: {
        pgName: 'apple_products:checkout_step_1:cart',
        pgType: 'checkout',
        pgSectionType: 'apple_products'
      }
    };
    fixture.detectChanges();
    spyOn(component['sharedService'], 'getUpdatedCartItems').and.returnValue(of(true));
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should call ngOnInit - routerObject else check', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    component['activateRoute'].snapshot.data = {
      brcrumb: 'Cart',
      theme: 'main-cart',
      pageName: 'BAG'
    };
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should call back click', () => {
    component.previousUrl = '/store/curated/ipad/ipad-accessories';
    spyOn(component, 'back').and.callThrough();
    component.back();
    expect(component.back).toHaveBeenCalled();
  });

  it('should call setDisableQty', () => {
    spyOn(component, 'setDisableQty').and.callThrough();
    component.cartObjItems = require('assets/mock/cart.json')['cartItems'];
    component.setDisableQty();
    expect(component.setDisableQty).toHaveBeenCalled();
  });

  it('should call getCurrentItemQuantities', () => {
    spyOn(component, 'getCurrentItemQuantities').and.callThrough();
    const cartItemData = require('assets/mock/cart.json')['cartItems'];
    component.cartObjItems = cartItemData;
    component.currentItemQty = [];
    component.getCurrentItemQuantities(cartItemData);
    expect(component.getCurrentItemQuantities).toHaveBeenCalled();
  });

  it('should call setHasEngraving', () => {
    spyOn(component, 'setHasEngraving').and.callThrough();
    component.cartObjItems = require('assets/mock/cart.json')['cartItems'];
    component.setHasEngraving();
    expect(component.setHasEngraving).toHaveBeenCalled();
  });

  it('should call getBalances when paymentType is points_only', () => {
    spyOn(component, 'getBalances').and.callThrough();
    component.config.paymentType = AppConstants.paymentType.points_only;
    component.getBalances();
    expect(component.getBalances).toHaveBeenCalled();
  });

  it('should call getBalances when paymentType is cc_variable', () => {
    spyOn(component, 'getBalances').and.callThrough();
    component.config.paymentType = 'cc_variable';
    component.cartTotals = {
      amount: 2405.36,
      currencyCode: 'USD',
      points: 445600
    };
    component.suppMinRewardsLimit = { amount: 0, points: 0};
    component.suppmMaxPaymentLimit = { amount: 0, points: 0};
    component.getBalances();
    expect(component.getBalances).toHaveBeenCalled();
  });

  it('should call getBalances  when paymentType is not points_only', () => {
    spyOn(component, 'getBalances').and.callThrough();
    component.config.paymentType = '';
    component.getBalances();
    expect(component.getBalances).toHaveBeenCalled();
  });

  it('should call onCartUpdateSuccess', () => {
    spyOn(component, 'onCartUpdateSuccess').and.callThrough();
    const cart = require('assets/mock/cart.json');
    cart.itemsSubtotalPrice = {
      amount: 2248,
      currencyCode: 'USD',
      points: 416400
    };
    cart.maxCartTotalExceeded = false;
    component.eppStatus = true;
    component.cartData = cart;
    component.onCartUpdateSuccess(cart);
    expect(component.onCartUpdateSuccess).toHaveBeenCalled();
  });

  it('should call onCartUpdateSuccess - cart total exceeded', () => {
    spyOn(component, 'onCartUpdateSuccess').and.callThrough();
    const cart = require('assets/mock/cart.json');
    cart.maxCartTotalExceeded = true;
    cart.itemsSubtotalPrice = {
      amount: 2248,
      currencyCode: 'USD',
      points: 416400
    };
    component.cartData = cart;
    component.onCartUpdateSuccess(cart);
    expect(component.onCartUpdateSuccess).toHaveBeenCalled();
  });

  it('should call onCartUpdateSuccess - no cart items', () => {
    spyOn(component, 'onCartUpdateSuccess').and.callThrough();
    spyOn(component, 'getCurrentItemQuantities').and.callFake(() => {});
    const cart = require('assets/mock/emptyCart.json');
    cart.itemsSubtotalPrice = {
      amount: 2248,
      currencyCode: 'USD',
      points: 416400
    };
    component.cartData = cart;
    component.onCartUpdateSuccess(cart);
    expect(component.onCartUpdateSuccess).toHaveBeenCalled();
  });

  it('should call cartFocus', () => {
    spyOn(component, 'cartFocus').and.callThrough();
    component.cartFocus(2);
    expect(component.cartFocus).toHaveBeenCalled();
  });

  it('should call updateCartObj', () => {
    component.currentItemQty = [];
    component.eppStatus = true;
    spyOn(component, 'updateCartObj').and.callThrough();
    spyOn(component, 'setCartSubtotal').and.callFake(() => {});
    const cartObj = require('assets/mock/cart.json');
    cartObj.maxCartTotalExceeded = true;
    cartObj.itemsSubtotalPrice = {
      amount: 2248,
      currencyCode: 'USD',
      points: 416400
    };
    component.updateCartObj(cartObj);
    expect(component.updateCartObj).toHaveBeenCalled();
  });

  it('should call verifySkipPaymentOption', () => {
    spyOn(component, 'verifySkipPaymentOption').and.callThrough();
    const program = require('assets/mock/program.json');
    component.program.redemptionOptions = program.redemptionOptions;
    const data = require('assets/mock/cart.json');
    spyOn(component['sharedService'], 'isPointsFixed').and.returnValue(true);
    component.verifySkipPaymentOption(data);
    expect(component.verifySkipPaymentOption).toHaveBeenCalled();
  });

  it('should call cartUpdateEvent - removeItem', () => {
    spyOn(component, 'cartUpdateEvent').and.callThrough();
    const data = {
      type: 'removeItem',
      index: 0,
      item: cartData['cartItems'][0]
    };
    component.cartUpdateEvent(data);
    expect(component.cartUpdateEvent).toHaveBeenCalled();
  });

  it('should call cartUpdateEvent - editItemQty', () => {
    spyOn(component, 'cartUpdateEvent').and.callThrough();
    const data = {
      type: 'editItemQty',
      index: 0,
      item: cartData['cartItems'][0],
      id: 230624,
      qty: 2
    };
    component.cartUpdateEvent(data);
    spyOn(component, 'editItemQty').and.callFake(() => true);
    expect(component.cartUpdateEvent).toHaveBeenCalled();
  });

  it('should call cartUpdateEvent - removeCartGiftItem', () => {
    spyOn(component, 'cartUpdateEvent').and.callThrough();
    const data = {
      type: 'removeCartGiftItem',
      item: cartData['cartItems'][0]
    };
    spyOn(component, 'onCartUpdateSuccess').and.callFake(() => {});
    component.cartUpdateEvent(data);
    expect(component.cartUpdateEvent).toHaveBeenCalled();
  });

  it('should call editItemQty - maxQtyReached', () => {
    spyOn(component, 'checkMaxQtyReached').and.returnValue(true);
    spyOn(component, 'editItemQty').and.callThrough();
    const cart = require('assets/mock/cart.json');
    component.currentItemQty = [
      {
        id: 230624,
        qty: 1,
        productGroupId: null
      }
    ];
    expect(component.editItemQty(0, 230624, '2', cart['cartItems'][0])).toBeFalsy();
    expect(component.editItemQty).toHaveBeenCalled();
  });

  it('should call checkMaxQtyReached - return true', () => {
    spyOn(component, 'checkMaxQtyReached').and.callThrough();
    component.cartObjItems[0].quantityLimitExceed = true;
    expect(component.checkMaxQtyReached(230624, 0)).toBeTruthy();
    expect(component.checkMaxQtyReached).toHaveBeenCalled();
  });

  it('should call checkMaxQtyReached - return false', () => {
    spyOn(component, 'checkMaxQtyReached').and.callThrough();
    const mockCart = require('assets/mock/cart.json');
    const dataItem = mockCart.cartItems[0];
    dataItem.maxQuantity = null;
    dataItem.quantityLimitExceed = null;
    dataItem.giftCardMaxQuantity = null;
    component.cartObjItems = [dataItem];
    expect(component.checkMaxQtyReached(230624, 0)).toBeFalsy();
    expect(component.checkMaxQtyReached).toHaveBeenCalled();
  });

  it('should call checkMaxQtyReached - with maxQuantity entry', () => {
    spyOn(component, 'checkMaxQtyReached').and.callThrough();
    const mockCart = require('assets/mock/cart.json');
    const dataItem = mockCart.cartItems[0];
    dataItem.maxQuantity = 1;
    dataItem.quantityLimitExceed = null;
    dataItem.giftCardMaxQuantity = null;
    component.cartObjItems = [dataItem];
    expect(component.checkMaxQtyReached(230624, 0)).toBeFalsy();
    expect(component.checkMaxQtyReached).toHaveBeenCalled();
  });

  it('should call cartFocus', () => {
    spyOn(component, 'cartFocus').and.callThrough();
    component.cartFocus(5);
    expect(component.cartFocus).toHaveBeenCalled();
  });

  it('should call verifySkipPaymentOption - cashonly paymentType', () => {
    spyOn(component, 'verifySkipPaymentOption').and.callThrough();
    const programValue = require('assets/mock/program.json');
    programValue['redemptionOptions'] = {
      cashonly: [
        {
          id: 1751,
          varId: 'Delta',
          programId: 'b2s_qa_only',
          paymentOption: 'cashonly',
          limitType: 'percentage',
          paymentMinLimit: 50,
          paymentMaxLimit: 0,
          orderBy: 1,
          paymentProvider: null,
          lastUpdatedBy: 'Appl_user',
          lastUpdatedDate: 1526570221080,
          active: true
        }
      ]
    };
    component.program = programValue;
    const data = require('assets/mock/emptyCart.json');
    component.verifySkipPaymentOption(data);
    expect(component.verifySkipPaymentOption).toHaveBeenCalled();
  });

  it('should call verifySkipPaymentOption - splitpay paymentType', () => {
    spyOn(component, 'verifySkipPaymentOption').and.callThrough();
    const programValue = require('assets/mock/program.json');
    programValue['redemptionOptions'] = {
      splitpay: [
        {
          id: 1751,
          varId: 'Delta',
          programId: 'b2s_qa_only',
          paymentOption: 'splitpay',
          limitType: 'percentage',
          paymentMinLimit: 50,
          paymentMaxLimit: 0,
          orderBy: 1,
          paymentProvider: null,
          lastUpdatedBy: 'Appl_user',
          lastUpdatedDate: 1526570221080,
          active: true
        }
      ]
    };
    component.program = programValue;
    component.config.paymentTemplate = AppConstants.paymentTemplate.points_fixed;
    const data = require('assets/mock/emptyCart.json');
    component.verifySkipPaymentOption(data);
    expect(component.verifySkipPaymentOption).toHaveBeenCalled();
  });

  it('should call verifySkipPaymentOption - when redemptionOptions is not available', () => {
    spyOn(component, 'verifySkipPaymentOption').and.callThrough();
    const programValue = require('assets/mock/program.json');
    programValue['redemptionOptions'] = {};
    component.program = programValue;
    const data = require('assets/mock/emptyCart.json');
    component.verifySkipPaymentOption(data);
    expect(component.verifySkipPaymentOption).toHaveBeenCalled();
  });

  it('should call verifySkipPaymentOption - with redemptionPaymentLimit available', () => {
    component.config.paymentTemplate = AppConstants.paymentTemplate.points_fixed;
    spyOn(component, 'verifySkipPaymentOption').and.callThrough();
    const data = require('assets/mock/emptyCart.json');
    data.redemptionPaymentLimit = {
      cashMaxLimit: {
        amount: 0,
        currencyCode: 'USD',
        points: 2598
      }
    };
    data.cost = 0;
    component.verifySkipPaymentOption(data);
    expect(component.verifySkipPaymentOption).toHaveBeenCalled();
  });

  it('should call setCartSubtotal - discountedItemsSubtotalPrice', () => {
    spyOn(component, 'setCartSubtotal').and.callThrough();
    const data: CartTotal = {
      price: { amount: 41.73, currencyCode: 'USD', points: 99999900 },
      discountedPrice: { amount: 10, currencyCode: 'USD', points: 100 },
      shippingPrice: { amount: 10, currencyCode: 'USD', points: 100 },
      itemsSubtotalPrice: { amount: 39, currencyCode: 'USD', points: 7400 },
      discountedItemsSubtotalPrice: { amount: 39, currencyCode: 'USD', points: 7400 },
      totalTaxes: { amount: 2.73, currencyCode: 'USD', points: 600 },
      totalFees: { amount: 0, currencyCode: 'USD', points: 0 }
    } as CartTotal;
    component.isUnbundled = true;
    component.displayCartTotal = data;
    component.pricingOption = 'unbundledCheckout';
    component.config.displayDiscountedItemPriceInPriceBreakdown = true;
    component.config.paymentTemplate = AppConstants.paymentTemplate.cash_subsidy;
    component.setCartSubtotal(data);
    expect(component.setCartSubtotal).toHaveBeenCalled();
  });

  it('should call setCartSubtotal - itemsSubtotalPrice', () => {
    spyOn(component, 'setCartSubtotal').and.callThrough();
    const data: CartTotal = {
      price: { amount: 41.73, currencyCode: 'USD', points: 99999900 },
      discountedPrice: { amount: 10, currencyCode: 'USD', points: 100 },
      shippingPrice: { amount: 10, currencyCode: 'USD', points: 100 },
      itemsSubtotalPrice: { amount: 39, currencyCode: 'USD', points: 7400 },
      totalTaxes: { amount: 2.73, currencyCode: 'USD', points: 600 },
      totalFees: { amount: 0, currencyCode: 'USD', points: 0 }
    } as CartTotal;
    component.isUnbundled = true;
    component.displayCartTotal = data;
    component.pricingOption = 'unbundledCheckout';
    component.config.displayDiscountedItemPriceInPriceBreakdown = false;
    component.config.paymentTemplate = AppConstants.paymentTemplate.cash_default;
    component.setCartSubtotal(data);
    expect(component.setCartSubtotal).toHaveBeenCalled();
  });

  it('should call editItemQty - cart is not modified', () => {
    spyOn(component, 'checkMaxQtyReached').and.returnValue(false);
    spyOn(component, 'editItemQty').and.callThrough();
    cartData.cartTotalModified = false;
    component.cartData = cartData;
    spyOn(cartService, 'modifyCart').and.callFake(() => of(cartData));
    component.editItemQty(0, 230624, 2, cartData.cartItems[0]);
    expect(component.editItemQty).toHaveBeenCalled();
  });

  it('should call editItemQty - cart is modified', () => {
    spyOn(component, 'checkMaxQtyReached').and.returnValue(false);
    spyOn(component, 'editItemQty').and.callThrough();
    component.config.paymentType = AppConstants.paymentType.cc_variable;
    component.config.splitTenderCart = {};
    cartData.cartTotalModified = true;
    cartData.cartItems[0].prevQuantity = 0;
    spyOn(cartService, 'modifyCart').and.callFake(() => of(cartData));
    component.editItemQty(0, 230624, 2, cartData.cartItems[0]);
    expect(component.editItemQty).toHaveBeenCalled();
  });

  it('should call editItemQty - cart is modified else check', () => {
    spyOn(component, 'checkMaxQtyReached').and.returnValue(false);
    spyOn(component, 'editItemQty').and.callThrough();
    component.config.paymentType = AppConstants.paymentType.cc_variable;
    component.config.splitTenderCart = null;
    cartData.cartTotalModified = true;
    cartData.cartItems[0].prevQuantity = 4;
    spyOn(cartService, 'modifyCart').and.callFake(() => of(cartData));
    component.editItemQty(0, 230624, 2, cartData.cartItems[0]);
    expect(component.editItemQty).toHaveBeenCalled();
  });

  it('should call editItemQty - 404 error response', () => {
    spyOn(component, 'checkMaxQtyReached').and.returnValue(false);
    spyOn(component, 'editItemQty').and.callThrough();
    const errorResponse = { status: 404, statusText: 'Not Found' };
    spyOn(cartService, 'modifyCart').and.callFake(() => throwError(errorResponse));
    component.editItemQty(0, 230624, 2, cartData['cartItems'][0]);
    expect(component.editItemQty).toHaveBeenCalled();
  });

  it('should call editItemQty - 0 error response', () => {
    spyOn(component, 'checkMaxQtyReached').and.returnValue(false);
    spyOn(component, 'editItemQty').and.callThrough();
    const errorResponse = { status: 0, statusText: 'Error Found' };
    spyOn(cartService, 'modifyCart').and.callFake(() => throwError(errorResponse));
    component.editItemQty(0, 230624, 2, cartData['cartItems'][0]);
    expect(component.editItemQty).toHaveBeenCalled();
  });

  it('should call updateCartObj - without cartItems', () => {
    spyOn(component, 'updateCartObj').and.callThrough();
    spyOn(component, 'setCartSubtotal').and.callFake(() => {});
    spyOn(component, 'getCurrentItemQuantities').and.callFake(() => {});
    component.eppStatus = false;
    const mockCartData = require('assets/mock/emptyCart.json');
    mockCartData.paymentLimit = {};
    component.updateCartObj(mockCartData);
    expect(component.updateCartObj).toHaveBeenCalled();
  });

  it('should call getCurrentItemQuantities - without cartItems', () => {
    spyOn(component, 'getCurrentItemQuantities').and.callThrough();
    const mockCartItems = [{}, {}] as CartItem[];
    component.getCurrentItemQuantities(mockCartItems);
    expect(component.getCurrentItemQuantities).toHaveBeenCalled();
  });

  it('should call setHasEngraving, setDisableQty - without cartItems', () => {
    spyOn(component, 'setHasEngraving').and.callThrough();
    spyOn(component, 'setDisableQty').and.callThrough();
    component.cartObjItems = null;
    component.setHasEngraving();
    component.setDisableQty();
    expect(component.setHasEngraving).toHaveBeenCalled();
    expect(component.setDisableQty).toHaveBeenCalled();
  });

  it('should call onCartUpdateSuccess - else check', () => {
    spyOn(component, 'onCartUpdateSuccess').and.callThrough();
    const cart = require('assets/mock/cart.json');
    cart.itemsSubtotalPrice = {
      amount: 2248,
      currencyCode: 'USD',
      points: 416400
    };
    cart.paymentLimit = {minNotMet: true, maxExceed: true};
    cart.maxCartTotalExceeded = false;
    component.eppStatus = false;
    component.cartData = cart;
    component.onCartUpdateSuccess(cart);
    expect(component.onCartUpdateSuccess).toHaveBeenCalled();
  });

  it('should call onCartUpdateSuccess - without paymentLimit', () => {
    spyOn(component, 'onCartUpdateSuccess').and.callThrough();
    const cart = require('assets/mock/cart.json');
    cart.maxCartTotalExceeded = true;
    cart.itemsSubtotalPrice = {
      amount: 2248,
      currencyCode: 'USD',
      points: 416400
    };
    component.eppStatus = false;
    component.cartData = cart;
    component.onCartUpdateSuccess(cart);
    expect(component.onCartUpdateSuccess).toHaveBeenCalled();
  });

  it('should call editItemQty - maxQtyReached and id mismatch', () => {
    spyOn(component, 'checkMaxQtyReached').and.returnValue(true);
    spyOn(component, 'editItemQty').and.callThrough();
    const cart = require('assets/mock/cart.json');
    component.currentItemQty = [
      {
        id: 230624,
        qty: 1,
        productGroupId: null
      }
    ];
    expect(component.editItemQty(0, 776268, '2', cart['cartItems'][0])).toBeFalsy();
    expect(component.editItemQty).toHaveBeenCalled();
  });
  
  it('should call isActionProcessing method', () => {
    component.isActionProcessing(true);
    expect(component.disableBtn).toBeTruthy();
  });

  it('should call verifySkipPaymentOption for pointsFixed redemption', () => {
    spyOn(component, 'verifySkipPaymentOption').and.callThrough();
    spyOn(component['sharedService'], 'isPointsFixed').and.returnValue(true);
    cartData.cost = 0;
    expect(component.verifySkipPaymentOption(cartData)).toBeTruthy();
    expect(component.verifySkipPaymentOption).toHaveBeenCalled();
  });

  it('should call removeItem method where no items in bag', () => {
    let removeResponse = Object.assign({} as Cart);
    removeResponse = {
      cartItems: []
    };
    spyOn(component['cartService'], 'modifyCart').and.returnValue(of(removeResponse));
    spyOn(component, 'removeItem').and.callThrough();
    component.removeItem(0, cartData.cartItems[0]);
    expect(component.removeItem).toHaveBeenCalled();
  });

  it('should call removeItem - success response', () => {
    spyOn(component, 'removeItem').and.callThrough();
    spyOn(cartService, 'modifyCart').and.returnValue(of(cartData));
    component.removeItem(0, cartData.cartItems[0]);
    expect(component.removeItem).toHaveBeenCalled();
  });

  it('should call removeItem - error 404 response', () => {
    spyOn(component, 'removeItem').and.callThrough();
    spyOn(cartService, 'modifyCart').and.returnValue(throwError({ status: 404, statusText: 'Not Found' }));
    component.removeItem(0, cartData.cartItems[0]);
    expect(component.removeItem).toHaveBeenCalled();
  });

  it('should call removeItem - error 0 response', () => {
    spyOn(component, 'removeItem').and.callThrough();
    spyOn(cartService, 'modifyCart').and.returnValue(throwError({ status: 0, statusText: 'Not Found' }));
    component.removeItem(0, cartData.cartItems[0]);
    expect(component.removeItem).toHaveBeenCalled();
  });

  it('should call getCart', () => {
    component.isCartUpdate = true;
    const mockCartResponse = require('assets/mock/cart.json');
    spyOn(cartService, 'getCart').and.returnValue(of(mockCartResponse));
    spyOn(component, 'getCart').and.callThrough();
    component.config.cartModified = true;
    component.eppStatus = true;
    fixture.detectChanges();
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  });

  it('should call getCart - eppStatus as true', () => {
    component.eppStatus = true;
    component.config.cartModified = true;
    const mockCartResponse = require('assets/mock/cart.json');
    mockCartResponse.selectedPaymentOption = 'points';
    spyOn(cartService, 'getCart').and.returnValue(of(mockCartResponse));
    spyOn(component, 'getCart').and.callThrough();
    fixture.detectChanges();
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  });

  it('should call getCart - eppStatus as true - selectedPaymentOption not available', () => {
    component.eppStatus = true;
    component.showFeeDetails = true;
    component.config.cartModified = false;
    component.returnedWithGenericError = true;
    component.isUnbundled = true;
    fixture.detectChanges();
    const mockCartResponse = require('assets/mock/cart-with-noSufficientPoints.json');
    mockCartResponse.selectedPaymentOption = null;
    mockCartResponse.discounts = [{
      discountCode: 'Testing',
      shortDescription: 'Testing',
      longDescription: 'Testing',
      discountType: 'Testing',
      discountAmount: 'Testing'
    }];
    mockCartResponse.paymentLimit = {};
    mockCartResponse.maxCartTotalExceeded = true;    
    spyOn(cartService, 'getCart').and.returnValue(of(mockCartResponse));
    spyOn(component, 'getCart').and.callThrough();
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  });

  it('should call getCart - eppStatus as true - maxCartTotalExceeded is false', () => {
    component.eppStatus = true;
    component.showFeeDetails = true;
    component.config.cartModified = true;
    component.returnedWithGenericError = true;
    component.isUnbundled = false;
    fixture.detectChanges();
    const mockCartResponse = require('assets/mock/cart-with-noSufficientPoints.json');
    mockCartResponse.selectedPaymentOption = null;
    mockCartResponse.discounts = [{
      discountCode: 'Testing',
      shortDescription: 'Testing',
      longDescription: 'Testing',
      discountType: 'Testing',
      discountAmount: 'Testing'
    }];
    mockCartResponse.paymentLimit = {minNotMet: false, maxExceed: false};
    mockCartResponse.maxCartTotalExceeded = false;    
    spyOn(cartService, 'getCart').and.returnValue(of(mockCartResponse));
    spyOn(component, 'getCart').and.callThrough();
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  });

  it('should call getCart - emptyCart items', () => {
    const emptyCart = require('assets/mock/emptyCart.json');
    emptyCart.cartItems = [];
    emptyCart.cost = 0;
    component.config.cartModified = false;
    component.eppStatus = false;
    component.showFeeDetails = false;
    component.quantityLimitExceed = true;
    fixture.detectChanges();
    spyOn(cartService, 'getCart').and.returnValue(of(emptyCart));
    spyOn(component, 'getCart').and.callThrough();
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  });

  it('should call getCart - error 404 response', () => {
    spyOn(component, 'getCart').and.callThrough();
    spyOn(cartService, 'getCart').and.returnValue(throwError({ status: 404, statusText: 'Not Found' }));
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  });

  it('should call getCart - error 0 response', () => {
    spyOn(component, 'getCart').and.callThrough();
    spyOn(cartService, 'getCart').and.returnValue(throwError({ status: 0, statusText: 'Not Found' }));
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  });

});
