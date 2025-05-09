import { Component, Input, OnChanges } from '@angular/core';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { BannerTemplate } from '@app/models/banner-template';
import { Config } from '@app/models/config';
import { Messages } from '@app/models/messages';
import { LandingBannerCategory } from '@app/models/banners';

@Component({
  selector: 'app-multi-product-banner',
  templateUrl: './multi-product-banner.component.html',
  styleUrls: ['./multi-product-banner.component.scss']
})

export class MultiProductBannerComponent implements OnChanges {

  messages: Messages;
  config: Config;
  isAdditionalInfoExist = false;
  @Input() multiProductBanner: BannerTemplate;
  @Input() multiProductBannerList: Array<string>;
  @Input() items: Array<Array<BannerTemplate>>;
  @Input() isMobile: boolean;
  @Input() multiBanner: LandingBannerCategory;

  constructor(
    private messageStore: MessagesStoreService,
    public userStore: UserStoreService
  ) {
    this.messages = this.messageStore.messages;
    this.config = this.userStore.config;
  }

  ngOnChanges(): void {
    if (this.items) {
      let additionalInfo;
      for (const rows of this.items) {
        additionalInfo = rows.find(item => (item.additionalInfo && item.additionalInfo.length > 0));

        if (additionalInfo) {
          this.isAdditionalInfoExist = true;
          break;
        }
      }
    }
  }
}
