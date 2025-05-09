import { Component, OnInit, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Address } from '@app/models/address';
import { User } from '@app/models/user';
import { UserStoreService } from '@app/state/user-store.service';
import { CartService } from '@app/services/cart.service';
import { TemplateStoreService } from '@app/state/template-store.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { Config } from '@app/models/config';
import { Router } from '@angular/router';
import { SessionService } from '@app/services/session.service';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { AddressService } from '@app/services/address.service';

@Component({
  selector: 'app-address-modal',
  templateUrl: './address-modal.component.html',
  styleUrls: ['./address-modal.component.scss']
})
export class AddressModalComponent implements OnInit {
  @Input() showSuggestedAddr: boolean;
  @Input() originalAddress: Address;
  @Input() suggestedAddress: Address;
  @Input() warningMessage: { [key: string]: string };

  user: User;
  isFromAddressPage = true; // not used in angular1x
  selectedAddress: string;
  isFormValid = true;
  buttonColor: string | null = null;
  config: Config;
  cartSummaryModified: boolean;
  constructor(
    private activeModal: NgbActiveModal,
    private userStore: UserStoreService,
    private cartService: CartService,
    private templateStoreService: TemplateStoreService,
    private sharedService: SharedService,
    private router: Router,
    private sessionService: SessionService,
    private matomoService: MatomoService,
    private addressService: AddressService
  ) {
    this.user = this.userStore.user;
    this.buttonColor = this.templateStoreService.buttonColor ? this.templateStoreService.buttonColor : '';
    this.config = this.userStore.config;
  }
  ngOnInit(): void {
    this.selectedAddress = !this.showSuggestedAddr ? 'original' : this.suggestedAddress ? 'suggested' : 'original';
    this.cartSummaryModified = this.suggestedAddress?.cartTotalModified ? this.suggestedAddress.cartTotalModified : undefined;
  }

  /**
   * @description close active modal
   */
  cancel() {
    this.activeModal.dismiss();
  }

  /**
   * @description update the shipping address
   * @param selectedAddress
   */
  useAddress(selectedAddress) {
    const addressType = selectedAddress;
    let addressToUse: Address;

    if (addressType === 'suggested') {
      addressToUse = this.suggestedAddress;
    } else {
      addressToUse = this.originalAddress;
    }
    addressToUse.ignoreSuggestedAddress = true;
    this.addressService.modifyAddress(addressToUse).subscribe(data => {
      // cm.applyChanges(data.data);
      this.applyChanges(data);
    }, error => {
      if (error.status === 401 || error.status === 0) {
        // TODO handle the session Mgmt
        // sessionMgmt.showTimeout();
      } else {
        this.activeModal.close(error);
        // TODO Handle the Error Modal
        // $scope.openErrorModal('shippingAddressError');
      }
      // Handle the sendError Error Modal
      this.matomoService.sendErrorToAnalyticService();
    });
  }


  /**
   * @param data selected address
   */
  applyChanges(data) {
    this.closeModal(data);
  }

  /**
   * @description close active modal
   * @param data selected address
   */
  closeModal(data) {
    this.sessionService.getSession();
    this.activeModal.dismiss();
    this.verifySuggestionToCheckoutFlow(data);

  }
  goToAddressFormPage() {
    this.cancel();
    this.router.navigate(['/store', 'shipping-address']);
    return false;
  }

  verifySuggestionToCheckoutFlow(data) {
    const skipPaymentOptionPage = this.sharedService.verifySkipPaymentOption(this.cartService.getProperty('cartData')) || false;
    this.cartSummaryModified = (this.cartSummaryModified || data.cartTotalModified);
    if (skipPaymentOptionPage || !this.config.fullCatalog) {
      this.router.navigate(['/store', 'checkout']);
    } else {
      this.router.navigate(['/store', 'payment']);
    }
  }

}
