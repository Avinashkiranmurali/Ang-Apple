import { Component, OnDestroy, OnInit, Injector, ElementRef, ViewChild } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router, RouterEvent } from '@angular/router';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { ProductService } from '@app/services/product.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { FilterProducts, SortOptions, FilterProductsPayload, SortOptionsItems } from '@app/models/filter-products';
import { ModalsService } from '@app/components/modals/modals.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { BreakPoint } from '@app/components/utils/break-point';
import { Messages } from '@app/models/messages';
import { Subscription } from 'rxjs';
import { UserStoreService } from '@app/state/user-store.service';
import { Config } from '@app/models/config';
import { User } from '@app/models/user';
import { NotificationRibbonService } from '@app/services/notification-ribbon.service';
import { NavStoreService } from '@app/state/nav-store.service';
import { GridEventHandlerService } from '@app/services/grid-event-handler.service';
import { Product } from '@app/models/product';
import { find, isEmpty } from 'lodash';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';
import { Category } from '@app/models/category';

@Component({
  selector: 'app-grid',
  templateUrl: './grid.component.html',
  styleUrls: ['./grid.component.scss'],
})

export class GridComponent extends BreakPoint implements OnInit, OnDestroy {
  messages: Messages;
  items: Array<Product>;
  moreItems: Array<Product>;
  item: Product;
  itemTotal: number;
  productLoadError = false;
  result = false;
  isProcessing = false;
  displayBackorderedProducts = true; // TODO: Make this dynamic
  routeParams: object;
  filterProductsData: FilterProducts;
  sortOptions: SortOptions;
  sortBy: SortOptionsItems;
  enableResetFlag: boolean;

  showFacetsFilters: boolean;
  categoryName: string;
  subcatName: string;
  addCatName: string;
  addCat;
  category;
  subcat;
  mainNav: Array<Category>;
  private subscriptions: Subscription[] = [];
  categoryNavigationTray = {
    show: false,
  };
  gridOnlyTemplate = false;
  resultOffSet: number;
  facetsFiltersObj: object;
  totalFound: number;
  config: Config;
  user: User;
  filterToggleClicked: boolean;
  latestFacetsFilter: object;
  analyticsUserObject: any;

  rootURL: string;
  eventsSubscription: Subscription = null;
  isInitLoaded = false;

  facetComponent: ElementRef;
  facetsScrollObj = {
    notificationRibbonEnabled: false,
    fixedFacetsContainer: false,
    endPositionFacetsContainer: false,
    scrollTopPrev: 0,
    scrollTop: 0,
    transition: 0,
    maxTransition: 0,
    scrollStarts: 0,
    topFreezedElementHeight: 0,
    filterBarTop: 0
  };

  constructor(
    private activatedRoute: ActivatedRoute,
    private messageStore: MessagesStoreService,
    private productService: ProductService,
    public sharedService: SharedService,
    public modalsService: ModalsService,
    public activeModal: NgbActiveModal,
    public injector: Injector,
    private router: Router,
    private userStore: UserStoreService,
    private notificationRibbonService: NotificationRibbonService,
    private ensightenService: EnsightenService,
    private mainNavStore: NavStoreService,
    private gridEventHandlerService: GridEventHandlerService,
  ) {
    super(injector);
    this.routeParams = {};
    this.analyticsUserObject = {};
    this.messages = this.messageStore.messages;
    this.showFacetsFilters = this.isDesktop || this.isTablet;
    this.resultOffSet = 12;
    this.config = this.userStore.config;
    this.user = this.userStore.user;
    this.filterToggleClicked = false;
    this.mainNav = this.mainNavStore.mainNav;
    window.addEventListener('scroll', this.scroll, true);
    window.addEventListener('scroll', this.facetsScroll, true);
    this.activatedRoute.params.subscribe((params) => {
      this.setRouteParams(params);
    });

    // Get error notification ribbon status based on emitters from child pages
    notificationRibbonService.changeEmitted$.subscribe(
      dataArray => {
        const bool = dataArray[0];
        this.notificationRibbonService.setNotificationRibbonShow(bool);
      });
  }


