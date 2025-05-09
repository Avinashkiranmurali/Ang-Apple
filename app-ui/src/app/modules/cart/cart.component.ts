import { Component, OnDestroy, OnInit, Injector, ViewChild, ElementRef, Renderer2 } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { CartService } from '@app/services/cart.service';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { SessionService } from '@app/services/session.service';
import { TemplateStoreService } from '@app/state/template-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { Address } from '@app/models/address';
import { Cart, CartItem, CartTotal } from '@app/models/cart';
import { RedemptionPaymentLimit  } from '@app/models/payment-limit';
import { SmartPrice } from '@app/models/smart-price';
import { Config } from '@app/models/config';
import { Messages } from '@app/models/messages';
import { Program } from '@app/models/program';
import { User } from '@app/models/user';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { TranslateService } from '@ngx-translate/core';
import { CartItemsComponent } from './cart-items/cart-items.component';
import { Subscription } from 'rxjs';
import { Price } from '@app/models/price';
import { SharedService } from '@app/modules/shared/shared.service';
import { AddressService } from '@app/services/address.service';
import { NotificationRibbonService } from '@app/services/notification-ribbon.service';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { AppConstants } from '@app/constants/app.constants';
import { TransitionService } from '@app/transition/transition.service';
import { BreakPoint } from '@app/components/utils/break-point';
import { HeapService } from '@app/analytics/heap/heap.service';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';
@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.scss']
})

export class CartComponent extends BreakPoint implements OnInit, OnDestroy {

  messages: Messages;
  user: User;
  config: Config;
  program: Program;
  cartTemplate: object;
  cartData: Cart;
  cartObjTotals: CartTotal;
  displayCartTotal: CartTotal;
  cartObjItems: Array<CartItem>;
  awpPayrollEnabled: boolean;
  cartLoadingError: boolean;
  emptyCart: boolean;
  isUnbundled: boolean;
  pricingOption: string;
  cartSubtotal: Price = {
    amount: 0,
    points: 0,
    currencyCode: ''
  };
  cartDiscountedSubtotal: {};
  shippingCost: object;
  totalTaxes: Price = {
    amount: 0,
    points: 0,
    currencyCode: ''
  };
  totalFees: Price = {
    amount: 0,
    points: 0,
    currencyCode: ''
  };
  cartTotals: Price = {
    amount: 0,
    points: 0,
    currencyCode: ''
  };
  maxInstallments: number | string;
  isCartActual: boolean;
  showCartItems: boolean;
  showTaxDisclaimer: boolean;
  isAddressValid: boolean;
  eppStatus: boolean;
  setCartTaxFeeTitle: string;
  discountMaxLimit: number | string;
  cartId: number;
  earnPoints: number;
  pointPurchaseRate: number;
  addPoints: number;
  cost: number;
  redemptionPaymentLimit: RedemptionPaymentLimit;
  returnedWithError = false;
  returnedWithGenericError = false;
  maxCartTotalExceeded = false;
  disableBtn = true;
  isProcessing = false;
  employerManaged;
  subTotalExt = '';
  paymentLimit = {
    minNotMet: false,
    maxExceed: false
  };
  offscreenText = {
    msg: ''
  };
  checkoutAddress: Address;
  pointsAvailable: number;
  negativeBalance: boolean;
  giftMessageError: boolean;
  giftMessageErrorText: string;
  hasEngraving: Array<boolean>;
  currentItemQty: Array<{ [k: string]: number }>;
  showFeeDetails: boolean;
  setEHFtitle: string;
  returnedError = false;
  quantityLimitExceed: boolean;
  paymentTemplate: string;
  paymentOption: string;
  purchaseType = {
    type: ''
  };
  skipPaymentOptionPage: boolean;
  paymentRequired: boolean;
  pointsToUse: number;
  pointsUsed: number;
  pointsPurchase: number;
  minPurchaseTotal: number;
  remainingBalance: number;
  totalPayment: number;
  totalPurchase: number;
  suppmMaxPaymentLimit: Price;
  suppMinRewardsLimit: Price;
  isCartUpdate: boolean;
  analyticsUserObject: any;
  discountedSubtotal: any;
  @ViewChild('cartItemComp') cartItemComp: CartItemsComponent;
  @ViewChild('cartTitleRef') cartTitleRef: ElementRef;
  subscriptions: Subscription[] = [];
  previousUrl: string;
  smartPrice: SmartPrice;

