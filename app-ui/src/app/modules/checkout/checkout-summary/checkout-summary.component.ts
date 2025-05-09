import { Component, OnInit, Input, OnChanges } from '@angular/core';
import { Address } from '@app/models/address';
import { Config } from '@app/models/config';
import { Messages } from '@app/models/messages';
import { User } from '@app/models/user';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { TemplateService } from '@app/services/template.service';
import { Price } from '@app/models/price';
import { CreditItem } from '@app/models/credit-item';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { TranslateService } from '@ngx-translate/core';
import { DecimalPipe, TitleCasePipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';

@Component({
  selector: 'app-checkout-summary',
  templateUrl: './checkout-summary.component.html',
  styleUrls: ['./checkout-summary.component.scss']
})
export class CheckoutSummaryComponent implements OnInit, OnChanges {

  user: User;
  config: Config;
  messages: Messages;
  buttonColor: string;
  currLocale: string;
  mercAddressLock: boolean;
  contactInfoLock: boolean;
  addressNameLock: boolean;
  businessNameLocked: boolean;
  isAddressFormEditable: boolean;
  isMultiAddress: boolean;
  addresses: Address[];
  userNameParam: { [k: string]: string };
  translateParams: { [key: string]: string };
  translateParamsWithCase: { [key: string]: string };
  paymentTemplate;

  @Input() checkoutAddress: Address;
  @Input() creditItem: CreditItem;
  @Input() pointsUsed: number;
  @Input() totalPayment: number;
  @Input() selectedRedemptionOption: string;

  constructor(
    public messageStore: MessagesStoreService,
    private templateService: TemplateService,
    public userStore: UserStoreService,
    private currencyPipe: CurrencyPipe,
    private currencyFormatPipe: CurrencyFormatPipe,
    private translateService: TranslateService,
    private titleCasePipe: TitleCasePipe
  ) {
    this.messages = this.messageStore.messages;
    this.config = this.userStore.config;
    this.user = this.userStore.user;
    this.addresses = this.user.addresses;
    this.isMultiAddress = (this.addresses && (this.addresses.length > 1));
    this.buttonColor = this.templateService.getTemplatesProperty('buttonColor');
    this.currLocale = this.user.locale.toLowerCase();
    this.mercAddressLock = this.config.MercAddressLocked ? this.config.MercAddressLocked : false;
    this.contactInfoLock = this.config.ContactInfoLocked ? this.config.ContactInfoLocked : false;
    this.addressNameLock = this.config.ShipToNameLocked ? this.config.ShipToNameLocked : false;
    this.businessNameLocked = this.config.businessNameLocked ? this.config.businessNameLocked : false;
    this.isAddressFormEditable = !(this.mercAddressLock && this.contactInfoLock && this.addressNameLock && this.businessNameLocked);
    this.paymentTemplate = this.config.paymentTemplate;
  }

  ngOnInit(): void {
  }

  ngOnChanges(): void {
    if (this.checkoutAddress) {
      this.userNameParam = {
        firstName: this.checkoutAddress.firstName,
        middleName: this.checkoutAddress.middleName,
        lastName: this.checkoutAddress.lastName
      };
    }

    this.updatePaymentMethod();
  }

  updatePaymentMethod(): void {
    const formatPointName = this.userStore?.program?.formatPointName ? this.userStore?.program?.formatPointName : '';
    const pointLabel = formatPointName ? this.translateService.instant(formatPointName) : '';

    this.translateParams = {
      pointLabel,
      totalPayment: this.config.showDecimal ? this.currencyPipe.transform(this.totalPayment) : this.currencyPipe.transform(this.totalPayment, '', 'symbol', '1.0-0'),
      ccLast4: this.creditItem?.ccLast4 ? this.creditItem?.ccLast4 : '',
      pointsUsed: this.pointsUsed ? this.currencyFormatPipe.transform(this.pointsUsed, this.config.pricingTemplate, this.user.locale) : ''
    };

    this.translateParamsWithCase = {
      pointLabel: this.titleCasePipe.transform(pointLabel)
    };
  }

}
