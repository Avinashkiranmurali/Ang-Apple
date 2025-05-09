import { Component, Input, OnInit, OnChanges } from '@angular/core';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { User } from '@app/models/user';
import { Messages } from '@app/models/messages';
import { Config } from '@app/models/config';
import { CartItem } from '@app/models/cart';
import { Offer } from '@app/models/offer';
import { TranslateService } from '@ngx-translate/core';
import { SharedService } from '@app/modules/shared/shared.service';

@Component({
  selector: 'app-cart-pricing-temp',
  templateUrl: './cart-pricing-temp.component.html',
  styleUrls: ['./cart-pricing-temp.component.scss']
})
export class CartPricingTempComponent implements OnInit, OnChanges {

  messages: Messages;
  user: User;
  config: Config;
  pricingTemplate: string;
  pointLabel: string;
  validTax: boolean;
  payFrequency: string;
  isDiscounted: { [key: string]: boolean };
  @Input() item: CartItem;
  @Input() offers: Offer;
  @Input() ext: string;
  @Input() verifyFullDiscount?: boolean;
  @Input() parentClass?: string;

  constructor(
    public messageStore: MessagesStoreService,
    private userStore: UserStoreService,
    private translateService: TranslateService,
    public pricingService: PricingService,
    public sharedService: SharedService
  ) {
    this.messages = messageStore.messages;
    this.user = this.userStore.user;
    this.config = this.userStore.config;
    this.pricingTemplate = this.config.pricingTemplate;
    this.payFrequency = this.config.payFrequency ? this.config.payFrequency : '';
    if (this.user.program.formatPointName !== ''){
      this.pointLabel = this.translateService.instant(this.user.program.formatPointName);
    }
  }

  ngOnInit(): void {
  }

  ngOnChanges(): void {
    if (this.offers) {
      this.isDiscounted = this.pricingService.checkDiscounts(this.offers);
    }
  }
}
