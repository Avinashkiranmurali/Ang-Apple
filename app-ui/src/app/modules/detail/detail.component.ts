import { Component, Injector, OnDestroy, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ViewportScroller } from '@angular/common';
import { CartService } from '@app/services/cart.service';
import { DetailService } from '@app/services/detail.service';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { TemplateStoreService } from '@app/state/template-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { Config } from '@app/models/config';
import { Messages } from '@app/models/messages';
import { ModalsService } from '@app/components/modals/modals.service';
import { Product } from '@app/models/product';
import { Program } from '@app/models/program';
import { User } from '@app/models/user';
import { SharedService } from '@app/modules/shared/shared.service';
import { TemplateService } from '@app/services/template.service';
import { Subscription } from 'rxjs';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { PricingModel } from '@app/models/pricing-model';
import { BreakPoint } from '@app/components/utils/break-point';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { AppConstants } from '@app/constants/app.constants';
import { SessionService } from '@app/services/session.service';
import { ProductInformationComponent } from '@app/modules/shared/product-information/product-information.component';
import { HeapService } from '@app/analytics/heap/heap.service';
import { isEmpty } from 'lodash';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';
import { SmartPrice } from '@app/models/smart-price';

@Component({
  selector: 'app-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.scss'],
  encapsulation: ViewEncapsulation.None,
})

export class DetailComponent extends BreakPoint implements OnInit, OnDestroy {

  @ViewChild(ProductInformationComponent) child: ProductInformationComponent;

  messages: Messages;
  user: User;
  program: Program;
  config: Config;
  detailsTemplate: {};
  detailLoadError: boolean;
  detailsLoading: boolean;
  translateParams: { [key: string]: any };
  psid: string;
  details: Product;
  routeParams: { [key: string]: string };
  optionsLength: number;
  addCatName: string;
  subcatName: string;
  categoryName: string;
  isShowOptions: boolean;
  showDetailSpecsByValue: boolean;
  showDetailSpecsByTitle: boolean;
  selectedOptions: object;
  disableRequired: boolean;
  variantInfo: object;
  psidVal: string;
  additionalDetailsPageMessage: string;
  priceModel: PricingModel;
  itemOptions: Array<any> = [];
  itemAdditionalInfo: object;
  configItemSku: string;
  configItemUpc: string;
  showTaxDisclaimer: boolean;
  currentAddCatData: object;
  selectedProductVariant: any;
  selectedVariant: object = {};
  checkoutAddress: object;
  withVariationCheck: boolean;
  // product-summary properties
  allOptionsSelected: boolean;
  hoverColor: string;
  isAccessory: boolean;
  index: number;
  width: number;
  withVariationRequired = true;
  showCellularNote: boolean;
  setTaxFeeTitle: string;
  displaySubNav: string;
  selection: string;
  optionsConfigurationColor: object;
  prodBySelected: Array<string>;
  nextOptSet: number;
  selectedProductVariants: Array<object>;
  progId: string;
  analyticsUserObject: any;
  paymentMaxLimit: number;
  isServicePlansExist: boolean;
  selectedAppleCareServicePsid: string;
  productByConfigOptions = {};
  userProductsFilterBySelections: Product[];
  private subscriptions: Subscription[] = [];
  detailPageCostBreakdownTemplate: string;
  detailPageTemplate: string;
  buttonColor: string;
  carouselImages: Array<string> = [];
  selectedVariantText: string;
  smartPrice: SmartPrice;
  showSplitPayMessage: boolean = false;

