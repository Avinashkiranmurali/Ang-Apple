import { Component, Input, OnInit, OnChanges } from '@angular/core';
import { UserStoreService } from '@app/state/user-store.service';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { User } from '@app/models/user';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { Program } from '@app/models/program';
import { Offer } from '@app/models/offer';
import { Config } from '@app/models/config';
import { TranslateService } from '@ngx-translate/core';
import { PricingModel } from '@app/models/pricing-model';
import { Messages } from '@app/models/messages';
import { SharedService } from '@app/modules/shared/shared.service';

@Component({
  selector: 'app-pricing-temp',
  templateUrl: './pricing-temp.component.html',
  styleUrls: ['./pricing-temp.component.scss']
})

export class PricingTempComponent implements OnInit, OnChanges {

  messages: Messages;
  user: User;
  config: Config;
  program: Program;
  pricingTemplate: string;
  pointLabel: string;
  isDiscounted: { [key: string]: boolean };

  @Input() offer: Offer;
  @Input() item?: object;
  @Input() ext: string;
  @Input() showTaxDisclaimer;
  @Input() payFrequency;
  @Input() priceModel?: PricingModel;
  @Input() verifyFullDiscount?: boolean;
  @Input() hideFullDiscount?: boolean;
  @Input() parentClass?: string;
  @Input() hideStrikethroughPrice?: boolean;
  @Input() showFromLabel?: boolean;

  constructor(
    public messageStore: MessagesStoreService,
    public pricingService: PricingService,
    public userStore: UserStoreService,
    private translateService: TranslateService,
    public sharedService: SharedService
  ) {
    this.messages = messageStore.messages;
    this.user = this.userStore.user;
    this.config = this.userStore.config;
    this.program = this.userStore.program;
  }

  ngOnInit(): void {
    if (this.program.formatPointName !== ''){
      this.pointLabel = this.translateService.instant(this.program.formatPointName);
    }
    if (this.item !== undefined && this.item !== null){
      this.priceModel = this.priceModel ? this.priceModel : this.item['additionalInfo']?.PricingModel;
    }
    // this.priceModel = null;
  }

  ngOnChanges(): void {
    if (this.offer) {
      this.isDiscounted = this.pricingService.checkDiscounts(this.offer);
    }
  }

}
