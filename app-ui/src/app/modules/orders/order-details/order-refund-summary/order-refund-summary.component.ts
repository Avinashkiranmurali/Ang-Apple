import { Component, Input, OnInit } from '@angular/core';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { Messages } from '@app/models/messages';
import { RefundSummary } from '@app/models/order-detail';
import { User } from '@app/models/user';
import { Config } from '@app/models/config';
import { Program } from '@app/models/program';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateService } from '@ngx-translate/core';
import { PricingService } from '@app/modules/pricing/pricing.service';

@Component({
  selector: 'app-order-refund-summary',
  templateUrl: './order-refund-summary.component.html',
  styleUrls: ['./order-refund-summary.component.scss']
})
export class OrderRefundSummaryComponent implements OnInit {

  messages: Messages;

  @Input() refundSummary: RefundSummary;
  @Input() ccLast4: number;
  pricingTemplate: string;
  user: User;
  config: Config;
  program: Program;
  pointLabel: string;
  pricingOption: string;

  constructor(
    public messageStore: MessagesStoreService,
    private translateService: TranslateService,
    private userStore: UserStoreService,
    private pricingService: PricingService) {
      this.messages = this.messageStore.messages;
      this.user = this.userStore.user;
      this.program = this.userStore.program;
      this.config = this.userStore.config;
      this.pricingTemplate = this.config.pricingTemplate;
    }

  ngOnInit(): void {
    this.pricingOption = this.pricingService.getPricingOption().option;

    if (this.program.formatPointName !== '') {
      this.pointLabel = this.translateService.instant(this.program.formatPointName);
    }
  }
}
