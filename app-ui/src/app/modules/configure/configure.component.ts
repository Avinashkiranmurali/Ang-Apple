import { Component, OnDestroy, OnInit, Injector, ViewChild, ViewEncapsulation } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ViewportScroller } from '@angular/common';
import { SharedService } from '@app/modules/shared/shared.service';
import { BreakPoint } from '@app/components/utils/break-point';
import { TemplateService } from '@app/services/template.service';
import { UserStoreService } from '@app/state/user-store.service';
import { Config } from '@app/models/config';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { Product } from '@app/models/product';
import { ModalsService } from '@app/components/modals/modals.service';
import { DetailService } from '@app/services/detail.service';
import { ProductService } from '@app/services/product.service';
import { NavStoreService } from '@app/state/nav-store.service';
import { Category } from '@app/models/category';
import { SmartPrice } from '@app/models/smart-price';
import { OptionDataConfig } from '@app/models/optionDataConfig';
import { ProductDataConfig } from '@app/models/productDataConfig';
import { Program } from '@app/models/program';
import { Messages } from '@app/models/messages';
import { Subscription } from 'rxjs';
import { User } from '@app/models/user';
import { ProductsWithConfiguration } from '@app/models/products-with-configuration';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';
import { NotificationRibbonService } from '@app/services/notification-ribbon.service';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { PricingModel } from '@app/models/pricing-model';
import { ProductInformationComponent } from '@app/modules/shared/product-information/product-information.component';
import { HeapService } from '@app/analytics/heap/heap.service';
import { AppConstants } from '@app/constants/app.constants';
import { isEmpty } from 'lodash';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';
import { AplImgSizePipe } from '@app/pipes/apl-img-size.pipe';
import { CartService } from '@app/services/cart.service';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-configure',
  templateUrl: './configure.component.html',
  styleUrls: ['./configure.component.scss'],
  encapsulation: ViewEncapsulation.None,
})

export class ConfigureComponent extends BreakPoint implements OnInit, OnDestroy {

  @ViewChild(ProductInformationComponent) child: ProductInformationComponent;
  detailPageCostBreakdownTemplate: string;

  constructor(
    private activatedRoute: ActivatedRoute,
    public viewportScroller: ViewportScroller,
    public sharedService: SharedService,
    public injector: Injector,
    private templateService: TemplateService,
    public userStore: UserStoreService,
    public messageStore: MessagesStoreService,
    public productService: ProductService,
    public detailService: DetailService,
    public modalsService: ModalsService,
    public mainNavStore: NavStoreService,
    private parsePsidPipe: ParsePsidPipe,
    public cartService: CartService,
    private ensightenService: EnsightenService,
    private notificationRibbonService: NotificationRibbonService,
    private currencyPipe: CurrencyPipe,
    private heapService: HeapService,
    private aplImgSizePipe: AplImgSizePipe,
    private http: HttpClient
  ) {
    super(injector);
    this.routeParams = {};
    this.config = this.userStore.config;
    this.user = this.userStore.user;
    this.messages = messageStore.messages;
    this.program = this.user.program;
    this.paymentMaxLimit = this.sharedService.getPaymentMaxLimit(this.program);
    this.pointLabel = this.config.pointLabel;
    this.showTaxDisclaimer = this.config.showTaxDisclaimer;
    this.mainNav = this.mainNavStore.mainNav;
    this.configImageBaseURL = this.config.imageServerUrl + '/apple-gr/assets/img/customizable/';
    this.allOptionsSelected = false;
    this.productLoadError = false;
    this.showCellularNote = false;
    this.showSummary = false;
    this.showSplitPayMessage = Boolean(this.config.showSplitPayMessage);
    this.analyticsUserObject = {};
    this.activatedRoute.params.subscribe(
      params => {
        this.selectedConfigItem = {};
        this.productByConfigOptions = {};
        this.configItemSku = '';
        this.configItemUpc = '';
        this.allOptionsSelected = false;
        this.subcat = params['subcat'];
        this.category = params['category'];
        this.psid = (params['sku'] || '').replace('-', '/');
        this.config.categoryName = this.category;
        this.config.subcatName = this.subcat;
        this.pageName = this.category;
        this.slug = this.category;
        this.carouselImages = [];
        const currentCat = this.mainNav.filter(nav => nav.slug === this.category);
        const currentSubCat = currentCat[0].subCategories.filter(subCat => subCat.slug === this.subcat);
        this.subcatName = currentSubCat[0]['i18nName'];
        const configImageBaseURL = (this.messages[this.subcat + '-configurationImageUrl']) ? this.messages[this.subcat + '-configurationImageUrl'] : currentSubCat[0].defaultImage;
        if (!configImageBaseURL) {
          this.getProductImage(currentSubCat[0]); // display default image if configImageURL is not available
        } else {
          this.displayImage = this.config.imageServerUrl + '/' + configImageBaseURL;
        }
        this.selectedAppleCareServicePsid = '';
        this.getProducts();
      }
    );
    this.detailPageCostBreakdownTemplate = this.detailPageData().costBreakdownTemplate ? this.detailPageData().costBreakdownTemplate : '';
  }

