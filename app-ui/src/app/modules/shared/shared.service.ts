import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {MessagesStoreService} from '@app/state/messages-store.service';
import {Messages} from '@app/models/messages';
import {catchError, map} from 'rxjs/operators';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {BehaviorSubject, Observable, Subject, throwError} from 'rxjs';
import {BaseService} from '@app/services/base.service';
import {Program} from '@app/models/program';
import {UserStoreService} from '@app/state/user-store.service';
import {AppConstants} from '@app/constants/app.constants';
import {SortOptions} from '@app/models/filter-products';
import {User} from '@app/models/user';
import {Config} from '@app/models/config';
import {CurrencyFormatPipe} from '@app/pipes/currency-format.pipe';
import {CurrencyPipe} from '@app/pipes/currency.pipe';
import {DecimalPipe} from '@angular/common';
import { CartItem } from '@app/models/cart';
import { Product } from '@app/models/product';
import { Redemption } from '@app/models/redemption';

@Injectable({
  providedIn: 'root'
})
export class SharedService extends BaseService {
  user: User;
  messages: Messages;
  propertyVal: object;
  selectedVariant: object;
  program: Program;
  paymentTemplate: string;
  anonSignInUrl;
  keepAliveOne$ = new BehaviorSubject<string>('');
  endSession$ = new BehaviorSubject<boolean>(false);
  triggerLogoutSyncEvent$ = new Subject();
  triggerPartnerSignOutUrls$ = new Subject();
  analyticsUserObject: any;
  sortOptions: SortOptions;
  currentEngraveProductDetail;
  config: Config;
  private readonly getCartItems = new BehaviorSubject<boolean>(false);
  private readonly openEngraveModal = new BehaviorSubject<object>({});
  private readonly isAbandonCartPopupOpen = new BehaviorSubject<boolean>(false);
  readonly getCartItems$ = this.getCartItems.asObservable();
  readonly openEngraveModal$ = this.openEngraveModal.asObservable();
  readonly isAbandonCartPopupOpen$ = this.isAbandonCartPopupOpen.asObservable();

  constructor(
    private router: Router,
    private messagesStore: MessagesStoreService,
    private http: HttpClient,
    public userStore: UserStoreService,
    private currencyFormatPipe: CurrencyFormatPipe,
    public currencyPipe: CurrencyPipe,
    private decimalPipe: DecimalPipe
  ) {
    super();
    this.messages = this.messagesStore.messages;
    this.analyticsUserObject = {};
    this.propertyVal = {
      catname: '',
      subcatname: '',
      addcatname: '',
      detailsname: '',
      theme: ''
    };
    this.program = this.userStore.program;
    this.config = this.userStore.config;
    this.sortOptions = {
      relevancy: {
        label: 'byRelevancy',
        sortBy: null,
        orderBy: null,
        key: 'relevancy',
        hidden: true,
      },
      popularity: {
        label: 'byPopularity',
        sortBy: 'SALES_RANK',
        orderBy: 'DESCENDING',
        key: 'popularity',
        hidden: false,
      },
      price_low_to_high: {
        label: 'byPriceLowToHigh',
        sortBy: 'DISPLAY_PRICE',
        orderBy: 'ASCENDING',
        key: 'price_low_to_high',
        hidden: false,
      },
      price_high_to_low: {
        label: 'byPriceHighToLow',
        sortBy: 'DISPLAY_PRICE',
        orderBy: 'DESCENDING',
        key: 'price_high_to_low',
        hidden: false,
      },
      product_name: {
        label: 'byProductName',
        sortBy: 'NAME',
        orderBy: 'ASCENDING',
        key: 'product_name',
        hidden: false,
      },
    };
  }

  getSelectedVariant(): any {
    return this.selectedVariant;
  }
  setSelectedVariant(val: any) {
    this.selectedVariant = val;
  }

  triggerLogoutSyncEvent() {
    this.triggerLogoutSyncEvent$.next();
  }
  triggerPartnerSignOutUrls(){
    this.triggerPartnerSignOutUrls$.next();
  }
  signOutInit(signOutUrl?) {
    const url = this.baseUrl + 'signOut';
    const httpOptions: object = {
      observe: 'response',
      responseType: 'text' as 'json'
    };
    return this.http.get(url, httpOptions).toPromise().then(() => {
      this.triggerLogoutSyncEvent();
      this.triggerPartnerSignOutUrls();
      if (signOutUrl){
        sessionStorage.clear();
        window.location.href = signOutUrl;
      }
    },
    (error: HttpErrorResponse) => {
      this.triggerLogoutSyncEvent();
      this.handleError(error);
      return throwError(error);
    });
  }

