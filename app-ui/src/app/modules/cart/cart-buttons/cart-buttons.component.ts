import { Address } from '@app/models/address';
import { Component, OnInit, Input, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Config } from '@app/models/config';
import { CartTotal } from '@app/models/cart';
import { CartService } from '@app/services/cart.service';
import { Subscription } from 'rxjs';
import { AddressService } from '@app/services/address.service';

@Component({
  selector: 'app-cart-buttons',
  templateUrl: './cart-buttons.component.html',
  styleUrls: ['./cart-buttons.component.scss']
})
export class CartButtonsComponent implements OnInit, OnDestroy {
  @Input() cartId: number;
  @Input() cartTemplate: object;
  @Input() cartObjTotals: CartTotal;
  @Input() isAddressValid: boolean;
  @Input() skipPaymentOptionPage: boolean;
  @Input() checkoutAddress: Address;
  @Input() eppStatus: boolean;
  @Input() disableBtn: boolean;
  @Input() isProcessing: boolean;
  @Input() negativeBalance: boolean;

  config: Config;
  subscriptions: Subscription[] = [];
  fullCatalog: string;
  cartButtonsTemplate: string;

  constructor(
    private cartService: CartService,
    private router: Router,
    private addressService: AddressService
  ) { }

  ngOnInit() {
    const tempObj = this.cartTemplate;
    this.cartButtonsTemplate = (tempObj && Object.keys(tempObj).length > 0 && tempObj.hasOwnProperty('cartItemsButtons')) ? this.cartTemplate['cartItemsButtons'].template : 'cart-button-default.htm';
  }

  goToCheckout() {
    if (!this.isAddressValid) {
      this.router.navigate(['/store', 'shipping-address']);
    } else {
      this.addressService.modifyShippingAddress(this.checkoutAddress, this.skipPaymentOptionPage);
    }
  }

  redirectUrl() {
    const storedUrls = sessionStorage.getItem('sessionURLs');
    const varUrls = JSON.parse(storedUrls);
    sessionStorage.clear();
    window.location = varUrls.sessionURL ? varUrls.sessionURL.navigateBackUrl : varUrls.navigateBackUrl;
  }

  passPaymentType(pmt: any) {
    // Modify cart with available payment option
    this.isProcessing = true;
    this.subscriptions.push(this.cartService.passPaymentType(this.cartId, { selectedPaymentOption: pmt }).subscribe(data => {
      this.cartService.updateRemainingCost(this.cartObjTotals);
      this.isProcessing = false;
      this.goToCheckout();
    }));
  }

 /* purchaseTypeValueSet() {
    // purchaseType.type = this.cartService.getProperty('purchaseType');
    return this.cartService.getProperty('purchaseType');
  }*/

  goToStore() {
    this.router.navigate(['/store']);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }


}