  program: Program;
  paymentMaxLimit: number;
  routeParams: { [key: string]: string };
  configImgAlt: string;
  filteredByPrimary: object;
  itemOptions: OptionDataConfig[];
  displayImage: string;
  userProductsFilterBySelections: ProductDataConfig[];
  selectedGfOption: { item: ProductDataConfig };
  tempOptionsList: string[];
  optionsLength: number;
  subcat: string;
  subcatName: string;
  isShowOptions: boolean;
  configItemSku: string;
  configItemUpc: string;
  additionalConfigPageMessage: string;
  priceModel: PricingModel;
  translateParams: { [key: string]: any };
  configProducts: ProductDataConfig[];
  details: Product;
  psid: string;
  configImageBaseURL: string;
  allOptionsSelected: boolean;
  showCellularNote: boolean;
  itemToCart: Product;
  selectedConfigItem = {};
  config: Config;
  user: User;
  messages: Messages;
  setTaxFeeTitle: boolean;
  pointLabel: string;
  showTaxDisclaimer: boolean;
  productLoadError: boolean;
  mainNav: Array<Category>;
  category: string;
  checkoutAddress: object;
  cartSummaryModified: boolean;
  fullCatalog: string;
  private subscriptions: Subscription[] = [];

  pageName: string;
  showSummary: boolean;
  slug: string;
  analyticsUserObject: any;
  productByConfigOptions = {};
  isServicePlansExist: boolean;
  selectedAppleCareServicePsid: string;
  buttonColor: string;
  carouselImages: Array<string> = [];
  smartPrice: SmartPrice;
  detailCallLoading: boolean = false;
  showSplitPayMessage: boolean = false;

  ngOnInit(): void {
    this.cartService.initError();

    // CONSTRUCT AND TRIGGER USER ANALYTICS OBJECT
    const routeData = this.activatedRoute.snapshot.data;
    if (routeData && routeData.analyticsObj) {
      this.analyticsUserObject.pgName = routeData.analyticsObj.pgName || '';
      this.analyticsUserObject.pgType = routeData.analyticsObj.pgType || '';
      this.analyticsUserObject.pgSectionType = routeData.analyticsObj.pgSectionType || '';
    }
    this.buttonColor = this.templateService.getBtnColor();
  }
  /**
   * uses the selectedGfOption.value ng-modal to sort through gift card products that are filter by the
   * user or to sort through the only list provided.
   *
   * @param parentIndex
   * @param optionSetName
   */
  filterProductsFromDropdown(parentIndex, optionSetName) {
    if (!this.selectedGfOption.item || !Object.keys(this.selectedGfOption.item).length) {
      this.selectedGfOption.item = this.userProductsFilterBySelections[0];
    }

    if (parentIndex < this.optionsLength - 1) {
      this.configSummaryText(this.selectedConfigItem);
    }

    if (Object.keys(this.selectedGfOption.item).length) {
      this.returnSelectedProduct(this.selectedGfOption.item, this.subcat, this.selectedConfigItem);
    }
  }

