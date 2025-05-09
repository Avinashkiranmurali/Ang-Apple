import { Component, Injector, Input, OnDestroy, OnInit } from '@angular/core';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { TemplateStoreService } from '@app/state/template-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { CartService } from '@app/services/cart.service';
import { TemplateService } from '@app/services/template.service';
import { ActivatedRoute, Router } from '@angular/router';
import { AdditionalInfo } from '@app/models/additional-info';
import { Address } from '@app/models/address';
import { Config } from '@app/models/config';
import { Cart, CartItem, CartTotal } from '@app/models/cart';
import { CreditItem } from '@app/models/credit-item';
import { RedemptionPaymentLimit  } from '@app/models/payment-limit';
import { MediaProduct } from '@app/models/media-product';
import { Messages } from '@app/models/messages';
import { User } from '@app/models/user';
import { Subscription } from 'rxjs';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { Price } from '@app/models/price';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { Program } from '@app/models/program';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { AppConstants } from '@app/constants/app.constants';
import { KeyStoneSyncService } from '@app/services/key-stone-sync.service';
import { SessionService } from '@app/services/session.service';
import { ModalsService } from '@app/components/modals/modals.service';
import { TransitionService } from '@app/transition/transition.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { BreakPoint } from '@app/components/utils/break-point';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.scss']
})

export class CheckoutComponent extends BreakPoint implements OnInit, OnDestroy {
  user: User;
  config: Config;
  messages: Messages;
  checkoutTemplate: object;
  cartData: Cart;
  cartObjTotals: CartTotal;
  displayCartTotal: CartTotal;
  cartObjItems: Array<CartItem>;
  checkoutAddress: Address;
  creditItem: CreditItem;
  earnPoints: number;
  balancePoints: number;
  selectedRedemptionOption: string;
  purchasePaymentOption: string;
  pointsAvailable: number;
  itemPricing: AdditionalInfo;
  isPointsOnlyRewards: boolean;
  redemptionPaymentLimit: RedemptionPaymentLimit;
  paymentRequired: boolean;
  paymentLimit = {
    minNotMet: false,
    maxExceed: false
  };
  cartUserAdds: number;
  isCartActual: boolean;
  cost: number;
  negativeBalance = false;
  eppStatus: boolean;
  setCartTaxFeeTitle: string;
  selectedPaymentOpt: string;
  buttonColor: string;
  showCartSplitUp: boolean;
  shippingCost: Price = {
    amount: 0,
    points: 0,
    currencyCode: ''
  };
  @Input() cartSubtotal?: Price = {
    amount: 0,
    points: 0,
    currencyCode: ''
  };
  totalTaxes: Price = {
    amount: 0,
    points: 0,
    currencyCode: ''
  };
  totalFees: Price = {
    amount: 0,
    points: 0,
    currencyCode: ''
  };
  cartTotals: Price = {
    amount: 0,
    points: 0,
    currencyCode: ''
  };
  isUnbundled: boolean;
  pricingOption: string;
  private subscriptions: Subscription[] = [];
  pointsUsed: number;
  totalPayment: number;
  promotionalSubscription: object = {};
  remainingBalance: number;
  pointsPurchase: number;
  translateParams: { [key: string]: string };
  showFeeDetails: boolean;
  showTaxDisclaimer: boolean;
  points: number;
  program: Program;
  policiesTermsContent: string;
  paymentTemplate;
  minPurchaseTotal;
  maxPurchaseTotal;
  suppPaymentPctMaxLimit;
  suppRewardsPctMinLimit;
  analyticsUserObject: any;
  subscribedMediaProducts: Array<MediaProduct>;
  enableAcknowledgeTermsConds: boolean;
  constructor(
    private activateRoute: ActivatedRoute,
    private messageStore: MessagesStoreService,
    private cartService: CartService,
    private templateStoreService: TemplateStoreService,
    private userStore: UserStoreService,
    private templateService: TemplateService,
    private pricingService: PricingService,
    private currencyPipe: CurrencyPipe,
    private ensightenService: EnsightenService,
    private matomoService: MatomoService,
    private keyStoneSyncService: KeyStoneSyncService,
    private sessionService: SessionService,
    private modalService: ModalsService,
    private router: Router,
    private transitionService: TransitionService,
    public sharedService: SharedService,
    public injector: Injector
  ) {
    super(injector);
    this.user = this.userStore.user;
    this.program = this.user.program;
    this.config = this.userStore.config;
    this.messages = this.messageStore.messages;
    this.checkoutTemplate = this.templateStoreService.checkoutTemplate;
    this.showCartSplitUp = this.config.showCartSplitUp;
    this.showFeeDetails = this.config.showFeeDetails;
    this.showTaxDisclaimer = this.config.showTaxDisclaimer;
    this.buttonColor = this.templateService.getBtnColor();
    this.points = this.user.balance;
    this.eppStatus = this.config.epp ? this.config.epp : false;
    this.analyticsUserObject = {};
    this.enableAcknowledgeTermsConds = Boolean(this.userStore.program['enableAcknowledgeTermsConds']);
  }