  constructor(
    public injector: Injector,
    private activateRoute: ActivatedRoute,
    private messageStore: MessagesStoreService,
    private userStore: UserStoreService,
    public cartService: CartService,
    private templateStoreService: TemplateStoreService,
    private pricingService: PricingService,
    private orderByPipe: OrderByPipe,
    private translateService: TranslateService,
    private renderer: Renderer2,
    private sessionService: SessionService,
    private sharedService: SharedService,
    private addressService: AddressService,
    private ensightenService: EnsightenService,
    private notificationRibbonService: NotificationRibbonService,
    private matomoService: MatomoService,
    private router: Router,
    private transitionService: TransitionService,
    private heapService: HeapService
  ) {
    super(injector);
    this.shippingCost = {};
    this.analyticsUserObject = {};
    this.messages = this.messageStore.messages;
    this.user = this.userStore.user;
    this.config = this.userStore.config;
    this.program = this.userStore.program;
    this.showTaxDisclaimer = this.config.showTaxDisclaimer;
    this.eppStatus = this.config.epp ? this.config.epp : false;
    this.awpPayrollEnabled = this.user.awpPayrollEnabled;
    this.cartTemplate = this.templateStoreService.cartTemplate;
    this.activateRoute.queryParams.subscribe(params => {
      this.quantityLimitExceed = params.quantityLimitExceed ? params.quantityLimitExceed : false;
      this.isCartUpdate = params.isCartUpdate ? params.isCartUpdate : false;
    });
    this.showFeeDetails = this.config.showFeeDetails ? this.config.showFeeDetails : false;
    this.cartService.initError();
    this.subscriptions.push(
      this.cartService.getUpdateCartObj().subscribe( data => {
        this.updateCartObj(data);
    }));
  }

  ngOnInit(): void {
    this.getCart();
    this.analyticsUserObject = this.sharedService.getAnalyticsUserObject(this.activateRoute.snapshot.data);
    this.subscriptions.push(
      this.sharedService.getUpdatedCartItems().subscribe((isCartItemUpdated: boolean) => {
      if (isCartItemUpdated) {
        this.sharedService.setUpdatedCartItem(false);
        this.getCart();
      }
    }));

  }

  verifySkipPaymentOption(cartData: Cart) {
    const redemptions = Object.keys(this.program.redemptionOptions);
    if (redemptions.length === 1) {
      this.sharedService.updateRedemptionOption(cartData.id, redemptions[0]);
      if (redemptions[0] === 'pointsonly' || (redemptions[0] === 'cashonly' && cartData.cost === 0)) {
        return true;
      }
    } else if (cartData && cartData.redemptionPaymentLimit && cartData.redemptionPaymentLimit.cashMaxLimit && cartData.cost === 0 && this.sharedService.isPointsFixed()) {
      this.sharedService.updateRedemptionOption(cartData.id, 'pointsfixed');
      return true;
    }
    return false;
  }

