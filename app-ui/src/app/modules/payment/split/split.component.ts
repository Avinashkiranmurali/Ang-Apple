import {
  Component,
  OnInit,
  OnDestroy,
  Injector,
  ViewChild
} from '@angular/core';
import { TitleCasePipe } from '@angular/common';
import { NgForm, NgModel } from '@angular/forms';
import { Router } from '@angular/router';
import { BreakPoint } from '@app/components/utils/break-point';
import { CartService } from '@app/services/cart.service';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { PaymentService } from '@app/services/payment.service';
import { PaymentStoreService } from '@app/state/payment-store.service';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { UserStoreService } from '@app/state/user-store.service';
import { User } from '@app/models/user';
import { Program } from '@app/models/program';
import { Config } from '@app/models/config';
import { TranslateService } from '@ngx-translate/core';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { Subject, Subscription } from 'rxjs';
import { retryWhen, tap } from 'rxjs/operators';
import { SessionService } from '@app/services/session.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';

@Component({
  selector: 'app-split',
  templateUrl: './split.component.html',
  styleUrls: ['./split.component.scss']
})

export class SplitComponent extends BreakPoint implements OnInit, OnDestroy {

  displayOrderSummOnMobile: boolean;
  isUnbundled: boolean;
  messages;
  user: User;
  state;
  program: Program;
  config: Config;
  pointLabel: string;
  translateParams: { [key: string]: string };
  translateParamsWithTitleCase: { [key: string]: string };
  payFrequency: string;
  pointsToUse = 0;
  pointsEntryPattern = new RegExp('^((?!e)[0-9]*)(?!-0(\.0+)?(e|$))-?(0|[1-9]\d*)(\.\d+)?(e-?(0|[1-9]\d*))?$', 'i');
  decimalPointsEntryPattern = new RegExp('^((?!e)[0-9]?(\.\d{0,2}?)*)(?!-0(\.0+)?(e|$))-?(0|[1-9]\d*)(\.\d+)?(e-?(0|[1-9]\d*))?$', 'i');
  inputStream$: Subject<number>;
  isPointsDecimalPricingTemplate = false;

  @ViewChild('splitPaymentsForm') public splitPaymentsForm: NgForm;
  @ViewChild('pointsToUseField') public pointsToUseField: NgModel;

  private subscriptions: Subscription[] = [];
  selectedPaymentType: string;
  private buttonSpinner: boolean;

