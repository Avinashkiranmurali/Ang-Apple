import { Component, OnInit, Input, Injector } from '@angular/core';
import { CartItem } from '@app/models/cart';
import { Config } from '@app/models/config';
import { Messages } from '@app/models/messages';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { TemplateService } from '@app/services/template.service';
import { UserStoreService } from '@app/state/user-store.service';
import { BreakPoint } from '@app/components/utils/break-point';

@Component({
  selector: 'app-order-confirm-items-list',
  templateUrl: './order-confirm-items-list.component.html',
  styleUrls: ['./order-confirm-items-list.component.scss']
})
export class OrderConfirmItemsListComponent extends BreakPoint implements OnInit {

  config: Config;
  messages: Messages;
  singleItemPurchase: boolean;
  showShippingAvailability: boolean;
  showTaxDisclaimer: boolean;
  setTaxFeeTitle: string;
  orderConfirmItemsListTemplate: string;

  cartProductDetailsExpanded = false;


  @Input() cartItems: Array<CartItem>;
  @Input() isUnbundled: boolean;

  constructor(
    public messageStore: MessagesStoreService,
    private templateService: TemplateService,
    public userStore: UserStoreService,
    public injector: Injector
  ) {
    super(injector);
    this.messages = this.messageStore.messages;
    this.config = this.userStore.config;
    this.singleItemPurchase = this.config.SingleItemPurchase ? this.config.SingleItemPurchase : false;
    this.showShippingAvailability = this.config.showShippingAvailability;
    this.showTaxDisclaimer = this.config.showTaxDisclaimer;
    this.setTaxFeeTitle = this.isUnbundled === false ? this.messages.taxFeeNotIncluded : this.showTaxDisclaimer === true ? this.messages.taxFeeNotIncluded : '';
    this.orderConfirmItemsListTemplate = this.orderConfirmData().orderConfirmItemsList.template ? this.orderConfirmData().orderConfirmItemsList.template : 'checkout-default.htm';
  }

  ngOnInit(): void {
  }

  orderConfirmData() {
    return this.templateService.getTemplatesProperty('orderConfirmation');
  }

}