  getCart(): void {
    this.transitionService.openTransition();
    this.subscriptions.push(
      this.cartService.getCart().subscribe(
        data => {
          if (this.isCartUpdate) {
            this.matomoService.broadcast(AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.UPDATE_CART, { payload: data });
          }
          setTimeout(() => { window.scrollTo({top: 0, behavior: 'smooth'}); }, 300);
          this.cartData = data;
          this.cartData.subscriptions = this.cartData.subscriptions ? this.cartData.subscriptions : [];
          this.cartObjItems = this.orderByPipe.transform(this.cartData.cartItems, 'desc', 'id');
          this.cartObjTotals = this.cartData.cartTotal;
          this.displayCartTotal = this.cartData.displayCartTotal;
          this.cartId = this.cartData.id;
          this.earnPoints = this.cartData.earnPoints;
          this.pointPurchaseRate = this.cartData.pointPurchaseRate ? this.cartData.pointPurchaseRate : 0; // Points to Dollar Conversion Rate
          this.addPoints = this.cartData.addPoints ? this.cartData.addPoints : 0;
          this.cost = this.cartData.cost ? this.cartData.cost : 0;
          this.redemptionPaymentLimit = this.cartData.redemptionPaymentLimit;
          this.paymentLimit = this.cartData.paymentLimit ? this.cartData.paymentLimit : { minNotMet: false, maxExceed: false };
          this.skipPaymentOptionPage = this.verifySkipPaymentOption(this.cartData) || false;
          this.maxCartTotalExceeded = this.cartData.maxCartTotalExceeded;
          this.cartLoadingError = (this.cartObjItems.length > 0 && this.cartObjTotals === null);
          this.emptyCart = (this.cartObjItems.length === 0 && this.cartObjTotals === null);
          this.showCartItems = !this.emptyCart && !this.cartLoadingError;
          this.isAddressValid = this.cartData.shippingAddress.validAddress;
          this.checkoutAddress = this.addressService.decodeAddress(this.cartData.shippingAddress);
          this.smartPrice = this.cartData.smartPrice;
          ({ option: this.pricingOption, isUnbundled: this.isUnbundled } = this.pricingService.getPricingOption());

          if (this.cartObjItems) {
           this.updateQuantityDropdown();
          }

          if (this.showCartItems) {
            this.pointsAvailable = this.user.balance;
            this.negativeBalance = false;
            this.giftMessageError = false;
            this.giftMessageErrorText = '';
            this.currentItemQty = [];
            this.shippingCost = { amount: 0, points: 0 };
            this.totalTaxes = { amount: 0, points: 0 };
            this.totalFees = { amount: 0, points: 0 };
            this.cartTotals = { amount: 0, points: 0 };
            this.minPurchaseTotal = (this.addPoints && this.addPoints > 0) ? this.addPoints : 0;
            // TODO: Client-specific change for maxCartTotalExceeded; Update below when multiple clients involved
            if (this.cartObjTotals !== null) {
              this.isCartActual = this.cartObjTotals.actual ? this.cartObjTotals.actual : false;
              this.discountMaxLimit = this.cartObjTotals.discountCodePerOrder ? this.cartObjTotals.discountCodePerOrder : '';
            }
            this.setDisableQty();
            this.setHasEngraving();
            if (this.cartData.discounts && this.cartData.discounts.length > 0) {
                this.sharedService.setProperty('hasDiscounts', true);
            }

            this.getCurrentItemQuantities(this.cartObjItems);

          }

          this.setEHFtitle = (this.showFeeDetails === true && this.messages) ? this.translateService.instant('environmentalHandlingFee') : '';

          // Check for modified cart
          const sessionSysModError = sessionStorage.getItem('systemModified');
          const savedCartModified = (sessionSysModError === 'true');
          const systemModifiedCart = this.config.cartModified || savedCartModified || this.cartData.cartModifiedBySystem;

          // showErrorBanner
          (() => {
            // In-page Error Check - Item no longer available
            if (systemModifiedCart || this.returnedWithError) {
              this.notificationRibbonService.emitChange([true, this.messages.removedItemNotAvailable]);
              if (systemModifiedCart) {
                this.config.cartModified = false;
                sessionStorage.removeItem('systemModified');
              }
            } else if (this.returnedWithGenericError) {
              this.notificationRibbonService.emitChange([true, this.messages.genericCartError]);
            } else {
              this.notificationRibbonService.emitChange([false, '']);
            }
            // Max Cart Total Exceeded Check
            if (this.maxCartTotalExceeded) {
              this.subTotalExt = 'exceeded_';
              this.notificationRibbonService.emitChange([true, this.messages.pricingFullError]);
            } else {
              if (!(this.paymentLimit.minNotMet || this.paymentLimit.maxExceed)) {
                this.disableBtn = false;
              }
              this.notificationRibbonService.emitChange([false, '']);
              this.subTotalExt = '';
              this.setCartTaxFeeTitle = this.isUnbundled === false ? (this.isCartActual === true ? this.translateService.instant('taxFeeIncluded') : this.translateService.instant('taxFeeNotIncluded')) : (this.showTaxDisclaimer === true ? this.translateService.instant('taxFeeNotIncluded') : '');
              if (this.showCartItems) {
                  this.setCartSubtotal(this.cartObjTotals);
              }
            }
          })();

          this.transitionService.closeTransition();
          this.ensightenService.broadcastEvent(this.analyticsUserObject, this.cartObjItems);
        },
        // eslint-disable-next-line @typescript-eslint/no-shadow
        error => {
          this.transitionService.closeTransition();
          if (error.status === 401 || error.status === 0) {
            // sessionMgmt.showTimeout();
          } else {
            this.emptyCart = false;
            this.cartLoadingError = true;
            this.cartObjItems = [];
            this.cartObjTotals = {} as CartTotal;
          }
        }
      ));
  }

