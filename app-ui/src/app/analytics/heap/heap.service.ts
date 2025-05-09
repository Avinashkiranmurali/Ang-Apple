import { DOCUMENT } from '@angular/common';
import { Inject, Injectable } from '@angular/core';
import { User } from '@app/models/user';
import { BaseService } from '@app/services/base.service';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateService } from '@ngx-translate/core';
import { AppConstants } from '@app/constants/app.constants';
import { CartService } from '@app/services/cart.service';
import { CartItem } from '@app/models/cart';
import { MediaProduct } from '@app/models/media-product';

export interface Heap {
  identify: (userId: string) => void;
  addUserProperties: (userProperties: object) => void;
  track: (eventName: string, eventProperties: object) => void;
  userId: string;
}

@Injectable({
  providedIn: 'root'
})
export class HeapService extends BaseService {

  user: User;
  isSessionEventBroadcasted = false;

  constructor(
    private userStoreService: UserStoreService,
    @Inject(DOCUMENT) private document: Document,
    public cartService: CartService,
    private translateService: TranslateService
  ) {
    super();
    this.user = this.userStoreService.user;

    // Updated user data once user login/balance update
    this.userStoreService.get().subscribe((userData: User) => {
      this.user = userData;
      if (userData && userData.initialUserBalance > 0) {
        this.addUserProperties();
      }
    });
  }

  loadInitialScript() {
    const data = 'analyticsEnabled = true';
    const body = this.document.getElementsByTagName('head')[0];
    const initialScript = this.document.createElement('script');
    initialScript.type = 'text/javascript';
    initialScript.text = data;
    body.appendChild(initialScript);
    this.loadHeapScript();
  }

  loadHeapScript() {
    const heapEndPoint = this.userStoreService.config.heapEndPoint; // https://cdn.heapanalytics.com/js
    const heapAppId = this.userStoreService.config.heapAppId; // DEV: 3691461206
    const head = this.document.getElementsByTagName('head')[0];
    const childScript = head.getElementsByTagName('script')[0];
    const heapScriptElememt = this.document.createElement('script');
    heapScriptElememt.type = 'text/javascript';
    heapScriptElememt.text = `window.heap=window.heap||[],
                              heap.load=function(e,t){window.heap.appid=e,window.heap.config=t=t||{};
                              var r=document.createElement('script');r.type='text/javascript',r.async=!0,
                              r.src='${heapEndPoint}'+'/heap-'+e+'.js';
                              var a=document.getElementsByTagName('script')[0];a.parentNode.insertBefore(r,a);
                              for(var n=function(e){return function(){heap.push([e].concat(Array.prototype.slice.call(arguments,0)))}},
                              p=['addEventProperties','addUserProperties','clearEventProperties','identify','resetIdentity','removeEventProperty','setEventProperties','track','unsetEventProperty'],o=0;
                              o<p.length;o++)heap[p[o]]=n(p[o])};heap.load('${heapAppId}');`;
    this.insertAfter(heapScriptElememt, childScript);
    setTimeout(() => {
      this.identifyUser();
      this.broadcastSessionEvent();
      this.addUserProperties();
    }, 4000);
  }

  insertAfter(newNode: HTMLScriptElement, referenceNode: Node) {
    referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
  }

  // IDENTIFY USER IS SECURED
  identifyUser() {
    if (window.heap && !this.userStoreService.config.loginRequired && this.user.userId) {
      window.heap.identify(this.user.hashedUserId);
    }
  }

  // Broadcast Session Started Event
  broadcastSessionEvent() {
    this.isSessionEventBroadcasted = sessionStorage.getItem('isSessionEventBroadcasted') ? (sessionStorage.getItem('isSessionEventBroadcasted') === 'true') : false;
    if(!this.isSessionEventBroadcasted) {
      sessionStorage.setItem('isSessionEventBroadcasted', 'true');
    }
    if (!this.userStoreService.config.loginRequired && !this.isSessionEventBroadcasted) {
      this.broadcastEvent(AppConstants.analyticServices.HEAP_EVENTS.SESSION_STARTED);
    }
  }

