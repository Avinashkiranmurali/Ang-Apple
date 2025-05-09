import { Component, OnInit, Input, Injector } from '@angular/core';
import { Router } from '@angular/router';
import { BreakPoint } from '@app/components/utils/break-point';
import { CartItem } from '@app/models/cart';
import { Config } from '@app/models/config';
import { Messages } from '@app/models/messages';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { TemplateService } from '@app/services/template.service';
import { UserStoreService } from '@app/state/user-store.service';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { AppConstants } from '@app/constants/app.constants';
import { SharedService } from '@app/modules/shared/shared.service';

@Component({
  selector: 'app-checkout-items',
  templateUrl: './checkout-items.component.html',
  styleUrls: ['./checkout-items.component.scss']
})
export class CheckoutItemsComponent extends BreakPoint  implements OnInit {

  config: Config;
  messages: Messages;
  singleItemPurchase: boolean;
  showShippingAvailability: boolean;
  cartProductDetailsExpanded = false;
  @Input() cartItems: Array<CartItem>;
  @Input() setCartTaxFeeTitle: string;
  checkoutItemsListTemplate: string;
  buttonColor: string;

  constructor(
    public messageStore: MessagesStoreService,
    private templateService: TemplateService,
    public userStore: UserStoreService,
    public injector: Injector,
    private matomoService: MatomoService,
    private router: Router,
    public sharedService: SharedService
  ) {
    super(injector);
    this.messages = this.messageStore.messages;
    this.config = this.userStore.config;
    this.singleItemPurchase = this.config.SingleItemPurchase ? this.config.SingleItemPurchase : false;
    this.showShippingAvailability = this.config.showShippingAvailability;
    this.checkoutItemsListTemplate  = this.checkoutData().checkoutItemsList.template ? this.checkoutData().checkoutItemsList.template : 'checkout-default.htm';
    this.buttonColor = this.templateService.getBtnColor();
  }

  ngOnInit(): void {
  }

  checkoutData() {
    return this.templateService.getTemplatesProperty('checkout');
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
}
