import { Injectable } from '@angular/core';
import { BaseService } from '@app/services/base.service';
import { UserStoreService } from '@app/state/user-store.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { AppConstants } from '@app/constants/app.constants';

@Injectable({
    providedIn: 'root'
})
export class MatomoService extends BaseService {

    configObject = {
        stateMap: []
    };
    cartItems;
    categories = [];
    matomoEnabled = false;

    constructor(private userStoreService: UserStoreService, private sharedService: SharedService) {
        super();
    }

  loadInitialScript() {
        const config = this.userStoreService.config;
        if (config.matomoEndPoint && config.matomoSiteId) {
            this.matomoEnabled = true;
            window['_paq'] = window['_paq'] || [];
            /* tracker methods like "setCustomDimension" should be called before "trackPageView" */
            // paq.push(["setDocumentTitle", document.domain + "/" + document.title]);
            // paq.push(["setDomains", ["*."]]);
            // paq.push(['trackPageView']);
            // paq.push(['enableLinkTracking']);
            window['_paq'].push(['setTrackerUrl', config.matomoEndPoint + 'matomo.php']);
            window['_paq'].push(['setSiteId', config.matomoSiteId]);

            const g = document.createElement('script');
            const s = document.getElementsByTagName('script')[0];
            g.type = 'text/javascript'; g.async = true; g.defer = true; g.src = config.matomoEndPoint + 'matomo.js'; s.parentNode.insertBefore(g, s);
            this.initConfig();
        }
    }

    initConfig() {
        this.configObject.stateMap[AppConstants.analyticServices.CANONICAL_CONSTANTS.MERCHANDISE_PRODUCT_DETAILS] = ['PDP', 'PCP', 'AGP'];
        this.configObject.stateMap[AppConstants.analyticServices.CANONICAL_CONSTANTS.MERCHANDISE_SEARCH_RESULTS] = ['SEARCH'];
        this.configObject.stateMap[AppConstants.analyticServices.CANONICAL_CONSTANTS.MERCHANDISE_BROWSE_RESULTS] = ['PGP', 'AGP'];
        this.configObject.stateMap[AppConstants.analyticServices.CANONICAL_CONSTANTS.MERCHANDISE_LANDING] = ['CLP', 'AGP'];
        this.configObject.stateMap[AppConstants.analyticServices.CANONICAL_CONSTANTS.PRODUCTS_CHECKOUT] = ['REVIEW'];
        this.configObject.stateMap[AppConstants.analyticServices.CANONICAL_CONSTANTS.PRODUCTS_ORDER_CONFIRMATION] = ['CONFIRMATION'];
        this.configObject.stateMap[AppConstants.analyticServices.CANONICAL_CONSTANTS.ENGRAVE_PRODUCT] = ['ENGRAVE'];
        this.configObject.stateMap[AppConstants.analyticServices.CANONICAL_CONSTANTS.TERMS_AND_CONDITIONS] = ['TERMS'];
        this.configObject.stateMap[AppConstants.analyticServices.CANONICAL_CONSTANTS.FAQS] = ['FAQS'];
        this.configObject.stateMap[AppConstants.analyticServices.CANONICAL_CONSTANTS.ADDRESS] = ['SHIPPING_ADDRESS'];
        this.configObject.stateMap[AppConstants.analyticServices.CANONICAL_CONSTANTS.PRODUCTS_CART] = ['BAG'];
        this.configObject.stateMap[AppConstants.analyticServices.CANONICAL_CONSTANTS.ORDER_HISTORY] = ['ORDERS_HISTORY'];
        this.configObject.stateMap[AppConstants.analyticServices.CANONICAL_CONSTANTS.ORDER_HISTORY_DETAILS] = ['ORDER_STATUS'];
        this.configObject.stateMap[AppConstants.analyticServices.CANONICAL_CONSTANTS.CURATED_PRODUCTS] = ['SHOP'];
        this.configObject.stateMap[AppConstants.analyticServices.CANONICAL_CONSTANTS.CATALOG_LANDING] = ['STORE'];
        this.configObject.stateMap[AppConstants.analyticServices.CANONICAL_CONSTANTS.POSTBACK] = ['POSTBACK'];
    }