  sessionTypeAction(type: string): void {
    const storedUrls = sessionStorage.getItem('sessionURLs');
    const varUrls = JSON.parse(storedUrls);

    switch (type) {
      case 'timeOutUrl':
      case 'timeOut':
        sessionStorage.clear();
        window.location.href = varUrls.timeOutUrl;
        break;
      case 'signOut':
        this.signOutInit(varUrls.signOutUrl);
        break;
      case 'signOutPost':
        sessionStorage.clear();
        // TODO $scope.logoutPost(varUrls.signOutUrl, $scope.anonSignInUrl);
        break;
      case 'signIn':
        sessionStorage.clear();
        this.anonSignInUrl = this.userStore.program.config.signinUrl ? this.userStore.program.config.signinUrl : '';
        if (this.anonSignInUrl) {
          window.location.href = this.anonSignInUrl;
        }
        break;
      case 'navigateToHome':
        sessionStorage.clear();
        window.location.href = varUrls.homeLinkUrl;
        break;
      case 'navigateToStore':
        this.router.navigate(['/store']);
        break;
      case 'navigateTo' :
        const keystoneNavigateBackUrl = varUrls.keystoneNavigateBackUrl;
        if (keystoneNavigateBackUrl) {
          window.location.href = keystoneNavigateBackUrl;
        }else{
          this.navBackAction(varUrls.navigateBackUrl);
        }
        break;
      default:
        this.navBackAction(varUrls.navigateBackUrl);
        break;
    }
  }
  navBackAction(navigateBackUrl){
    if (navigateBackUrl) {
      // Below condition check is, specific to WF
      if (Boolean(this.config.killUserSession) && this.userStore.user && this.userStore.user.navflag) {
        this.signOutInit(navigateBackUrl);
      } else {
        window.location.href = navigateBackUrl;
      }
    }
  }

