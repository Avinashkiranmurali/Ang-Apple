import { AfterViewInit, Component, ElementRef, Input, OnInit, Renderer2 } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Router, NavigationExtras } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Messages } from '@app/models/messages';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { Config } from '@app/models/config';
import { UserStoreService } from '@app/state/user-store.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { CartService } from '@app/services/cart.service';
import { NotificationRibbonService } from '@app/services/notification-ribbon.service';
import { AppConstants } from '@app/constants/app.constants';
import { User } from '@app/models/user';

@Component({
  selector: 'app-engrave-modal',
  templateUrl: './engrave-modal.component.html',
  styleUrls: ['./engrave-modal.component.scss']
})
export class EngraveModalComponent implements OnInit, AfterViewInit {

  @Input() cartItemId;
  @Input() isGiftPromo;
  @Input() psIdSlug;
  @Input() isEdit;
  @Input() detailData;
  @Input() updateCart;
  @Input() qualifyingProduct;

  isImagePreview;
  previewUrl: string;
  isUpperCaseEnabled: boolean;
  isDefaultPreviewEnabled: boolean;
  isCartUpdate: boolean;
  engraveData;
  messages: Messages;
  disallowedCharErrorLine1: boolean;
  disallowedCharErrorLine2: boolean;
  invalidCharactersLine1: string;
  specialCharErrorLine1: boolean;
  specialCharErrorLine2: boolean;
  engrave = {
    line1: '',
    line2: ''
  };
  category;
  categoryName: string;
  subcat;
  subcatName: string;
  config: Config;
  productLevels;
  hasBadWordsSpecialChars;
  engraveItemName;
  engraveTemplateClass;
  noOfLines;
  engraveMaxLength;
  baseUrlEngrave;
  engraveItemDisplayName: string;
  configItemSku: string;
  engravingImage;
  shortLocale;
  engravingTxtColor = 'dkgray-txt';
  slug: string;
  invalidCharactersLine2: string;
  hasBadWordsSpecialCharsLine1;
  hasBadWordsSpecialCharsLine2;
  subCategory;
  addCat;
  engraveItemType: string;
  user: User;
  errorCount = 0;
  errorCountLabel = '';
  isLoading = false;
  engraveHeader: string;
  emptyFieldError = false;
  engraveImageOverlay;
  engraveModalOverlay;
  hasError = false;
  errorObject = {
    line1 : false,
    line2: false
  };
  engraveFormStatus;

  constructor(
    public messageStore: MessagesStoreService,
    private userStore: UserStoreService,
    private sharedService: SharedService,
    private cartService: CartService,
    private route: Router,
    private notificationRibbonService: NotificationRibbonService,
    private translateService: TranslateService,
    private activeModal: NgbActiveModal,
    private ele: ElementRef,
    private renderer: Renderer2
  ) {
    this.messages = this.messageStore.messages;
    this.user = this.userStore.user;
    this.config = this.userStore.config;
  }

  ngAfterViewInit(): void {
    this.engraveModalOverlay = this.ele.nativeElement.querySelector('.engrave-overlay-wrapper');
    this.engraveImageOverlay = this.ele.nativeElement.querySelector('.engraving-image-wrapper');
    this.renderer.addClass(this.engraveImageOverlay , 'engrave-loading');
  }

  ngOnInit(): void {
    this.initEngrave();
  }

  initEngrave() {
    this.isCartUpdate = true;
    this.engraveData = {};
    if (this.isEdit && this.sharedService.currentEngraveProductDetail) {
      this.engrave = {
        line1: this.sharedService.currentEngraveProductDetail.engrave.line1,
        line2: this.sharedService.currentEngraveProductDetail.engrave.line2
      };
    } else {
      this.engrave = {
        line1: '',
        line2: ''
      };
    }
    this.sharedService.currentEngraveProductDetail = this.detailData;
    this.sharedService.currentEngraveProductDetail.cartItemId = this.cartItemId;
    this.engraveData = this.detailData.engrave;
    this.productLevels = this.getLevelsFromCategories(this.detailData);
    this.loadEngraving();
  }

