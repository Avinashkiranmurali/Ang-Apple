import { Component, Injector, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router, RouterEvent } from '@angular/router';
import camelCase from 'lodash/camelCase';
import { Subscription } from 'rxjs';
import { NavStoreService } from '@app/state/nav-store.service';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { BannerService } from '@app/services/banner.service';
import { BannerStoreService } from '@app/state/banner-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { BannerTemplate } from '@app/models/banner-template';
import { Messages } from '@app/models/messages';
import { Config } from '@app/models/config';
import { Category } from '@app/models/category';
import { BreakPoint } from '@app/components/utils/break-point';
import { LandingBannerCategory } from '@app/models/banners';
import { AppConstants } from '@app/constants/app.constants';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';

@Component({
  selector: 'app-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss']
})

export class LandingComponent extends BreakPoint implements OnInit, OnDestroy  {
  messages: Messages;
  productBanner: {};
  programBanner: {};
  familyBanner: Array<any>;
  familyBannerOnMobile: object;
  bannerData: {};
  config: Config;
  mainNav: Array<Category>;
  category: string;
  multiProductBanner: BannerTemplate;
  multiProductBannerActiveList: Array<BannerTemplate>;
  multiProductBannerList: Array<string>;
  marketingBanner: BannerTemplate;
  marketingBannerItems: Array<BannerTemplate>;
  marketingBannerList: Array<string>;
  displayProductBnr: boolean;
  displayProgramBnr: boolean;
  displayMarketingBnr: boolean;
  rowConfig: number[];
  isNPIActive: boolean;
  productFamilyBanners: Array<LandingBannerCategory>;
  whatsNewBanners: Array<LandingBannerCategory>;
  private subscriptions: Subscription[] = [];
  landingBanners: Array<LandingBannerCategory>;
  singleProductBanners: Array<LandingBannerCategory>;
  multiBanner: Array<LandingBannerCategory>;

  constructor(
    public injector: Injector,
    private bannerService: BannerService,
    public messageStore: MessagesStoreService,
    public userStore: UserStoreService,
    private bannerStoreService: BannerStoreService,
    public mainNavStore: NavStoreService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private ensightenService: EnsightenService,
  ) {
    super(injector);
    this.familyBannerOnMobile = {};
    this.messages = messageStore.messages;
    this.config = this.userStore.config;
    this.isNPIActive = Boolean(this.config.isNPIActive);
  }