  filterProducts(prodList, filter, key?) {
    const results = [];

    if (prodList !== undefined) {
      prodList.map(item => {
        item.options.filter(option => {
          const optKey = option['key'] ? option['key'].replace(/\s/, '') : '';
          const filterKey = filter.replace(/\s/, '');
          if ((!key && optKey === filterKey) || (key && key === option['name'] && optKey === filterKey)) {
            results.push(item);
          }
        });
      });
      return results;
    }

  }

  filterProductsFromUserSelections(parentIndex, optionSetName, optionValue, optionKey, preSelection?) {
    if (optionValue) {
      this.selectedConfigItem[optionSetName] = optionKey;
    }
    this.productFindingByConfigOptions(this.configProducts);
  }

  /**
   * controls the summary of the options displayed to the user
   *
   * @param selectedOpts
   */
  configSummaryText(selectedOpts) {
    const selectOptsArr = Object.keys(selectedOpts).map((itm) => selectedOpts[itm]);
    const arrIndex = (selectedOpts.hasOwnProperty('model')) ? 1 : 0;
    const subcatModel = (arrIndex === 1 && selectedOpts.model.indexOf(this.subcatName) >= 0);

    if (!subcatModel) {
      selectOptsArr.splice(arrIndex, 0, this.subcatName);
    }
    this.configImgAlt = selectOptsArr.join(' ');
  }

  /**
   * Provides the config page image url based on option selection (first option selection)
   *
   * @param newProd
   * @param selectedOpts
   */
  configuredProductImage(newProd, selectedConfigItem) {
    if (newProd[0]) {
      const optionKeys = Object.keys(selectedConfigItem);
      if (this.subcat && optionKeys.length === 1) {
        const key = this.subcat + '-' + selectedConfigItem[optionKeys[0]].replace(/\s/g, '') + '-configurationImageUrl';
        if (this.messages[key]) {
          this.displayImage = this.config.imageServerUrl + '/' + this.messages[key];
        } else {
          this.getProductImage(newProd[0]);
        }
      } else {
        this.getProductImage(newProd[0]);
      }
    }
  }

  // Displays product image by selected color
  getProductImage(prod) {
    if (prod && Object.keys(prod.images).length === 0 ) {
      return;
    }
    this.displayImage = this.aplImgSizePipe.transform(prod.images.large, '540', '');
  }

  /**
   * show selected item to UI and build engraving image with provided user selected values
   *
   * @param item
   * @param subcat
   * @param userSelections
   */
  returnSelectedProduct(item, subcat, userSelections) {
    this.itemToCart = item;
    this.configImgAlt = this.itemToCart['name'];
    if (this.itemToCart['psid']) {
      this.getProductDetail('', '/' + this.itemToCart['psid']);
    }
  }

