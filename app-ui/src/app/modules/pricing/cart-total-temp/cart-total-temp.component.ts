import { Component, Input, OnInit } from '@angular/core';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { Messages } from '@app/models/messages';
import { Config } from '@app/models/config';
import { Program } from '@app/models/program';
import { User } from '@app/models/user';
import { Price } from '@app/models/price';

@Component({
  selector: 'app-cart-total-temp',
  templateUrl: './cart-total-temp.component.html',
  styleUrls: ['./cart-total-temp.component.scss']
})
export class CartTotalTempComponent implements OnInit {

  messages: Messages;
  config: Config;
  pricingTemplate: string;
  ctPriceTempMsg: string;
  @Input() totalVal: Price = {
    amount: 0,
    points: 0,
    currencyCode: ''
  };
  @Input() cartSubtotal: Price = {
    amount: 0,
    points: 0,
    currencyCode: ''
  };
  cartObjTotals: object;
  @Input() ext: string;
  @Input() showMinus: boolean;
  pointLabel: string;
  program: Program;
  user: User;
  isPoints: boolean;
  @Input() showTaxDisclaimer;
  @Input() parentClass?: string;

  constructor(
    public messageStore: MessagesStoreService,
    private userStore: UserStoreService
  ) {
    this.messages = messageStore.messages;
    this.config = this.userStore.config;
    this.pricingTemplate = this.config.pricingTemplate;
    this.program = this.userStore.program;
    this.user = this.userStore.user;
    // get the point lable from order-details from messages
    this.pointLabel = this.messages[this.program.formatPointName];
  }

  ngOnInit(): void {
  }

  getCartTotalPriceTemp(ctTempType, ctExt) {
    const splitArr = ctTempType.split('_');
    this.isPoints = (splitArr[0] === 'points');
    if (ctExt === 'exceeded') {
      return (this.isPoints) ? 'exceeded_total-points-template.htm' : 'exceeded_total-cash-template.htm';
    } else if (ctExt === 'sub') {
      this.ctPriceTempMsg = (this.isPoints) ? 'totalPointsPrice' : 'totalCashPrice';
      return 'sub-total_pd-finance.htm';
    } else {
      if (ctTempType === 'points_cash' || ctTempType === 'cash_points') {
        this.ctPriceTempMsg = '';
        return 'total-' + ctTempType + '-template.htm';
      } else if (ctTempType === 'no_pay') {
        this.ctPriceTempMsg = '';
        return '';
      } else {
        return 'total-single-template.htm';
      }
    }
  }

}
