import { Component, Injector, OnInit } from '@angular/core';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { TemplateStoreService } from '@app/state/template-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { OrderInformationService } from '@app/services/order-information.service';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { UserService } from '@app/services/user.service';
import { CartTotal } from '@app/models/cart';
import { DiscountCode } from '@app/models/discount-code';
import { MediaProduct } from '@app/models/media-product';
import { Messages } from '@app/models/messages';
import { OrderConfirm, OrderStatus } from '@app/models/order-detail';
import { Price } from '@app/models/price';
import { Program } from '@app/models/program';
import { Config } from '@app/models/config';
import { User } from '@app/models/user';
import { AddressService } from '@app/services/address.service';
import { TransitionService } from '@app/transition/transition.service';
import { DecimalPipe } from '@angular/common';
import { BreakPoint } from '@app/components/utils/break-point';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { AppConstants } from '@app/constants/app.constants';
import { ActivatedRoute } from '@angular/router';
import { HeapService } from '@app/analytics/heap/heap.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { formatNumber } from '@angular/common';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';

@Component({
  selector: 'app-confirmation',
  templateUrl: './confirmation.component.html',
  styleUrls: ['./confirmation.component.scss']
})

export class ConfirmationComponent extends BreakPoint implements OnInit {

  config: Config;
  user: User;
  messages: Messages;
  program: Program;
  orderConfirmUser: User;
  confirmationTemplate: { [key: string]: any };
  confirmOrder: OrderConfirm;
  currentOrderDate: number;
  savedSessionData: OrderConfirm;
  isOrderConfirm = true;
  dataLoadError = false;
  displayContinueShopping;
  payrollAgreementEnabled: boolean;
  employerManaged: boolean;
  showVarId: boolean;
  orderLocale: string;
  paymentTemplate: string;
  paymentInfo: string;
  purchasePaymentOption: string;
  orderData: OrderStatus;
  discounts: DiscountCode[];
  cartObjTotals: CartTotal;
  earnedPoints: number;
  showVarOrderId: boolean;
  pointsAvailable: number;
  pointsUsed: number;
  totalPayment: number;
  remainingBalance: number;
  // splitWithPurchase: boolean;
  paymentRequired: boolean;
  orderDate: number;
  onHoldTime: number;
  totalPurchase: number;
  orderOnHold: boolean;
  setEHFtitle: string;
  points: number;
  initialUserBalance: number;
  cartSubtotal: Price = {
    amount: 0,
    points: 0
  };
  shippingCost: Price = {
    amount: 0,
    points: 0
  };
  totalTaxes: Price = {
    amount: 0,
    points: 0
  };
  totalFees: Price = {
    amount: 0,
    points: 0
  };
  cartTotals: Price = {
    amount: 0,
    points: 0
  };
  isUnbundled: boolean;
  pricingOption: string;
  translateParams: { [key: string]: string };
  locale: string;
  mediumDate: string;
  subscribedMediaProducts: Array<MediaProduct>;
  pointsBeforePurchase: number;

  constructor(
    private messageStore: MessagesStoreService,
    private templateStoreService: TemplateStoreService,
    private orderInformationservice: OrderInformationService,
    private userService: UserService,
    private userStore: UserStoreService,
    public sharedService: SharedService,
    private pricingService: PricingService,
    private transitionService: TransitionService,
    private addressService: AddressService,
    private decimalPipe: DecimalPipe,
    private ensightenService: EnsightenService,
    public injector: Injector,
    private matomoService: MatomoService,
    private heapService: HeapService,
    private activatedRoute: ActivatedRoute
  ) {
    super(injector);
    this.messages = this.messageStore.messages;
    this.config = this.userStore.config;
    this.user = this.userStore.user;
    this.program = this.user.program;
    this.showVarId = this.config.showVarOrderId ? this.config.showVarOrderId : false;
    this.displayContinueShopping = this.config.displayContinueShopping;
    this.confirmationTemplate = this.templateStoreService.orderConfirmationTemplate;
    this.locale = this.user.locale.replace('_', '-');
    this.mediumDate = (this.config.mediumDate !== undefined) ? this.config.mediumDate : 'mediumDate';
  }

