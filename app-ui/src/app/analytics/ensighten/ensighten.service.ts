import { DOCUMENT } from '@angular/common';
import { Inject, Injectable } from '@angular/core';
import { Router, ActivatedRoute, RouterEvent, NavigationEnd } from '@angular/router';
import { User } from '@app/models/user';
import { BaseService } from '@app/services/base.service';
import { UserStoreService } from '@app/state/user-store.service';
import { AnalyticsObject, ProductDataLayer, UserDataLayer } from './ensighten';

@Injectable({
  providedIn: 'root'
})
export class EnsightenService extends BaseService {

  configObject = {
    productGlobalVariableName: 'datalayer_b2s_prod',
    userGlobalVariableName: 'datalayer_b2s_user'
  };
  analyticsObject: AnalyticsObject = {
    datalayer_b2s_user: {} as UserDataLayer,
    datalayer_b2s_prod: [] || {} as ProductDataLayer
  };
  currentPage = '';
  user: User;

  constructor(
    private userStoreService: UserStoreService,
    @Inject(DOCUMENT) private document: Document,
    private router: Router,
    private activatedRoute: ActivatedRoute
  ) {
    super();
    this.user = this.userStoreService.user;
    this.router.events.subscribe((event: RouterEvent) => {
      if (event instanceof NavigationEnd) {
        const routeParams = this.getChild(this.activatedRoute);
        routeParams.data.subscribe(data => {
          this.currentPage = data.pageName;
        });
      }
    });

    // Update balance points once order placed
    this.userStoreService.get().subscribe(userData => {
      this.user = userData;
    });
  }

  loadInitialScript() {
    const data = 'analyticsEnabled = true';
    const body = this.document.getElementsByTagName('head')[0];
    const initialScript = this.document.createElement('script');
    initialScript.type = 'text/javascript';
    initialScript.text = data;
    body.appendChild(initialScript);
    this.loadEnsightenScript();
  }

  loadEnsightenScript() {
    const body = this.document.getElementsByTagName('head')[0];
    const scriptElememt = this.document.createElement('script');
    scriptElememt.type = 'text/javascript';
    scriptElememt.defer = true;
    scriptElememt.src = this.userStoreService.config.ensightenEndPoint;
    body.appendChild(scriptElememt);
  }

  broadcastEvent(userObject?, prodObject?) {
    userObject.pgCountryCode = this.user.country ? this.user.country.toUpperCase() : '';
    userObject.pgCountryLanguage = (this.user.additionalInfo && this.user.additionalInfo.languageCode) ? this.user.additionalInfo.languageCode.toUpperCase() : '';
    userObject.userMembershipID = this.user.userId ? this.user.userId.toLowerCase() : '';
    userObject.userMembershipTier = this.user.programId ? this.user.programId.toUpperCase() : '';
    userObject.userPoints = userObject && userObject.points ? userObject.points : this.user.points;
    userObject.userStatus = this.user.program.config.login_required ? 'A' : 'L';
    if (this.currentPage === 'CONFIRMATION') {
      prodObject.products = this.buildProductArray(prodObject['products']);
      this.analyticsObject.datalayer_b2s_prod = prodObject;
    } else {
      this.analyticsObject.datalayer_b2s_prod = this.buildProductArray(prodObject);
    }
    this.analyticsObject.datalayer_b2s_user = userObject;
    this.updateAnalyticsObject(this.analyticsObject);

    if (window['analyticsDebugger']) {
      // eslint-disable-next-line no-console
      console.log('Bakkt:analyticsUserObject', userObject);
    }

    if (window.Bootstrapper) {
      window.Bootstrapper?.ensEvent.trigger('tygrAnalyticsPageView');
    }
  }

  /**
   * This method updates the analytics object when on the appropriate page and triggers the dispatch event
   * @param eventObj
   */
   updateAnalyticsObject(eventObj) {
    this.analyticsObject = eventObj;
    window[this.configObject.productGlobalVariableName] = this.analyticsObject.datalayer_b2s_prod;
    window[this.configObject.userGlobalVariableName] = this.analyticsObject.datalayer_b2s_user;
  }

  /**
   * this extracts the array needed for product analytics
   * @param array
   * @returns {Array}
   */
  buildProductArray(array) {

    const productArray = [];
    if (array && array.constructor === Array && array.length && array[0]['productDetail']) {
      array.forEach(item => {
        productArray.push({
          prodProductCategory: (item['productDetail'] && item['productDetail']['categories'] && item['productDetail']['categories'].length && item['productDetail']['categories'][0]['name']) ? item['productDetail']['categories'][0]['name'].toLowerCase() : '',
          prodProductName: (item['productName']) ? item['productName'].toLowerCase() : '',
          prodProductPSID: (item['productDetail']['psid']) ? item['productDetail']['psid'].toLowerCase() : '',
          prodProductSKU: (item['productDetail']['sku']) ? item['productDetail']['sku'].toLowerCase() : '',
          prodProductType: 'apple',
          prodProductQuantity: item['quantity'],
          prodProductUPC: (item['productDetail']['upc']) ? item['productDetail']['upc'].toLowerCase() : '',
          prodProductPoints: (item['productDetail'] && item['productDetail']['offers'] && item['productDetail']['offers'].length && item['productDetail']['offers'][0]['displayPrice'] && item['productDetail']['offers'][0]['displayPrice']['points']) ? item['productDetail']['offers'][0]['displayPrice']['points'] : ''
        });
      });
    } else if (array && array.constructor === Array && array.length && array[0]['psid']) {
      array.forEach(item => {
        productArray.push({
          prodProductCategory: (item['categories'] && item['categories'].length && item['categories'][0]['name']) ? item['categories'][0]['name'].toLowerCase() : '',
          prodProductName: (item['name']) ? item['name'].toLowerCase() : '',
          prodProductPSID: (item['psid']) ? item['psid'].toLowerCase() : '',
          prodProductSKU: (item['sku']) ? item['sku'].toLowerCase() : '',
          prodProductType: 'apple',
          prodProductUPC: (item['upc']) ? item['upc'].toLowerCase() : '',
          prodProductPoints: (item['offers'] && item['offers'].length && item['offers'][0]['displayPrice'] && item['offers'][0]['displayPrice']['points']) ? item['offers'][0]['displayPrice']['points'] : ''
        });
      });
    } else if (array && array.constructor === Array && array.length) {
      array.forEach(item => {
        productArray.push({
          prodProductCategory: (item['prodProductCategory']) ? item['prodProductCategory'].toLowerCase() : '',
          prodProductName: (item['prodProductType']) ? item['prodProductType'].toLowerCase() : ''
        });
      });
    }
    return productArray;
  }

  getChild(activatedRoute: ActivatedRoute) {
    if (activatedRoute.firstChild) {
      return this.getChild(activatedRoute.firstChild);
    } else {
      return activatedRoute;
    }
  }

}