  constructor(
    public injector: Injector,
    public messageStore: MessagesStoreService,
    public stateService: PaymentStoreService,
    private pricingService: PricingService,
    public userStore: UserStoreService,
    private translateService: TranslateService,
    private titleCasePipe: TitleCasePipe,
    private currencyPipe: CurrencyPipe,
    private cartService: CartService,
    private router: Router,
    private paymentService: PaymentService,
    private ensightenService: EnsightenService,
    private sessionService: SessionService,
    private sharedService: SharedService
  ) {
    super(injector);
    this.displayOrderSummOnMobile = false;
    this.messages = this.messageStore.messages;
    this.user = this.userStore.user;
    this.program = this.userStore.program;
    this.config = this.userStore.config;
    this.isPointsDecimalPricingTemplate = this.config.pricingTemplate === 'points_decimal';
    this.buttonSpinner = false;

    if (this.program.formatPointName !== '') {
      this.pointLabel = this.translateService.instant(this.program.formatPointName);
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
        let rewardsToUse = 0;

        if (this.state.cart) {
          let points: number;
          let useMaxPoints: number;

          if (this.state.cart.cartTotal) {
            points = this.state.cart.cartTotals.points;
          }
          if (this.state.cart.redemptionPaymentLimit && this.state.cart.redemptionPaymentLimit.useMaxPoints) {
            useMaxPoints = this.state.redemptionPaymentLimit.useMaxPoints.points;
          }
          if (points && useMaxPoints) {
            this.pointsToUse = (points <= useMaxPoints) ? points : useMaxPoints;
            rewardsToUse = points - this.pointsToUse;
          }
        }
        ({ params: this.translateParams, titleCaseParams: this.translateParamsWithTitleCase } = this.sharedService.getTranslateParams(this.translateParams, this.translateParamsWithTitleCase, this.state));
        if (this.state.selections && this.state.selections.payment && this.state.selections.payment.splitPayOption) {
          if (this.state.selections.payment.splitPayOption.subView?.name === 'useCustomPoints') {
            this.state.selections.payment.splitPayOption.pointsToUse = this.pointsToUse;
            this.state.selections.payment.pointsToUse = this.pointsToUse;
            this.state.selections.payment.splitPayOption.isValid = true;
            this.inputStream$ = this.paymentService.getPurchasePointsObservable();
            this.subscriptions.push(
              this.inputStream$.pipe(retryWhen((errorObservable) => errorObservable.pipe(tap(error => {
                  this.buttonSpinner = false;
                  this.state.selections.payment.splitPayOption.isValid = false;
                  if (error.status === 401 || error.status === 0 || error.status === 403) {
                    this.sessionService.showTimeout();
                  }
                  })))).subscribe((cashAmount) => {
                /**
                 * only set the amount to valid when user stops changing the amount
                 * and we know the response has returned
                 */
                this.buttonSpinner = false;
                if (this.pointsToUse !== null && this.pointsToUse !== undefined && this.pointsToUseField && !this.pointsToUseField.errors) {
                  this.state.selections.payment.splitPayOption.isValid = true;
                }
                this.state.selections.payment.splitPayOption.cashToUse = cashAmount;
                this.state.selections.payment.cashToUse = cashAmount;

                if (this.state.selections.payment.splitPayOption.cashToUse > 0) {
                  this.state.selections.payment.splitPayOption.isPaymentRequired = true;
                  this.state.selections.payment.splitPayOption.nextStepBtnLabel = 'nextStepPayment';
                } else {
                  this.state.selections.payment.splitPayOption.isPaymentRequired = false;
                  this.state.selections.payment.splitPayOption.nextStepBtnLabel = 'nextStepReviewYourOrder';
                }
              })
            );
            this.inputStream$.next(rewardsToUse);
          }
        }
      })
    );
    const pricingOptions = this.pricingService.getPricingOption();
    this.isUnbundled = pricingOptions['isUnbundled'];
  }

  ngOnInit(): void {
    this.payFrequency = this.config['payFrequency'] || '';

    // Analytics object
    const userAnalyticsObj = {
      pgName: '',
      pgType: '',
      pgSectionType: ''
    };
    this.ensightenService.broadcastEvent(userAnalyticsObj, []);
  }

  nextBtnMsg(): string {
    let buttonMsg = '';
    if (this.state.selections && this.state.selections.payment) {
      if (this.state.selections.payment.splitPayOption === undefined) {
        buttonMsg = 'nextStepDefault';
        return this.translateService.instant(buttonMsg, {});
      }
      else if (this.buttonSpinner) {
        buttonMsg = 'buttonSpinner';
      }
      else if (this.splitPaymentsForm && !this.splitPaymentsForm.form.valid) {
        buttonMsg = 'nextStepDefault';
      }
      else if (this.state.selections.payment.splitPayOption) {
        buttonMsg = this.state.selections.payment.splitPayOption.nextStepBtnLabel || '';
      }
    }
    if (this.config.showDecimal) {
      return buttonMsg.length === 0 ? '' : this.translateService.instant(buttonMsg, { cashToUse: this.currencyPipe.transform(this.state.selections.payment.splitPayOption.cashToUse) });
    } else {
      return buttonMsg.length === 0 ? '' : this.translateService.instant(buttonMsg, { cashToUse: this.currencyPipe.transform(this.state.selections.payment.splitPayOption.cashToUse, '', 'symbol', '1.0-0') });
    }
  }

  backToPaymentPage() {
    delete this.state.selections.payment.splitPayOption;
    this.state.selections.payment.paySummaryTemplate = '';
    delete this.state.selections.payment.pointsToUse;
    delete this.state.selections.payment.cashToUse;
    this.stateService.set(this.state);
    this.router.navigate(['/store', 'payment']);
  }

  submitPaymentDetails() {

    if (this.state.selections.payment?.splitPayOption?.subView?.name === 'useCustomPoints'){
      this.selectedPaymentType = this.translateService.instant('selectedPaymentType', { pointLabel: this.messages[this.program.formatPointName]});
    }

    // TODO submitPaymentDetails
    const rewardsToUse = this.state.cart.cartTotals.points - this.state.selections.payment.pointsToUse;
    this.cartService.addPurchasePoints(rewardsToUse).subscribe((response) => {
      if (response && response.cashAmount > 0) {
        this.router.navigate(['/store', 'payment', 'card'], {skipLocationChange: true});
      } else {
        sessionStorage.removeItem('paymentApiDet');
        this.router.navigate(['/store', 'checkout']);
      }
    },  (error) => {
    });
  }

  toggleOrderSummary(event) {
    this.displayOrderSummOnMobile = !this.displayOrderSummOnMobile;
  }

  splitPointsChanged(customValue) {

    // FORMAT CUSTOM POINTS VALUE
    if (customValue && this.isPointsDecimalPricingTemplate) {
      const regExp = /^\d+(\.\d{0,2})?$/;
      if (!regExp.test(customValue)) {
        this.pointsToUseField.control.patchValue(this.state.selections.payment.pointsToUse / 100);
        return false;
      }
      this.pointsToUse = parseInt(customValue.toFixed(2).replace('.', ''), 10);
    } else {
      this.pointsToUse = customValue;
    }

    if (this.state.selections.payment.pointsToUse === this.pointsToUse) {
      return false;
    }

    this.state.selections.payment.splitPayOption.isValid = false;
    this.state.selections.payment.splitPayOption.pointsToUse = this.pointsToUse;
    this.state.selections.payment.pointsToUse = this.pointsToUse;
    /**
     * This block is important because it stops the flow until errors are cleared
     * handle these errors in other components using the following:
     * this.state.selections.payment.splitPayOption.isValid
     * ['selectPaymentsForm'] $valid state
     */
    const rewardsToUse = this.state.cart.cartTotals.points - this.pointsToUse;
    this.buttonSpinner = true;
    this.inputStream$.next(rewardsToUse);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }
}
