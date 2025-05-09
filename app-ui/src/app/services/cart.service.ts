import { Injectable, Injector } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { Observable, Subject, throwError } from 'rxjs';
import { BaseService } from './base.service';
import { Cart, AddToCartResponse } from '@app/models/cart';
import { PurchasePointsResponse } from '@app/models/pricing-model';
import { User } from '@app/models/user';
import { UserStoreService } from '@app/state/user-store.service';
import { CartItem, CartTotal } from '@app/models/cart';
import { Product } from '@app/models/product';
import { AppConstants } from '@app/constants/app.constants';
import { SharedService } from '@app/modules/shared/shared.service';
import { NotificationRibbonService } from './notification-ribbon.service';
import { TransitionService } from '@app/transition/transition.service';
import { Messages } from '@app/models/messages';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { NavigationExtras, Router } from '@angular/router';
import { Config } from '@app/models/config';
import { ModalsService } from '@app/components/modals/modals.service';
import { HeapService } from '@app/analytics/heap/heap.service';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { AddressService } from '@app/services/address.service';
import { Address } from '@app/models/address';
import { TranslateService } from '@ngx-translate/core';

type CartItemResponse = {cartItems: Array<CartItem>; error: any | null};

@Injectable({
  providedIn: 'root'
})

export class CartService extends BaseService {

  user: User;
  messages: Messages;
  cost: string | number = 0;
  costDown: string | number = 0;
  isEligibleForPayrollDeduction = false;
  isPayrollOnly = false;
  disableCheckoutBtn: boolean;
  disableQty: boolean[] = [];
  hasEngraving: boolean[] = [];
  private readonly cartItems = new Subject<CartItemResponse>();
  private readonly updateCartObj = new Subject<any>();
  private readonly isEmptyCartBag = new Subject<boolean>();
  private readonly cartUpdateMessage = new Subject<string>();
  readonly updateCartObj$ = this.updateCartObj.asObservable();
  readonly isEmptyCartBag$ = this.isEmptyCartBag.asObservable();
  readonly cartItems$ = this.cartItems.asObservable();
  readonly cartUpdateMessage$ = this.cartUpdateMessage.asObservable();
  public cartItemsTotalCount: number;
  public pointsBalance: number;
  config: Config;
  cartFullError: boolean;
  pricingFullError: boolean;
  addCartError: boolean;
  checkoutAddress: Address;
  routeParams: { [key: string]: string };

  constructor(
    private http: HttpClient,
    private userStore: UserStoreService,
    private sharedService: SharedService,
    private notificationRibbonService: NotificationRibbonService,
    private transitionService: TransitionService,
    private injector: Injector,
    private route: Router,
    private messageStoreService: MessagesStoreService,
    private translateService: TranslateService
  ) {
    super();
    this.messages = this.messageStoreService.messages;
    this.user = this.userStore.user;
    this.cartItemsTotalCount = 0;
  }

  /**
   * Get Cart
   *
   * @summary Get user's cart
   * @returns {Observable<Cart>}
   */
  getCart(): Observable<Cart> {
    const url = this.baseUrl + 'cart';
    return this.http.get<Cart>(url, this.httpOptions)
      .pipe(
        map((response) => {
          response.cartItems = this.sharedService.giftAttributeUpdates(response.cartItems);
          this.setProperty('cartData', response);
          this.cartItemsTotalCount = response.cartItemsTotalCount;
          this.setPointsBalance(response.pointsBalance);
          this.setCartItems({cartItems: response.cartItems, error: null});
          this.setUpdateCartObj(response);
          return response;
        }),
        catchError((error: HttpErrorResponse) => {
          this.handleError(error);
          this.setCartItems({cartItems: [], error});
          this.setUpdateCartObj({});
          return throwError(error);
        })
      );
  }