  constructor(
    public viewportScroller: ViewportScroller,
    public messageStore: MessagesStoreService,
    public userStore: UserStoreService,
    public injector: Injector,
    private templateStoreService: TemplateStoreService,
    private detailService: DetailService,
    public sharedService: SharedService,
    private templateService: TemplateService,
    private activatedRoute: ActivatedRoute,
    private parsePsidPipe: ParsePsidPipe,
    private currencyPipe: CurrencyPipe,
    public router: Router,
    public modalsService: ModalsService,
    public cartService: CartService,
    private ensightenService: EnsightenService,
    private matomoService: MatomoService,
    private sessionService: SessionService,
    private heapService: HeapService
  ) {
    super(injector);
    this.messages = messageStore.messages;
    this.user = this.userStore.user;
    this.program = this.userStore.program;
    this.config = this.userStore.config;
    this.detailsTemplate = this.templateStoreService.detailsTemplate;
    this.disableRequired = true;
    this.details = {} as Product;
    this.allOptionsSelected = true;
    this.analyticsUserObject = {};
    this.paymentMaxLimit = this.sharedService.getPaymentMaxLimit(this.program);
    this.productByConfigOptions = {};
    this.showSplitPayMessage = Boolean(this.config.showSplitPayMessage);
    this.activatedRoute.params.subscribe(
      params => {
        this.routeParams = {
          category: params['category'],
          subcat: params['subcat'],
          psid: params['psid'].replace('-', '/')
        };
        this.analyticsUserObject = this.sharedService.getAnalyticsUserObject(this.activatedRoute.snapshot.data);
        this.detailsLoading = true;
        this.isAccessory = false;
        this.itemOptions = [];
        this.selectedAppleCareServicePsid = '';
        this.carouselImages = [];
        this.getDetails();
        this.cartService.initError();
      });
    this.detailPageCostBreakdownTemplate = this.detailPageData().costBreakdownTemplate ? this.detailPageData().costBreakdownTemplate : '';
    this.detailPageTemplate = this.detailPageData().template ? this.detailPageData().template : '';
    this.buttonColor = this.templateService.getBtnColor();
  }

  ngOnInit(): void {
  }


