import { Component, OnInit, Input, OnChanges } from '@angular/core';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { BannerTemplate } from '@app/models/banner-template';
import { Config } from '@app/models/config';
import { Messages } from '@app/models/messages';

@Component({
  selector: 'app-marketing-banner',
  templateUrl: './marketing-banner.component.html',
  styleUrls: ['./marketing-banner.component.scss']
})

export class MarketingBannerComponent implements OnInit, OnChanges {

  messages: Messages;
  config: Config;
  @Input() marketingBanner: BannerTemplate;
  @Input() marketingBannerList: Array<string>;
  @Input() items: Array<BannerTemplate>;
  @Input() doubleBannerData: Array<BannerTemplate>;
  @Input() template: Array<BannerTemplate>;
  constructor(
    private messageStore: MessagesStoreService,
    public userStore: UserStoreService
  ) {
    this.messages = this.messageStore.messages;
    this.config = this.userStore.config;
  }

  ngOnInit(): void {
  }

  ngOnChanges(): void {
    if (this.items) {
      for (const item of this.items) {
        if (item.template === 'multi-product.htm' && item.listDetails) {
          item.isAdditionalInfoExist = false;

          for (const listDetails of item.listDetails) {
            for (const detail of listDetails) {
              if (detail.additionalInfo && detail.additionalInfo.length > 0) {
                item.isAdditionalInfoExist = true;
                break;
              }
            }
          }
        }
      }
    }
  }
}
