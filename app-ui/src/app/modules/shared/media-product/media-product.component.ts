import { Component, OnInit, Input, ViewChild, ElementRef, Renderer2, Output, EventEmitter } from '@angular/core';
import { UserStoreService } from '@app/state/user-store.service';
import { Config } from '@app/models/config';
import { MediaProduct } from '@app/models/media-product';
import { User } from '@app/models/user';
import { ModalsService } from '@app/components/modals/modals.service';
import { ActivatedRoute } from '@angular/router';
import { CartService } from '@app/services/cart.service';
import { SessionService } from '@app/services/session.service';
import { MediaProductModalComponent } from '@app/modules/shared/media-product-modal/media-product-modal.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Messages } from '@app/models/messages';

@Component({
  selector: 'app-media-product',
  templateUrl: './media-product.component.html',
  styleUrls: ['./media-product.component.scss']
})
export class MediaProductComponent implements OnInit {

  config: Config;
  user: User;
  isConfirmationPage = false;
  @Input() mediaProducts: Array<MediaProduct>;
  @Input() readOnly: boolean;
  @Input() messages: Messages;
  @Input() disableBtn: boolean;
  @Output() triggerActionProcessing: EventEmitter<boolean> = new EventEmitter<boolean>();

  constructor(
    private userStore: UserStoreService,
    private modalService: ModalsService,
    private activatedRoute: ActivatedRoute,
    private cartService: CartService,
    private sessionService: SessionService,
    private bootstrapModal: NgbModal
  ) {
    this.config = this.userStore.config;
    this.user = this.userStore.user;
  }
  isProcessing = false;

  ngOnInit(): void {
    const pageName = this.activatedRoute.snapshot.data?.pageName;
    this.isConfirmationPage = (pageName === 'CONFIRMATION');
  }
  doMediaSubscribe(product) {
    this.isProcessing = true;
    const doDisableBtn = !this.disableBtn;
    const postData = {
      subscriptions: {
        itemId: product.itemId,
        quantity: (product.addedToCart) ? 0 : 1
      }
    };
    if (doDisableBtn) {
      this.triggerActionProcessing.emit(true);
    }
    this.cartService.modifyCart(0, postData).subscribe((data) => {
      if (doDisableBtn) {
        this.triggerActionProcessing.emit(false);
      }
      this.isProcessing = false;
      this.mediaProducts = (data && data.subscriptions) ? data.subscriptions : [];  // update subscription list
    }, (error) => {
      this.isProcessing = false;
      if (doDisableBtn) {
        this.triggerActionProcessing.emit(false);
      }
      /* if (error.status === 401 || error.status === 0) {
        // sessionMgmt.showTimeout();
      } else {
        const msg = (this.translateService.instant('cartQtyModifyError')).concat(' ', this.translateService.instant('tryAgainLater'));
        this.notificationRibbonService.emitChange([true, msg]);
      } */
    });
    this.sessionService.getSession();
  }

  openMediaProductModal(mediaProduct: MediaProduct) {
    const mediaProductModal = this.bootstrapModal.open(MediaProductModalComponent, {
      windowClass: 'media-product-modal modal-theme-817 in',
      size: 'lg',
      backdropClass: 'in',
      ariaLabelledBy: 'media-product-modal-title'
    });
    mediaProductModal.componentInstance.messages = this.messages;
    mediaProductModal.componentInstance.config = this.config;
    mediaProductModal.componentInstance.mediaProduct = mediaProduct;
    mediaProductModal.result.then((data) => {
      if (data) {
        this.mediaProducts = data;
      }
    }).catch(err => { });
    this.modalService.settop(); // Set Modal top
  }

}