  getDetails(dataVal?: any): void {

    const isAccessory = false;
    if (isAccessory && this.variantInfo['withVariationCheck']) {
      sessionStorage.removeItem('detailData');
    }
    if (dataVal !== undefined) {
      this.variantInfo = dataVal;
      this.psidVal = (this.variantInfo['psid']) ? this.variantInfo['psid'] : this.routeParams['psid'];
      this.withVariationCheck = (this.variantInfo['withVariationCheck'] !== '') ? this.variantInfo['withVariationCheck'] : '';
    } else {
      this.psidVal = this.routeParams['psid'];
      this.withVariationCheck = false;
    }
    const variation = (this.withVariationCheck) ? '' : '?withVariations=' + this.withVariationCheck;
    const params = this.psidVal + variation;

    this.subscriptions.push(
      this.detailService.getDetails(params).subscribe(
        data => {
          this.details = data;
          const productDetail = data;
          this.userStore.detailsname = data.name;
          sessionStorage.setItem('detailDataInfo', JSON.stringify(productDetail));
          sessionStorage.setItem('callType', '/');
          this.psid = productDetail.psid;
          const giftEligibleMessagePsid = 'giftQualifiedDetailsPageMessage-' + this.parsePsidPipe.transform(this.psid, '');
          this.additionalDetailsPageMessage = this.messages[giftEligibleMessagePsid];
          if (productDetail) {
            this.priceModel = productDetail.additionalInfo['PricingModel'];
            this.translateParams = {
              paymentValue: this.currencyPipe.transform((this.priceModel && this.priceModel.paymentValue) ? this.priceModel.paymentValue : 0),
              repaymentTerm: (this.priceModel && this.priceModel?.repaymentTerm) ? (this.priceModel.repaymentTerm) : 0,
              minPaymentValue: 0
            };
            if (productDetail.optionsConfigurationData && Object.keys(productDetail.optionsConfigurationData).length > 0) {

              this.itemOptions = this.itemOptions.length === 0 ? this.sharedService.transformOptions(productDetail.optionsConfigurationData, this.routeParams['subcat'], this.disableRequired) : this.itemOptions;

              if (this.itemOptions.length > 0) {
                this.itemOptions[0].optionData = this.itemOptions[0].optionData.sort((a, b) => (a.value === b.value) ? 1 : -1);
                if (this.itemOptions && this.itemOptions.length > 0) {
                  this.itemOptions[0].optionData = this.sharedService.getProductRedValue(this.itemOptions[0].optionData, ['(product)red', 'product_red']);
                }
              }
              this.optionsLength =  this.sharedService.setOptionLength(this.itemOptions);

              const nameVal = {};
              for (const key of Object.keys(productDetail.options)) {
                const obj = productDetail.options[key];
                nameVal[obj['name']] = obj['key'];
                this.selectedVariantText = obj['value'];
              }
              this.selectedVariant = nameVal;
              this.sharedService.constructDependedOptions(this.itemOptions, this.productByConfigOptions);
              this.productFindingByConfigOptions(this.details.variations); // configProducts
            }
            productDetail.options.forEach((opt) => {
              const optOrder = ['storage', 'memory', 'processor', 'graphics'];
              opt.orderBy = optOrder.indexOf(opt.name);
            });

            this.setProductDetailData(productDetail);
            this.showTaxDisclaimer = this.config.showTaxDisclaimer;
            const currentAddCatData = this.details['categories'][0];
            this.currentAddCatData = currentAddCatData;
            const currentSubCatData = currentAddCatData ? currentAddCatData.parents[0] : '';
            const currentCatData = currentAddCatData ? currentSubCatData['parents'][0] : '';

            this.addCatName = (currentAddCatData && currentAddCatData.i18nName) ? currentAddCatData.i18nName : '';
            this.subcatName = (this.subcatName && this.subcatName !== '') ? this.subcatName : (currentSubCatData) ? currentSubCatData.i18nName || '' : '';
            this.categoryName = (this.categoryName && this.categoryName !== '') ? this.categoryName : (currentCatData) ? currentCatData.i18nName || '' : '';
            // Set; Breadcrumb; data;
            this.sharedService.setProperty('catname', this.categoryName);
            this.sharedService.setProperty('subcatname', this.subcatName);
            if (this.addCatName !== '') {
              this.sharedService.setProperty('addcatname', this.addCatName);
            }
            this.sharedService.setProperty('detailsname', this.details['name']);
            this.isShowOptions = true;
            const prodConfig = this.productConfigurationData();
            if (prodConfig) {
              this.isShowOptions = this.sharedService.getOptionsDisplay(prodConfig, productDetail.categories[0].parents[0].slug, productDetail.categories[0].slug);
            }
            let pageName = this.analyticsUserObject.pgName || '';
            pageName = pageName.replace('<product>', this.details.name ? this.details.name : '');
            this.analyticsUserObject.pgName = pageName.toLowerCase();
            this.ensightenService.broadcastEvent(this.analyticsUserObject, [this.details]);
            this.heapService.broadcastEvent(AppConstants.analyticServices.HEAP_EVENTS.ITEM_VIEWED, this.details);

            // set default selected variant only when item options exists and selected variant is null
            if (Object.keys(this.selectedVariant).length === 0 && this.itemOptions.length !== 0) {
              this.setDefaultVariant(this.details);
            }
            this.carouselImages = this.details.carouselImages;
          } else {
            this.detailLoadError = true;
          }
          this.showDetailSpecsByTitle = this.getShowDetailSpecsByTitle('');
          this.showDetailSpecsByValue = this.getShowDetailSpecsByValue('');
          this.detailsLoading = false;
          this.isServicePlansExist = !isEmpty(this.details.addOns.servicePlans);
          this.selectedAppleCareServicePsid = '';
          this.smartPrice = this.details.smartPrice;
        },
        error => {
          this.detailsLoading = false;
          if (error.status === 401 || error.status === 0) {
            this.sessionService.showTimeout();
          } else {
            this.detailLoadError = true;

            this.matomoService.broadcast(AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.CANONICAL_PAGE, {
              payload: {
                location: location.href,
                canonicalTitle: AppConstants.analyticServices.CANONICAL_CONSTANTS.ERROR
              }
            });
          }
        }
      ));
  }
  productFindingByConfigOptions(configProducts) {
    // Set filtered products
    this.setFilteredProducts(configProducts);
    // this.loadProductDetails();
    // 1. Loop the Product List
    for (const product of configProducts) {
      // 2. Loop the options List inside the product
      for (const configOption of product.options) {
        // 3. Define new product option key if not initiated and assign the product
        const key = configOption.key.split(' ').join('');
        if (!this.productByConfigOptions[configOption.name]) {
          continue;
        }
        this.productByConfigOptions[configOption.name].options[key].hidden = false;
        // minProduct configuration not required in details component
        // 4.1 config selection exist then find the product options satisfied with the selection and compare
        if (Object.keys(this.selectedVariant).length > 0) {
          const productOptionMatched = this.sharedService.verifyProductOptionOnProduct(this.selectedVariant, product, this.productByConfigOptions[configOption.name].options[key].dependedOptions);
          this.productByConfigOptions[configOption.name].options[key].hidden = !productOptionMatched;
        }
      }
    }
  }
  setFilteredProducts(configProducts){
    this.userProductsFilterBySelections = [...configProducts];
    let enableNextOption = true;
    let disableAllNextOption = false;
    let allOptionSelected = true;
    for (const option in this.productByConfigOptions){
      if (option){
        this.productByConfigOptions[option].filteredProducts = [];
        if (enableNextOption){
          this.productByConfigOptions[option].disabled = !enableNextOption;
        }else{
          delete this.selectedVariant[option];
          allOptionSelected = false;
        }
        if (disableAllNextOption){
          allOptionSelected = false;
          this.productByConfigOptions[option].disabled = true;
        }
        // Set Filtered Products and enable next options
        enableNextOption = false;
        if (this.selectedVariant[option]){ // Selected key and Value
          this.userProductsFilterBySelections = this.sharedService.filterProducts(this.userProductsFilterBySelections, this.selectedVariant[option]);
          this.productByConfigOptions[option].filteredProducts = this.userProductsFilterBySelections;
          if (this.userProductsFilterBySelections.length > 0){
            enableNextOption = true;
          }else{
            allOptionSelected = false;
            disableAllNextOption = true;
            delete this.selectedVariant[option];
          }
        }else{
          allOptionSelected = false;
        }
      }
    }
    this.allOptionsSelected = allOptionSelected;
  }

