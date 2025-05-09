import { trigger, state, style, transition, animate } from '@angular/animations';
import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { Config } from '@app/models/config';
import { Messages } from '@app/models/messages';
import { OrderDetail } from '@app/models/order-detail';
import { Program } from '@app/models/program';
import { User } from '@app/models/user';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateService } from '@ngx-translate/core';
import { SharedService } from '@app/modules/shared/shared.service';

@Component({
  selector: 'app-order-payment-summary',
  templateUrl: './order-payment-summary.component.html',
  styleUrls: ['./order-payment-summary.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
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

export class OrderPaymentSummaryComponent implements OnInit {

  messages: Messages;
  config: Config;
  pointLabel: string;
  program: Program;
  user: User;
  animationHeight: number;
  animationState = 'out';
  pricingOption;
  isPointsOnlyRewards;
  @Input() orderDetails: OrderDetail;

  constructor(
    private messageStoreService: MessagesStoreService,
    private userStore: UserStoreService,
    private translateService: TranslateService,
    private pricingService: PricingService,
    private sharedService: SharedService
  ) {
    this.messages = this.messageStoreService.messages;
    this.config = this.userStore.config;
    this.program = this.userStore.program;
    this.user = this.userStore.user;
    this.isPointsOnlyRewards = this.sharedService.isPointsOnlyRewards();
  }

  ngOnInit(): void {
    this.pricingOption = this.pricingService.getPricingOption().option;
    if (this.program.formatPointName !== '') {
      this.pointLabel = this.translateService.instant(this.program.formatPointName);
    }
  }

  toggleShowDetails(event) {
    event.preventDefault();
    this.animationHeight = document.getElementsByClassName('summary-details')[0].scrollHeight + 7;
    this.animationState = this.animationState === 'in' ? 'out' : 'in';
  }

  showAdditionalPayment(){
    if (this.sharedService.isPointsFixed()){
      return this.orderDetails.paymentInfo.awardsPurchasedPrice > 0;
    }else{
      return !this.sharedService.isPointsOnlyRewards();
    }
  }

}