  getBalances() {
    this.negativeBalance = false;
    const paymentNeeded = (this.cartTotals.points > this.pointsAvailable);
    this.negativeBalance = paymentNeeded;
    this.remainingBalance = (!paymentNeeded) ? (this.pointsAvailable - this.cartTotals.points) : 0;
  }

  setCartSubtotal(data: CartTotal) {
    const pricingData = this.sharedService.getPriceData(data);
    this.cartSubtotal = (this.isUnbundled === true) ? (this.config.displayDiscountedItemPriceInPriceBreakdown && data.discountedItemsSubtotalPrice) ? {
      amount: data.discountedItemsSubtotalPrice.amount,
      points: data.discountedItemsSubtotalPrice.points
    } : {
      amount: data.itemsSubtotalPrice.amount,
      points: data.itemsSubtotalPrice.points
    } : {
      amount: data.price.amount,
      points: data.price.points
    };
    this.cartDiscountedSubtotal = pricingData.cartDiscountedSubtotal;
    this.discountedSubtotal = pricingData.discountParams;
    this.cartService.setProperty('discountedSubtotal', pricingData.discountParams);
    // TODO: Remove below conditional when Pricing Service can fix the issue
    this.shippingCost = pricingData.shippingCost;
    this.totalFees = pricingData.totalFees;
    this.totalTaxes = pricingData.totalTaxes;
    this.cartTotals = pricingData.cartTotals;

    this.getBalances();

    if (this.config.paymentTemplate === AppConstants.paymentTemplate.cash_subsidy) {
      this.cartSubtotal = this.displayCartTotal.itemsSubtotalPrice;
      this.cartTotals = this.displayCartTotal.price;
    }
  }

  cartFocus(timeoutValue: number) {
    setTimeout(() => {
      this.renderer.setAttribute(this.cartTitleRef.nativeElement, 'tabIndex', '-1');
      this.cartTitleRef.nativeElement?.focus();
    }, timeoutValue);
  }

  /*** Remove Cart Item ***/
  removeItem(index: number, removedItem: CartItem) {
    /* TODO removeItem FUNCTIONALITY */
    this.offscreenText.msg = '';
    this.isProcessing = true;
    this.disableBtn = true;
    let removeItemName: string;
    this.notificationRibbonService.emitChange([false, '']);
    for (const cart of this.cartObjItems) {
      if (cart && cart.id === removedItem.id) {
        removeItemName = cart.productName;
      }
    }

    const element: ElementRef = this.cartItemComp.cartItemsRef.find((item, position) => position === index);
    this.renderer.addClass(element.nativeElement, 'removing');

    this.subscriptions.push(
      this.cartService.modifyCart(removedItem.id, {quantity: 0}).subscribe((data) => {
        this.cartService.setCartUpdateMessage(this.translateService.instant(
          (data && data.cartItems.length > 0 ? 'itemRemovedFromBag' : 'allItemsRemovedFromBag'),
          {productName: removedItem.productName}));
        this.ensightenService.broadcastEvent(this.analyticsUserObject, data.cartItems);
        this.matomoService.broadcast(AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.REMOVE_FROM_CART, {
            payload: {
                product: removedItem,
                cart: data ? data : 0
            }
        });
        this.renderer.removeClass(element.nativeElement, 'removing');
        removedItem.quantity = 0;
        this.heapService.broadcastEvent(AppConstants.analyticServices.HEAP_EVENTS.ITEM_REMOVED_FROM_CART, removedItem);
        this.onCartUpdateSuccess(data);
        setTimeout(() => {
          this.offscreenText.msg = removeItemName.concat(' ', this.translateService.instant('hasBeenRemoved'));
        });
        this.cartFocus(100);
      }, (error) => {
        /* TODO ERROR LOGIC */
        this.isProcessing = false;
        if (error.status === 500 || error.status === 401 || error.status === 0) {
          // sessionMgmt.showTimeout();
        } else {
          const msg = (this.translateService.instant('cartRemoveItemError')).concat(' ', this.translateService.instant('tryAgainLater'));
          this.renderer.removeClass(element.nativeElement, 'removing');
          this.notificationRibbonService.emitChange([true, msg]);
        }
      })
    );
    this.sessionService.getSession();
  }

