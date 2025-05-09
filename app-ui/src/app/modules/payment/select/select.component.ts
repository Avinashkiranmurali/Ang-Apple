import { Component, OnDestroy, Injector, OnInit } from '@angular/core';
import { TitleCasePipe } from '@angular/common';
import { BreakPoint } from '@app/components/utils/break-point';
import { CartService } from '@app/services/cart.service';
import { User } from '@app/models/user';
import { PaymentStoreService } from '@app/state/payment-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { Subscription } from 'rxjs';
import { SharedService } from '@app/modules/shared/shared.service';
import { Router } from '@angular/router';
import { Config } from '@app/models/config';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { TranslateService } from '@ngx-translate/core';
import { ModalsService } from '@app/components/modals/modals.service';
import { TransitionService } from '@app/transition/transition.service';
import { Cart } from '@app/models/cart';

@Component({
  selector: 'app-select',
  templateUrl: './select.component.html',
  styleUrls: ['./select.component.scss']
})

export class SelectComponent extends BreakPoint implements OnInit, OnDestroy {

  displayOrderSummOnMobile: boolean;
  isUnbundled;
  state;
  user: User;
  config: Config;
  payFrequency: string;
  showCartSplitUp;
  paymentRequired: boolean;
  pointsUsed: number;
  pointsAvailable: number;
  pointLabel: string;
  translateParams: { [key: string]: string };
  translateParamsWithTitleCase: { [key: string]: string };
  private subscriptions: Subscription[] = [];

  constructor(
    public injector: Injector,
    public cartService: CartService,
    public stateService: PaymentStoreService,
    public userStore: UserStoreService,
    private sharedService: SharedService,
    private route: Router,
    private pricingService: PricingService,
    private translateService: TranslateService,
    private titleCasePipe: TitleCasePipe,
    private modalsService: ModalsService,
    private transitionService: TransitionService
  ) {
    super(injector);
    this.user = this.userStore.user;
    this.config = this.userStore.config;
    this.showCartSplitUp = this.user.program.config.showCartSplitUp ? this.user.program.config.showCartSplitUp : false;
    const pricingOptions = this.pricingService.getPricingOption();
    this.isUnbundled = pricingOptions['isUnbundled'];
    if (this.userStore.program.formatPointName !== '') {
      this.pointLabel = this.translateService.instant(this.userStore.program.formatPointName);
      this.translateParams = {
        pointLabel: this.pointLabel
      };
      this.translateParamsWithTitleCase = {
        pointLabel: this.titleCasePipe.transform(this.pointLabel)
      };
    }
  }

  ngOnInit(): void {
    this.state =  this.stateService.getInitial();
    setTimeout(() => {
      this.getProgram();
    }, 120);

    this.payFrequency = this.config['payFrequency'] || '';
  }

  setStates(data) {
    this.state = data;
    // scotia : Move the page to card page if user has eligible for pay
    if (this.state.paymentInfo && this.sharedService.isPointsFixed() && this.state.cart) {
      if (this.state.redemptionPaymentLimit.cashMaxLimit && this.state.redemptionPaymentLimit.cashMaxLimit.amount && this.state.cart.cost <= this.state.redemptionPaymentLimit.cashMaxLimit.amount && this.state.cart.cost !== 0) {
        // Do the selection page Selection
        this.state.selections.payment = this.state.payments['splitpay'];
        this.state.selections.payment.splitPayOption = this.state.selections.payment.splitPayOptions['useMaxPoints'];
        this.state = this.sharedService.updateSplitPayOption(this.state);
        this.sharedService.updateRedemptionOption(this.state.cart.id, this.state.selections.payment.name);
        this.route.navigate(['/store', 'payment', 'card'], {skipLocationChange: true});
      }
    } else if (this.state.paymentInfo && this.sharedService.isCashOnlyRedemption() && this.state.cart && this.state.cart.cost > 0) {
      this.state.selections.payment = this.state.payments['cashonly'];
      this.route.navigate(['/store', 'payment', 'card'], {skipLocationChange: true});
    }
  }