  addItemToCart(id: string, appleCareServicePlanPsid?: string): Observable<AddToCartResponse> {
    const url = this.baseUrl + 'cart/add';
    const payload = {
      psId: id,
      servicePlanPsId: appleCareServicePlanPsid
    };
    const httpOptions = {
      observe: 'response' as const
    };

    return this.http.post<AddToCartResponse>(url, payload, httpOptions)
      .pipe(
        map((response) => response.body),
        catchError((error: HttpErrorResponse) =>
          /* TODO ERROR HANDLING */
          // if (status !== 401 || status !== 0) {
          //     log to server-side
          //     $rootScope.errorMsg = 'Error: add to cart REST service failed to GET @psid:' + id + ', to add to cart service data';
          //     errorLogService(status, $rootScope.errorMsg);
          // }
           this.handleError(error)
        )
      );
  }

  initError(){
      this.cartFullError = false;
      this.pricingFullError = false;
      this.addCartError = false;
  }

  addToCart(psid: string, detl: Product, updateCart = false, servicePlanPsid?) {
    const modalService = this.injector.get(ModalsService);
    const heapService = this.injector.get(HeapService);
    const parsePsidPipe = this.injector.get(ParsePsidPipe);
    const sharedService = this.injector.get(SharedService);
    const matomoService = this.injector.get(MatomoService);
    const addressService = this.injector.get(AddressService);
    this.config = this.userStore.config;
    if (this.config.loginRequired) {
      // TODO: write the logic
      const expirationDate = new Date(new Date().getTime() + (15 * 60 * 1000));
      const dataToStore = {
        itemDetails: detl,
        expirationDate: expirationDate.toISOString(),
        secure: true
      };
      window.sessionStorage.setItem('itemToCart', JSON.stringify(dataToStore));
      modalService.openAnonModalComponent();
      return;
    }
    else if (this.userStore.user.browseOnly) {
      modalService.openBrowseOnlyComponent();
      return;
    } else {
      /* TODO ELSE BLOCK */
      this.addItemToCart(psid, servicePlanPsid).subscribe(
        (data: AddToCartResponse) => {
          this.setCartUpdateMessage(this.translateService.instant('itemAddedToBagMsg', {productName: detl.name}));
          const cartItemId = data.cartItemId;
          const cartId = data.cartItemId;
          const isUpdatedCart = updateCart;
          const isEngravable = detl.isEngravable ? detl.isEngravable : false;
          const cartPageParam: NavigationExtras = {
            queryParams: {
              quantityLimitExceed: data.quantityLimitExceed,
              isCartUpdate: true
            }
          };
          const relatedProductsPageParam: NavigationExtras = {
            queryParams: {
              hasRelatedProduct: detl.hasRelatedProduct
            }
          };
          heapService.broadcastEvent(AppConstants.analyticServices.HEAP_EVENTS.ITEM_ADDED_TO_CART, detl );
          if (isEngravable) {
            // single or multiple gift item(s)
            // do not add gift and navigate to qualifying product engrave page, regardless of the q-ty of the gifts
            // this.modalService.openEngraveModalComponent(cartItemId, this.parsePsidPipe.transform(detl.psid, '-'), false, false, updateCart);
            // single gift item
            const engraveItemObj = {
              cartItemId: cartId,
              psIdSlug: parsePsidPipe.transform(detl.psid, '-'),
              isGiftPromo: false,
              isEdit: false,
              updateCart: isUpdatedCart,
              qualifyingProduct: {
                psid: detl.psid,
                hasRelatedProduct: detl.hasRelatedProduct
              }
            };
            modalService.openEngraveModalComponent(engraveItemObj); // should updateCart be true
          } else if (!this.config.fullCatalog || this.config.skipBagPage) {
            this.transitionService.openTransition();
            this.getCart().subscribe((data1: Cart) => {
                this.transitionService.closeTransition();
                this.checkoutAddress = data1.shippingAddress;
                if (this.config.skipBagPage && !this.checkoutAddress.validAddress) {
                  this.route.navigate(['/store', 'shipping-address']);
                  return;
                }
                this.checkoutAddress = addressService.decodeAddress(this.checkoutAddress);
                addressService.modifyShippingAddress(this.checkoutAddress, true);
              },
              error => {
                this.transitionService.closeTransition();
              });
          } else if (detl.addOns.availableGiftItems.length > 0) {
            // Qualifying product not engravable
            // Single item
            if (detl.addOns.availableGiftItems && detl.addOns.availableGiftItems.length === 1) {
              if (detl.hasRelatedProduct){
                this.route.navigate(['store', 'related-products', sharedService.psidSlugConvert(detl.psid)]);
              }else{
                this.route.navigate(['./store/cart'], cartPageParam);
              }
            } else if (detl.addOns.availableGiftItems && detl.addOns.availableGiftItems.length > 1) {
              // multi gift items
              const URL =  `./store/gift-promo/${cartItemId}/${parsePsidPipe.transform(detl.psid, '-')}`;
              this.route.navigate([URL], relatedProductsPageParam);
            }
          } else {
            if (updateCart) {
              sharedService.setUpdatedCartItem(true);
            } else {
              if (detl.hasRelatedProduct){
                this.route.navigate(['store', 'related-products', sharedService.psidSlugConvert(detl.psid)]);
              }else{
                this.route.navigate(['./store/cart'], cartPageParam);
              }
            }
          }
        }, (error) => {
          this.cartFullError = false;
          this.pricingFullError = false;
          this.addCartError = false;

          if (error.status === 403) {
            this.sharedService.showSessionTimeOut(true);
          } else if (error.status === 400) {
            if (error.error) {
              const data = error.error;
              if (data.pricingFull) {
                this.pricingFullError = true;
                matomoService.broadcast(AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.CANONICAL_PAGE, {
                  payload: {
                    location: location.href,
                    canonicalTitle: AppConstants.analyticServices.CANONICAL_CONSTANTS.ERROR
                  }
                });
              }
            } else {
              this.cartFullError = true;
            }
          } else {
            this.addCartError = true;
          }
        });

    }
  }