  ngOnInit(): void {
    this.messages = this.messageStore.messages;
    this.mainNav = this.mainNavStore.mainNav;
    this.activatedRoute.params.subscribe(
      params => {
        this.category = params['category'];
        this.activatedRoute.data.subscribe(
          data => {
            let pgName = '';
            let prodAnalyticsObject = [];
            const userAnalyticsObject = data.analyticsObj || {};
            if (data.pageName === 'CLP') {
              // if category exists, replace <category> with category name in pgName
              pgName = userAnalyticsObject.pgName.replace('<category>', this.category);
              prodAnalyticsObject = [{
                prodProductCategory: this.category,
                prodProductType: 'apple'
              }];
            } else if (data.pageName === 'STORE') {
              pgName = userAnalyticsObject.pgName;
            }
            // userAnalyticsObject.pgName = pgName.toLowerCase();
            const analyticsUserObj = {
              pgName: pgName.toLowerCase() || userAnalyticsObject.pgName,
              pgType: userAnalyticsObject.pgType || '',
              pgSectionType: userAnalyticsObject.pgSectionType || ''
            };
            this.ensightenService.broadcastEvent(analyticsUserObj, prodAnalyticsObject);
          }
        );
      }
    );
    this.router.events.subscribe(
      (event: RouterEvent) => {
        if (event instanceof NavigationEnd) {
          this.mainNav = this.mainNavStore.mainNav;
          this.initializeBanner();
        }
      });
    this.bannerData = (this.bannerStoreService.banner.length > 0) ? this.bannerStoreService.banner[0] : {};
    if (Object.keys(this.bannerData).length === 0) {
      this.subscriptions.push(
        this.bannerService.getBanners().subscribe(
          response => {
            this.bannerData = response[0];
            this.initializeBanner();
          },
          error => {}
      ));
    } else {
      this.initializeBanner();
    }
    this.productFamilyBanners = (Object.keys(this.bannerStoreService.banners).length > 0) ? this.bannerStoreService.banners['family'] : [];
    this.whatsNewBanners = (Object.keys(this.bannerStoreService.banners).length > 0) ? this.bannerStoreService.banners['whatsnew'] : [];
    this.landingBanners = (Object.keys(this.bannerStoreService.banners).length > 0) ? this.bannerStoreService.banners['landing'] : [];
    this.multiBanner = this.landingBanners ? this.landingBanners.filter(banner => banner.bannerType === AppConstants.MULTI_PRODUCT_BANNER_TYPE) : [];
    this.singleProductBanners = this.landingBanners ? this.landingBanners.filter(banner => banner.bannerType !== AppConstants.MULTI_PRODUCT_BANNER_TYPE) : [];
    if (this.productFamilyBanners.length === 0 && this.whatsNewBanners.length === 0) {
      this.getBanners();
    }
  }
  initializeBanner() {
    this.buildProgramBannerConfig();
    this.productBanner =  this.bannerData['productBanner'];
    this.bannerData['banners'] = this.buildBannerConfig();
    this.loadFamilyBanner();
    this.initMultiBanner();
    this.displayProductBnr = this.displayProductBanner();
    this.displayProgramBnr = this.displayProgramBanner(this.messages[this.programBanner['active']], this.category);
    this.displayMarketingBnr = this.displayProgramBanner(this.messages[this.marketingBanner['active']], this.category);
  }
  buildProgramBannerConfig() {
    const programBannersObj = this.bannerData;
    const programBanners = programBannersObj['programBanners'];
    const bannerTemplateObj = programBannersObj['bannerTemplateObj'];

    // loop all the banners and create a keys for the banners.
    // eslint-disable-next-line guard-for-in
    for (const banners in programBanners) {
      this.bannerData[banners] = {};

      // eslint-disable-next-line guard-for-in
      for (const bannerKeys in bannerTemplateObj){
        programBannersObj[banners][bannerKeys] = banners + bannerTemplateObj[bannerKeys];
      }
    }
    this.bannerData = programBannersObj;
  }
  buildBannerConfig() {
    const programBannersObj = this.bannerData;
    const bannerTemplateObj = programBannersObj['bannerTemplateObj'];
    const bannerArr = [];
    const homeObj = {
      name: 'home',
      subcat: []
    };
    this.mainNav.forEach( (navObj, i) => {
      const subCategories: Array<any> = navObj['subCategories'];
      const bannerObj = {
        name: navObj.slug,
        subcat: []
      };
      // const navSlugName = navObj.slug.replace(/[^a-zA-Z0-9]+(.)/g, function (g) { return g[1].toUpperCase(); });
      const navSlugName = camelCase(navObj['slug']);
      // console.log('navSlugName: ' + navSlugName);
      // const navSlugName = navObj.slug;
      const homeSubCatObj = {
        name: navObj.slug
      };

      Object.keys(bannerTemplateObj).forEach( (key, idx) => {
        homeSubCatObj[key] = navSlugName + 'Family' + bannerTemplateObj[key];
      });
      homeObj['subcat'].push(homeSubCatObj);

      subCategories.forEach((subCat, j) => {
        // const slugName = subCat['slug'].replace(/[^a-zA-Z0-9]+(.)/g, function (g) { return g[1].toUpperCase(); });
        const slugName = camelCase(subCat['slug']);
        // console.log('Sub - slugName: ' + slugName);
        // const slugName = subCat.slug;
        const subCatObj = {
          name: subCat.slug
        };
        Object.keys(bannerTemplateObj).forEach( (key, idx) => {
          subCatObj[key] = slugName + bannerTemplateObj[key];
        });
        bannerObj['subcat'].push(subCatObj);
      });
      bannerArr.push(bannerObj);
    });


    bannerArr.push(homeObj);
    return bannerArr;
  }
  loadFamilyBanner() {
    this.postBanners();
    this.setMobileBanners();
  }
  displayProgramBanner(activeState: string, category): boolean {
    return (activeState === 'true' && category === undefined);
  }
  displayProductBanner(): boolean {
    return (this.category === undefined || this.category === null);
  }
  postBanners() {
    const bannerData = this.bannerData;
    const category = (this.category) ? this.category : 'home';
    // const category = 'home';
    const setBanners = bannerData['banners'].filter( banner => banner.name === category);
    // const setBanners = filter(bannerData['banners'], {name: category}, true);
    let navArray = [];
    const displayBanners = [];
    // set program banner and banner template data
    this.programBanner = (bannerData['program']) ? bannerData['program'] : {};
    this.productBanner = (bannerData['productBanner']) ? bannerData['productBanner'] : {};
    navArray = (category === 'home') ? this.mainNav : this.getSubNav();
        // filter banner data using available category data
    if (navArray && navArray.length > 0) {
        navArray.forEach(nav => {
          setBanners[0].subcat.forEach(banner => {
            if (banner.name === nav.slug) {
              banner.displayOrderBy = banner.hasOwnProperty('displayOrderBy') ? banner.displayOrderBy : this.messages[banner.orderBy];
              if (nav.detailUrl) {
                banner.detailUrl = nav.detailUrl;
              }
              if (nav.new) {
                banner.new = nav.new;
              }
              displayBanners.push(banner);
            }
          });
        });
      }
    // set banner data after filtering categories
    this.familyBanner = displayBanners;
    // console.log(displayBanners);

    /*
    // set accessories banners if category is accessories or accessories subcategory
    if (category == CONSTANTS.slugName.accessories || ($scope.subcat && $scope.subcat.indexOf(CONSTANTS.slugName.accessories) >= 0)) {
      setBanners = filter(bannerData.banners, {name: CONSTANTS.slugName.accessories}, true);
      vm.getAccBanners(setBanners[0], $scope.subcat);
    }*/
  }
  getBannerClass(cat, slug) {
    const templateClass = this.messages[cat.templateClass] || ''; // loyalty-app family-banner
    if (slug === '' || slug === undefined) { // family landing
      return templateClass + ' ' + 'landing-' + cat.name ;
    } else { // category landing
      return templateClass + ' ' + 'category-landing ' + cat['name'];
    }
  }
  setMobileBanners() {
    this.mainNav.forEach( (family, familyIndex) => {
      const slug = {};
      slug['i18nName'] = family['i18nName'];
      family.subCategories.forEach( (subCat, index) => {
        // Grab first product image for Family Banner
        if (index < 1) {
          slug['images'] = subCat['images'];
          slug['subCats'] = [];
        }
        const cat = {};
        cat['images'] = subCat['images'];
        cat['name'] = subCat.slug;
        cat['i18nName'] = subCat['i18nName'];
        slug['subCats'].push(cat);
        this.familyBannerOnMobile[subCat.slug] = cat;
      });
      this.familyBannerOnMobile[family.slug] = slug;
    });
  }
  getSubNav() {
    if (this.category !== undefined) {
      const categoryNav = this.mainNav.filter(mainNav =>  this.category === mainNav['slug']);
      return categoryNav[0].subCategories;
    } else {
      return [];
    }
  }
  buildBannerLink(cat: Category): string {
    const bannerState = cat['state'];
    const bannerParams = cat['params'];
    if (this.messages[bannerState] && this.messages[bannerParams]) {
      const category = JSON.parse(this.messages[bannerParams])['category'];
      const subCat = JSON.parse(this.messages[bannerParams])['subcat'];
      const params = JSON.parse(this.messages[bannerParams]);
      const addCat = JSON.parse(this.messages[bannerParams])['addCat'];
      const psid = JSON.parse(this.messages[bannerParams])['psid'];
      const productUrl = '/' + (category) + ((subCat) ? '/' + subCat : '') + ((addCat) ? '/' + addCat : '') + ((psid) ? '/' + psid : '');

      if (cat.detailUrl) {
        let detailsState = '/store/curated';
        const urlSplit = cat.detailUrl.split('/');
        params.psid = urlSplit[urlSplit.length - 1];

        if (urlSplit.length > 1) {
          params.addCat = urlSplit[urlSplit.length - 2];
        }
        if (params.subcat === params.addCat) {
          detailsState = '/store/browse';
          detailsState += `/${params.category}/${params.subcat}/${params.psid}`;
        } else {
          detailsState += `/${params.category}/${params.subcat}/${params.addCat}/${params.psid}`;
        }
        return detailsState;
      } else {
        return this.messages[bannerState] + productUrl;
      }
    } else {
      return '';
    }
  }