  getCart() {
    let tempState;
    this.stateService.get().subscribe(data => {
      tempState =  data;
      if (data && data.payments) {
        this.setStates(data);
      }
    });
    this.transitionService.openTransition();
    this.subscriptions.push(
      this.cartService.getCart().subscribe((cartDetails: Cart) => {
        this.transitionService.closeTransition();
        if (cartDetails.cartItems.length === 0 ) {
          this.route.navigate(['/store/cart']);
          return;
        }
        const cartObjTotals = cartDetails.cartTotal;
        cartDetails['cartTotals'] = {amount: cartObjTotals.price.amount, points: cartObjTotals.price.points};
        const discountedTotal = cartObjTotals.discountedPrice;
        if (discountedTotal) {
          cartDetails['cartTotals'] = {amount: discountedTotal.amount, points: discountedTotal.points};
        }
        tempState['cart'] = cartDetails;
        tempState['paymentInfo'] = {};
        tempState['paymentInfo']['invalidCartTotal'] = false;
        tempState['paymentInfo']['pointsSplitUnavailable'] = false;
        // Logic to find the min and max total
        tempState['redemptionPaymentLimit'] = cartDetails.redemptionPaymentLimit ? cartDetails.redemptionPaymentLimit : {};

        if (tempState['payments']['pointsonly']){
          tempState['payments']['pointsonly'].isDisabled = this.user.points < cartDetails['cartTotals']['points'];
        }
        if (tempState['payments']['splitpay']){
          tempState['payments']['splitpay']['isDisabled'] = false;
          if (tempState['redemptionPaymentLimit'].useMinPoints && tempState['redemptionPaymentLimit'].useMinPoints.points && tempState['redemptionPaymentLimit']['useMinPoints'].points > this.user.points){
            tempState['paymentInfo'].invalidCartTotal = true;
            tempState['payments']['splitpay'].isDisabled = true;
            if (this.user.points > 0){
              tempState['paymentInfo'].pointsSplitUnavailable = true;
            }
          }
        }
        // point fixed validation
        if (tempState['redemptionPaymentLimit'].cashMaxLimit && tempState['redemptionPaymentLimit'].cashMaxLimit.amount && cartDetails.cost > tempState['redemptionPaymentLimit'].cashMaxLimit.amount){
          tempState['paymentInfo'].invalidCartTotal = true;
        }

        if (this.sharedService.isPointsFixed() && tempState['paymentInfo'].invalidCartTotal){
          tempState['paymentInfo'].pointsSplitUnavailable = true;
          for (const payment of Object.keys(tempState['payments'])){
            tempState['payments'][payment].isDisabled = true;
          }
        }
        this.stateService.set(tempState);
        ({ params: this.translateParams, titleCaseParams: this.translateParamsWithTitleCase } = this.sharedService.getTranslateParams(this.translateParams, this.translateParamsWithTitleCase, this.state));
        this.pointsAvailable = this.state.cart.pointsBalance;
        this.paymentRequired = (this.state.cart.cartTotal.price.points > this.pointsAvailable);

        const addPoints = this.state.cart.addPoints ? this.state.cart.addPoints : 0;
        this.pointsUsed = this.paymentRequired ? this.pointsAvailable : this.state.cart.cartTotal.price.points - addPoints;
      },
      error => {
        this.transitionService.closeTransition();
      }));
  }

  toggleOrderSummary(event) {
    this.displayOrderSummOnMobile = !this.displayOrderSummOnMobile;
  }

  getProgram() {
    const tempState = this.state;
    const redemptionOptions = this.user && this.user.program && this.user.program.redemptionOptions;
    // update redemptionOptions once received from BE
    // Combine RedemptionOption(BE) and PaymentOption(UI) objects
    const updateRedemptionOptions = () => {
      const payments = {};
      const redemptions = [];
      for (const redemptionOption of Object.keys(redemptionOptions)){
         payments[redemptionOption] = tempState.payments[redemptionOption];
         payments[redemptionOption].redemptionOptions = redemptionOptions[redemptionOption];
         redemptions.push(payments[redemptionOption].redemptionOptions[0]);
       }
      tempState['payments']  = payments;
      tempState['redemptions'] = redemptions;
      tempState.selections = {};
    };
    updateRedemptionOptions();
    this.stateService.set(tempState);
    this.getCart();
  }

  openConsentForm() {
    this.modalsService.openConsentFormComponent();
    return true;
  }

  routeValidation() {
    const selectedOption = this.state.selections.payment;
    if (selectedOption.name === 'pointsonly') {
      this.cartService.addPurchasePoints(0).subscribe(response => {
        // sessionStorage.removeItem('paymentApiDet'); @todo: analyze about session storage
      });
    } else if (selectedOption.name === 'cashonly') {
      this.cartService.addPurchasePoints(this.state.cart.cartTotals.points).subscribe(response => {
        // SUCCESS LOGIC
      });
    }
    if (this.config.consentForm && ['splitpay', 'cashonly', 'splitpay_finance', 'cashonly_finance'].indexOf(this.state.selections.payment.name) >= 0){
      this.openConsentForm();
      return false;
    }
    return true;
  }

  routeChange() {
    if (this.routeValidation()) {
      this.route.navigate(this.state.selections.payment.actionPanel.nextStep, {skipLocationChange: this.state.selections.payment.actionPanel.skipLocationChange });
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }
}