  ngOnInit(): void {
    this.getCart();
    this.paymentTemplate = this.config.paymentTemplate;
    this.policiesTermsContent = this.msgWithArgs(this.messages.policiesTermsContent, this.messages.termUrl);

    if (this.keyStoneSyncService.isKeyStoneSync('balanceUpdate')) {
       this.subscriptions.push(this.sessionService.getSessionURLs().subscribe(data => {
          const initData = data;
          if (initData.hasOwnProperty('updatedPointsBalance')){
              this.keyStoneSyncService.setHeaderBalance(initData.updatedPointsBalance);
              this.modalService.openOopsModalComponent('placeOrderError');
          }
      }));
    }
    this.analyticsUserObject = this.sharedService.getAnalyticsUserObject(this.activateRoute.snapshot.data);
  }

  hasDiscounts() {
    return (this.discountedSubtotal() ? this.discountedSubtotal()['discountAmt'] > 0 : false);
  }

  discountedSubtotal() {
    return this.sharedService.getProperty('discountedSubtotal');
  }

  // update subtotal when any change occurs
  setCartSubtotal(data) {
    this.negativeBalance = false;
    const pricingData = this.sharedService.getPriceData(data);
    this.cartSubtotal = (this.isUnbundled === true || this.showCartSplitUp) ? (this.config.displayDiscountedItemPriceInPriceBreakdown && data.discountedItemsSubtotalPrice) ?
        {amount: data.discountedItemsSubtotalPrice.amount, points: data.discountedItemsSubtotalPrice.points} :
        {amount: data.itemsSubtotalPrice.amount, points: data.itemsSubtotalPrice.points} :
      {amount: data.price.amount, points: data.price.points};
    const discountParams = pricingData.discountParams;
    this.sharedService.setProperty('discountedSubtotal', discountParams);
    this.shippingCost = pricingData.shippingCost;
    this.totalFees = pricingData.totalFees;
    this.totalTaxes = pricingData.totalTaxes;
    this.cartTotals = pricingData.cartTotals;
    this.totalPayment = this.sharedService.isPointsOnlyRewards() ? 0 : this.cost;
    this.isPointsOnlyRewards = this.sharedService.isPointsOnlyRewards();
    this.remainingBalance = this.balancePoints;
    if (this.sharedService.isCashOnlyRedemption() && this.cost > 0) {
      this.paymentRequired = true;
    }
    if (this.config.paymentTemplate === AppConstants.paymentTemplate.cash_subsidy) {
      this.cartSubtotal = this.displayCartTotal.itemsSubtotalPrice;
      this.cartTotals = this.displayCartTotal.price;
    }
  }