  parseSku(item): string {
    return item.psid.replace(/\//g, '-');
  }
  setProperty(key, val) {
    this.setKeyObj(this.propertyVal, key);
    this.propertyVal[key] = val;
  }
  getProperty(prop) {
    if (!this.propertyVal.hasOwnProperty(prop)) {
      return this.propertyVal;
    }
    return this.propertyVal[prop];
  }
  setKeyObj(obj, key) {
    if (!obj.hasOwnProperty(key)) {
      return obj[key] = {};
    }
  }

  getAnalyticsUserObject(routeData, points?) {
    if ( routeData && routeData.analyticsObj ) {
      this.analyticsUserObject.pgName = routeData.analyticsObj.pgName || '';
      this.analyticsUserObject.pgType = routeData.analyticsObj.pgType || '';
      this.analyticsUserObject.pgSectionType = routeData.analyticsObj.pgSectionType || '';
      if (points) {
        this.analyticsUserObject.points = points;
      }
    }
    return this.analyticsUserObject;
  }

  getOptionsDisplay(prodConfig, cat, subcat) {
    if (cat && prodConfig[cat]) {
      const categoryConfig = prodConfig[cat];
      // if show options is false for the category set options to false
      if (!categoryConfig['categoryShowOptions']) {
        return false;
      } else {
        return !(categoryConfig['subCategoryExclusions'] && categoryConfig['subCategoryExclusions'].length && categoryConfig['subCategoryExclusions'].indexOf(subcat) >= 0);
      }
    }
    return false;
  }

  /***********************Product Object**********************************
   @description Get the init products from the server.
   Gets a "set" of the products based on the category slug passed by the controller.
   @param {string} string of query string parameters to add to the service call
   @returns {array} an array of products
   TODO 7/28 consider combining this and product search service
   *************************************************************************/
  getProducts(params) {
    // get the params and add them to the URL
    const urlWparams = '/apple-gr/service/products' + params;
    return this.http.get<any>(urlWparams, this.httpOptions)
      .pipe(
        map((data) => {
          if (data.length === 0) {
            // TODO:
            // $rootScope.errorMsg = 'No products were returned for: ' + params;
            // errorLogService(400, $rootScope.errorMsg);
          }
          return data;
        }),
        catchError((error: HttpErrorResponse) => {
          this.handleError(error);
          return throwError(error);
        })
      );
  }

  transformProducts(list, subcat?, disableRequired?) {
    const prods = [];
    let pIndex = 0;
    list.forEach((prod, key) => {
      prods[pIndex] = {
        name: prod.name,
        images: prod.images,
        psid: prod.psid,
        options: prod.options,
        offers: prod.offers
      };
      pIndex++;
    });
    return prods;
  }

  transformOptions(options, subcat, disableRequired) {
    const config = [];
    let optionIndex = 0;
    for (const key of Object.keys(options)) {
      const obj = options[key];
      if (obj.length > 1) {
        const titleVal = obj[0]['i18Name'] ? obj[0]['i18Name'] : obj[0]['name'];
        config[optionIndex] = {
          name: key,
          title: key === 'caseColor' ? this.messages['caseFinish'] : titleVal,
          optionData: [],
          disabled: disableRequired ? (optionIndex > 0) : false,
          hidden: (key === 'bandSize'),
          orderBy: optionIndex,
          isDenomination: key === 'denomination'
        };
        obj.forEach((val, index) => {
          const optItem = {};
          optItem['value'] = val['value'];
          optItem['image'] = val['swatchImageUrl'] ? val['swatchImageUrl'] : '';
          optItem['key'] = val['key'];
          optItem['optDisable'] = false;
          optItem['optHidden'] = false;
          optItem['tabindex'] = (optionIndex > 0) ? -1 : 0;
          optItem['points'] = val['points'];
          config[optionIndex]['optionData'].push(optItem);

        });
        optionIndex++;
      }
    }
    return config;
  }

  psidSlugConvert(psidSlug: string) {
    return psidSlug.replace(/[&\/\\#,+()$~%.'":*?<>{}]/g, '-');
  }

  constructRouterUrl(productDetail: Product) {
    const tempType = productDetail.categories[0].templateType;
    const addCat = (productDetail.categories[0].parents[0].parents.length > 0) ? productDetail.categories[0].slug : '';
    const subcat = (addCat === '') ? productDetail.categories[0].slug : productDetail.categories[0].parents[0].slug;
    const category = (addCat === '') ? productDetail.categories[0].parents[0].slug : productDetail.categories[0].parents[0].parents[0].slug;
    const psid = this.psidSlugConvert(productDetail.psid);

    if (addCat !== '') {
      return `/store/curated/${category}/${subcat}/${addCat}/${psid}`;
    } else {
      if (tempType === 'CONFIGURABLE') {
        return `/store/configure/${category}/${subcat}/${psid}`;
      } else {
        return `/store/browse/${category}/${subcat}/${psid}`;
      }
    }
  }

  verifySkipPaymentOption(cartData) {
    this.program = this.userStore.program;
    const redemptions = Object.keys(this.program.redemptionOptions);
    if (redemptions.length === 1) {
      this.updateRedemptionOption(cartData.id, redemptions[0]);
      if (redemptions[0] === 'pointsonly' || (redemptions[0] === 'cashonly' && cartData.cost === 0)) {
        return true;
      }
    }
    else if (cartData && cartData.redemptionPaymentLimit && cartData.redemptionPaymentLimit.cashMaxLimit && cartData.cost === 0 && this.isPointsFixed()) {
      this.updateRedemptionOption(cartData.id, 'pointsfixed');
      return true;
    }
    return false;
  }

  updateRedemptionOption(cartId, selectedRedemptionOption) {
    const modifyCartUrl = this.baseUrl + '/cart/' + cartId;
    const data = { selectedRedemptionOption };
    return this.http.put(modifyCartUrl, data, this.httpOptions).subscribe((dara) => data);
  }

  showSessionTimeOut(isTimeoutError){
    this.endSession$.next(isTimeoutError);
  }

  stripUnit(pxValue) {
    return pxValue / (pxValue * 0 + 1);
  }

  convertToRemUnit(pxValue) {
    return this.stripUnit(pxValue) * 0.0668155;
  }

  getUpdatedCartItems() {
    return this.getCartItems$;
  }

  setUpdatedCartItem(value) {
    this.getCartItems.next(value);
  }

  isEngraveModalOpen() {
    return this.openEngraveModal$;
  }

  openEngraveModalDialog(obj) {
    this.openEngraveModal.next(obj);
  }

  isAbandonCartPopoverOpen() {
    return this.isAbandonCartPopupOpen$;
  }

  closeAbandonCartPopup(value) {
    this.isAbandonCartPopupOpen.next(value);
  }

  getProductBackImage(item, engraveImageUrl, product, baseUrlEngrave?) {
    baseUrlEngrave = engraveImageUrl || baseUrlEngrave;
    if (engraveImageUrl) {
      return engraveImageUrl;
    }
    let backImage;
    const itemOptions = item.options;
    let colorVal = '';
    let sizeVal = '';
    let commuicationVal = '';

    itemOptions.map(opt => {
      switch (opt.name) {
        case 'color':
          colorVal = this.convertVal('color', opt.key);
          break;
        case 'model':
          sizeVal = (opt.key === '10_5inch') ? '10in'
            : (opt.key === '11inch') ? '11in'
            : (opt.key === '12_9inch') ? '12in' : '';
          break;
        case 'communication':
          commuicationVal = (opt.key === 'nocarrier') ? 'wifi+cellular' : this.convertVal('comm', opt.key);
          break;
      }
    });

    if (commuicationVal === '') {
      backImage = baseUrlEngrave + product + '-' + colorVal + '.png';
    } else {
      if (sizeVal === '') {
        backImage = baseUrlEngrave + product + '-' + colorVal + '-' + commuicationVal + '.png';
      } else {
        backImage = baseUrlEngrave + product + '-' + sizeVal + '-' + colorVal + '-' + commuicationVal + '.png';
      }
    }
    return backImage;
  }

  convertVal(type, value) {
    const lowercaseVal = value.toLowerCase();
    return lowercaseVal.replace(' ', '');
  }

  isCarouselEnabled(pageName: string) {
    return pageName ? this.userStore.program?.carouselPages?.indexOf(pageName.toLowerCase()) > -1 : false;
  }

  isRewardsRedemption(){
    this.program = this.userStore.program;
    const redemptions = Object.keys(this.program.redemptionOptions);
    return (redemptions.indexOf(AppConstants.redemptions.pointsonly) >= 0) || (redemptions.indexOf(AppConstants.redemptions.pointsfixed) >= 0 ) || (redemptions.indexOf(AppConstants.redemptions.splitpay) >= 0);
  }

  isCashOnlyRedemption(){
    this.program = this.userStore.program;
    const redemptions = Object.keys(this.program.redemptionOptions);
    if (redemptions.length === 1){
      return (redemptions.indexOf(AppConstants.redemptions.cashonly) >= 0);
    }
    return false;
  }

  isPointsOnlyRewards() {
    this.program = this.userStore.program;
    const redemptions = Object.keys(this.program.redemptionOptions);
    return redemptions.length === 1 && redemptions.indexOf(AppConstants.redemptions.pointsonly) >= 0;
  }

  isPointsFixed(){
    this.program = this.userStore.program;
    const redemptions = Object.keys(this.program.redemptionOptions);
    return (redemptions.indexOf(AppConstants.redemptions.pointsfixed) >= 0);
  }

  filterProducts(prodList, filter, key?) {
    const results = [];
    if (prodList !== undefined) {
      prodList.map((item, index) => {
        item.options.map((option, i) => {
          const optKey = option['key'] ? option['key'].replace(/\s/, '') : '';
          const filterKey = filter.replace(/\s/, '');
          if (optKey === filterKey) {
            results.push(item);
          }
        });
      });
    }
    return results;
  }

  isPayrollType() {
    return !!([AppConstants.paymentType.payroll_default, AppConstants.paymentType.pd_variable, AppConstants.paymentType.pd_fixed, AppConstants.paymentType.pd_only, AppConstants.paymentType.cash_only].indexOf(this.config.paymentType) >= 0 && this.config.epp);
  }

  prepareErrorWarningObj(obj) {
    const message = obj || {};
    for (const key in message) {
      if (message.hasOwnProperty(key)) {
        const splitKeys = key.split(',');
        if (splitKeys.length > 1) {
          splitKeys.forEach((value) => {
            message[value] = message[key];
          });
          delete message[key];
        }
      }
    }
    return message;
  }

  returnZero() {
    return 0;
  }

  updateSplitPayOption(state) {
    if (state.selections.payment.splitPayOption) {
      if (state.selections.payment.splitPayOption.name === AppConstants.USE_MAX_POINTS) {
        state.selections.payment.splitPayOption.pointsToUse = state.redemptionPaymentLimit.useMaxPoints.points;
        state.selections.payment.pointsToUse = state.redemptionPaymentLimit.useMaxPoints.points;

        state.selections.payment.splitPayOption.cashToUse = state.redemptionPaymentLimit.useMaxPoints.amount;
        state.selections.payment.cashToUse = state.redemptionPaymentLimit.useMaxPoints.amount;

        if (state.selections.payment.splitPayOption.cashToUse > 0) {
          state.selections.payment.splitPayOption.isPaymentRequired = true;
          state.selections.payment.splitPayOption.nextStepBtnLabel = AppConstants.NEXT_STEP_PAYMENT;
        }
      } else if (state.selections.payment.splitPayOption.name === AppConstants.USE_MIN_POINTS) {
        state.selections.payment.splitPayOption.pointsToUse = state.redemptionPaymentLimit.useMinPoints.points;
        state.selections.payment.pointsToUse = state.redemptionPaymentLimit.useMinPoints.points;

        state.selections.payment.splitPayOption.cashToUse = state.redemptionPaymentLimit.useMinPoints.amount;
        state.selections.payment.cashToUse = state.redemptionPaymentLimit.useMinPoints.amount;

      } else if (state.selections.payment.splitPayOption.name === AppConstants.USE_CUSTOM_POINTS) {
        state.selections.payment.splitPayOption.pointsToUse = state.redemptionPaymentLimit.useMaxPoints.points;
        state.selections.payment.pointsToUse = state.redemptionPaymentLimit.useMaxPoints.points;

        state.selections.payment.splitPayOption.cashToUse = state.redemptionPaymentLimit.useMaxPoints.amount;
        state.selections.payment.cashToUse = state.redemptionPaymentLimit.useMaxPoints.amount;
      }

      if (state.selections.payment.splitPayOption.cashToUse > 0) {
        state.selections.payment.splitPayOption.isPaymentRequired = true;
        state.selections.payment.splitPayOption.nextStepBtnLabel = AppConstants.NEXT_STEP_PAYMENT;
      } else {
        state.selections.payment.splitPayOption.isPaymentRequired = false;
        state.selections.payment.splitPayOption.nextStepBtnLabel = AppConstants.NEXT_STEP_REVIEW_YOUR_ORDER;
      }
    }
    return state;
  }

  getTranslateParams(params, paramsWithTitleCase, state): { [key: string]: any } {
    const translatedParams = {
      params: {...params},
      titleCaseParams: {...paramsWithTitleCase}
    };

    if (state.cart && state.cart.cartTotal) {
      translatedParams.params.points = this.currencyFormatPipe.transform(state.cart.cartTotal.price.points, this.userStore.config.pricingTemplate, this.userStore.user.locale);
      translatedParams.titleCaseParams.points = this.currencyFormatPipe.transform(state.cart.cartTotal.price.points, this.userStore.config.pricingTemplate, this.userStore.user.locale);
      if (this.userStore.config.showDecimal) {
        translatedParams.params.amount = this.currencyPipe.transform(state.cart.cartTotal.price.amount);
        translatedParams.titleCaseParams.amount = this.currencyPipe.transform(state.cart.cartTotal.price.amount);
      } else {
        translatedParams.params.amount = this.currencyPipe.transform(state.cart.cartTotal.price.amount, '', 'symbol', '1.0-0');
        translatedParams.titleCaseParams.amount = this.currencyPipe.transform(state.cart.cartTotal.price.amount, '', 'symbol', '1.0-0');
      }
    }

    if (state.cart && state.cart.redemptionPaymentLimit) {
      if (state.cart.redemptionPaymentLimit.useMinPoints) {
        translatedParams.params.useMinPoints = this.currencyFormatPipe.transform(state.cart.redemptionPaymentLimit.useMinPoints.points, this.userStore.config.pricingTemplate, this.userStore.user.locale);
        translatedParams.titleCaseParams.useMinPoints = this.currencyFormatPipe.transform(state.cart.redemptionPaymentLimit.useMinPoints.points, this.userStore.config.pricingTemplate, this.userStore.user.locale);
        if (this.userStore.config.showDecimal) {
          translatedParams.titleCaseParams.useMinAmount = this.currencyPipe.transform(state.cart.redemptionPaymentLimit.useMinPoints.amount);
          translatedParams.params.useMinAmount = this.currencyPipe.transform(state.cart.redemptionPaymentLimit.useMinPoints.amount);
        } else {
          translatedParams.titleCaseParams.useMinAmount = this.currencyPipe.transform(state.cart.redemptionPaymentLimit.useMinPoints.amount, '', 'symbol', '1.0-0');
          translatedParams.params.useMinAmount = this.currencyPipe.transform(state.cart.redemptionPaymentLimit.useMinPoints.amount, '', 'symbol', '1.0-0');
        }
      }
      if (state.cart.redemptionPaymentLimit.useMaxPoints) {
        translatedParams.params.useMaxPoints = this.currencyFormatPipe.transform(state.cart.redemptionPaymentLimit.useMaxPoints.points, this.userStore.config.pricingTemplate, this.userStore.user.locale);
        translatedParams.titleCaseParams.useMaxPoints = this.currencyFormatPipe.transform(state.cart.redemptionPaymentLimit.useMaxPoints.points, this.userStore.config.pricingTemplate , this.userStore.user.locale);
        if (this.userStore.config.showDecimal) {
          translatedParams.params.useMaxAmount = this.currencyPipe.transform(state.cart.redemptionPaymentLimit.useMaxPoints.amount);
          translatedParams.titleCaseParams.useMaxAmount = this.currencyPipe.transform(state.cart.redemptionPaymentLimit.useMaxPoints.amount);
        } else {
          translatedParams.params.useMaxAmount = this.currencyPipe.transform(state.cart.redemptionPaymentLimit.useMaxPoints.amount, '', 'symbol', '1.0-0');
          translatedParams.titleCaseParams.useMaxAmount = this.currencyPipe.transform(state.cart.redemptionPaymentLimit.useMaxPoints.amount, '', 'symbol', '1.0-0');
        }
      }
      if (state.cart.redemptionPaymentLimit.cartMaxLimit) {
        translatedParams.params.cartMaxLimit = this.decimalPipe.transform(state.cart.redemptionPaymentLimit.cartMaxLimit.points);
        translatedParams.titleCaseParams.cartMaxLimit = this.decimalPipe.transform(state.cart.redemptionPaymentLimit.cartMaxLimit.points);
      }
      if (state.selections && state.selections.payment && state.selections.payment.splitPayOption) {
        translatedParams.params.cashToUse = this.currencyPipe.transform(state.selections.payment.splitPayOption.cashToUse);
      }
    }
    return translatedParams;
  }

  getPaymentMaxLimit(program): number {
    const redemptionOptions = Object.keys(program.redemptionOptions);

    for (let i = 0; i < redemptionOptions.length; i++) {
      if (redemptionOptions[i] === 'splitpay') {
        // @ts-ignore
        return program.redemptionOptions.splitpay[i].paymentMaxLimit;
      }
    }
  }

  scrollToProductDetails(elementId: string, child?, viewportScroller?): void {
    if (child?.prodInfoSection.collapsed) {
      child.prodInfoSection.toggle();
    }

    setTimeout(() => {
      viewportScroller.scrollToAnchor(elementId);
    }, 300);
  }
  // Determines the number of selectable options available
  setOptionLength(itemOptions) {
    let count = 0;
    if (itemOptions !== undefined) {
      itemOptions.forEach((opts, index) => {
        if (opts.hidden === false) {
          count++;
        }
      });
    }
    return count;
  }

  verifyProductOptionOnProduct(selectedConfigItem, product, dependedOptions) {
    const productOption = {};
    for (const option of product.options) {
      productOption[option['name']] = option['key'];
    }
    for (const optionSelection in selectedConfigItem) {
      if (dependedOptions.indexOf(optionSelection) >= 0) {
        if (selectedConfigItem[optionSelection] !== productOption[optionSelection]) { // Next option selection not match with the product
          return false;
        }
      }
    }
    return true;
  }


  getPricingTemp(tempType, ext, verifyFullDiscount?, isDiscounted?, hideFullDiscount?): string {
    if (verifyFullDiscount && isDiscounted.fullPointDiscounted && isDiscounted.fullCashDiscounted){
      if (hideFullDiscount){
        return 'no-pricing.htm';
      }
      return ext + 'full-discounted.htm';
    }
    const splitTemp = tempType.split('_');
    const isPoints = (splitTemp[0] === 'points');
    const  isSinglePrice = ['points_only', 'cash_only', 'points_decimal'].indexOf(tempType) >= 0;
    if ((ext === 'full_' || ext === 'alt_') && tempType === 'pd_cash') {
      return ext + tempType + '-template.htm';
    }
    else if (ext === 'dual_'){
      /*specifically for checkout - check and provide and conversion*/
      return  ext + 'points_and_currency-template.htm';
    }
    else {
      if (isSinglePrice) {
        if (!isPoints) {
          return (ext === 'qty_') ? ext + 'cash-template.htm' : 'cash-template.htm';
        } else {
          return (ext === 'qty_') ? ext + 'rewards-template.htm' : 'rewards-template.htm';
        }
      } else if (tempType === 'no_pay') {
        return '';
      }
      else {
        return (ext === 'qty_') ? ext + tempType + '-template.htm' : tempType + '-template.htm';
      }
    }
  }

  constructDependedOptions(itemOptions, productByConfigOptions) {
    // prepare min product config object
    const dependedOptions = [];
    for (const configOption of itemOptions) {
      productByConfigOptions[configOption.name] = configOption;
      productByConfigOptions[configOption.name].options = {};
      productByConfigOptions[configOption.name].showOptionsFromPrice = false;

      for (const option of configOption.optionData) {
        // option['minProduct'] = '';
        option['dependedOptions'] = [...dependedOptions];
        productByConfigOptions[configOption.name].options[option.key.split(' ').join('')] = option;
      }
      dependedOptions.push(configOption.name);
    }
  }

  giftAttributeUpdates(cartItems: CartItem[]){
    if (cartItems){
      for (const cart in cartItems) {
        if (cartItems[cart]) {
          cartItems[cart].productDetail.isEligibleForGift = cartItems[cart].productDetail.addOns.availableGiftItems.length > 0;
          cartItems[cart].productDetail.isMultiGiftAvailable = cartItems[cart].productDetail.addOns.availableGiftItems.length > 1;
        }
      }
    }
    return cartItems;
  }

  getPriceData(data) {
   const shippingCost = {
      amount: data.shippingPrice.amount,
      points: data.shippingPrice.points
    };
    const totalTaxes = {amount: data.totalTaxes.amount, points: data.totalTaxes.points};
    const totalFees = {amount: data.totalFees.amount, points: data.totalFees.points};

    const discountParams = (data.discountedItemsSubtotalPrice) ? {
      amount: data.discountedItemsSubtotalPrice.amount,
      points: data.discountedItemsSubtotalPrice.points,
      discountAmt: data.discountAmount
    } : null;

    const cartTotals = (data.discountedPrice) ? {
      amount: data.discountedPrice.amount,
      points: data.discountedPrice.points
    } : {amount: data.price.amount, points: data.price.points};

  const cartDiscountedSubtotal = (data.discountedItemsSubtotalPrice) ? {
      amount: data.discountedItemsSubtotalPrice.amount,
      points: data.discountedItemsSubtotalPrice.points
    } : null;

    return {
      shippingCost: shippingCost,
      totalTaxes: totalTaxes,
      totalFees: totalFees,
      discountParams: discountParams,
      cartTotals: cartTotals,
      cartDiscountedSubtotal: cartDiscountedSubtotal
    };
  }

  getProductRedValue(productData, productRed) {
    const productValue = [];
    productData.forEach((e) => {
      if (productRed.indexOf(e['key']) >= 0) {
        productValue.push(e);
      } else {
        productValue.unshift(e);
      }
    });
    return productValue;
  }

  /**
   * Get Split Pay Option By Limit Type
   * @param {string} type - Limit Type
   * @returns {Redemption}
   */
   getSplitPayLimitType(type: string): Redemption  {
    const splitPayOption = this.userStore.program.redemptionOptions['splitpay']?.find(option => option.limitType === type);

    return splitPayOption;
  }
}