  checkMaxQtyReached(id: number, index: number) {
    const cartItem: CartItem = this.cartObjItems[index];
    const cartItems = this.cartObjItems.filter(item => item.productGroupId === cartItem.productGroupId); // to hold cart items which are belongs to same product group
    let totalQty = 0;

    for (const item of cartItems) {
      if (item) {
        totalQty += item.quantity;
      }
    }

    if ((cartItem.hasOwnProperty('maxQuantity') && cartItem.maxQuantity != null ) || (cartItem.hasOwnProperty('quantityLimitExceed') && cartItem.quantityLimitExceed)) {
      if ((cartItem.maxQuantity != null && totalQty > cartItem.maxQuantity) || (cartItem.quantityLimitExceed)) {
        const cartQtyObj = this.currentItemQty.find(item => item.id === id);
        const currentItemsQty = this.currentItemQty.filter(item => item.productGroupId === cartItem.productGroupId);
        cartItem.currentQty = cartQtyObj.qty;
        totalQty = 0;
        for (const item of currentItemsQty) {
          if (item) {
            totalQty += item.qty;
          }
        }
        cartItem.totalQty = totalQty;
        return true;
      }
    }
    return false;
  }

  /*** Update Cart Item Quantity ***/
  editItemQty(index: number, id: number, qty: string | number, item: CartItem) {

    const valType = typeof qty;

    if (this.checkMaxQtyReached(id, index)) {
      qty = 0;
    }

    if (valType !== 'number') {
      qty = parseInt(qty as string, 10);
    }

    if (qty <= 0) {
      this.currentItemQty.forEach((val, pos) => {
        if (val.id === id) {
          this.cartObjItems[pos].quantity =  val.qty;
        }
      });
      return false;
    } else {
      this.isProcessing = true;
      this.disableBtn = true;
      this.notificationRibbonService.emitChange([false, '']);
    }
    this.subscriptions.push(
      this.cartService.modifyCart(id, {
        quantity: qty,
        itemIndex: index
      }).subscribe((data) => {
        if (data.cartTotalModified) {
          this.cartService.setCartUpdateMessage(this.translateService.instant(
            (item.prevQuantity <= item.quantity? 'itemQuantityIncreasedTo' : 'itemQuantityDecreasedTo'),
            {productName: item.productName, quantity: qty}));
          this.matomoService.broadcast(AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.UPDATE_CART, { payload: data });
          this.ensightenService.broadcastEvent(this.analyticsUserObject, data.cartItems);
          if (item.quantity > item.prevQuantity) {
            this.heapService.broadcastEvent(AppConstants.analyticServices.HEAP_EVENTS.ITEM_ADDED_TO_CART, item);
          } else {
            this.heapService.broadcastEvent(AppConstants.analyticServices.HEAP_EVENTS.ITEM_REMOVED_FROM_CART, item);
          }
        }
        this.onCartUpdateSuccess(data);
        setTimeout(() => {
          const itemQtyElement: HTMLElement = document.querySelector('#cart-item-' + index + '-qty');
          itemQtyElement?.focus();
        }, 100);
      }, (error) => {
        this.isProcessing = false;
        if (error.status === 401 || error.status === 0) {
          // sessionMgmt.showTimeout();
        } else {
          const msg = (this.translateService.instant('cartQtyModifyError')).concat(' ', this.translateService.instant('tryAgainLater'));
          this.notificationRibbonService.emitChange([true, msg]);
        }
      })
    );
    this.sessionService.getSession();
  }

