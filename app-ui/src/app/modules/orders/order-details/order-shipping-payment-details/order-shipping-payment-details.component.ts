import { Component, OnInit, Input } from '@angular/core';
import { DeliveryAddress } from '@app/models/order-detail';
import { ContactInfo } from '@app/models/contact-info';
import { PaymentInfo } from '@app/models/payment';
import { BillTo } from '@app/models/address';
import { UserStoreService } from '@app/state/user-store.service';
import { Config } from '@app/models/config';
import { User } from '@app/models/user';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { Messages } from '@app/models/messages';
import { Program } from '@app/models/program';

@Component({
  selector: 'app-order-shipping-payment-details',
  templateUrl: './order-shipping-payment-details.component.html',
  styleUrls: ['./order-shipping-payment-details.component.scss']
})
export class OrderShippingPaymentDetailsComponent implements OnInit {
  @Input() shippingAddress: DeliveryAddress;
  @Input() billToAddress: BillTo;
  @Input() paymentInfo: PaymentInfo;
  @Input() contactInfo: ContactInfo;
  messages: Messages;
  locale: string;
  currLocale: string;
  config: Config;
  user: User;
  pointLabel: string;
  program: Program;
  pricingTemplate: string;

constructor(
    private userStore: UserStoreService,
    private messageStore: MessagesStoreService
  ) {
  this.user = this.userStore.user;
  this.config = this.userStore.config;
  this.program = this.userStore.program;
  this.locale = this.user.locale.replace('_', '-');
  this.currLocale = this.user.locale.toLowerCase();
  this.messages = this.messageStore.messages;
  this.config = this.userStore.config;
  this.pricingTemplate = this.config.pricingTemplate;
  }

  ngOnInit(): void {
    this.pointLabel = this.messages[this.program.formatPointName];
  }
}