  subscribeBreakpointChanges() {
    this.subscriptions.push(
      this.breakPointObservable$.subscribe(() => {
        if (this.multiProductBanner && this.multiProductBanner['list']) {
          this.multiProductBannerActiveList = this.messages[this.multiProductBanner['list']] ? this.buildMultiBannerActiveGroup(this.messages[this.multiProductBanner['list']].split(',')) : [];
        }
      })
    );
  }

  getDefaultRowConfiguration(lists: string[]) {
    const threshold = this.isDesktop ? 5 : (this.isTablet ? 4 : 2);
    const productsLength = lists.length;
    const arrayLength = Math.ceil(productsLength / threshold);
    const productsPerRow = Math.ceil(productsLength / arrayLength);
    return new Array(arrayLength).fill(productsPerRow, 0);
  }

  setRowConfiguration(group: string, lists: string[]) {
    const bannerKey = this.isDesktop ? 'desktopRowConfig' : (this.isTablet ? 'tabletRowConfig' : 'mobileRowConfig');
    const rowConfig = this.messages[group + '-' + this.multiProductBanner[bannerKey]] ? this.messages[group + '-' + this.multiProductBanner[bannerKey]].split(',').map(x => parseInt(x, 10)) : [];
    this.rowConfig = rowConfig.length > 0 ? rowConfig : this.getDefaultRowConfiguration(lists);
  }