  getEngraveImage() {
    let line1 = this.engrave.line1 || '';
    line1 = 'th=' + encodeURIComponent(line1);
    let line2 = this.engrave.line2 || '';
    line2 = (this.noOfLines > 1) ? '&tl=' + encodeURIComponent(line2) : '';
    let engravingText = line1 + line2;

    // Same logic will be available in engrave.html, if any logic changes happen we need to change in both location
    if ((!this.engrave.line1 && !this.engrave.line2) && this.isDefaultPreviewEnabled) {
      if (this.noOfLines > 1) {
        line1 = 'th=' + this.messages.yourEngrv;
        line2 = '&tl=' + this.messages.goesHere;
      } else if (this.noOfLines === 1) {
        line1 = 'th=' + (this.isUpperCaseEnabled ? this.messages.yourEngraving : this.messages.yourEngrv);
        line2 = '';
      }

      engravingText = line1 + line2;
    }
    return this.previewUrl.replace('{{itemSKU}}', this.configItemSku).replace('{{engraveText}}', engravingText);
  }

  convertToUpperCase(val, isUpperCase) {
    val = val || '';
    return (isUpperCase) ? val.toUpperCase() : val;
  }

  verifyAndNavigateToNextPage() {
    this.detailData.addOns.availableGiftItems = this.detailData.addOns.availableGiftItems || [];
    if (this.isGiftPromo) { // if we are engraving for gift
      this.redirectToCartPage();
    }else if (this.detailData.addOns.availableGiftItems.length > 1) {
      this.activeModal.dismiss();
      const queryParam: NavigationExtras = {
        queryParams: {
          hasRelatedProduct: this.qualifyingProduct.hasRelatedProduct
        }
      };
      const URL = `./store/gift-promo/${this.cartItemId}/${this.psIdSlug}`;
      this.route.navigate([URL], queryParam);
    } else { // Product not eligible for gift
      this.redirectToCartPage();
    }
  }

  redirectToCartPage() {
    const cartPageParam: NavigationExtras = {
      queryParams: {
        isCartUpdate: true
      }
    };
    this.sharedService.currentEngraveProductDetail = null;
    this.isLoading = false;
    this.renderer.removeClass(this.engraveModalOverlay, 'engrave-loading');
    this.activeModal.dismiss();
    if (this.qualifyingProduct.hasRelatedProduct) {
      this.route.navigate(['store', 'related-products', this.qualifyingProduct.psid.replace('/', '-')]);
    }else{
      this.route.navigate(['./store/cart'], cartPageParam);
    }
    if (this.updateCart && !this.isGiftPromo) {
      this.sharedService.setUpdatedCartItem(true);
    }
  }

