import { Component, EventEmitter, Injector, Input, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';
import { Messages } from '@app/models/messages';
import { GiftPromoService } from '@app/services/gift-promo.service';
import { CartService } from '@app/services/cart.service';
import { CartItem } from '@app/models/cart';
import { SessionService } from '@app/services/session.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { TransitionService } from '@app/transition/transition.service';
import { isEmpty } from 'lodash';
import { BreakPoint } from '@app/components/utils/break-point';

@Component({
  selector: 'app-cart-gift-promo',
  templateUrl: './cart-gift-promo.component.html',
  styleUrls: ['./cart-gift-promo.component.scss']
})

export class CartGiftPromoComponent extends BreakPoint implements OnInit {
  @Input() item: CartItem;
  @Input() giftItem: CartItem;
  @Input() showShippingAvailability: boolean;
  @Input() messages: Messages;
  @Input() review: boolean;
  giftPromoCartMessage: string;
  giftItemEngraveCollapsed: boolean;
  giftHasEngraving: boolean;
  isEngraved: boolean;
  editEngravingOptions: boolean;
  showGiftShippingAvailability: boolean;
  productLoadError: boolean;
  @Output() addOrUpdateCartEvent = new EventEmitter<any>();
  isServicePlansExist: boolean;

  constructor(
    public injector: Injector,
    public router: Router,
    public giftPromoService: GiftPromoService,
    public cartService: CartService,
    private sessionService: SessionService,
    private sharedServices: SharedService,
    private transitionService: TransitionService,
  ) {
    super(injector);
   }

  ngOnInit(): void {
    this.giftItemEngraveCollapsed = true;
    this.isEngraved = (this.isEngraved === undefined ? (this.giftItem?.engrave.line1.length > 0 || this.giftItem?.engrave.line2.length > 0) : this.isEngraved);
    this.giftHasEngraving = true;
    const giftMsg = 'giftPromoCartMessage-' + this.item.productId.replace(/[&\/\\#,+()$~%.'":*?<>{}]/g, '');
    this.giftPromoCartMessage = this.messages[giftMsg];
    this.item.productDetail.addOns.availableGiftItems[0].quantity = 1;
    this.isServicePlansExist = this.giftItem?.selectedAddOns?.servicePlan ? true : !isEmpty(this.giftItem?.productDetail?.addOns?.servicePlans) && !this.review;
  }

  changeGiftSelection() {
    const URL =  `./store/gift-promo/${this.item.id}/${this.item.productId.replace(/[&\/\\#,+()$~%.'":*?<>{}]/g, '-')}`;
    this.router.navigate([URL]);
  }

  /*** Remove Cart Gift Item ***/
  removeGiftItem(cartgiftitem) {
    this.transitionService.openTransition();
    this.cartService.modifyCart(cartgiftitem.id, {giftItem: {}}).subscribe((data) => {
        this.transitionService.closeTransition();
        this.addOrUpdateCartEvent.emit({
          type: 'removeCartGiftItem',
          item: data
        });
      }, (error) => {
        this.transitionService.closeTransition();
        if (error.status === 401 || error.status === 0) {
          // TO DO handle the session Mgmt
          // sessionMgmt.showTimeout();
        } else {
          this.productLoadError = true;
        }
      });
    this.sessionService.getSession();
  }

  /*** Add Cart Gift Item ***/
  addFreeGift() {
    const giftItemParams = {
      giftItem: {
        productId : this.item.productDetail.addOns.availableGiftItems[0].psid,
      }
    };
    this.giftPromoService.giftItemModify(giftItemParams, this.item.id).subscribe((data) => {
      this.addOrUpdateCartEvent.emit({
        type: 'addCartGiftItem',
        item: data.body
      });
    }, (error) => {
      if (error.status === 401 || error.status === 0) {
        // TO DO handle the session Mgmt
        // sessionMgmt.showTimeout();
      } else {
        this.productLoadError = true;
      }
    });
    this.sessionService.getSession();
  }

  editEngraveGiftTxt() {
    this.sharedServices.currentEngraveProductDetail = this.item.selectedAddOns.giftItem;
    const engraveItemObj = {
      cartItemId: this.item.id,
      psIdSlug: this.item.selectedAddOns.giftItem.productId,
      isGiftPromo: true,
      isEdit: true
    };
    this.sharedServices.openEngraveModalDialog(engraveItemObj);
  }

  cartUpdateEvent(event): void {
    this.addOrUpdateCartEvent.emit({
      type: event.type,
      item: event.item
    });
  }

}
