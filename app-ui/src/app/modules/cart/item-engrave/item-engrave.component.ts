import { Component, Input, OnInit } from '@angular/core';
import { CartItem } from '@app/models/cart';
import { CartService } from '@app/services/cart.service';
import { NotificationRibbonService } from '@app/services/notification-ribbon.service';
import { SessionService } from '@app/services/session.service';
import { SharedService } from '@app/modules/shared/shared.service';

@Component({
    selector: 'app-item-engrave',
    templateUrl: './item-engrave.component.html',
    styleUrls: ['./item-engrave.component.scss']
})
export class ItemEngraveComponent implements OnInit {

    @Input() messages;
    @Input() engraveIndex: number;
    @Input() item: CartItem;

    cartEngrave = [];
    engraveItemObj;
    showEngraveOptions: boolean;
    editEngravingOptions: boolean;
    offscreenText: any = {};

    constructor(
        private cartService: CartService,
        private sessionService: SessionService,
        private notificationRibbonService: NotificationRibbonService,
        private sharedServices: SharedService
    ) { }

    ngOnInit(): void {
        this.engravingInit(this.engraveIndex, this.item);
    }

    // Initialize the engraving option for any item that is engraveable
    engravingInit(index: number, item: CartItem) {
        this.engraveItemObj = item.engrave;
        this.showEngraveOptions = (this.engraveItemObj.line1.length > 0 || this.engraveItemObj.line2.length > 0);
        this.editEngravingOptions = (this.engraveItemObj.line1.length === 0);
        this.cartEngrave[index] = {};
        this.cartEngrave[index].line1 = this.engraveItemObj.line1;
        this.cartEngrave[index].line2 = this.engraveItemObj.line2;
        this.cartEngrave[index].templateClass = this.engraveItemObj.templateClass;
        this.cartEngrave[index].noOfLines = this.engraveItemObj.noOfLines;
        this.cartService.setDisableQty(index, (this.engraveItemObj.line1.length > 0));
        this.cartService.setHasEngraving(index, (this.engraveItemObj.line1.length > 0 || this.engraveItemObj.line2.length > 0));
        this.offscreenText.msg = '';
    }

    editEngraveGiftTxt() {
        this.sharedServices.currentEngraveProductDetail = this.item;
        const engraveItemObj = {
            cartItemId: this.item.id,
            psIdSlug: this.item.productId,
            isGiftPromo: false,
            isEdit: true
        };
        this.sharedServices.openEngraveModalDialog(engraveItemObj);
    }

    removeEngraving(index, id) {
        this.cartService.setDisableCheckoutBtn(true);
        this.cartService.modifyCart(id, { engrave: { line1: '', line2: '' } }).subscribe(data => {
            this.cartService.setUpdateCartObj(data);
            this.cartService.setDisableCheckoutBtn(false);
            this.cartService.setDisableQty(index, false);
            this.cartService.setHasEngraving(index, false);
            this.showEngraveOptions = false;
            this.editEngravingOptions = true;
            setTimeout(() => {
                this.offscreenText.msg = this.messages.yourEngrv.concat(' ', this.messages.hasBeenRemoved);
            });
            this.cartEngrave[index].line1 = '';
            this.cartEngrave[index].line2 = '';
        }, error => {
            if (error.status === 401 || error.status === 0) {
                this.cartService.setDisableCheckoutBtn(false);
                this.sessionService.showTimeout();
            } else {
                // NOTIFICATION NIBBON ERROR MESSAGE
                this.notificationRibbonService.emitChange([true, this.messages.cartEngraveRemoveError.concat(' ', this.messages.tryAgainLater)]);
            }

        });
        this.sessionService.getSession();
    }
}