  // HEAP USER PROPERTIES API
  addUserProperties() {
    if (window.heap) {
      this.user.program = this.userStoreService.program;
      const userProperties = {
        [AppConstants.analyticServices.HEAP_CONSTANTS['VAR_ID']]: this.user.program.varId ? this.user.program.varId : '',
        [AppConstants.analyticServices.HEAP_CONSTANTS['VAR_NAME']]: this.user.program.varId ? this.user.program.varId : '',
        [AppConstants.analyticServices.HEAP_CONSTANTS['PROGRAM_ID']]: this.user.program.programId ? this.user.program.programId : '',
        [AppConstants.analyticServices.HEAP_CONSTANTS['PROGRAM_NAME']]: this.user.program.name ? this.user.program.name : '',
        [AppConstants.analyticServices.HEAP_CONSTANTS['BALANCE']]: this.user.balance ? this.user.balance : '',
        [AppConstants.analyticServices.HEAP_CONSTANTS['POINTS_NAME']]: this.user.program.formatPointName ? this.translateService.instant(this.user.program.formatPointName) : '',
        [AppConstants.analyticServices.HEAP_CONSTANTS['LOYALTY_PARTNER_USER_ID']]: this.user.userId ? this.user.userId : ''
      };
      window.heap.addUserProperties(userProperties);
    }
  }