  addToCart(psid: string, detl: Product) {
    this.cartService.addToCart(psid, detl, null, this.selectedAppleCareServicePsid);
  }

  mouseHoverEvent(hoverValue: string) {
    this.hoverColor = hoverValue;
  }

  mouseLeaveEvent() {
    this.hoverColor = '';
  }

  getDetailswithPsid(psid, withVariationCheck) {
    this.getDetails({psid, withVariationCheck});
  }

  detailPageData() {
    return this.templateService.getTemplatesProperty('detail');
  }

  changeVariantOption(parentIndex, optionObj, optionSet, isInitialState?) {
    this.selectedVariant[optionSet['name']] = optionObj;
    if (!isInitialState) { // Should be called only on change event
      this.productFindingByConfigOptions(this.details.variations);
      this.selectedVariantText = this.userProductsFilterBySelections[0].options[0].value;
      this.getDetailswithPsid(this.userProductsFilterBySelections[0].psid, !this.withVariationRequired);
    }
  }

  setDefaultVariant(detailData) {
    for (const option of detailData.options) {
      this.selectedVariant[option.name] = option.key;
    }
    const selectedVariantKeys = Object.keys(this.selectedVariant);
    this.changeVariantOption(0, this.selectedVariant[selectedVariantKeys[0]], detailData.options[0], true);
    this.allOptionsSelected = true;
  }

  productConfigurationData() {
    return this.templateService.getProperty('productConfiguration');
  }

  getShowDetailSpecsByTitle(title) {
    const isTitle = title.search('Title');
    const hasDimensionOpReqs = (title === 'displayWidth' || title === 'displayHeight' || title === 'displayDepth' || title === 'displayWeight' || title === 'displayLength' || title === 'displayDiameter' || title === 'operatingRequirements');
    return (isTitle < 0 && !hasDimensionOpReqs);
  }

  getShowDetailSpecsByValue(spec) {
    return !(spec === '' || spec === '&nbsp;');
  }

  setOptionLength() {
    let count = 0;
    if (this.itemOptions !== undefined) {
      this.itemOptions.forEach(opts => {
        if (opts.hidden === false) {
          count++;
        }
      });
    }
    this.optionsLength = count;
  }

  setProductDetailData(productDetail) {
    this.details = productDetail;
    this.itemAdditionalInfo = {...this.details['additionalInfo']};
    this.configItemSku = (productDetail.offers[0].appleSku) ? this.details['offers'][0].appleSku : productDetail['offers'][0].sku;
    this.configItemUpc = productDetail.upc;
  }

  errorHandler(event) {
    /* TODO - CHECK THIS FUNCTIONALITY */
    for (const key in event) {
      if (event[key] !== undefined) {
        this[key] = event[key];
      }
    }
  }

  selectedAppleCareServicePlan(appleCarePsid) {
    this.selectedAppleCareServicePsid = appleCarePsid;
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }
}
