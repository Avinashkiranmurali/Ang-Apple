import { Component, Input, OnChanges } from '@angular/core';
import { SmartPrice } from '@app/models/smart-price';
import { Config } from '@app/models/config';
import { Messages } from '@app/models/messages';
import { Program } from '@app/models/program';
import { User } from '@app/models/user';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-smart-price',
  templateUrl: './smart-price.component.html',
  styleUrls: ['./smart-price.component.scss']
})
export class SmartPriceComponent implements OnChanges {

  @Input() smartPrice: SmartPrice;
  @Input() ext: string;
  @Input() parentClass: string;
  @Input() placement: string;
  user: User;
  program: Program;
  config: Config;
  messages: Messages;
  pointLabel: string;
  translateParams: { [key: string]: string };

  constructor(
    private userStoreService: UserStoreService,
    private messageStore: MessagesStoreService,
    private translateService: TranslateService,
    private currencyFormatPipe: CurrencyFormatPipe,
    private currencyPipe: CurrencyPipe
  ) {
    this.user = this.userStoreService.user;
    this.program = this.userStoreService.program;
    this.config = this.userStoreService.config;
    this.messages = this.messageStore.messages;

    if (this.program.formatPointName !== '') {
      this.pointLabel = this.translateService.instant(this.program.formatPointName);
    }
  }

  ngOnChanges(): void {
    if (this.smartPrice) {
      const params: { [key: string]: string } = {};
      params.points = this.currencyFormatPipe.transform(this.smartPrice.points, this.config.pricingTemplate, this.user.locale);
      if (this.config.showDecimal) {
        params.amount = this.currencyPipe.transform(this.smartPrice.amount);
      } else {
        params.amount = this.currencyPipe.transform(this.smartPrice.amount, '', 'symbol', '1.0-0');
      }
      this.translateParams = { pointLabel: this.pointLabel, ...params };
    }
  }

  getSplitPayTemplate() {
    if (this.ext) {
      if (this.ext === 'header-section') {
        return this.ext + '-template.htm';
      } else if (this.ext === 'cta-section') {
        return this.ext + '-template.htm';
      } else {
        return '';
      }
    }
    return '';
  }

}