  onCartUpdateSuccess(cart: Cart) {
    this.isProcessing = false;
    this.currentItemQty = [];
    this.cartObjItems = this.orderByPipe.transform(cart.cartItems, 'desc', 'id');
    this.cartObjTotals = cart.cartTotal;
    this.addPoints = cart.addPoints;
    this.cost = cart.cost;
    this.earnPoints = cart.earnPoints;
    this.redemptionPaymentLimit = cart.redemptionPaymentLimit;
    this.paymentLimit = cart.paymentLimit ? cart.paymentLimit : {
        minNotMet: false,
        maxExceed: false
    };
    this.skipPaymentOptionPage = this.verifySkipPaymentOption(cart);
    this.maxCartTotalExceeded = cart.maxCartTotalExceeded;
    this.cartData.subscriptions = cart.subscriptions || [];
    this.smartPrice = cart.smartPrice;
    this.getCurrentItemQuantities(this.cartObjItems);
    this.setDisableQty();
    this.setHasEngraving();
    if (this.cartObjItems && this.cartObjItems.length > 0) {
      for (const item of this.cartObjItems) {
        if (item) {
          item.quantityDropdown = [];
          const maxQuantity = item.maxQuantity || 10;
          for (let i = 1; i <= maxQuantity; i++) {
            item.quantityDropdown.push(i);
          }
          item.prevQuantity = item.quantity;
        }
      }

      if (this.maxCartTotalExceeded) {
        const msg = this.translateService.instant('pricingFullError');
        this.subTotalExt = 'exceeded_';
        this.notificationRibbonService.emitChange([true, msg]);
      } else {
        this.subTotalExt = '';
        this.setCartSubtotal(cart.cartTotal);
        if (!(this.paymentLimit.minNotMet || this.paymentLimit.maxExceed)) {
          this.disableBtn = false;
        }
      }
    } else {
      this.subTotalExt = '';
      this.emptyCart = true;
      this.showCartItems = false;
    }
  }

  setDisableQty() {
    if (this.cartObjItems) {
      for (const item of this.cartObjItems) {
        item.disableQty = false;
      }
    }
  }

  setHasEngraving() {
    if (this.cartObjItems) {
      for (const item of this.cartObjItems) {
        item.hasEngraving = item && item.productDetail
          && item.productDetail.additionalInfo
          && item.productDetail.additionalInfo.engravable
          && item.productDetail.additionalInfo.engravable === 'true';
      }
    }
  }

  getCurrentItemQuantities(obj: CartItem[]) {
    for (const item of obj) {
      if (item) {
        this.currentItemQty.push({id: item.id, qty: item.quantity, productGroupId: item.productGroupId});
      }
    }
  }

  cartUpdateEvent(event) {
    if (event.type === 'removeItem') {
      this.removeItem(event.index, event.item);
      return;
    } else if (event.type === 'removeCartGiftItem' || event.type === 'addCartGiftItem' || event.type === 'updateGiftEngraveLines' || event.type === 'addOrUpdateServicePlan') {
      this.onCartUpdateSuccess(event.item);
      return;
    }
    this.editItemQty(event.index, event.id, event.qty, event.item);
  }

  updateCartObj(obj) {
    this.disableBtn = true;
    this.currentItemQty = [];
    this.cartObjItems = obj.cartItems;
    this.cartObjTotals = obj.cartTotal;
    this.maxCartTotalExceeded = obj.maxCartTotalExceeded;
    this.paymentLimit = obj.paymentLimit ? obj.paymentLimit : {minNotMet: false, maxExceed: false};
    if (obj.cartItems.length !== 0) {
      this.setCartSubtotal(obj.cartTotal);
    }
    this.getCurrentItemQuantities(this.cartObjItems);
    if (this.maxCartTotalExceeded) {
        const msg = this.messages.pricingFullError;
        this.subTotalExt = 'exceeded_';
        this.notificationRibbonService.emitChange([true, msg]);
    }
    if (!(this.paymentLimit.minNotMet || this.paymentLimit.maxExceed)) {
        this.disableBtn = false;
    }
    if (this.cartObjItems) {
      this.updateQuantityDropdown();
    }
  }

  updateQuantityDropdown(){
    for (const cart of this.cartObjItems) {
      if (cart) {
        cart.quantityDropdown = [];
        const maxQuantity = cart.maxQuantity || 10;

        for (let i = 1; i <= maxQuantity; i++) {
          cart.quantityDropdown.push(i);
        }
        cart.prevQuantity = cart.quantity;
      }
    }
  }

  back(): void {
    this.previousUrl = JSON.parse(sessionStorage.getItem('previousUrl'));
    this.router.navigate([this.previousUrl]);
  }

  isActionProcessing(isProcessing: boolean): void {
    this.disableBtn = isProcessing;
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }
}
