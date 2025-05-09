import { Component, OnDestroy } from '@angular/core';
import { TitleCasePipe } from '@angular/common';
import { CartService } from '@app/services/cart.service';
import { User } from '@app/models/user';
import { Payments } from '@app/models/payment-options-info';
import { PaymentStoreService } from '@app/state/payment-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { Subscription } from 'rxjs';
import { SharedService } from '@app/modules/shared/shared.service';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { TranslateService } from '@ngx-translate/core';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { Config } from '@app/models/config';
@Component({
  selector: 'app-select-option',
  templateUrl: './option.component.html',
  styleUrls: ['./option.component.scss']
})
export class SelectionOptionComponent implements OnDestroy {

  state;
  payments: Payments[];
  user: User;
  pointLabel: string;
  translateParams: { [key: string]: string };
  translateParamsWithTitleCase: { [key: string]: string };
  disablePaymentPointsOnlyDescParams: { [key: string]: string };
  isStateValueChanged: boolean;
  config: Config;
  private subscriptions: Subscription[] = [];

  constructor(
    public cartService: CartService,
    public stateService: PaymentStoreService,
    public userStore: UserStoreService,
    private sharedService: SharedService,
    private translateService: TranslateService,
    private titleCasePipe: TitleCasePipe,
    private orderByPipe: OrderByPipe,
    private currencyFormatPipe: CurrencyFormatPipe
  ) {
    this.user = this.userStore.user;
    this.config = this.userStore.config;
    if (this.userStore.program.formatPointName !== '') {
      this.pointLabel = this.translateService.instant(this.userStore.program.formatPointName);
      this.disablePaymentPointsOnlyDescParams = {
        pointLabel: this.pointLabel,
        points: this.currencyFormatPipe.transform(this.user.points, this.config.pricingTemplate , this.user.locale)
      };
      this.translateParams = {
        pointLabel: this.pointLabel
      };
      this.translateParamsWithTitleCase = {
        pointLabel: this.titleCasePipe.transform(this.pointLabel)
      };
    }
    this.subscriptions.push(
      this.stateService.get().subscribe(data => {
        this.state =  data;
        if (this.state && this.state.selections && this.state.selections.payment) {
          if (data.selections.payment.name === 'splitpay' && !this.isStateValueChanged){
            this.removeDefaultSelect();
          }
        }
        this.payments = [];
        if (this.state) {
          let redemptions = [];

          if (this.state.redemptions) {
            redemptions = this.orderByPipe.transform(this.state.redemptions, 'asc', 'orderBy');
          }

          for (const redemption of redemptions) {
            const paymentOption = this.state['payments'][redemption.paymentOption];
            if (paymentOption) {
              this.payments.push(paymentOption);
            }
          }
          ({ params: this.translateParams, titleCaseParams: this.translateParamsWithTitleCase } = this.sharedService.getTranslateParams(this.translateParams, this.translateParamsWithTitleCase, this.state));
        }
      })
    );
  }

  selectionChanged() {
    this.sharedService.updateRedemptionOption(this.state.cart.id, this.state.selections.payment.name);
    this.stateService.set(this.state);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

  removeDefaultSelect(){
    if (this.state && this.state.selections && this.state.selections.payment) {
      delete this.state.selections.payment.splitPayOption;
      this.state.selections.payment.paySummaryTemplate = '';
      delete this.state.selections.payment.pointsToUse;
      delete this.state.selections.payment.cashToUse;
      this.isStateValueChanged = true;
      this.stateService.set(this.state);
    }
  }
}
