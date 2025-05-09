import { Component, ElementRef, Input, OnInit, QueryList, ViewChildren } from '@angular/core';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { Config } from '@app/models/config';
import { Messages } from '@app/models/messages';
import { LineItem } from '@app/models/order-detail';
import { User } from '@app/models/user';
import { TemplateStoreService } from '@app/state/template-store.service';

@Component({
  selector: 'app-order-items',
  templateUrl: './order-items.component.html',
  styleUrls: ['./order-items.component.scss']
})
export class OrderItemsComponent implements OnInit {

  @Input() lineItems: Array<LineItem>;
  config: Config;
  messages: Messages;
  singleItemPurchase: boolean;
  user: User;
  locale: string;
  mediumDate: string;
  orderHistoryTemplate: object;
  progressLevel = {
    0: 0,
    1: 77,
    2: 405,
    3: 740
  };
  @ViewChildren('progressBarElem') progressBarElements: QueryList<ElementRef>;

  constructor(
    public messageStore: MessagesStoreService,
    public userStore: UserStoreService,
    private templateStoreService: TemplateStoreService,
  ) {
    this.messages = this.messageStore.messages;
    this.user = this.userStore.user;
    this.config = this.userStore.config;
    this.singleItemPurchase = this.config.SingleItemPurchase ? this.config.SingleItemPurchase : false;
    this.locale = this.user.locale.replace('_', '-');
    this.mediumDate = (this.config.mediumDate !== undefined) ? this.config.mediumDate : 'mediumDate';
    this.orderHistoryTemplate = this.templateStoreService.orderHistoryTemplate;
  }

  orderItemStatus(item) {
    if (item.delayedShippingInfo) {
      return 'delayed';
    } else if (item.shipmentInfo) {
      return 'shipped';
    } else {
      return 'processing';
    }
  }

  ngOnInit(): void {
    setTimeout(() => {
      this.progressBarElements.forEach((progressElement, index) => {
        const element = progressElement.nativeElement.querySelector('.progress-bar');
        element?.setAttribute('aria-label', this.messages['orderStatus-' + this.lineItems[index].orderLineProgress.status])
      });
    }, 500);
  }

}
