import { Component, Input } from '@angular/core';
import { Messages } from '@app/models/messages';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { User } from '@app/models/user';
import { Program } from '@app/models/program';
import { Config } from '@app/models/config';
import { OrderStatus } from '@app/models/order-detail';
import { Price } from '@app/models/price';

@Component({
  selector: 'app-order-confirm-shipping-details',
  templateUrl: './order-confirm-shipping-details.component.html',
  styleUrls: ['./order-confirm-shipping-details.component.scss']
})
export class OrderConfirmShippingDetailsComponent {

  // TODO Temporary assignments are below
  messages: Messages;
  isDRP: boolean;
  pointLabel: string;
  pricingTemplate: string;
  user: User;
  program: Program;
  config: Config;

  @Input() confirmationTemplate: object;
  @Input() confirmOrder: object;
  @Input() cartTotals: Price;
  @Input() isCashOnly: string;
  @Input() pointsUsed: number;
  @Input() totalPurchase: number;
  @Input() currLocale: string;
  @Input() orderData: OrderStatus;

  constructor(
    private messageStore: MessagesStoreService,
    private userStore: UserStoreService ) {
    this.messages = messageStore.messages;
    this.user = this.userStore.user;
    this.program = this.userStore.program;
    this.config = this.userStore.config;
    this.isDRP = !!this.user.awpEmployeeGroup;
    this.pointLabel = this.messages[this.program.formatPointName];
  }

  // ToDo Temporary assigmennt function
  displayPaymentAgreement(): boolean {
    return false;
  }

}
