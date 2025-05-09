import { Injectable } from '@angular/core';
import { AnonModalComponent } from '@app/components/modals/anon/anon-modal.component';
import { BrowseOnlyComponent } from '@app/components/modals/browse-only/browse-only.component';
import { User } from '@app/models/user';
import { Config } from '@app/models/config';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { TemplateStoreService } from '@app/state/template-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { NgbModal, ModalDismissReasons } from '@ng-bootstrap/ng-bootstrap';
import { Messages } from '@app/models/messages';
import { FacetsFiltersComponent } from '@app/modules/facets-filters/facets-filters.component';
import { AddressModalComponent } from '@app/components/modals/address-modal/address-modal.component';
import { Address } from '@app/models/address';
import { OopsModalComponent } from '@app/components/modals/oops-modal/oops-modal.component';
import { ConsentFormComponent } from '@app/components/modals/consent-form/consent-form.component';
import { IdologyModelComponent } from '@app/components/modals/idology-model/idology-model.component';
import { WFSpanishComponent } from '@app/components/modals/spanish-modal/wf-footer-spanish-modal.component';
import { Subject } from 'rxjs';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { AppConstants } from '@app/constants/app.constants';
import { EngraveModalComponent } from './engrave-modal/engrave-modal.component';
import { NavigationExtras, Router } from '@angular/router';
import { SharedService } from '@app/modules/shared/shared.service';
import { NotificationRibbonService } from '@app/services/notification-ribbon.service';
import { CartService } from '@app/services/cart.service';

@Injectable({
  providedIn: 'root',
})

// @ts-ignore
export class ModalsService {
  messages: Messages;
  user: User;
  config: Config;

  private readonly anonymousModal: object;
  private readonly triggerafterclosed = new Subject<boolean>();
  readonly triggerAfterClosed$ = this.triggerafterclosed.asObservable();

  constructor(
    private messagesStore: MessagesStoreService,
    private bootstrapModal: NgbModal,
    private templateStoreService: TemplateStoreService,
    public userStore: UserStoreService,
    private matomoService: MatomoService,
    private route: Router,
    private sharedService: SharedService,
    private notificationRibbonService: NotificationRibbonService,
    private cartService: CartService
  ) {
    this.messages = this.messagesStore.messages;
    this.user = this.userStore.user;
    this.config = this.userStore.config;
    this.anonymousModal = this.templateStoreService.anonymousModal;

    this.sharedService.isEngraveModalOpen().subscribe((engraveObj) => {
      if (engraveObj['cartItemId'] && engraveObj['psIdSlug']) {
        this.openEngraveModalComponent(engraveObj);
      }
    });
  }

  openMobileFacetsFiltersComponent(
    filterProductsData,
    sortBy,
    routeParams
  ): void {
    const facetsFiltersModal = this.bootstrapModal.open(
      FacetsFiltersComponent,
      {
        backdrop: 'static',
        size: 'lg',
        windowClass: 'modal fade mobile-facets-modal in',
        backdropClass: 'in',
      }
    );
    facetsFiltersModal.componentInstance.messages = this.messages;
    facetsFiltersModal.componentInstance.showFilterModal = true;
    facetsFiltersModal.componentInstance.filterProductsData = filterProductsData;
    facetsFiltersModal.componentInstance.sortBy = sortBy;
    facetsFiltersModal.componentInstance.routeParams = routeParams;
    this.settop();
  }

  openBrowseOnlyComponent(): void {
    const browseModal = this.bootstrapModal.open(BrowseOnlyComponent, {
      backdrop: 'static',
      size: 'lg',
      windowClass: 'message-modal login-modal template2 modal-theme-817 in',
      backdropClass: 'in',
      ariaLabelledBy: 'browse-only-modal-title'
    });
    browseModal.componentInstance.messages = this.messages;
    this.settop();
  }