  // Get product detail information with provided PSID
  getProductDetail(callType, productPsid) {
    productPsid = productPsid.replace('/', '');

    const variation = (callType) ? '' : '?withVariations=' + 'false';
    const params = productPsid + variation;
    this.detailCallLoading = true;

    // Get configurable product (general) details
    this.subscriptions.push(
      this.detailService.getDetails(params).subscribe(data => {
        const detailData = data;
        this.details = data;
        this.detailCallLoading = false;
        sessionStorage.setItem('detailData', JSON.stringify(detailData));
        sessionStorage.setItem('callType', callType);
        this.getItemAdditionalInfo(callType, detailData);
        this.psid = detailData?.psid;
        if (this.psid !== undefined) {
          const giftEligibleMessagePsid = 'giftQualifiedConfigPageMessage-' + this.parsePsidPipe.transform(this.psid, '');
          this.additionalConfigPageMessage = this.messages[giftEligibleMessagePsid];
          this.priceModel = detailData.additionalInfo['PricingModel'];
          this.translateParams = {
            paymentValue: this.currencyPipe.transform((this.priceModel && this.priceModel.paymentValue) ? this.priceModel.paymentValue : 0),
            repaymentTerm: (this.priceModel && this.priceModel?.repaymentTerm) ? (this.priceModel.repaymentTerm) : 0,
            minPaymentValue: 0
          };
        }
        if (this.allOptionsSelected) {
          this.smartPrice = this.details.smartPrice;
          this.heapService.broadcastEvent(AppConstants.analyticServices.HEAP_EVENTS.ITEM_VIEWED, this.details);
        }
        setTimeout(() => {
          this.carouselImages = this.details.carouselImages;
        }, 200);
        this.isServicePlansExist = !isEmpty(this.details.addOns.servicePlans);
        this.selectedAppleCareServicePsid = '';
      }, this.errorCallback));
  }

  errorCallback = (e) => {
    if (e.status === 500 || e.status === 400) {
      this.loadError();
    } else if (e.status === 403) {
      this.sharedService.showSessionTimeOut(true);
    }
  }

  getItemAdditionalInfo(callType, detailData) {
    this.details = detailData;
    const prodConfig = this.productConfigurationData();
    if (prodConfig) {
      this.isShowOptions = this.sharedService.getOptionsDisplay(prodConfig, detailData?.categories[0].parents[0].slug, detailData?.categories[0].slug);
    }
    if (callType !== 'init') {
      this.configItemSku = (detailData.offers[0].appleSku) ? detailData.offers[0].appleSku : detailData.offers[0].sku;
      this.configItemUpc = detailData.upc;
    }
  }

  productConfigurationData() {
    return this.templateService.getProperty('productConfiguration');
  }

  detailPageData() {
    return this.templateService.getTemplatesProperty('detail');
  }

  addToCart(psid, detl) {
    this.cartService.addToCart(psid, detl, null, this.selectedAppleCareServicePsid);
  }

  /*
 * GET products based on the subcategory selected
 * OPTIONS come with the return data
 */
  getProducts() {
    const disableRequired = true;
    this.subscriptions.push(
      this.productService.getProductsWopts('/?categorySlug=' + this.subcat).subscribe((data: ProductsWithConfiguration) => {
        const prodData = data;
        if (prodData['products'] === undefined || prodData['products'].length === 0) {
          this.loadError();
        } else if (!(prodData && (Object.keys(prodData['optionsConfigurationData']).length < 1) || prodData['products'].length === 1)) {
          const configOptions = prodData['optionsConfigurationData'];
          this.configProducts = this.sharedService.transformProducts(prodData['products']);

          this.itemOptions = this.sharedService.transformOptions(configOptions, this.subcat, disableRequired);
          this.optionsLength = this.sharedService.setOptionLength(this.itemOptions);

          if (this.itemOptions && this.optionsLength > 0) {
            const optionList = this.itemOptions;
            const primaryOptValData = optionList[0].optionData;
            const primaryValIfo = [];
            for (const primaryOptVal of primaryOptValData) {
              const primaryVal = primaryOptVal.key;
              primaryValIfo[primaryVal] = this.filterProducts(this.configProducts, primaryVal);
            }
            this.filteredByPrimary = primaryValIfo;
          }
          // prepare min product config object
          const dependedOptions = [];
          for (const configOption of this.itemOptions) {
            this.productByConfigOptions[configOption.name] = configOption;
            this.productByConfigOptions[configOption.name].options = {};
            this.productByConfigOptions[configOption.name].showOptionsFromPrice = ['caseFinish', 'bandColor', 'color'].indexOf(configOption.name) < 0;
            for (const option of configOption.optionData) {
              option['minProduct'] = '';
              option['dependedOptions'] = [...dependedOptions];
              this.productByConfigOptions[configOption.name].options[option.key.split(' ').join('')] = option;
            }
            dependedOptions.push(configOption.name);
          }
          this.productFindingByConfigOptions(this.configProducts);

          let preSelectionProduct = this.isProductExist(this.configProducts, this.psid);

          if (!preSelectionProduct) {  // Skip the init call if PSID present in the config URL //Call the init if
            // Invalid PSID.
            this.getProductDetail('init', '/' + this.configProducts[0].psid);
          } else {
            this.productPreSelection(preSelectionProduct);
          }
          // Pre-load all the configured images
          prodData['products'].forEach((product: Product) => {
            if (product && Object.keys(product.images).length === 0 ) {
              return;
            }
            const imageUrl = this.aplImgSizePipe.transform(product.images.large, '540', '') || '';
            this.http.get(imageUrl, {responseType: 'blob'}).subscribe(() => {});
          });
          // PDP analytics object for configuration page
          // ANALYTICS OBJECT
          let pageName = this.analyticsUserObject.pgName || '';
          pageName = pageName.replace('<product>', this.subcatName ? this.subcatName : '');
          const analyticsUserObj = {
            pgName: pageName.toLowerCase(),
            pgType: this.analyticsUserObject.pgType || '',
            pgSectionType: this.analyticsUserObject.pgSectionType || ''
          };
          this.ensightenService.broadcastEvent(analyticsUserObj, prodData.products);
          this.showTaxDisclaimer = this.config.showTaxDisclaimer;
        }
      }, this.errorCallback));
  }

