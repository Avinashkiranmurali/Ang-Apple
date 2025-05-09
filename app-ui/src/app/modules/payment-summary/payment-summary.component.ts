import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { Messages } from '@app/models/messages';
import { Config } from '@app/models/config';
import { Program } from '@app/models/program';
import { User } from '@app/models/user';
import { Price } from '@app/models/price';
import { AdditionalInfo } from '@app/models/additional-info';
import { CartTotal } from '@app/models/cart';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { TranslateService } from '@ngx-translate/core';
import { animate, state, style, transition, trigger } from '@angular/animations';
import { Router } from '@angular/router';

@Component({
  selector: 'app-payment-summary',
  templateUrl: './payment-summary.component.html',
  styleUrls: ['./payment-summary.component.scss'],
  animations: [
    trigger('slideInOut', [
      state('in', style({
          visibility: 'visible', 'margin-bottom': '16px', 'padding-bottom': '6px', overflow : 'hidden', height: '{{animationHeight}}px'
      }), {params: {animationHeight: 0}}),
      state('out', style({
          visibility: 'hidden', 'margin-bottom': '-1px', 'padding-bottom': '0px', overflow : 'hidden', height: '0px'
      })),
      transition('in <=> out', [
        animate('0.3s')
      ])
    ])
  ]
})

export class PaymentSummaryComponent implements OnInit {
  messages: Messages;
  config: Config;
  pointLabel: string;
  program: Program;
  user: User;
  animationHeight: number;
  showTaxDisclaimer = false;
  pricingOption;
  paymentTemplate;
  isPointsOnlyRewards;
  payFrequency;
  expanderBtnText;
  setEHFtitle: string;
  isUnbundled: boolean;
  animationState = 'out';
  translateParams: { [key: string]: string };
  isConfPage: any;
  @Input() negativeBalance: boolean;
  @Input() paymentRequired: boolean;
  @Input() purchasePaymentOption;
  @Input() earnPoints: number;
  @Input() cartSubtotal?: Price = {
    amount: 0,
    points: 0,
    currencyCode: ''
  };
  @Input() shippingCost: object;
  @Input() cartTotals: object;
  @Input() cartObjTotals: CartTotal;
  @Input() totalTaxes: object;
  @Input() totalFees: object;
  @Input() pointsUsed: number;
  @Input() totalPayment: number;
  @Input() itemPricing: AdditionalInfo;
  @Input() points: number;
  @Input() balancePoints: number;
  @Input() isReviewPage: boolean;
  @Input() parentClass: string;
  @Input() pointsBeforePurchase: number;
  @Output() hasDiscounts = new EventEmitter<any>();
  @Output() discountedSubtotal = new EventEmitter<any>();
  constructor(
    private messageStore: MessagesStoreService,
    private userStore: UserStoreService,
    private pricingService: PricingService,
    private translateService: TranslateService,
    private sharedService: SharedService,
    private router: Router
  ) {
    this.messages = this.messageStore.messages;
    this.config = this.userStore.config;
    this.program = this.userStore.program;
    this.user = this.userStore.user;
    if (this.userStore.program.formatPointName !== '') {
      this.pointLabel = this.translateService.instant(this.userStore.program.formatPointName);
      this.translateParams = {
        pointLabel: this.pointLabel,
        paymentAmount: this.messages['paymentAmount'],
        pointsToUse: this.messages['pointsToUse']
      };
    }
    this.paymentTemplate = this.config.paymentTemplate;
    this.payFrequency = this.config.payFrequency ? this.config.payFrequency : '';
    this.isPointsOnlyRewards = this.sharedService.isPointsOnlyRewards();
  }

  ngOnInit(): void {
    this.expanderBtnText = this.messages['viewDetails'];
    this.setEHFtitle = this.config.showFeeDetails ? this.messages.environmentalHandlingFee : '';
    ({ option: this.pricingOption, isUnbundled: this.isUnbundled } = this.pricingService.getPricingOption());
    this.isConfPage = this.router.url?.includes('confirmation');
  }

  getPaymentSummaryTemp() {
    if (this.sharedService.isCashOnlyRedemption()){
      if (this.paymentTemplate === 'cash_subsidy' || this.paymentTemplate === 'installment_monthly') {
        return this.paymentTemplate + '.htm';
      }
      return 'cash_default.htm';
    }
    return;
  }

  toggleShowDetails(event) {
    event.preventDefault();
    this.animationHeight = document.getElementsByClassName('unbundled-breakdown')[0].scrollHeight + 7;
    this.animationState = this.animationState === 'in' ? 'out' : 'in';
    this.expanderBtnText = this.animationState === 'out' ? this.messages['viewDetails'] : this.messages['hideDetails'];
  }

  hasDiscount() {
    this.hasDiscounts.emit();
  }

  discountedSubTotal() {
    this.discountedSubtotal.emit();
  }

  showAdditionalPayment(){
    if (this.sharedService.isPointsFixed()) {
      return this.paymentRequired;
    } else{
      return !this.sharedService.isPointsOnlyRewards();
    }
  }
}
