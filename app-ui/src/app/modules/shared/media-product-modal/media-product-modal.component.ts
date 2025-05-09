import { Component, OnInit, Input } from '@angular/core';
import { Config } from '@app/models/config';
import { Messages } from '@app/models/messages';
import { MediaProduct } from '@app/models/media-product';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { CartService } from '@app/services/cart.service';
import { SessionService } from '@app/services/session.service';
import { Cart } from '@app/models/cart';

@Component({
  selector: 'app-media-product-modal',
  templateUrl: './media-product-modal.component.html',
  styleUrls: ['./media-product-modal.component.scss']
})
export class MediaProductModalComponent implements OnInit {

  translateParams: { [key: string]: string } = {};

  @Input() messages: Messages;
  @Input() config: Config;
  @Input() mediaProduct: MediaProduct;
  isProcessing = false;

  constructor(
    private activeModal: NgbActiveModal,
    private cartService: CartService,
    private sessionService: SessionService
  ) {}

  ngOnInit(): void {

  }

  /**
   * @description close active modal
   */
  cancel() {
    this.activeModal.dismiss();
    return false;
  }

  doMediaSubscribe(mediaProduct: MediaProduct) {
    this.isProcessing = true;
    const postData = {
      subscriptions: {
        itemId: mediaProduct.itemId,
        quantity: 1
      }
    };
    this.cartService.modifyCart(0, postData).subscribe((data: Cart) => {
      this.isProcessing = false;
      // Close modal and  Update subscription list
      this.activeModal.close(data.subscriptions || []);
    }, (error) => {
      this.isProcessing = false;
      /* if (error.status === 401 || error.status === 0) {
        // sessionMgmt.showTimeout();
      } else {
        const msg = (this.translateService.instant('cartQtyModifyError')).concat(' ', this.translateService.instant('tryAgainLater'));
        this.notificationRibbonService.emitChange([true, msg]);
      } */
    });
    this.sessionService.getSession();

  }

}