  productFindingByConfigOptions(configProducts) {
    // Set filtered products
    this.setFilteredProducts(configProducts);
    this.loadProductDetails();
    // 1. Loop the Product List
    for (const product of configProducts) {
      // 2. Loop the options List inside the product
      for (const configOption of product.options) {
        // 3. Define new product option key if not initiated and assign the product
        const key = configOption.key.split(' ').join('');
        if (!this.productByConfigOptions[configOption.name]) {
          continue;
        }
        // this.productByConfigOptions[configOption.name].options[key].hidden = true;
        if (!this.productByConfigOptions[configOption.name].options[key].minProduct) {
          this.productByConfigOptions[configOption.name].options[key].minProduct = product;
          this.productByConfigOptions[configOption.name].options[key].hidden = false;
        } else {
          // 4. if product assigned already then compare with current product, and reassign min product
          // 4.1 config selection exist then find the product options satisfied with the selection and compare
          if (Object.keys(this.selectedConfigItem).length > 0) {
            const productOptionMatched = this.sharedService.verifyProductOptionOnProduct(this.selectedConfigItem, product, this.productByConfigOptions[configOption.name].options[key].dependedOptions);
            const currentProductOptionMatched = this.sharedService.verifyProductOptionOnProduct(this.selectedConfigItem, this.productByConfigOptions[configOption.name].options[key].minProduct, this.productByConfigOptions[configOption.name].options[key].dependedOptions);

            if (productOptionMatched && currentProductOptionMatched) {
              this.productByConfigOptions[configOption.name].options[key].minProduct = this.findMinPriceProduct(this.productByConfigOptions[configOption.name].options[key].minProduct, product);
            } else if (productOptionMatched && !currentProductOptionMatched) {
              this.productByConfigOptions[configOption.name].options[key].minProduct = product;
            }
            this.productByConfigOptions[configOption.name].options[key].hidden = !(productOptionMatched || currentProductOptionMatched);
          } else {
            this.productByConfigOptions[configOption.name].options[key].minProduct = this.findMinPriceProduct(this.productByConfigOptions[configOption.name].options[key].minProduct, product);
          }
        }
      }
    }
  }