  initMultiBanner(): void {
    const bannerData = this.bannerData;
    this.multiProductBanner = bannerData['multiProductBanner'] ? bannerData['multiProductBanner'] : {};
    this.subscribeBreakpointChanges();
    this.multiProductBannerList = (this.multiProductBanner['list'] && this.messages[this.multiProductBanner['list']]) ? this.messages[this.multiProductBanner['list']].split(',') : [];
    // this.multiProductBannerActiveList = (this.multiProductBanner['list'] && this.messages[this.multiProductBanner['list']]) ? this.buildMultiBannerActiveGroup(this.messages[this.multiProductBanner['list']].split(',')) : [];
    this.marketingBanner = this.bannerData['marketingBanner'] ? this.bannerData['marketingBanner'] : {};
    this.marketingBannerList = (this.marketingBanner['list'] && this.messages[this.marketingBanner['list']]) ? this.messages[this.marketingBanner['list']].split(',') : [];
    this.marketingBannerItems = this.buildMarketBannerData(this.marketingBannerList, this.marketingBanner,  '');
  }

  buildMultiBannerActiveGroup(group: string[]): Array<BannerTemplate> {
    const multiBannerData: BannerTemplate[] = [];

    group.forEach((item, index) => {
      const obj: {} = {};

      for (const bannerKey of Object.keys(this.multiProductBanner)) {
        if (bannerKey === 'list') {
          let productsLength = 0;
          let currentRow = 0;
          const lists: string[] = this.messages[item + '-' + this.multiProductBanner[bannerKey]] ? this.messages[item + '-' + this.multiProductBanner[bannerKey]].split(',') : [];
          const listDetails = this.buildMultiBannerData(lists, this.multiProductBanner, group[index] );
          const multiBannerDataPerRow: BannerTemplate[][] = [];

          this.setRowConfiguration(item, lists);

          listDetails.forEach((listItem, listIndex) => {
            const rowIndex = productsLength - listIndex;

            if (rowIndex === 0) {
              if (listIndex > 0) {
                currentRow++;
              }
              productsLength += this.rowConfig[currentRow];
            }

            multiBannerDataPerRow[currentRow] ? multiBannerDataPerRow[currentRow].push(listItem) : multiBannerDataPerRow[currentRow] = [listItem];
          });

          obj['listDetails'] = multiBannerDataPerRow;
        } else {
          obj[bannerKey] = item + '-' + this.multiProductBanner[bannerKey];
        }
      }
      multiBannerData.push(obj as BannerTemplate);
    });
    return multiBannerData;
  }