  ngOnInit(): void {

    const storedData = sessionStorage.getItem('confirmOrder');
    ({ option: this.pricingOption, isUnbundled: this.isUnbundled } = this.pricingService.getPricingOption());

    this.userService.getUser().toPromise().then(
      (data: User) => {
        /* TODO SUCCESS LOGIC... */
        this.orderConfirmUser = data;
        this.points = this.orderConfirmUser.balance;
        this.initialUserBalance = this.orderConfirmUser.initialUserBalance;
        this.orderLocale = this.user.locale.toLowerCase();
        this.orderConfirmUser.shipTo = this.addressService.decodeAddress(this.orderConfirmUser.shipTo);

        const ev = new CustomEvent('setPointsBalance', {
           detail: {
             formatted: formatNumber(data.balance, data.locale),
             integer: data.balance
           }
         });
        window.dispatchEvent(ev);

        if (storedData) {
          this.savedSessionData = JSON.parse(storedData);
          sessionStorage.removeItem('confirmOrder');
          // payment summary template variables
          this.paymentTemplate = this.savedSessionData.paymentTemp;
          this.purchasePaymentOption = this.savedSessionData.purchasePaymentOption;
          this.subscribedMediaProducts = this.savedSessionData.subscribedMediaProducts;
          this.pointsBeforePurchase = this.savedSessionData.points;
        } else {
          /* TODO ELSE BLOCK... */
          if (this.paymentTemplate === AppConstants.paymentTemplate.cash_subsidy) {
            this.continueShopping();
          } else {
            /* TODO ELSE BLOCK... */
            // timer = $timeout(function () {
            //   $rootScope.openTransition('failure');
            //   $timeout.cancel(timer);
            // }, 1200);
          }
        }

        return this.orderInformationservice.getOrderInformation().toPromise();
      },
      error => {
        /* TODO ERROR LOGIC... */
        /* if (error.status === 401 || error.status === 0) {
          sessionMgmt.showTimeout();
        } else {
          if ($scope.paymentTemplate === 'cash_subsidy') {
            $scope.continueShopping();
          } else {
            timer = $timeout(function () {
              $rootScope.openTransition('failure');
              $timeout.cancel(timer);
            }, 1200);
          }
        } */
      }
    ).then(
      (data: OrderStatus) => {
        /* TODO SUCCESS LOGIC... */
        this.orderData = data;
        // $rootScope.emptyCart = true;
        if (this.orderData !== undefined && Object.keys(this.orderData).length > 0) {
          this.confirmOrder = this.savedSessionData;
          this.confirmOrder['ccLast4'] = (this.orderData.last4) ? this.orderData.last4 : undefined;
          this.confirmOrder['contactInfo'] = {
            firstName: this.orderConfirmUser.firstName, lastName: this.orderConfirmUser.lastName
          };
          this.confirmOrder['shipAddress'] = this.orderConfirmUser.shipTo;
          this.confirmOrder['billAddress'] = this.orderConfirmUser.billTo ? this.orderConfirmUser.billTo : ( (this.confirmOrder.paymentType === 'noPayment') ? undefined : this.user.billTo );
          this.discounts = this.orderData.discountCodes;
          this.cartObjTotals = this.orderData.cartTotal;
          this.earnedPoints = this.orderData.earnedPoints;
          this.showVarOrderId = this.orderData.showVarOrderId;
          this.pointsAvailable = this.confirmOrder.points;
          this.pointsUsed = this.confirmOrder.usedPoints ? this.confirmOrder.usedPoints : this.confirmOrder.points - this.confirmOrder.remainingBalance;
          this.totalPayment = this.confirmOrder.totalPayment;
          this.paymentRequired = (this.confirmOrder.purchasedPoints > this.orderConfirmUser.balance);
          this.confirmOrder.orderID = (this.showVarOrderId) ? this.orderData.varOrderId : this.orderData.b2sOrderId;
          this.currentOrderDate = this.orderData.orderDate;

          // On hold bool and duration
          this.onHoldTime = this.orderData.orderHoldDurationInDays;
          this.orderOnHold = (this.onHoldTime !== undefined && this.onHoldTime > 0);
          this.transitionService.openTransition('success');
          this.translateParams = {onHoldTime  : this.decimalPipe.transform(this.onHoldTime)};

          // $rootScope.openTransition('success');

          // increased the time the success message displays because one second is not enough time for even a non disabled user to read it
          const timer = setTimeout(() => {
            this.transitionService.closeTransition();
            clearTimeout(timer);
          }, 2000);

          this.cartSubtotal = {
            amount: (this.isUnbundled === true || this.config.showCartSplitUp) ? (this.config.displayDiscountedItemPriceInPriceBreakdown && this.cartObjTotals.discountedItemsSubtotalPrice) ? this.cartObjTotals.discountedItemsSubtotalPrice.amount : this.cartObjTotals.itemsSubtotalPrice.amount : this.cartObjTotals.price.amount,
            points: (this.isUnbundled === true || this.config.showCartSplitUp) ? (this.config.displayDiscountedItemPriceInPriceBreakdown && this.cartObjTotals.discountedItemsSubtotalPrice) ? this.cartObjTotals.discountedItemsSubtotalPrice.points : this.cartObjTotals.itemsSubtotalPrice.points : this.cartObjTotals.price.points
          };
          this.shippingCost = {
            amount: (this.cartObjTotals.shippingPrice.amount) ? this.cartObjTotals.shippingPrice.amount : 0,
            points: (this.cartObjTotals.shippingPrice.points) ? this.cartObjTotals.shippingPrice.points : 0
          };
          if (this.cartObjTotals.hasOwnProperty('totalTaxes') && this.cartObjTotals.totalTaxes != null) {
            this.totalTaxes = {
              amount: (this.cartObjTotals.totalTaxes.amount) ? this.cartObjTotals.totalTaxes.amount : 0,
              points: (this.cartObjTotals.totalTaxes.points) ? this.cartObjTotals.totalTaxes.points : 0
            };
          }
          if (this.cartObjTotals.hasOwnProperty('totalFees') && this.cartObjTotals.totalFees != null) {
            this.totalFees = {
              amount: (this.cartObjTotals.totalFees.amount) ? this.cartObjTotals.totalFees.amount : 0,
              points: (this.cartObjTotals.totalFees.points) ? this.cartObjTotals.totalFees.points : 0
            };
          }
          this.cartTotals = {
            amount: this.cartObjTotals.price.amount,
            points: this.cartObjTotals.price.points
          };

          this.remainingBalance = this.confirmOrder.remainingBalance;

          // Discounted Cart Total
          if (this.cartObjTotals.hasOwnProperty('discountedPrice') && this.cartObjTotals.discountedPrice != null) {
            this.cartTotals = this.cartObjTotals.discountedPrice;
          }

          this.totalPurchase = (this.sharedService.isCashOnlyRedemption()) ? this.cartTotals.amount : this.totalPayment;

          this.paymentRequired = (this.totalPurchase > 0);

          // Set and show EHF and Tax disclaimer text - unused variable in template
          this.setEHFtitle = this.config.showFeeDetails === true ? this.messages.environmentalHandlingFee : '';
          const userAnalyticsObj = this.sharedService.getAnalyticsUserObject( this.activatedRoute.snapshot.data, this.orderConfirmUser.points);
          if (Object.keys(userAnalyticsObj).length > 0 ) {
            this.ensightenService.broadcastEvent(userAnalyticsObj, {
              products: this.confirmOrder.items,
              cashUsed: this.totalPayment,
              pointsUsed: this.pointsUsed,
              orderId: this.confirmOrder.orderID
            });
          }
          // Heap analytics
          this.heapService.broadcastEvent( AppConstants.analyticServices.HEAP_EVENTS.ORDER_SUCCESS, {
            payload: {
              products: this.confirmOrder,
              productTotals: this.cartObjTotals,
            }
          });

          // Heap Analytics
          this.heapService.broadcastEvent(AppConstants.analyticServices.HEAP_EVENTS.ORDER_LINE_PLACED, {
            payload: {
              products: this.confirmOrder,
              productTotals: this.cartObjTotals
            }
          });

          // matomo analytics
          this.matomoService.broadcast(AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.ORDER_SUCCESS, {
            payload: {
              products: this.confirmOrder.items,
              productTotals: this.cartObjTotals,
              orderId: this.confirmOrder.orderID
            }
          });

          setTimeout(() => {
            this.matomoService.broadcast(AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.ENGRAVING, {
              payload: {
                cartItems: this.confirmOrder.items
              }
            });
          });
         } else {
          /* TODO ERROR LOGIC... */
          // if ($scope.paymentTemplate === 'cash_subsidy') {
            /* TODO IF BLOCK... */
          //   $scope.continueShopping();
          // } else {
            /* TODO ELSE BLOCK... */
          //   timer = $timeout(function () {
          //     $rootScope.openTransition('failure');
          //     $timeout.cancel(timer);
          //   }, 1200);
          // }
        }
      },
      error => {
        /* TODO ERROR LOGIC... */
        // $rootScope.emptyCart = false;
        // if ($scope.paymentTemplate === 'cash_subsidy') {
          /* TODO IF BLOCK... */
        //     $scope.continueShopping();
        // } else {
          /* TODO ELSE BLOCK... */
        //     timer = $timeout(function () {
        //         $rootScope.openTransition('failure');
        //         $interval.cancel($rootScope.pageLoader);
        //         $timeout.cancel(timer);
        //     }, 1200);
        // }
      }
    );
  }

  continueShopping() {
    /* TODO continueShopping FUNCTION... */
  }

  hasDiscounts() {
    return (this.discountedSubtotal() ? this.discountedSubtotal()['discountAmt'] > 0 : false);
  }

  discountedSubtotal() {
    return this.sharedService.getProperty('discountedSubtotal');
  }

}