  loadEngravableGiftItem() {
    const params = '/' + this.psIdSlug?.replace('-', '/') + '?withVariations=false&withEngraveConfig=true';
    this.sharedService.getProducts(params).subscribe((data) => {
      this.renderer.addClass(this.engraveImageOverlay, 'engrave-loading');
      this.detailData = data;
      this.isGiftPromo = true;
      this.initEngrave();
      this.isLoading = false;
      this.renderer.removeClass(this.engraveModalOverlay, 'engrave-loading');
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

  updateErrorMessage() {
    if (this.errorCount > 0) {
      if (this.errorCount === 1) {
        this.errorCountLabel = this.translateService.instant('singleErrorCountLabel', { errorCount: this.errorCount });
      } else {
        this.errorCountLabel = this.translateService.instant('multiErrorCountLabel', { errorCount: this.errorCount });
      }
    } else {
      this.errorCountLabel = '';
    }
  }

  validateText(inputValue) {
    const invalidCharacters = [];
    const whiteList = this.messages.allowedCharacters;
    if (whiteList) {
      inputValue.split('').forEach((char) => {
        if (whiteList.indexOf(char) === -1
          && invalidCharacters.indexOf(char) === -1
          && char !== ''
          && char !== undefined) {
          invalidCharacters.push(char);
        }
      });
    }
    return invalidCharacters;
  }

  updateEngraving() {
    this.engraveFormStatus = '';
    const engraveObj = {};
    engraveObj['line1'] = this.engrave.line1;
    engraveObj['line2'] = this.engrave.line2 ? this.engrave.line2 : '';
    this.specialCharErrorLine1 = false;
    this.specialCharErrorLine2 = false;
    this.emptyFieldError = false;
    this.errorCount = 0;

    if (this.isEmptyFieldError()) {
      this.updateErrorMessage();
      this.setFocusToInputErrorField();
      return false;
    }

    const postData = {};
    if (this.isGiftPromo) {
      postData['giftItem'] = {
        productId: this.detailData.psid,
        engrave: engraveObj
      };
    } else {
      postData['engrave'] = engraveObj;
    }
    this.isLoading = true;
    this.renderer.addClass(this.engraveModalOverlay, 'engrave-loading');
    this.cartService.modifyCart(this.cartItemId, postData).subscribe((data) => {
      // verify and navigate to next page
      this.engraveFormStatus = this.messages.engravingFormStatus;
      if (this.isEdit) {
          this.sharedService.currentEngraveProductDetail = null;
          this.sharedService.setUpdatedCartItem(true);
          this.isLoading = false;
          this.renderer.removeClass(this.engraveModalOverlay, 'engrave-loading');
          this.activeModal.close();
      } else {
        this.verifyAndNavigateToNextPage();
      }
    },
    error => {
      if (error.status === 401 || error.status === 0) {
        // sessionMgmt.showTimeout();
      } else {
        this.renderer.removeClass(this.engraveModalOverlay, 'engrave-loading');
        this.hasError = true;
        if (error.status === 400) {
          for (const i in error.error) {
            if (error.error[i].inputField === 'line1') {
              this.specialCharErrorLine1 = true;
              this.hasBadWordsSpecialCharsLine1 = error.error[i];
              this.errorCount++;
            } else if (error.error[i].inputField === 'line2') {
              this.specialCharErrorLine2 = true;
              this.hasBadWordsSpecialCharsLine2 = error.error[i];
              this.errorCount++;
            }
          }
          this.updateErrorMessage();
          this.setFocusToInputErrorField();
        } else {
          this.activeModal.close();
          this.notificationRibbonService.emitChange([true, this.messages.cartEngraveAddError.concat(' ', this.messages.engravingAddErrorGoToCart)]);
        }
      }
    });
  }

  loadEngraving() {
    this.category = this.productLevels.category.slug;
    this.categoryName = this.productLevels.category.name;
    this.subcat = this.productLevels.subcat.slug;
    this.subcatName = this.productLevels.subcat.name;

    this.specialCharErrorLine1 = false;
    this.specialCharErrorLine2 = false;
    this.disallowedCharErrorLine1 = false;
    this.disallowedCharErrorLine2 = false;
    this.hasBadWordsSpecialChars = '';
    this.errorCount = 0;

    this.engraveItemName = this.detailData.name;
    this.engraveItemType = this.detailData?.categories[0]?.slug ? this.detailData?.categories[0]?.slug : this.detailData.name;
    this.engraveHeader = this.messages['engraveHeaderText' + '-' + this.engraveItemType];
    if (!this.engraveHeader) {
      this.engraveHeader = this.translateService.instant('engraveHeaderText', { engraveCategory: this.detailData?.categories[0]?.name });
    }
    this.engraveTemplateClass = this.engraveData.templateClass;
    this.noOfLines = this.engraveData.noOfLines;
    this.engraveMaxLength = parseInt(this.engraveData.maxCharsPerLine, 10);
    this.baseUrlEngrave = this.engraveData.engraveBgImageLocation ? this.engraveData.engraveBgImageLocation : '';
    // /**Engrave Image Preview**/
    this.isImagePreview = this.engraveData.isPreview;
    this.isUpperCaseEnabled = this.engraveData.isUpperCaseEnabled;
    this.isDefaultPreviewEnabled = this.engraveData.isDefaultPreviewEnabled;
    this.previewUrl = this.engraveData.previewUrl;
    // get shortLocale from localeMapper constant
    const locale = this.user.locale.toLowerCase();
    const localeMapper = AppConstants.localeMapper[locale];
    if (localeMapper) {
      this.shortLocale = localeMapper.shortLocale;
    } else {
      this.shortLocale = locale.split('_')[1];
    }
    const callType = '/';
    this.engraveItemDisplayName = (this.engraveData.isSkuBasedEngraving) ? this.engraveItemName : this.subcatName;
    this.getItemAdditionalInfo(callType, this.detailData);

    if (this.cartItemId) {
      // get the proper back image from the merch service
      const engraveImageUrl = (this.engraveData.isSkuBasedEngraving) ? this.engraveData.engraveBgImageLocation : '';
      let product = this.detailData.categories[0].slug;
      product = product.replace(/-/g, '');
      const glbImgDomain = this.config.imageServerUrl;
      this.engravingImage = glbImgDomain + '/' + this.sharedService.getProductBackImage(this.detailData, engraveImageUrl, product, this.baseUrlEngrave);
    }
  }

  getLevelsFromCategories(data) {
    const productLevels = {};
    let categories = data.categories;
    // Below condition is applicable only when feeds point to accessories slug
    /* if (data.accessoryItem) {
      productLevels['addCat'] = categories[0];
      categories = categories[0].parents;
    } */
    // set subcat
    productLevels['subcat'] = categories[0];
    categories = categories[0].parents;
    // set category (main)
    productLevels['category'] = categories[0];

    return productLevels;
  }

  getItemAdditionalInfo(callType, detailData) {
    if (callType !== 'init') {
      this.configItemSku = (detailData.offers[0].appleSku) ? detailData.offers[0].appleSku : detailData.offers[0].sku;
    }
  }

  cancel() {
    this.sharedService.currentEngraveProductDetail = null;
    if (!this.isEdit) {
      if (this.isGiftPromo && this.updateCart) {
        this.cartService.updateGiftItemModifyCart(this.cartItemId, this.detailData.psid);
      }
      this.verifyAndNavigateToNextPage();
    } else {
      this.activeModal.close();
    }
    return false;
  }

  imagePreviewLoad() {
    this.renderer.removeClass(this.engraveImageOverlay, 'engrave-loading');
  }

  engraveLine1KeyUp() {
    this.emptyFieldError = false;
    this.specialCharErrorLine1 = false;
    this.disallowedCharErrorLine1 = false;
    this.invalidCharactersLine1 = '';
    this.errorCount = 0;
    this.hasError = false;

    // client validation
    const invalidCharsLine1 = this.validateText(this.engrave.line1);

    if (invalidCharsLine1.length !== 0) {
      this.hasError = true;
      this.disallowedCharErrorLine1 = true;
      this.invalidCharactersLine1 = invalidCharsLine1.join('');
      this.errorCount++;
    }

    if (this.hasError) {
      this.errorObject.line1 = true;
    } else {
      this.errorObject.line1 = false;
      if (this.errorObject.line2 || this.emptyFieldError) {
        this.hasError = true;
      }
    }
    this.updateErrorMessage();
  }

  engraveLine2KeyUp() {
    const engravingObj = {};
    engravingObj['line1'] = this.engrave.line1;
    engravingObj['line2'] = this.engrave.line2 ? this.engrave.line2 : '';
    this.specialCharErrorLine2 = false;
    this.disallowedCharErrorLine2 = false;
    this.invalidCharactersLine2 = '';
    this.errorCount = 0;
    this.hasError = false;
    // client validation
    const invalidCharsLine2 = this.validateText(engravingObj['line2']);
    if (invalidCharsLine2.length !== 0) {
      this.hasError = true;
      this.disallowedCharErrorLine2 = true;
      this.invalidCharactersLine2 = invalidCharsLine2.join('');
      this.errorCount++;
    }

    if (this.hasError) {
      this.errorObject.line2 = true;
    } else {
      this.errorObject.line2 = false;
      if (this.errorObject.line1 || this.emptyFieldError) {
        this.hasError = true;
      }
    }
    // if second line becomes empty, then make emptyFieldError flag as false and enable the save button
    if (this.emptyFieldError && engravingObj['line2'] === '') {
      this.emptyFieldError = false;
      this.hasError = false;
    }
    this.updateErrorMessage();
  }

  isEmptyFieldError() {
    if ((this.engrave.line1 === '' || this.engrave.line1 === undefined) && (this.engrave.line2 && this.engrave.line2.length > 0)) {
      this.hasError = true;
      this.emptyFieldError = true;
      this.errorCount++;
      return true;
    } else {
      return false;
    }
  }

  setFocusToInputErrorField() {
    setTimeout(() => {
      const inputElement = this.ele.nativeElement.querySelector('.input-error-row');
      inputElement?.firstElementChild.focus();
    }, 200);
  }

  // RESET ENGRAVE FORM
  /* resetEngraveLine(line: string) {
    this.engrave[line] = '';
    if (line === 'line1') {
      this.specialCharErrorLine1 = false;
      this.disallowedCharErrorLine1 = false;
    } else {
      this.specialCharErrorLine2 = false;
      this.disallowedCharErrorLine2 = false;
    }
    return false;
  } */

}