  modifyCart(id: number, cartData): Observable<Cart> {
    const url = this.baseUrl + 'cart/modify/' + id;
    const httpOptions = {
      observe: 'response' as const
    };

    return this.http.post<Cart>(url, cartData, httpOptions)
      .pipe(
        map((response) => {
          const cartResponse: Cart = response.body;
          cartResponse.cartItems = this.sharedService.giftAttributeUpdates(cartResponse.cartItems);
          this.cartItemsTotalCount = cartResponse.cartItemsTotalCount;
          this.setPointsBalance(cartResponse.pointsBalance);
          this.setCartItems({cartItems: cartResponse.cartItems, error: null});
          this.setUpdateCartObj(cartResponse);
          return cartResponse;
        }),
        catchError((error: HttpErrorResponse) => {
          this.setCartItems({cartItems: [], error});
          this.setUpdateCartObj({});
          // if (status !== 401 || status !== 0) {
          // log to server-side
          // $rootScope.errorMsg = 'Error: modify cart REST service failed to POST, modifying cart item #' + id;
          // errorLogService(status, $rootScope.errorMsg);
          // }
          return this.handleError(error);
        })
      );
  }

  addPurchasePoints(amount): Observable<PurchasePointsResponse> {
    const url = this.baseUrl + 'cart/' + 'ccDollarValue/' + amount + '?nocache=' + new Date().getTime();

    return this.http.get<PurchasePointsResponse>(url, this.httpOptions)
      .pipe(
        map((response) => response),
        catchError((error: HttpErrorResponse) => {
          // general and service level error actions
          this.handleError(error);
          return throwError(error);
        })
      );
  }

  getPurchasePoints(amount): Observable<number> {
    const url = this.baseUrl + 'cart/' + 'ccDollarValueOnChange/' + amount + '?nocache=' + new Date().getTime();
    return this.http.get<PurchasePointsResponse>(url, this.httpOptions)
      .pipe(
        map((response) => response.cashAmount),
        catchError((error: HttpErrorResponse) => {
          // general and service level error actions
          this.handleError(error);
          return throwError(error);
        })
      );
  }