  broadcastEvent(eventName: string, viewProperties?) {
    if (window.heap && window.heap['userId']) {
      // TODO: move to scope
      const commonProperties = {
        [AppConstants.analyticServices.HEAP_CONSTANTS['VAR_ID']]: this.user.program.varId ? this.user.program.varId : '',
        [AppConstants.analyticServices.HEAP_CONSTANTS['VAR_NAME']]: this.user.program.varId ? this.user.program.varId : '',
        [AppConstants.analyticServices.HEAP_CONSTANTS['PROGRAM_ID']]: this.user.program.programId ? this.user.program.programId : '',
        [AppConstants.analyticServices.HEAP_CONSTANTS['PROGRAM_NAME']]: this.user.program.name ? this.user.program.name : '',
        [AppConstants.analyticServices.HEAP_CONSTANTS['PLATFORM']]: AppConstants.analyticServices.HEAP_CONSTANTS.APPLE,
        [AppConstants.analyticServices.HEAP_CONSTANTS['STOREFRONT_TYPE']]: AppConstants.analyticServices.HEAP_CONSTANTS.MERCHANDISE,
        [AppConstants.analyticServices.HEAP_CONSTANTS['STOREFRONT_NAME']]: AppConstants.analyticServices.HEAP_CONSTANTS.APPLE,
        [AppConstants.analyticServices.HEAP_CONSTANTS['ON_BEHALF_OF']]: (this.user.proxyUserId !== null) ? 'true' : 'false',
        [AppConstants.analyticServices.HEAP_CONSTANTS['EVENT_SOURCE']]: AppConstants.analyticServices.HEAP_CONSTANTS.STOREFRONT,
      };
      switch (eventName) {
        case (AppConstants.analyticServices.HEAP_EVENTS.ITEM_VIEWED): {
          const distinctViewProperties = {
            [AppConstants.analyticServices.HEAP_CONSTANTS['ITEM_SKU']]: viewProperties.sku,
            [AppConstants.analyticServices.HEAP_CONSTANTS['ITEM_NAME']]: viewProperties.name,
            [AppConstants.analyticServices.HEAP_CONSTANTS['ITEM_CATEGORY']]: viewProperties.categories[0].parents[0].name,
            [AppConstants.analyticServices.HEAP_CONSTANTS['ITEM_SUBCATEGORY']]: viewProperties.categories[0].name,
            [AppConstants.analyticServices.HEAP_CONSTANTS['ITEM_BRAND']]: viewProperties.brand,
            [AppConstants.analyticServices.HEAP_CONSTANTS['ITEM_SUPPLIER']]: viewProperties.manufacturer,
            [AppConstants.analyticServices.HEAP_CONSTANTS['ITEM_PRICE']]: viewProperties.offers[0].displayPrice.amount,
            [AppConstants.analyticServices.HEAP_CONSTANTS['ITEM_POINTS']]: viewProperties.offers[0].displayPrice.points
          };
          const eventProperties = {...commonProperties, ...distinctViewProperties};
          window.heap.track(AppConstants.analyticServices.HEAP_EVENTS.ITEM_VIEWED, eventProperties);
          break;
        }
        // ITEM ADDED TO CART
        case(AppConstants.analyticServices.HEAP_EVENTS.ITEM_ADDED_TO_CART): {
          this.triggerCartUpdateEvent(viewProperties, commonProperties, AppConstants.analyticServices.HEAP_EVENTS.ITEM_ADDED_TO_CART);
          break;
        }
        // ITEM REMOVED FROM CART
        case(AppConstants.analyticServices.HEAP_EVENTS.ITEM_REMOVED_FROM_CART): {
          this.triggerCartUpdateEvent(viewProperties, commonProperties, AppConstants.analyticServices.HEAP_EVENTS.ITEM_REMOVED_FROM_CART);
          break;
        }
        // ORDER SUCCESS
        case(AppConstants.analyticServices.HEAP_EVENTS.ORDER_SUCCESS): {
          const orderEvent = viewProperties.payload;
          let orderLineItem = 0;
          let giftItems;
          orderEvent.products.items.forEach(item => {
            let items = [this.getOrderItem(item, orderEvent.products.orderID)];
            giftItems = this.getGiftItems([], item, orderEvent.products.orderID);
            items = items.concat(giftItems);
            orderLineItem += items.length;
          });
          const pointsUsed = orderEvent.products.points - orderEvent.products.remainingBalance;
          const subscribedMediaProducts = orderEvent.products?.subscribedMediaProducts ? orderEvent.products.subscribedMediaProducts.length : 0 ;
          const orderLines = orderLineItem + subscribedMediaProducts;
          const distinctOrderProperties = {
            [AppConstants.analyticServices.HEAP_CONSTANTS['ORDER_ID']]: orderEvent.products.orderID,
            [AppConstants.analyticServices.HEAP_CONSTANTS['ORDER_ITEM_TOTAL']]: orderEvent.productTotals.itemsSubtotalPrice ? orderEvent.productTotals.itemsSubtotalPrice.amount : 0,
            [AppConstants.analyticServices.HEAP_CONSTANTS['ORDER_ITEM_TOTAL_POINTS']]: orderEvent.productTotals.itemsSubtotalPrice ? orderEvent.productTotals.itemsSubtotalPrice.points : 0,
            [AppConstants.analyticServices.HEAP_CONSTANTS['ORDER_TOTAL']]: orderEvent.productTotals.price.amount,
            [AppConstants.analyticServices.HEAP_CONSTANTS['ORDER_TOTAL_POINTS']]: orderEvent.productTotals.price.points,
            [AppConstants.analyticServices.HEAP_CONSTANTS['BALANCE_OLD']]: orderEvent.products.points,
            [AppConstants.analyticServices.HEAP_CONSTANTS['BALANCE_NEW']]: orderEvent.products.remainingBalance,
            [AppConstants.analyticServices.HEAP_CONSTANTS['SPLIT_TENDER']]: (orderEvent.products.totalPayment > 0 && pointsUsed > 0 ) ? 'true' : 'false',
            [AppConstants.analyticServices.HEAP_CONSTANTS['ORDER_LINES']]: orderLines,
            [AppConstants.analyticServices.HEAP_CONSTANTS['ORDER_QUANTITY']]: this.cartService.cartItemsTotalCount,
          };
          const eventProperties = {...commonProperties, ...distinctOrderProperties};
          window.heap.track(AppConstants.analyticServices.HEAP_EVENTS.ORDER_SUCCESS, eventProperties);
          break;
        }
        // ORDER LINE PLACED
        case(AppConstants.analyticServices.HEAP_EVENTS.ORDER_LINE_PLACED): {
          const orderEvent = viewProperties.payload;
          let orderLineNumber = 0;

          orderEvent.products.items.forEach(item => {
            let items = [this.getOrderItem(item, orderEvent.products.orderID)];
            const giftItems = this.getGiftItems([], item, orderEvent.products.orderID);

            items = items.concat(giftItems);
            this.trackOrderLinePlaced(items, orderLineNumber);
            orderLineNumber += items.length;
          });

          if (orderEvent.products.subscribedMediaProducts?.length) {
            const subscribedMediaProducts = this.getSubscribedMediaProducts(orderEvent.products.subscribedMediaProducts, orderEvent.products.orderID);
            this.trackOrderLinePlaced(subscribedMediaProducts, orderLineNumber);
          }
          break;
        }
        // SESSION STARTED
        case (AppConstants.analyticServices.HEAP_EVENTS.SESSION_STARTED): {
          window.heap.track(AppConstants.analyticServices.HEAP_EVENTS.SESSION_STARTED, commonProperties);
          break;
        }
      }
    }
  }
  triggerCartUpdateEvent(item, commonProperties, event) {
    const productDetail = item?.productDetail ? item.productDetail : item;
    if (productDetail){
      const itemProperties = {
        [AppConstants.analyticServices.HEAP_CONSTANTS['ITEM_NAME']]: productDetail.name,
        [AppConstants.analyticServices.HEAP_CONSTANTS['ITEM_POINTS']]: productDetail.offers[0].displayPrice.points,
        [AppConstants.analyticServices.HEAP_CONSTANTS['ITEM_PRICE']]: productDetail.offers[0].displayPrice.amount,
        [AppConstants.analyticServices.HEAP_CONSTANTS['ITEM_SKU']]: productDetail.sku,
        [AppConstants.analyticServices.HEAP_CONSTANTS['ITEM_CATEGORY']]: productDetail.categories[0].parents[0].name,
        [AppConstants.analyticServices.HEAP_CONSTANTS['ITEM_BRAND']]: productDetail.brand,
        [AppConstants.analyticServices.HEAP_CONSTANTS['ITEM_QUANTITY']]: (item.quantity !== undefined) ? item.quantity : 1 ,
      };
      const eventProperties = {...commonProperties, ...itemProperties};
      window.heap.track(event, eventProperties);
    }
  }

