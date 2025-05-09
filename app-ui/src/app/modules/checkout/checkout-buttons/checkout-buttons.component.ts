import {Component, Input, OnInit, SimpleChanges} from '@angular/core';
import { Router } from '@angular/router';
import { CartItem } from '@app/models/cart';
import { PaymentLimit } from '@app/models/payment-limit';
import { MediaProduct } from '@app/models/media-product';
import { Config } from '@app/models/config';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { TemplateService } from '@app/services/template.service';
import { UserStoreService } from '@app/state/user-store.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { TransitionService } from '@app/transition/transition.service';
import { SessionService } from '@app/services/session.service';
import { ModalsService } from '@app/components/modals/modals.service';
import { CartService } from '@app/services/cart.service';
import { TemplateStoreService } from '@app/state/template-store.service';
import { TimedoutModalService } from '@app/services/timedout-modal.service';
import { IdologyLibService } from '@bakkt/idology-lib';
import { User } from '@app/models/user';
import { AcknowledgeTcModalComponent } from '@app/components/modals/acknowledge-tc-modal/acknowledge-tc-modal.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-checkout-buttons',
  templateUrl: './checkout-buttons.component.html',
  styleUrls: ['./checkout-buttons.component.scss'],
})
export class CheckoutButtonsComponent implements OnInit {
  negativeBalance: boolean;
  viewOnly: boolean;
  acknowledgeTcSelected: boolean;
  pricingTemplate: string;
  confirmOrder: object;
  paymentLimit: PaymentLimit;
  config: Config;
  user: User;
  isPageAccessible: boolean;
  checkoutButtonTemplate: object;
  templateType: string;
  buttonColor: string;
  enableAcknowledgeTermsConds: boolean;

  @Input() pointsAvailable: number;
  @Input() cartObjItems: Array<CartItem>;
  @Input() pointsUsed: number;
  @Input() remainingBalance: number;
  @Input() promotionalSubscription: object;
  @Input() purchasePaymentOption: string;
  @Input() totalPayment: number;
  @Input() pointsPurchase: number;
  @Input() public userAcknowledge;
  @Input() subscribedMediaProducts: Array<MediaProduct>;

  constructor(
    public templateService: TemplateService,
    private router: Router,
    public userStore: UserStoreService,
    private pricingService: PricingService,
    public sharedService: SharedService,
    private transitionService: TransitionService,
    private sessionService: SessionService,
    private modalService: ModalsService,
    private idologyService: IdologyLibService,
    private cartService: CartService,
    private bootstrapModal: NgbModal,
    public activeModal: NgbActiveModal,
    private templateStoreService: TemplateStoreService,
    private timeoutModelService: TimedoutModalService
  ) {
    this.acknowledgeTcSelected = false;
    this.config = this.userStore.config;
    this.user = this.userStore.user;
    this.enableAcknowledgeTermsConds = Boolean(this.userStore.program['enableAcknowledgeTermsConds']);
    this.viewOnly = this.config.viewOnly;
    this.checkoutButtonTemplate = this.templateStoreService.checkoutTemplate['checkoutButtons'];
    this.buttonColor = this.templateService.getBtnColor();
  }

  openAcknowledgeModal(type) {
    const acknowledgeTcModal = this.bootstrapModal.open(AcknowledgeTcModalComponent, {
      windowClass: 'acknowledge-tc-modal in',
      size: 'lg',
      backdropClass: 'in',
      centered: true
    });
    acknowledgeTcModal.componentInstance.config = this.config;
    acknowledgeTcModal.componentInstance.user = this.user;
    setTimeout(function (){
      document.querySelector( '.modal').scrollTo({top:0,behavior:'smooth'});
    }, 100);
    acknowledgeTcModal.result.then((ackResult) => {
      if (ackResult) {
        this.acknowledgeTcSelected = true;
        const transitionType = this.getTransitionType(type);
        this.transitionService.openTransition(transitionType);
        this.submitOrder(type);
      }
      else {
        this.acknowledgeTcSelected = false;
        this.router.navigate(['/store', 'checkout']);
      }
    }).catch(err => { });
    this.modalService.settop();
  }

  changeItemSelection() {
    this.router.navigate(['/store']);
  }

  checkTimedoutModal() {
    this.timeoutModelService.checkRemainingTime().subscribe(data => {
      this.timeoutModelService.showTimedoutModal(data);
      if (!data['timedOut']) {
        this.placeOrder('noPayment', null);
      }
    });
  }

  getTransitionType(type) {
    let transitionType;

    switch (type) {
      case 'noPayment': transitionType = 'processing'; break;
      case 'payment': transitionType = 'processing-cc'; break;
      case 'payroll': transitionType = 'transfer'; break;
      default: transitionType = 'loading'; break;
    }
    return transitionType;
  }

  placeOrder(type, event) {

    // if idology enabled, skip the order placement and open idology Modal
    if (this.config.idologyEnabled) {
      const transitionType = this.getTransitionType(type);
      this.transitionService.openTransition(transitionType);
      this.initializeIdology(type);
      return;
    }
    if (type === 'payroll') {
      // this.transferToPayroll();
    } else {
      this.checkTcSubmitOrder(type);
    }
  }