  buildMultiBannerData(list: string[], bannerTemplateObj: object, group ): Array<BannerTemplate> {
    const multiBannerData: BannerTemplate[] = [];
    list.forEach((item, index) => {
      const obj: {} = {};
      for (const bannerKey of Object.keys(bannerTemplateObj)) {
        const bannerMessageKey = (group) ? group + '-' + item + '-' + bannerTemplateObj[bannerKey] : item + '-' + bannerTemplateObj[bannerKey];
        obj[bannerKey] = this.messages[bannerMessageKey] || '' ;
      }
      multiBannerData.push(obj as BannerTemplate);
    });
    return multiBannerData;
  }

  buildMarketBannerData(list: string[], bannerTemplateObj: object, group ): Array<BannerTemplate> {
    const multiBannerData: BannerTemplate[] = [];
    list.forEach((item, index) => {
      const obj: {} = {};
      for (const bannerKey of Object.keys(bannerTemplateObj)) {
        const bannerMessageKey = (group) ? group + '-' + item + '-' + bannerTemplateObj[bannerKey] : item + '-' + bannerTemplateObj[bannerKey];
        obj[bannerKey] = this.messages[bannerMessageKey] || '' ;
        if (bannerKey === 'list' && obj[bannerKey]){
          const lists = this.messages[item + '-' + this.marketingBanner[bannerKey]] ? this.messages[item + '-' + this.marketingBanner[bannerKey]].split(',') : [];
          obj['listDetails'] = [ this.buildMultiBannerData(lists, this.marketingBanner, item ) ];
        }
      }
      multiBannerData.push(obj as BannerTemplate);
    });
    return multiBannerData;
  }

  getBanners() {
    this.bannerService.getStoreLandingBanners().subscribe(bannerData => {
      this.productFamilyBanners = bannerData && bannerData['family'];
      this.whatsNewBanners = bannerData && bannerData['whatsnew'];
      this.landingBanners = bannerData && bannerData['landing'];
      this.multiBanner = this.landingBanners?.filter(banner => banner.bannerType === AppConstants.MULTI_PRODUCT_BANNER_TYPE);
      this.singleProductBanners = this.landingBanners.filter(banner => banner.bannerType !== AppConstants.MULTI_PRODUCT_BANNER_TYPE);
    },
    error => {
      /* if (error.status === 401 || error.status === 0) {
        // this.sessionService.showTimeout();
      } else {
        this.notificationRibbonService.emitChange([true, this.translateService.instant('unknownError')]);
      } */
    });
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

}