  getCart(): void {
    this.transitionService.openTransition();
    this.subscriptions.push(
      this.cartService.getCart().subscribe(
        data => {
          this.transitionService.closeTransition();
          this.cartData = data;
          this.subscribedMediaProducts = this.cartData.subscriptions ? this.cartData.subscriptions.filter(item => item.addedToCart) : [];
          this.creditItem = this.cartData.creditItem;
          this.balancePoints = this.cartData.pointsBalance;

          this.selectedRedemptionOption = this.cartData.selectedRedemptionOption;
          if (!this.config.fullCatalog) { // Call this only for non fullCatalog which is not having access for CART page.
            /* TODO IF BLOCK */
            this.matomoService.broadcast(AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.UPDATE_CART, { payload: data });
          }
          this.earnPoints = this.cartData.earnPoints;
          this.promotionalSubscription = (this.cartData && this.cartData['promotionalSubscription'] && Object.keys(this.cartData['promotionalSubscription']).length !== 0) ? this.cartData['promotionalSubscription'] : {
            displayCheckbox: false, // if true display the checkbox with text, if false don't display the checkbox | these are just default values if the object isn't sent
            isChecked: false // if true checkbox should be in checked status, if false not checked status | these are just default values if the object isn't sent
          };

          ({ option: this.pricingOption, isUnbundled: this.isUnbundled } = this.pricingService.getPricingOption());
          this.purchasePaymentOption = this.cartData.paymentType;

          if (this.cartData.cartItems.length > 0) {
            this.cartObjTotals = this.cartData.cartTotal;
            this.cartObjItems = this.cartData.cartItems;
            this.displayCartTotal = this.cartData.displayCartTotal;

            // specific for single item purchase
            const item = this.cartData.cartItems[0];
            this.itemPricing = item.productDetail.additionalInfo.PricingModel;

            // Payment values and payment Terms Translateing block
            this.translateParams = {
              repaymentTerm: (this.itemPricing && this.itemPricing.repaymentTerm) ? this.itemPricing.repaymentTerm : 0,
              paymentValue: this.currencyPipe.transform((this.itemPricing && this.itemPricing.paymentValue) ? this.itemPricing.paymentValue : 0 )
            };
            this.paymentLimit = this.cartData.paymentLimit ? this.cartData.paymentLimit : {
              minNotMet: false,
              maxExceed: false
            };
            this.cartUserAdds = this.cartData.addPoints;
            this.pointsUsed = this.cartData.pointsPayment;
            this.isCartActual = this.cartData.cartTotal.actual;
            this.cost = this.cartData.cost;
            // this.pointsAvailable = this.cartData.pointsBalance;
            this.pointsAvailable = this.user.balance;
            this.checkoutAddress = this.cartData.shippingAddress;

            // EPP only
            if (this.eppStatus) {
              /* TODO IF BLOCK */
              this.selectedPaymentOpt = this.cartData.selectedPaymentOption;
            }
            // discount bool
            if (this.cartData.discounts && this.cartData.discounts.length > 0) {
              this.sharedService.setProperty('hasDiscounts', true);
            }

            this.setCartSubtotal(this.cartObjTotals);
            this.setCartTaxFeeTitle = this.isUnbundled === false ? (this.isCartActual === true ? this.messages.taxFeeIncluded : this.messages.taxFeeNotIncluded) : (this.showTaxDisclaimer ? this.messages.taxFeeNotIncluded : '');
          } else {
            this.router.navigate(['/store/cart']);
          }
          this.ensightenService.broadcastEvent(this.analyticsUserObject, this.cartObjItems);
        },
        // eslint-disable-next-line @typescript-eslint/no-shadow
        error => {
          this.transitionService.closeTransition();
          /* TODO ERROR LOGIC */
          if (error.status === 401 || error.status === 0) {
            /* TODO IF BLOCK */
            // $rootScope.closeTransition();
            // sessionMgmt.showTimeout();
          } else {
            /* TODO ELSE BLOCK */
            this.cartObjItems = [];
            // this.cartObjTotals = {};
            // $state.go('store.cart', {}, {reload: true});
          }
        }
      ));
  }
  msgWithArgs(msg: string, args: string) {
    if (msg !== undefined) {
      msg = msg.split('{0}').join(args);
    }
    return msg;
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

}