  @ViewChild('facetComponent') set gridContainerElem(element: ElementRef | null){
    if (!element){return; }
    if (!this.facetsScrollObj.scrollStarts){
      const facetsComponentElem = document.getElementsByTagName('app-facets-filters-component')[0];
      this.facetsScrollObj.scrollStarts = facetsComponentElem.getBoundingClientRect().top - 80;
    }
  }

  setRouteParams(params) {
    this.routeParams['addCat'] = params['addCat'];
    this.routeParams['category'] = params['category'];
    this.routeParams['subcat'] = params['subcat'];
    const category = this.routeParams['category'];
    const subcat = this.routeParams['subcat'];
    const currentCatData = this.mainNav.filter(nav => nav.slug === category);
    const currentSubCatObj = (currentCatData && currentCatData.length && currentCatData[0].subCategories) ? currentCatData[0].subCategories : [];
    const currentSubCatData = currentSubCatObj.filter(subCatData => subCatData.slug === subcat );
    this.categoryName = (currentCatData && currentCatData.length && currentCatData[0].i18nName) ? currentCatData[0].i18nName : '';
    this.subcatName = (currentSubCatData && currentSubCatData.length && currentSubCatData[0].i18nName) ? currentSubCatData[0].i18nName : '';
    if (this.routeParams['addCat'] !== '' && this.routeParams['addCat'] !== undefined ){
      const addCatData = (currentSubCatData && currentSubCatData.length && currentSubCatData[0].subCategories) ? currentSubCatData[0].subCategories : [];
      const currentAddCarData = addCatData.filter(addCat => addCat.slug === this.routeParams['addCat'] );
      this.addCatName = (currentAddCarData && currentAddCarData.length && currentAddCarData[0].i18nName) ? currentAddCarData[0].i18nName : '';
    }
  }
  scroll = (event): void => {
    const footerHeight = document.getElementsByTagName('footer')[0].offsetHeight;
    if (window.innerHeight + window.scrollY >= (document.body.offsetHeight - footerHeight - 60)) {
      if (this.items?.length === 12) {
        this.items = [...this.items, ...this.moreItems];
      }

      if (!this.isProcessing && this.itemTotal > this.items?.length) {
        this.resultOffSet = this.resultOffSet + 12;
        this.scrollToLoadProducts();
      }
    }
  };

  facetsScroll = (event): void => {
    this.facetsScrollObj = this.gridEventHandlerService.facetsScroll(event, this.facetsScrollObj);
  };

  setPostArg() {
    const postArg: FilterProductsPayload = {
      facetsFilters: this.facetsFiltersObj ? this.facetsFiltersObj : {},
      pageSize: 12,
      resultOffSet: this.resultOffSet,
      order: this.sortBy.orderBy,
      sort: this.sortBy.sortBy
    };

    if (this.router.url.indexOf('webshop') > -1) {
      postArg.promoTag = this.routeParams['category'];
    } else {
      postArg.categorySlugs = [
        this.routeParams['addCat']
          ? this.routeParams['addCat']
          : this.routeParams['category'] === 'accessories'
          ? 'all-accessories'
          : this.routeParams['subcat'],
      ];
    }

    return postArg;
  }

  scrollToLoadProducts() {
    this.isProcessing = true;
    const postArg = this.setPostArg();

    this.productService.getFilteredProducts(postArg).subscribe((items) => {
      this.items = [...this.items, ...items.products];
      this.isProcessing = false;
      this.triggerAnalyticsEvent();
    });
  }

  loadMoreProducts() {
    this.resultOffSet = 12;
    const postArg = this.setPostArg();

    this.productService.getFilteredProducts(postArg).subscribe((items) => {
      this.moreItems = items.products;
      // this.result = true;
    });
  }

