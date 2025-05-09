import { Component, Input, OnChanges, OnInit } from '@angular/core';
import { Messages } from '@app/models/messages';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { User } from '@app/models/user';
import { UserStoreService } from '@app/state/user-store.service';
import { PaymentStoreService } from '@app/state/payment-store.service';
import { CartService } from '@app/services/cart.service';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { TranslateService } from '@ngx-translate/core';
import { TitleCasePipe } from '@angular/common';
import { Config } from '@app/models/config';

@Component({
  selector: 'app-payment-summary',
  templateUrl: './payment-summary.component.html',
  styleUrls: ['./payment-summary.component.scss']
})
export class PaymentSummaryComponent implements OnInit, OnChanges {

  messages: Messages;
  user: User;
  config: Config;
  @Input() template;
  @Input() subtitle;
  @Input() heading;
  pointLabel: string;
  @Input() state;
  @Input() pointsToUseChangeEvent;
  @Input() splitPayOptionChangeEvent;
  selections;
  isUnbundled: boolean;
  pointsToUse: number;
  translateParams: { [key: string]: string };

  constructor(
    public messageStore: MessagesStoreService,
    public userStore: UserStoreService,
    public stateService: PaymentStoreService,
    public cartService: CartService,
    public pricingService: PricingService,
    private translateService: TranslateService,
    private titleCasePipe: TitleCasePipe,
  ) {
    this.messages = this.messageStore.messages;
    this.user = this.userStore.user;
    this.config = this.userStore.config;
    const pricingOptions = this.pricingService.getPricingOption();
    this.isUnbundled = pricingOptions['isUnbundled'];
    if (this.userStore.program.formatPointName !== '') {
      this.pointLabel = this.translateService.instant(this.userStore.program.formatPointName);
      this.translateParams = {
        pointLabel: this.titleCasePipe.transform(this.pointLabel)
      };
    }
  }

  ngOnInit(): void {}

  ngOnChanges(changes: any): void {
    if (this.state.selections.payment && this.state.selections.payment.splitPayOption) {
      this.pointsToUse = this.state.selections.payment.splitPayOption.pointsToUse;
    }
  }
}