  checkTcSubmitOrder(type){
    if (this.enableAcknowledgeTermsConds && !this.acknowledgeTcSelected) {
      this.openAcknowledgeModal(type);
    }
    else {
      const transitionType = this.getTransitionType(type);
      this.transitionService.openTransition(transitionType);
      this.submitOrder(type);
    }
  }

  submitOrder(type) {
    const input = {
      isPromotionChecked: false,
    };

    // TODO
    // if(this.promotionalSubscription && (Object.keys(this.promotionalSubscription).length !== 0) && this.promotionalSubscription.hasOwnProperty('isChecked')){
    //     input.isPromotionChecked = this.promotionalSubscription.isChecked;
    // }

    if (type === 'payment') {
      const sessionPaymentApiDet = sessionStorage.getItem('paymentApiDet');
      const paymentApiDet = JSON.parse(sessionPaymentApiDet);
      input['transactionId'] = paymentApiDet?.transactionId;
    }

    this.pricingService.placeOrder(input).subscribe((response) => {
      const data = response.body;
      this.isPageAccessible = this.userStore.isPageAccessible('CONFIRMATION');
      if (this.isPageAccessible) {
        this.cartService.cartItemsTotalCount = 0;
        this.cartService.setCartItems({cartItems: [], error: null});
        let orderId;
        if (this.config.showVarOrderId) {
          orderId = data?.varOrderId;
        } else {
          orderId = data?.b2sOrderId;
        }
        this.confirmOrder = {
          items: this.cartObjItems,
          usedPoints: this.pointsUsed,
          orderID: orderId,
          remainingBalance: this.remainingBalance,
          points: this.pointsAvailable,
          paymentTemp: this.config.paymentTemplate,
          paymentType: type,
          promotionalSubscription: this.promotionalSubscription,
          purchasePaymentOption: this.purchasePaymentOption,
          purchasedPoints: type === 'payment' ? this.pointsPurchase : 0,
          totalPayment: type === 'payment' ? this.totalPayment : 0,
          subscribedMediaProducts: this.subscribedMediaProducts
        };

        sessionStorage.setItem(
          'confirmOrder',
          JSON.stringify(this.confirmOrder)
        );
        setTimeout(() => {
          this.router.navigate(['store/confirmation']);
          clearTimeout();
        }, 1500);
      } else {
        setTimeout(() => {
          this.router.navigate(['store/postback']);
          clearTimeout();
        }, 1500);
      }
    }, error => {
      this.transitionService.closeTransition();
      if (error.status === 401 || error.status === 0) {
        this.sessionService.showTimeout();
      } else {
        const promoUseExceeded = error.error.promotionUseExceeded;
        if (promoUseExceeded) {
          this.modalService.openOopsModalComponent('promoUseExceeded');
        } else if (error.error && error.error.errorCode) {
          this.modalService.openOopsModalComponent('placeOrderError', { errorCode: error.error.errorCode });
        } else {
          this.negativeBalance = true;
          this.modalService.openOopsModalComponent('placeOrderError');
        }
      }
    });
  }

  transferToPayroll() {}

  initializeIdology(type) {
    this.idologyService.setApiRootContext('apple-gr/services');
    this.idologyService.validatePerson().subscribe(res => {
      // let response = {"idNumber":"3138811100","questions":[{"prompt":"From whom did you purchase the property at 222333 PEACHTREE PLACE?","type":"purchased.property.from","answers":["JOE ANDERSON","CHRIS THOMAS","ELAINE RYAN","None of the above","Skip Question"]},{"prompt":"What type of residence is 222333 PEACHTREE PLACE?","type":"residence.type","answers":["Townhome","Apartment","Single Family Residence","None of the above","Skip Question"]},{"prompt":"What are the first two digits of your Social Security Number?","type":"ssn.digits.start","answers":["11","81","18","None of the above","Skip Question"]},{"prompt":"In which month were you born?","type":"month.of.birth","answers":["April","February","October","None of the above","Skip Question"]}],"status":"MATCH"};
      const data = res; // response = res.data
      if (data && data.questions && data.questions.length > 0) {
        this.modalService.openIdologyModalComponent(type, res);
        const unsubscribeIdologyService = this.modalService.triggerAfterClosed$.subscribe(popUpData => {
          if (popUpData) {
            if (type === 'payroll') {
              unsubscribeIdologyService.unsubscribe();
              this.transferToPayroll();
            } else {
              unsubscribeIdologyService.unsubscribe();
              this.submitOrder(type);
            }
          }
        });

      } else {
        if (type === 'payroll') {
          this.transferToPayroll();
        } else {
          this.submitOrder(type);
        }
      }
    }, error => {
      if (type === 'payroll') {
        this.transferToPayroll();
      } else {
        this.submitOrder(type);
      }
    });
  }

  amountZero() {
    return 0;
  }

  ngOnInit(): void {
    this.pricingTemplate = this.config.pricingTemplate;
    this.templateType = this.sharedService.isPayrollType() ? 'payroll' : (this.pricingTemplate === 'installment') ? 'installment' : 'points_cash';
  }
}