  closeModal(event) {
    this.showFacetsFilters = event;
    document.body.style.overflow = 'auto';
  }
  setSortBy(){
    const queryParams = this.activatedRoute.snapshot.queryParams;

    // set SortBy for Product Grid Page
    if (queryParams.sort){
      this.sortBy = find(this.sortOptions, {label: queryParams.sort});
    }else if (this.isAccesseries())
    {
      this.sortBy = this.sortOptions['popularity'];
    } else {
      this.sortBy = this.sortOptions['price_low_to_high'];
    }
  }
  loadInitData(isInit, isFacetsTriggered){
    const queryParams = this.activatedRoute.snapshot.queryParams;
    const params = this.activatedRoute.snapshot.params;
    this.setSortBy();
    // Set/Reset filters
    // Reset Products
    this.result = false;
    if (isInit){
      this.items = [];
      if (isEmpty(this.facetsFiltersObj)) {
        this.facetsFiltersObj = {};
      }
    }
    const postArg: FilterProductsPayload = {
      facetsFilters: {},
      pageSize: 12,
      resultOffSet: this.resultOffSet = 0,
      order: this.sortBy.orderBy,
      sort: this.sortBy.sortBy
    };
    const deepCopyQueryParams = {...queryParams};
    delete deepCopyQueryParams.sort;

    this.enableResetFlag = false;
    if (!isEmpty(deepCopyQueryParams)){
      this.enableResetFlag = true;
      postArg.facetsFilters = this.facetsFiltersObj;
    }

    if (this.router.url.indexOf('webshop') > -1) {
      postArg.promoTag = params['category'];
    } else {
      postArg.categorySlugs = [
        params['addCat']
          ? params['addCat']
          : params['category'] === 'accessories'
          ? 'all-accessories'
          : params['subcat'],
      ];
    }
    // Load Initial Products
    this.getProductList(postArg, this.isInitLoaded, isFacetsTriggered);
  }
  isAccesseries(){
    const params = this.activatedRoute.snapshot.params;
    return (params['subcat']?.split('-')[1] === 'accessories' || params['category'] === 'accessories');
  }
  ngOnInit(): void {
    this.sortOptions = this.sharedService.sortOptions;
    this.rootURL = this.activatedRoute['_routerState'].snapshot.url.split('?')[0];
    this.isInitLoaded = true;
    this.loadInitData(true, false);

    this.subscriptions.push(this.router.events
      .subscribe((event: RouterEvent) => {
      if (event instanceof NavigationEnd) {
        const url = this.activatedRoute['_routerState'].snapshot.url.split('?')[0];
        if (url !== this.rootURL){
          this.rootURL = url;
          this.isInitLoaded = true;
          this.refreshFacetsFilterComponent();
          this.loadInitData(true, false);
        }else if (isEmpty(this.filterProductsData?.facetsFilters)){
           // Sort or refresh //no facets filters
           this.loadInitData(true, false);
        }

      }
    }));
    this.analyticsUserObject = this.sharedService.getAnalyticsUserObject(this.activatedRoute.snapshot.data);
  }

  filterToggle() {
    this.filterToggleClicked = true;
    if (this.isMobile) {
      document.body.style.overflow = 'hidden';
      document.getElementById('btn-modalDone')?.focus();
    }
    this.showFacetsFilters = !this.showFacetsFilters;
    if (!this.showFacetsFilters && this.isMobile) {
      this.showFacetsFilters = !this.showFacetsFilters;
    }
  }

  filteredItems(data) {
    this.result = true;
    this.items = this.gridEventHandlerService.cleanData(data.products);
    this.itemTotal = data.totalFound;
    this.productLoadError = this.items.length === 0;
    this.triggerAnalyticsEvent();
  }

  getCategory() {
    if (this.router.url.indexOf('webshop') > -1) {
      return this.routeParams['category'];
    } else {
      return  this.routeParams['addCat'] ? this.routeParams['addCat'] : this.routeParams['category'] === 'accessories' ? 'all-accessories' : this.routeParams['subcat'];
    }
  }