    getPrice(product: any, quantity: number) {
        // this.getCartTotal('ddd');
        const user = this.userStoreService.user;
        // const paymentOption = user.program.config.paymentOption;
        const pricingTemplate = user.program.config.pricingTemplate;
        quantity = quantity || 1;
        let price = 0;
        // POINTS, CASH, PAYROLL_DEDUCTION, NO_PAY
        if (product.offers && product.offers.length > 0) {
            if (this.sharedService.isRewardsRedemption()){
              if (pricingTemplate === 'points_decimal') {
                price = product.offers[0].totalPrice.amount;
              } else {
                price = quantity * product.offers[0].displayPrice.points;
              }
            }
            else if (this.sharedService.isCashOnlyRedemption()) {
                price = product.offers[0].totalPrice.amount;
            }
        }
        return price;
    }

    getCartTotal(displayCartTotal: any) {

        const user = this.userStoreService.user;
        // const paymentOption = user.program.config.paymentOption;
        const pricingTemplate = user.program.config.pricingTemplate;
        let price = 0;
        const splitArr = pricingTemplate.split('_');
        const isPoints = (splitArr[0] === 'points');

        const pricingOption = (() => {
            let option;
            switch (user.program.bundledPricingOption) {
                case 'UNBUNDLED_DETAIL_PAGE_BUNDLED_CHECKOUT':
                    option = 'unbundledDetails';
                    break;
                case 'BASE_PRICE_SHOPPING_UNBUNDLED_CHECKOUT':
                    option = 'unbundledCheckout';
                    break;
                default:
                    option = 'bundled';
            }
            return option;
        })();

        // POINTS, CASH, PAYROLL_DEDUCTION, NO_PAY

        if (this.sharedService.isRewardsRedemption()) {
            if (pricingOption === 'unbundledCheckout') {
                if (pricingTemplate === 'points_cash') {
                    price = displayCartTotal.itemsSubtotalPrice.points;
                } else if (pricingTemplate === 'cash_points' || pricingTemplate === 'points_decimal') {
                    price = displayCartTotal.itemsSubtotalPrice.amount;
                } else {
                    price = isPoints ? displayCartTotal.itemsSubtotalPrice.points : displayCartTotal.itemsSubtotalPrice.amount;
                }
            } else {
                if (pricingTemplate === 'points_cash') {
                    price = displayCartTotal.price.points;
                } else if (pricingTemplate === 'cash_points' || pricingTemplate === 'points_decimal') {
                    price = displayCartTotal.price.amount;
                } else {
                    price = isPoints ? displayCartTotal.price.points : displayCartTotal.price.amount;
                }
            }
        } else if (this.sharedService.isCashOnlyRedemption()) {
            price = displayCartTotal.price.amount;
        }

        return price;
    }

    getCanonicalName(stateMap, pageView) {
        for (const canonicalName in stateMap) {
            if (stateMap.hasOwnProperty(canonicalName) && stateMap[canonicalName].indexOf(pageView.routeName) !== -1) {
                return canonicalName;
            }
        }
        return AppConstants.NONE;
    }

    getCategory(product) {
        const category = product.productDetail ? product.productDetail.categories : product.categories;
        if (category && category.length > 0
            && category[0].parents && category[0].parents.length > 0
        ) {
            return category[0].parents[0].name;
        }
        return '';
    }

    sendErrorToAnalyticService() {
        this.broadcast(AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.ERROR, {
            payload: {
                error:  'Error',
                location:  location.href || '',
                canonicalTitle: AppConstants.analyticServices.CANONICAL_CONSTANTS.ERROR
            }
        });
    }