  trackOrderLinePlaced(items, orderLineNumber) {
    items.forEach(order => {
      order.orderLineNumber = ++orderLineNumber;
      window.heap.track('Order Line Placed', order);
    });
  }

  getSubscribedMediaProducts(subscribedMediaProducts: Array<MediaProduct>, orderID: string) {
    const products = [];

    subscribedMediaProducts.forEach(mediaProduct => {
      const data = {productDetail: {sku: mediaProduct.itemId, name: mediaProduct.itemId}, quantity: 1} as CartItem;
      products.push(this.getOrderItem(data, orderID));
    });

    return products;
  }

  getGiftItems(giftItems, cartItem: CartItem, orderID: string) {
    if (cartItem?.giftItem) {
      const data = cartItem.giftItem;

      giftItems.push(this.getOrderItem(data, orderID));
      this.getGiftItems(giftItems, data.giftItem, orderID);
    }
    return giftItems;
  }

  getOrderItem(cartItem: CartItem, orderID: string): { [key: string]: any } {
    return {
      [AppConstants.analyticServices.HEAP_CONSTANTS['VAR_ID']]: this.user.program.varId ? this.user.program.varId : '',
      [AppConstants.analyticServices.HEAP_CONSTANTS['VAR_NAME']]: this.user.program.varId ? this.user.program.varId : '',
      [AppConstants.analyticServices.HEAP_CONSTANTS['PROGRAM_ID']]: this.user.program.programId ? this.user.program.programId : '',
      [AppConstants.analyticServices.HEAP_CONSTANTS['PROGRAM_NAME']]: this.user.program.name ? this.user.program.name : '',
      [AppConstants.analyticServices.HEAP_CONSTANTS['PLATFORM']]: AppConstants.analyticServices.HEAP_CONSTANTS.APPLE,
      [AppConstants.analyticServices.HEAP_CONSTANTS['STOREFRONT_TYPE']]: AppConstants.analyticServices.HEAP_CONSTANTS.MERCHANDISE,
      [AppConstants.analyticServices.HEAP_CONSTANTS['STOREFRONT_NAME']]: AppConstants.analyticServices.HEAP_CONSTANTS.APPLE,
      [AppConstants.analyticServices.HEAP_CONSTANTS['ON_BEHALF_OF']]: (this.user.proxyUserId !== null) ? 'true' : 'false',
      [AppConstants.analyticServices.HEAP_CONSTANTS['EVENT_SOURCE']]: AppConstants.analyticServices.HEAP_CONSTANTS.STOREFRONT,
      [AppConstants.analyticServices.HEAP_CONSTANTS['ORDER_ID']]: orderID,
      [AppConstants.analyticServices.HEAP_CONSTANTS['ORDER_LINE_QUANTITY']]: cartItem.quantity, // '/cart$cartItems.quantity';
      [AppConstants.analyticServices.HEAP_CONSTANTS['ORDER_LINE_ITEM_PRICE']]: cartItem.productDetail?.defaultOffer ? cartItem.productDetail.defaultOffer.displayPrice.amount : 0, // '/cart$cartItems.productDetail.defaultOffer.displayPrice.amount';
      [AppConstants.analyticServices.HEAP_CONSTANTS['ORDER_LINE_TOTAL']]: cartItem.productDetail?.defaultOffer ? cartItem.productDetail.defaultOffer.totalPrice.amount : 0, // '/cart$cartItems.productDetail.defaultOffer.totalPrice.amount';
      [AppConstants.analyticServices.HEAP_CONSTANTS['ORDER_LINE_ITEM_POINTS']]: cartItem.productDetail?.defaultOffer ? cartItem.productDetail.defaultOffer.displayPrice.points : 0, // '/cart$cartItems.productDetail.defaultOffer.displayPrice.points';
      [AppConstants.analyticServices.HEAP_CONSTANTS['ORDER_LINE_TOTAL_POINTS']]: cartItem.productDetail?.defaultOffer ? cartItem.productDetail.defaultOffer.totalPrice.points : 0, // '/cart$cartItems.productDetail.defaultOffer.totalPrice.points';
      [AppConstants.analyticServices.HEAP_CONSTANTS['ITEM_SKU']]: cartItem.productDetail?.sku, // '/cart$cartItems.productDetail.sku';
      [AppConstants.analyticServices.HEAP_CONSTANTS['ITEM_NAME']]: cartItem.productDetail?.name, // '/cart$cartItems.productDetail.name';
      [AppConstants.analyticServices.HEAP_CONSTANTS['ITEM_CATEGORY']]: this.getCategory(cartItem), // 'Derive the Category from the Category API call';
      [AppConstants.analyticServices.HEAP_CONSTANTS['ITEM_SUBCATEGORY']]: cartItem.productDetail?.categories ? cartItem.productDetail.categories[0]?.name : '', // '/cart$cartItems.productDetail.categories[0].name';
      [AppConstants.analyticServices.HEAP_CONSTANTS['ITEM_BRAND']]: cartItem.productDetail?.brand, // '/cart$cartItems.productDetail.brand';
      [AppConstants.analyticServices.HEAP_CONSTANTS['ITEM_SUPPLIER']]: cartItem.productDetail?.manufacturer  // '/cart$cartItems.productDetail.manufacturer';
    };
  }

  getCategory(product) {
    const category = product.productDetail ? product.productDetail.categories : product.categories;
    if (category && category.length > 0
      && category[0].parents && category[0].parents.length > 0
    ) {
      return category[0].parents[0].name;
    }
    return '';
  }
}
