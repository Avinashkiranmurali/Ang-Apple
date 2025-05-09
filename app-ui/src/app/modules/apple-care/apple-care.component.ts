import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { Config } from '@app/models/config';
import { Program } from '@app/models/program';
import { User } from '@app/models/user';
import { Messages } from '@app/models/messages';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AppleCareModalComponent } from '@app/components/modals/apple-care-modal/apple-care-modal.component';
import { ModalsService } from '@app/components/modals/modals.service';
import { CartService } from '@app/services/cart.service';
import { CartItem } from '@app/models/cart';
import { Product } from '@app/models/product';
import { SessionService } from '@app/services/session.service';
import { NotificationRibbonService } from '@app/services/notification-ribbon.service';
import { TransitionService } from '@app/transition/transition.service';

@Component({
  selector: 'app-apple-care',
  templateUrl: './apple-care.component.html',
  styleUrls: ['./apple-care.component.scss']
})
export class AppleCareComponent implements OnInit, OnDestroy {

  @Input() appleCareServicePlans: Array<Product>;
  @Input() selectedServicePlan: CartItem;
  @Input() cartItem: CartItem;
  @Input() giftItem: CartItem;
  @Input() readOnly: boolean;
  @Input() isGiftView: boolean;
  @Input() isProductView: boolean;
  @Output() selectedAppleCareService: EventEmitter<string> = new EventEmitter<string>();
  @Output() addOrUpdateCartEvent: EventEmitter<object> = new EventEmitter<object>();
  selectedServicePlans: Array<Product>;
  pointLabel: string;
  user: User;
  program: Program;
  config: Config;
  messages: Messages;
  isProcessing = false;
  private subscriptions: Subscription[] = [];

  constructor(
    private translateService: TranslateService,
    private userStore: UserStoreService,
    private messageStore: MessagesStoreService,
    private bootstrapModal: NgbModal,
    private modalService: ModalsService,
    private cartService: CartService,
    private sessionService: SessionService,
    private transitionService: TransitionService,
    private notificationRibbonService: NotificationRibbonService) {
    this.messages = this.messageStore.messages;
    this.user = this.userStore.user;
    this.program = this.userStore.program;
    this.config = this.userStore.config;
  }

  ngOnInit(): void {
    this.selectedServicePlans = [];
    if (this.program.formatPointName !== '') {
      this.pointLabel = this.translateService.instant(this.program.formatPointName);
    }
  }

  addAppleCareServicePlan(detail) {
    // GET ONE SELECTED SERVICE PLAN AT ONCE
    this.selectedServicePlans = this.appleCareServicePlans.filter(service => service.psid === detail.psid);
    if (this.isProductView) {
      // PRODUCT VIEW - DETAIL/CONFIG PAGE
      detail.selected = !detail.selected;
      if (detail.selected) {
        this.selectedAppleCareService.emit(this.selectedServicePlans[0]?.psid || '');
      } else {
        this.selectedAppleCareService.emit('');
      }
    } else {
      // API SERVICE - CART VIEW
      this.transitionService.openTransition();
      if (this.selectedServicePlans[0]?.psid) {
        const param = this.isGiftView ? {
          giftItem: {
            productId: this.giftItem.productDetail.psid,
            servicePlan: this.giftItem.productDetail.addOns.servicePlans[0].psid
          }
        } : {
          servicePlan: this.selectedServicePlans[0].psid
        };
        this.addOrRemoveServicePlan(param);
      }
    }
  }

  removeAppleCareServicePlan() {
    this.transitionService.openTransition();
    const param = this.isGiftView ? {
      giftItem: {
        productId: this.giftItem.productDetail.psid,
        servicePlan: ''
      }
    } : {
      servicePlan: ''
    };
    this.addOrRemoveServicePlan(param);
  }

  addOrRemoveServicePlan(param) {
    this.subscriptions.push(
      this.cartService.modifyCart(this.cartItem.id, param).subscribe(data => {
        this.transitionService.closeTransition();
        this.addOrUpdateCartEvent.emit({
          type: 'addOrUpdateServicePlan',
          item: data
        });
      }, error => {
        this.transitionService.closeTransition();
        if (error.status === 401 || error.status === 0) {
          this.cartService.setDisableCheckoutBtn(false);
          this.sessionService.showTimeout();
        } else {
          // NOTIFICATION RIBBON ERROR MESSAGE
          this.notificationRibbonService.emitChange([true, this.messages.unknownError]);
        }
      })
    );
  }

  openAppleCareModal(appleCare) {
    appleCare.selected = this.selectedServicePlan ? true : appleCare.selected;
    const appleCareModal = this.bootstrapModal.open(AppleCareModalComponent, {
      windowClass: 'apple-care-modal modal-theme-817 in',
      size: 'md',
      backdropClass: 'in',
      ariaLabelledBy: 'apple-care-modal-title'
    });
    appleCareModal.componentInstance.messages = this.messages;
    appleCareModal.componentInstance.config = this.config;
    appleCareModal.componentInstance.user = this.user;
    appleCareModal.componentInstance.appleCareService = appleCare;
    appleCareModal.result.then((data: Product) => {
      if (data && !data.selected) {
        this.addAppleCareServicePlan(data);
      }
    }).catch(err => { });
    this.modalService.settop();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((subscription) => subscription.unsubscribe());
  }
}