    broadcast(event, args) {
        if (!this.matomoEnabled){
          return;
        }
        switch (event) {
            case (AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.ORDER_SUCCESS):
                {
                    const orderEvent = args.payload;
                    if (orderEvent && window['_paq']) {

                        orderEvent.products.forEach((product) => {
                            const item = [];
                            const productName = product.productName;

                            const productPrice = this.getPrice(product.productDetail, product.quantity);
                            const productCategoryName = this.getCategory(product);
                            item.push(
                                'addEcommerceItem',
                                product.productDetail.sku,
                                productName,
                                productCategoryName,
                                productPrice,
                                product.quantity
                            );
                            window['_paq'].push(item);
                        });
                        window['_paq'].push(['trackEcommerceOrder', orderEvent.orderId, orderEvent.productTotals.price.points]);
                    }
                    break;
                }
            case (AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.ROUTE):
                {
                    const pageView = args.payload;
                    window['_paq'] = window['_paq'] || [];
                    if (pageView && window['_paq']) {

                        const user = this.userStoreService.user;
                        const stateMap = this.configObject.stateMap;
                        const theCanonicalName = this.getCanonicalName(stateMap, pageView);

                        window['_paq'].push(['deleteCustomVariables', 'page']);
                        if (!user.program.config.loginRequired && user.userId) {
                            window['_paq'].push(['setUserId', user.userId]);
                        }
                        window['_paq'].push(['setCustomVariable', '1', 'varId', user.program.varId, 'visit']);
                        window['_paq'].push(['setCustomVariable', '2', 'programId', user.program.programId, 'visit']);
                        window['_paq'].push(['setCustomUrl', pageView.location]);
                        window['_paq'].push(['setDocumentTitle', theCanonicalName]);
                        window['_paq'].push(['trackPageView']);
                    }
                    break;
                }
            case (AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.PRODUCT_VIEW):
                {
                    const product = args.payload;
                    if (product && window['_paq']) {
                        const productName = product.name;
                        const productId = product.sku;
                        const storeCategoryName = this.getCategory(product);
                        const price = this.getPrice(product, product.quantity);

                        const item = [
                            'setEcommerceView',
                            productId,
                            productName,
                            storeCategoryName,
                            price
                        ];

                        if (productId) {
                            window['_paq'].push(item);
                            window['_paq'].push(['trackPageView']);
                        }
                    }
                    break;
                }
            case (AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.REMOVE_FROM_CART):
                {
                    const cartAnalytics = args.payload;
                    const user = this.userStoreService.user;

                    if (cartAnalytics && window['_paq']) {
                        let isRemove = true;
                        if (cartAnalytics.cart && cartAnalytics.cart.cartItems && cartAnalytics.cart.cartItems.length > 0 && !user.program.config.SingleItemPurchase) {
                            const isExist = cartAnalytics.cart.cartItems.some((value) => value.productId === cartAnalytics.product.productId);

                            if (isExist) {
                                isRemove = false;
                                this.broadcast(AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.UPDATE_CART, { payload: cartAnalytics.cart });
                            }
                        }

                        if (isRemove) {
                            const price = this.getPrice(cartAnalytics.product.productDetail, cartAnalytics.product.quantity);
                            const productSku = cartAnalytics.product.productDetail.sku;
                            const productName = cartAnalytics.product.productDetail.name;
                            const productCategoryName = this.getCategory(cartAnalytics.product.productDetail);
                            const quantity = cartAnalytics.product.quantity;

                            window['_paq'].push(['removeEcommerceItem', productSku, productName, productCategoryName, price, quantity]);
                            window['_paq'].push(['trackEcommerceCartUpdate', (cartAnalytics.cart && cartAnalytics.cart.displayCartTotal) ? this.getCartTotal(cartAnalytics.cart.displayCartTotal) : 0]);
                            this.cartItems = null;
                        }
                    }
                    break;
                }
            case (AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.UPDATE_CART):
                {
                    const cartAnalytics = args.payload;
                    const user = this.userStoreService.user;

                    if (cartAnalytics && window['_paq']) {
                        if (user.program.config.SingleItemPurchase) {
                            if (this.cartItems) {
                                this.cartItems.cartItems.map((value) => {
                                    this.broadcast(AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.REMOVE_FROM_CART, {
                                        payload: {
                                            product: value,
                                            cart: this.cartItems
                                        }
                                    });
                                });
                            }
                            setTimeout(() => {
                                this.cartItems = cartAnalytics;
                            }, 10);
                        }
                        const productList = {};
                        // loop
                        cartAnalytics.cartItems.forEach((product) => {
                            const productCategoryName = this.getCategory(product) || '';
                            // Quantity calculation for duplicate products
                            if (productList[product.productDetail.sku]) {
                                productList[product.productDetail.sku].quantity += product.quantity;
                            } else {
                                productList[product.productDetail.sku] = {
                                    sku: product.productDetail.sku,
                                    quantity: product.quantity
                                };
                            }
                            const price = product.productDetail ? this.getPrice(product.productDetail, productList[product.productDetail.sku].quantity) : this.getPrice(product, productList[product.productDetail.sku].quantity);

                            const item = [
                                'addEcommerceItem',
                                product.productDetail.sku,
                                product.productName,
                                productCategoryName,
                                price,
                                productList[product.productDetail.sku].quantity
                            ];
                            window['_paq'].push(item);
                        });
                        window['_paq'].push(['trackEcommerceCartUpdate', cartAnalytics.displayCartTotal ? this.getCartTotal(cartAnalytics.displayCartTotal) : 0]);
                    }
                    break;
                }
            case (AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.CATEGORY_SEARCH):
                {
                    const searchPayload = args.payload;
                    if (searchPayload && window['_paq']) {
                        // clear top level category array
                        this.categories = [];

                        searchPayload.results.forEach((product) => {
                            // search through product categories and push unique name and parent name onto array
                            product.categories.forEach((category) => {
                                // search through product categories and push unique name and parent name onto
                                this.categories.push(category.name);

                                // recursively search through category parents and push on the array
                                if (category.parents) {
                                    this.extractParentCategoryName(category);
                                }
                            });
                        });

                        this.categories = this.uniqueArray(this.categories);

                        window['_paq'].push(['trackSiteSearch',
                            // Search keyword searched for
                            searchPayload.searchTerm,
                            // Search category selected in your search engine. If you do not need this, set to false
                            this.categories.length ? this.categories : false,
                            // Number of results on the Search results page. Zero indicates a 'No Result Search Keyword'. Set to false if you don't know
                            searchPayload.results.length
                        ]);
                    }
                    break;
                }
            case (AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.ERROR):
            case (AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.CANONICAL_PAGE):
                {
                    this.errorEvents(args);
                    break;
                }
            case (AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.ENGRAVING):
                {
                    const cartAnalytics = args.payload;
                    if (cartAnalytics && window['_paq']) {
                        const productList = {};

                        cartAnalytics.cartItems.forEach((product) => {
                            if (product.productDetail.additionalInfo.engravable && product.productDetail.additionalInfo.engravable === 'true') {
                                const action = (product.engrave.line1 || product.engrave.line2) ? AppConstants.analyticServices.CANONICAL_CONSTANTS.ADD_ENGRAVING : AppConstants.analyticServices.CANONICAL_CONSTANTS.SKIP_ENGRAVING;
                                const suffix = (product.engrave.line1 || product.engrave.line2) ? '_add' : '_skip';
                                const item = [
                                    'trackEvent',
                                    AppConstants.analyticServices.EVENTS.ENGRAVING,
                                    action,
                                    product.productName
                                ];

                                // Quantity calculation for duplicate products
                                if (productList[product.productDetail.sku + suffix]) {
                                    productList[product.productDetail.sku + suffix].quantity += product.quantity;
                                    productList[product.productDetail.sku + suffix].item = item;
                                    productList[product.productDetail.sku + suffix].item.push(productList[product.productDetail.sku + suffix].quantity);
                                } else {
                                    productList[product.productDetail.sku + suffix] = {
                                        sku: product.productDetail.sku + suffix,
                                        quantity: product.quantity,
                                        item
                                    };
                                    productList[product.productDetail.sku + suffix].item.push(product.quantity);
                                }
                            }
                        });
                        for (const x in productList) {
                            if (productList[x]) {
                                window['_paq'].push(productList[x].item);
                            }
                        }
                    }
                    break;
                }
        }
    }

    /*
    * helper function to extract search result product category name
    @param parent
    */


  errorEvents(args) {
    const pageView = args.payload;
    if (pageView && window['_paq']) {
      const user = this.userStoreService.user;

      window['_paq'].push(['deleteCustomVariables', 'page']);
      if (user.program.config.loginRequired && user.userId) {
        window['_paq'].push(['setUserId', user.userId]);
      }
      window['_paq'].push(['setCustomVariable', '1', 'varId', user.program.varId, 'visit']);
      window['_paq'].push(['setCustomVariable', '2', 'programId', user.program.programId, 'visit']);
      window['_paq'].push(['setCustomUrl', pageView.location]);
      window['_paq'].push(['setDocumentTitle', pageView.canonicalTitle]);
      window['_paq'].push(['trackPageView']);
    }
  }

    extractParentCategoryName(parent) {
        this.categories.push(parent.name);
    }

    uniqueArray(array) {
        const temp = array.reduce((previous, current) => {
            previous[current] = true;
            return previous;
        }, {});

        return Object.keys(temp);
    }

}