  openAnonModalComponent(): void {
    const anonModal = this.bootstrapModal.open(AnonModalComponent, {
      backdrop: 'static',
      size: this.anonymousModal['size'] ? this.anonymousModal['size'] : 'lg',
      windowClass:
        'message-modal login-modal template2 modal-theme-817 in ' +
        (this.anonymousModal['className']
          ? this.anonymousModal['className']
          : ''),
      backdropClass: 'in',
      ariaLabelledBy: 'anon-modal-title'
    });
    anonModal.componentInstance.messages = this.messages;
    anonModal.componentInstance.config = this.config;
    anonModal.componentInstance.user = this.user;
    this.settop();
  }

  openSuggestAddressModalComponent(isWarning: boolean, originalAddress: Address, suggestedAddress: Address, message: object): void {
    const SuggestAddressModal = this.bootstrapModal.open(AddressModalComponent, {
      windowClass: 'address-modal modal-theme-817 in',
      size: 'lg',
      backdrop: 'static',
      backdropClass: 'in',
      ariaLabelledBy: 'address-modal-title'
    });
    SuggestAddressModal.componentInstance.showSuggestedAddr = isWarning;
    SuggestAddressModal.componentInstance.originalAddress = originalAddress;
    SuggestAddressModal.componentInstance.suggestedAddress = suggestedAddress;
    SuggestAddressModal.componentInstance.warningMessage = message;
    SuggestAddressModal.result.then((data) => {
      if (data) {
        this.openOopsModalComponent('shippingAddressError');
      }
    }).catch(err => {});
    this.settop();
  }

  openOopsModalComponent(template: string, pData?: object): void {
    const oopsModal = this.bootstrapModal.open(OopsModalComponent, {
      backdrop: 'static',
      size: 'lg',
      windowClass: 'error-modal in',
      backdropClass: 'in',
      ariaLabelledBy: 'oops-modal-title'
    });
    oopsModal.componentInstance.template = template;
    oopsModal.componentInstance.pData = pData;
    this.settop('oopsModal');
    this.matomoService.broadcast(AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.CANONICAL_PAGE, {
      payload: {
        errorTitle: template,
        location: location.href,
        canonicalTitle: AppConstants.analyticServices.CANONICAL_CONSTANTS.ERROR
      }
    });
  }

  openConsentFormComponent(): void {
    const consentFormModal = this.bootstrapModal.open(ConsentFormComponent, {
      backdrop: true,
      size: 'lg',
      windowClass: 'consent-form-modal in',
      backdropClass: 'in',
      centered: true,
      ariaLabelledBy: 'consent-form-modal-title'
    });
    consentFormModal.componentInstance.messages = this.messages;
    this.settop();
  }

  openIdologyModalComponent(type, data) {
    const idologyModal = this.bootstrapModal.open(IdologyModelComponent, {
      windowClass: 'address-modal in',
      size: 'md',
      backdrop: 'static',
      backdropClass: 'in',
    });
    idologyModal.componentInstance.validator = data;
    idologyModal.result.then((status) => {
      this.triggerafterclosed.next(true);
    });
    this.settop();
  }

  public get modalAfterClosed() {
    return this.triggerafterclosed;
  }

  settop(popupName?: string) {
    setTimeout(() => {
      // Sets the top position of the modal
      if (popupName === 'oopsModal') {
        document.querySelector('.modal-lg').classList.remove('modal-lg'); // added bez error modal is wide compare to old app unnecessary added modal-lg class
      }
      const appWidth = window.innerWidth || document.documentElement.clientWidth;
      const isMobile = (appWidth < 720);
      if (!isMobile) {
        let windowHeight = window.innerHeight;
        let topPos;
        if (window.parentIFrame) {
          windowHeight = window.iFrameResizer.getParentWindow().outerHeight;
        }
        // Waits for CSS transition to initiate
        const modalHeight = document.querySelector('.modal-dialog').clientHeight;
        const heightDiff = windowHeight - modalHeight;
        // if modal height is less than window height
        if (heightDiff > 0) {
          topPos = Math.floor(heightDiff / 3);
          const modaldialog: any = document.querySelector('.modal-dialog');
          modaldialog.style.top = topPos + 'px';
        } else {
          const modaldialog: any = document.querySelector('.modal-dialog');
          modaldialog.style.top = 0 + 'px';
        }
      }
    }, 300);
  }

