import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { Messages } from '@app/models/messages';
import { Config } from '@app/models/config';
import { Program } from '@app/models/program';
import { User } from '@app/models/user';
import { Price } from '@app/models/price';

@Component({
  selector: 'app-order-summary',
  templateUrl: './order-summary.component.html',
  styleUrls: ['./order-summary.component.scss']
})
export class OrderSummaryComponent implements OnInit {
  messages: Messages;
  config: Config;
  pointLabel: string;
  program: Program;
  user: User;
  pricingOption;
  setEHFtitle: string;
  @Input() paySumExt: string;
  @Input() cartSubtotal: Price = {
    amount: 0,
    points: 0,
    currencyCode: ''
  };
  @Input() shippingCost: Price = {
    amount: 0,
    points: 0,
    currencyCode: ''
  };
  @Input() totals: Price = {
    amount: 0,
    points: 0,
    currencyCode: ''
  };
  negativeBalance = false;
  @Input() payFrequency: string;
  @Input() totalTaxes: Price = {
    amount: 0,
    points: 0,
    currencyCode: ''
  };
  @Input() totalFees: Price = {
    amount: 0,
    points: 0,
    currencyCode: ''
  };
  @Input() paymentRequired;
  @Input() displayOrderSummOnMobile: boolean;
  @Input() pointsUsed: number;
  @Output() displayOrderSummary = new EventEmitter();
  constructor(
    public messageStore: MessagesStoreService,
    private userStore: UserStoreService,
    private sharedService: SharedService
  ) {
    this.messages = this.messageStore.messages;
    this.config = this.userStore.config;
    this.program = this.userStore.program;
    this.user = this.userStore.user;
    this.pointLabel = this.messages[this.program.formatPointName];
    this.setEHFtitle = this.config.showFeeDetails === true ? this.messages.environmentalHandlingFee : '';
  }

  ngOnInit(): void {
  }

  toggleOrderSummary() {
    this.displayOrderSummOnMobile = !this.displayOrderSummOnMobile;
    this.displayOrderSummary.emit(this.displayOrderSummOnMobile);
  }
  isPointsFixed(){
    return this.sharedService.isPointsFixed();
  }
}