  setFilteredProducts(configProducts) {
    this.userProductsFilterBySelections = [...configProducts];
    let enableNextOption = true;
    let disableAllNextOption = false;
    let allOptionSelected = true;
    for (const option in this.productByConfigOptions) {
      if (option) {
        this.productByConfigOptions[option].filteredProducts = [];
        if (enableNextOption) {
          this.productByConfigOptions[option].disabled = !enableNextOption;
        } else {
          delete this.selectedConfigItem[option];
          allOptionSelected = false;
        }
        if (disableAllNextOption) {
          allOptionSelected = false;
          this.productByConfigOptions[option].disabled = true;
        }
        // Set Filtered Products and enable next options
        enableNextOption = false;
        if (this.selectedConfigItem[option]) { // Selected key and Value
          this.userProductsFilterBySelections = this.filterProducts(this.userProductsFilterBySelections, this.selectedConfigItem[option], option);
          this.productByConfigOptions[option].filteredProducts = this.userProductsFilterBySelections;
          if (this.userProductsFilterBySelections.length > 0) {
            enableNextOption = true;
          } else {
            allOptionSelected = false;
            disableAllNextOption = true;
            delete this.selectedConfigItem[option];
          }
        } else {
          allOptionSelected = false;
        }
      }
    }
    this.allOptionsSelected = allOptionSelected;
  }

  loadProductDetails() {
    if (this.allOptionsSelected) {
      this.returnSelectedProduct(this.userProductsFilterBySelections[0], this.subcat, this.selectedConfigItem);
    } else {
      this.configSummaryText(this.selectedConfigItem);
    }
    if (Object.keys(this.selectedConfigItem).length > 0) {
      this.configuredProductImage(this.userProductsFilterBySelections, this.selectedConfigItem);
    }

  }

  findMinPriceProduct(product1: Product, product2) {
    const product1DisplayPrice = product1.offers[0].displayPrice;
    const product2DisplayPrice = product2.offers[0].displayPrice;
    if (product1DisplayPrice && product2DisplayPrice) {
      if (product1DisplayPrice.points <= product2DisplayPrice.points) {
        return product1;
      } else {
        return product2;
      }
    }
    return product1;
  }

  productPreSelection(detailData) {
    const availableOptions = {};
    // 1. make it disabled false from item options
    /* eslint-disable-next-line */
    for (let itemIdx = 0; itemIdx < this.itemOptions.length; itemIdx++) {
      this.itemOptions[itemIdx].disabled = false;
      // loop the options and set Selected Options --selectedOptions[parentIndex] = optionKey;
      for (const option of detailData.options) {
        /*if (this.selectedOptions === undefined) {
          /!*this.selectedOptions = [];*!/
          /!*this.selectedOptionsWithKeys = [];*!/
        }*/
        const optionSet = option;
        if (this.itemOptions[itemIdx].name === optionSet.name) {
          // 2. Prepare selectedOptions Array//restrict for preselect
          /*this.selectedOptions[itemIdx] = option.key;*/
          /*this.selectedOptionsWithKeys[itemIdx] = {
            [optionSet.name]: option.key
          };*/
          availableOptions[optionSet.name] = optionSet.value;
        }
      }
    }

    // 3. prepare $scope.selectedConfigItem Object //Option: {name:value}
    for (const option of detailData.options) {
      const optionSet = option;
      if (availableOptions[optionSet.name]) {
        this.selectedConfigItem[optionSet.name] = optionSet.key;
      }
    }
    // 4. make allOption Selected as true.
    this.allOptionsSelected = true;

    // 5. Do the preselection product call
    this.filterProductsFromUserSelections(0, null, null, null, true);

  }

  isProductExist(productList, psid) {
    // let filteredProduct = $filter('filter')(productList,{psid: psid},true);
    const filteredProduct = productList.filter(product => product.psid === psid);
    if (filteredProduct.length === 1) {
      return filteredProduct[0];
    }
    return false;
  }

  selectedAppleCareServicePlan(appleCarePsid) {
    this.selectedAppleCareServicePsid = appleCarePsid;
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

  loadError(): void {
    this.itemOptions = [];
    this.notificationRibbonService.emitChange([true, this.messages.configurationLoadingError.concat(' ', this.messages.tryAgainLater)]);
  }
}