  refreshFacetsFilterComponent(){
    this.enableResetFlag = false;
    this.filterProductsData = undefined;
    this.facetsFiltersObj = {};
    this.showFacetsFilters = this.isDesktop || this.isTablet;
  }

  getProductList(param, isInitLoaded, isFacetsTriggered) {
      this.productService.getFilteredProducts(param).subscribe((items) => {
        if (isEmpty(this.filterProductsData)){
          this.filterProductsData = items;
        }
        this.latestFacetsFilter = items.facetsFilters;
        this.result = true;
        this.productLoadError = false;
        const allItems = items.products;
        const allItemsTotal = items.totalFound;
        if (allItems && allItems.length > 0) {
          // don't run if there are no products
          this.item = allItems[0]; // TODO: verify this assignment for correctness;
          this.items = this.gridEventHandlerService.cleanData(allItems);
          this.itemTotal = allItemsTotal;
          // AGP analytics object
          let pageName = this.analyticsUserObject.pgName || '';
          pageName = pageName.replace('<product>', this.getCategory().replace(/-/g, ' '));
          const analyticsUserObj = {
            pgName: pageName.toLowerCase(),
            pgType: this.analyticsUserObject.pgType || '',
            pgSectionType: this.analyticsUserObject.pgSectionType || ''
          };
          if (isInitLoaded) {
            this.ensightenService.broadcastEvent(analyticsUserObj, this.items);
          }
          const deepCopyParams = {...this.activatedRoute.snapshot.queryParams};
          delete deepCopyParams.sort;

          if (this.items.length >= 12 && this.items.length !== this.itemTotal && (!this.filterProductsData.facetsFilters || Object.keys(param.facetsFilters).length > 0 || isFacetsTriggered)) {
            // Call load more if no facetFilters available
            this.loadMoreProducts();
            // if facet filters available on refresh , then loadMore will call from Facets Emitter function
          }
          if (isFacetsTriggered || (isInitLoaded && allItems.length < 12)){
            this.isInitLoaded = false;
          }
        } else {
          this.productLoadError = true;
        }
      }, (error) => {
        this.isInitLoaded = true;
        if (error.status === 401 || error.status === 0) {
          // sessionMgmt.showTimeout();
        } else {
          this.productLoadError = true;
        }
      });

  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((subscription) => subscription.unsubscribe());
    window.removeEventListener('scroll', this.scroll, true);
    window.removeEventListener('scroll', this.facetsScroll, true);
  }

  filterStickyTopOffset() {
    this.facetsScrollObj = this.gridEventHandlerService.filterStickyTopOffset(this.facetsScrollObj);

    return this.facetsScrollObj.filterBarTop;
  }

  toggleCategoryNavigationTray() {
    this.categoryNavigationTray.show = !this.categoryNavigationTray.show;
  }
  triggerScroll(){
    this.facetsScroll(false);
  }
  triggerApi(facetsFiltersObj) {
    const url = this.activatedRoute['_routerState'].snapshot.url.split('?')[0];
    if (url !== this.rootURL) {
      // Occured when filters applied and navigate to differnt grid
     return;
    }
    this.facetsFiltersObj = facetsFiltersObj;

    if (this.isInitLoaded && Object.keys(this.facetsFiltersObj).length === 0){
      // load the second call and activate the scroll
      if (this.items.length >= 12) {
        this.loadMoreProducts();
        this.isInitLoaded = false;
      }
      // After call done make it this.initLoaded = false
    }else{
      this.loadInitData(true, true);
    }
  }

  triggerAnalyticsEvent() {
    let pageName = this.analyticsUserObject.pgName || '';
    pageName = pageName.replace('<product>', this.getCategory().replace(/-/g, ' '));
    const analyticsUserObj = {
      pgName: pageName.toLowerCase(),
      pgType: this.analyticsUserObject.pgType || '',
      pgSectionType: this.analyticsUserObject.pgSectionType || ''
    };
    this.ensightenService.broadcastEvent(analyticsUserObj, this.items);
  }
}