  getProperty<T>(prop: string): T {
    return this[prop];
  }

  setProperty<T>(key: string, value: T) {
    this[key] = value;
  }

  setAmounts(type, total): number {
    return ((total - type) > 0) ? (total - type) : 0;
  }

  setDecVal(total): string {
    return (total / 100).toFixed(2);
  }

  isCashOnlyRedemption(){
    const redemptions = Object.keys(this.userStore.program.redemptionOptions);
    if (redemptions.length === 1){
      return (redemptions.indexOf(AppConstants.redemptions.cashonly) >= 0);
    }
    return false;
  }

  updateRemainingCost(obj: CartTotal) {
    const userCostBal = (this.isCashOnlyRedemption()) ? 0 : this.user.balance;
    const userDownBal = this.user.balance;

    const total = (obj.discountApplied) ? obj.discountedPrice.points : obj.price.points;

    // set default costDown for price display
    this.costDown = this.setDecVal(this.setAmounts(userDownBal, total));

    // set purchasePoints and actual cost
    const costTotal = this.setAmounts(userCostBal, total);
    this.cost = this.setDecVal(costTotal);
    this.addPurchasePoints(costTotal);
  }

  passPaymentType(id: number, pmt: { [key: string]: string }) {
    const modifyURL = this.baseUrl + '/cart/' + id;
    return this.http.put(modifyURL, pmt, this.httpOptions)
      .pipe(
        map((response) => response),
        catchError((error: HttpErrorResponse) =>
          /* TODO ERROR HANDLING */
          //  if (status !== 401 || status !== 0) {
          //    // log to server-side
          //    $rootScope.errorMsg = 'Error: Passing payment type failed with a status: ' + status;
          //    errorLogService(status, $rootScope.errorMsg);
          //  }
           this.handleError(error)
        )
      );
  }

  setDisableQty(index, boolVal) {
    this.disableQty[index] = boolVal;
  }

  getDisableQty(index) {
    return this.disableQty ? this.disableQty[index] : undefined;
  }

  setHasEngraving(index, boolVal) {
    this.hasEngraving[index] = boolVal;
  }

  getHasEngraving(index) {
    return this.hasEngraving ? this.hasEngraving[index] : undefined;
  }

  // Function for child controllers to enable/disable checkout button
  setDisableCheckoutBtn(boolVal: boolean) {
    this.disableCheckoutBtn = boolVal;
  }

  getDisableCheckoutBtn() {
    return this.disableCheckoutBtn;
  }

  getObservable() {
    return this.updateCartObj$;
  }

  setUpdateCartObj(cartObj) {
    this.updateCartObj.next(cartObj);
  }

  getUpdateCartObj() {
    return this.updateCartObj$;
  }

  setCartItems(response: CartItemResponse) {
    response.error ? this.cartItems.error(response) : this.cartItems.next(response);
  }

  setCartUpdateMessage(message: string) {
    this.cartUpdateMessage.next(message);
  }

  setPointsBalance(val) {
    this.pointsBalance = val;
  }

  updateGiftItemModifyCart(cartItemId, psid) {
    this.transitionService.openTransition();
    const postData = {
      giftItem: {
        productId: psid
      }
    };
    this.modifyCart(cartItemId, postData).subscribe(data => {
      this.sharedService.setUpdatedCartItem(true);
      this.transitionService.closeTransition();
    }, error => {
      this.transitionService.closeTransition();
      if (error.status === 401 || error.status === 0) {
        // TODO: sessionMgmt.showTimeout();
      } else {
        // NOTIFICATION RIBBON ERROR MESSAGE
        this.notificationRibbonService.emitChange([true, this.messages.detailsLoadingError.concat(' ', this.messages.engravingAddErrorGoToCart)]);
      }
    });
  }

}
