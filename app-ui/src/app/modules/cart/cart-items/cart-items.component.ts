import { Component, EventEmitter, OnInit, Input, Injector, Output, ViewChildren, QueryList, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { BreakPoint } from '@app/components/utils/break-point';
import { CartItem } from '@app/models/cart';
import { Config } from '@app/models/config';
import { Messages } from '@app/models/messages';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { TemplateService } from '@app/services/template.service';
import { UserStoreService } from '@app/state/user-store.service';
import { CartService } from '@app/services/cart.service';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { AppConstants } from '@app/constants/app.constants';
import { isEmpty } from 'lodash';

@Component({
  selector: 'app-cart-items',
  templateUrl: './cart-items.component.html',
  styleUrls: ['./cart-items.component.scss']
})
export class CartItemsComponent extends BreakPoint implements OnInit {

  config: Config;
  messages: Messages;
  singleItemPurchase: boolean;
  showShippingAvailability: boolean;
  cartProductDetailsExpanded = [];
  disableQty: boolean[];
  hasEngraving: boolean[];
  @Input() cartItems: Array<CartItem>;
  @Input() setCartTaxFeeTitle: string;
  @Output() cartUpdateEvent = new EventEmitter<any>();
  @ViewChildren('cartItemsRef') cartItemsRef: QueryList<ElementRef>;
  cartItemsListTemplate: string;

  constructor(
    public messageStore: MessagesStoreService,
    private templateService: TemplateService,
    public userStore: UserStoreService,
    public injector: Injector,
    private router: Router,
    public sharedService: SharedService,
    public cartService: CartService,
    private matomoService: MatomoService
  ) {
    super(injector);
    this.messages = this.messageStore.messages;
    this.config = this.userStore.config;
    this.singleItemPurchase = this.config.SingleItemPurchase ? this.config.SingleItemPurchase : false;
    this.showShippingAvailability = this.config.showShippingAvailability;
    this.cartItemsListTemplate = this.cartData().cartItemsList.template ? this.cartData().cartItemsList.template : 'cart-default.htm';
  }
  ngOnInit(): void {
  }

  cartData() {
    return this.templateService.getTemplatesProperty('cart');
  }

  removeItem(index: number, removedItem): void {
    this.cartUpdateEvent.emit({
      type: 'removeItem',
      index,
      item: removedItem
    });
  }

  editItemQty($event, index, id, qty, item) {
    this.cartUpdateEvent.emit({
      type: 'editItemQty',
      index,
      item,
      id,
      qty
    });
  }

  addOrUpdateCartEvent(event): void {
    this.cartUpdateEvent.emit({
      type: event.type,
      item: event.item
    });
  }

  changeItemSelection(item): void {
    /* TODO changeItemSelection FUNCTIONALITY*/
    if (!this.config.fullCatalog) {
      this.matomoService.broadcast(AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.REMOVE_FROM_CART, {
        payload: {
          product: item
        }
      });
    }
    this.router.navigate(['/store']);
  }

  isServicePlansExist(item) {
    return item.selectedAddOns?.servicePlan ? true : !isEmpty(item.productDetail?.addOns?.servicePlans);
  }
}