  hasAnyOpenModal(): boolean {
    return this.bootstrapModal.hasOpenModals();
  }

  dismissAllModals(): void {
    this.bootstrapModal.dismissAll();
  }

  openEngraveModalComponent(engraveObj) {

    const params = '/' + engraveObj.psIdSlug?.replace('-', '/') + '?withVariations=false&withEngraveConfig=true';
    this.sharedService.getProducts(params).subscribe((data) => {
      const engraveModal = this.bootstrapModal.open(EngraveModalComponent, {
        windowClass: 'engrave-modal in',
        size: 'md',
        backdropClass: 'in',
        ariaLabelledBy: 'engrave-modal-title'
      });
      engraveModal.componentInstance.cartItemId = engraveObj.cartItemId;
      engraveModal.componentInstance.psIdSlug = engraveObj.psIdSlug;
      engraveModal.componentInstance.isGiftPromo = engraveObj.isGiftPromo;
      engraveModal.componentInstance.isEdit = engraveObj.isEdit;
      engraveModal.componentInstance.detailData = data;
      engraveModal.componentInstance.updateCart = engraveObj.updateCart;
      engraveModal.componentInstance.qualifyingProduct = engraveObj.qualifyingProduct;

      const relatedProductsPageParam: NavigationExtras = {
          queryParams: {
            hasRelatedProduct: engraveObj.qualifyingProduct?.hasRelatedProduct
          }
        };

      engraveModal.result.then((status) => {
        // TO DO Later
      }, (reason) => {
        if (reason === ModalDismissReasons.BACKDROP_CLICK) {
          const detailData = this.sharedService.currentEngraveProductDetail;
          if (detailData && detailData.addOns.availableGiftItems.length > 0 && !engraveObj.isEdit) {
            if (detailData.addOns.availableGiftItems && detailData.addOns.availableGiftItems.length > 1) {
              const URL = `./store/gift-promo/${detailData.cartItemId}/${detailData.psid.replace(/[&\/\\#,+()$~%.'":*?<>{}]/g, '-')}`;
              this.route.navigate([URL], relatedProductsPageParam);
              return;
            }else{
              if (detailData.hasRelatedProduct){
                this.route.navigate(['store', 'related-products', this.sharedService.psidSlugConvert(detailData.psid)]);
              }else{
                this.route.navigate(['./store/cart']);
              }
            }
          } else if (engraveObj.updateCart && engraveObj.isGiftPromo && !engraveObj.isEdit) {
            this.cartService.updateGiftItemModifyCart(engraveObj.cartItemId, detailData.psid);
          }
          this.sharedService.currentEngraveProductDetail = null;
          if (this.route.url.indexOf('cart') === -1) {
            if (!engraveObj.isEdit){
              if (engraveObj.qualifyingProduct.hasRelatedProduct){
                this.route.navigate(['store', 'related-products', this.sharedService.psidSlugConvert(engraveObj.qualifyingProduct.psid)]);
              }else{
                this.route.navigate(['./store/cart']);
              }
            }else{
              this.route.navigate(['./store/cart']);
            }
          } else if (engraveObj.updateCart && !engraveObj.isGiftPromo) {
            this.sharedService.setUpdatedCartItem(true);
          }
        }
      });
    },
    error => {
      if (error.status === 401 || error.status === 0) {
        // TODO: sessionMgmt.showTimeout();
      } else {
        // NOTIFICATION NIBBON ERROR MESSAGE
        this.notificationRibbonService.emitChange([true, this.messages.detailsLoadingError.concat(' ', this.messages.engravingAddErrorGoToCart)]);
      }
    });
  }

  openWfSpanishComponent(): void {
    const wfSpanishModal = this.bootstrapModal.open(WFSpanishComponent, {
      backdrop: true,
      size: 'lg',
      windowClass: 'template2 wf-footer modal-theme-817 in',
      backdropClass: 'in',
      centered: true,
      ariaLabelledBy: 'wf-spanish-modal-title'
    });
    wfSpanishModal.componentInstance.messages = this.messages;
    this.settop();
  }
}