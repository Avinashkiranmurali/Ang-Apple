import { AfterContentChecked, ChangeDetectorRef, Component, Injector, OnDestroy, OnInit, ElementRef, ViewChild } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { BreakPoint } from '@app/components/utils/break-point';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { ProductService } from '@app/services/product.service';
import { Messages } from '@app/models/messages';
import { Product } from '@app/models/product';
import { FilterProducts, SortOptions, SortOptionsItems } from '@app/models/filter-products';
import { Subscription } from 'rxjs';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ModalsService } from '@app/components/modals/modals.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { User } from '@app/models/user';
import { Config } from '@app/models/config';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { AppConstants } from '@app/constants/app.constants';
import { find, isEmpty } from 'lodash';
import { NotificationRibbonService } from '@app/services/notification-ribbon.service';
import { GridEventHandlerService } from '@app/services/grid-event-handler.service';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss'],
})

export class SearchComponent extends BreakPoint implements OnInit, AfterContentChecked, OnDestroy {
  messages: Messages;
  user: User;
  terms: string;
  searchPage = true;
  contentLoading = true;
  noSearchResults = false;
  searchTerm: {};
  currLocale: string;
  moreSearchToLoad: boolean;
  showTaxDisclaimer: boolean;
  enableResetFlag: boolean;
  resetFiltersObj: boolean;
  filterProductsData: FilterProducts;

  result: boolean;
  displayBackorderedProducts = true; // TODO: Make this dynamic

  items: Array<Product>;
  item: Product;
  itemTotal: number;
  private subscriptions: Subscription[] = [];

  moreSearchItems: [];

  sortBy: SortOptionsItems;
  sortOptions: SortOptions;
  showFacetsFilters: boolean;
  routeParams: object;
  facetsFiltersObj: object;
  config: Config;
  analyticsUserObject: any;

  latestFacetsFilter: object;
  isInitLoaded = false;
  isRootChanged = false;
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
    public messageStore: MessagesStoreService,
    public injector: Injector,
    public userStore: UserStoreService,
    public activeModal: NgbActiveModal,
    public modalsService: ModalsService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    public sharedService: SharedService,
    private ensightenService: EnsightenService,
    public matomoService: MatomoService,
    private cdRef: ChangeDetectorRef,
    private notificationRibbonService: NotificationRibbonService,
    private gridEventHandlerService: GridEventHandlerService,
  ) {
    super(injector);
    this.routeParams = {};
    this.analyticsUserObject = {};
    this.user = this.userStore.user;
    this.config = this.userStore.config;
    this.messages = this.messageStore.messages;
    this.showFacetsFilters = this.isDesktop || this.isTablet;
    window.addEventListener('scroll', this.facetsScroll, true);

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

  filterToggle() {
    if (!(this.isDesktop || this.isTablet)) {
      document.body.style.overflow = 'hidden';
    }
    this.showFacetsFilters = !this.showFacetsFilters;
    if (!this.showFacetsFilters && !(this.isDesktop || this.isTablet)) {
      this.showFacetsFilters = !this.showFacetsFilters;
    }
  }

  closeModal(event) {
    this.showFacetsFilters = event;
    document.body.style.overflow = 'auto';
  }

  setSortBy(): void{
    const queryParams = this.activatedRoute.snapshot.queryParams;
    if (queryParams.sort) {
      this.sortBy = find(this.sortOptions, {label: queryParams.sort});
    }else{
      this.sortBy = this.sortOptions['relevancy'];
    }
  }
  loadInitData(isInit, isFacetsTriggered): void{
    const params = this.activatedRoute.snapshot.params;
    const queryParams = this.activatedRoute.snapshot.queryParams;
    this.setSortBy();
    this.moreSearchToLoad = false;
    this.terms = params['keyword'];
    this.searchTerm = {
      text: this.terms,
      value: this.terms,
    };
    if (isInit){
      this.items = [];
      if (isEmpty(this.facetsFiltersObj)) {
        this.facetsFiltersObj = {};
      }
    }

    const postArg = {
      facetsFilters: {},
      keyword: this.terms,
      pageSize: 12,
      resultOffSet: 0,
      order: this.sortBy.orderBy,
      sort: this.sortBy.sortBy,
      withVariations: true
    };

    const deepCopyQueryParams = {...queryParams};
    delete deepCopyQueryParams.sort;

    this.enableResetFlag = false;
    if (!isEmpty(deepCopyQueryParams)){
      this.enableResetFlag = true;
      postArg.facetsFilters = this.facetsFiltersObj;
    }
    // Load Initial Products
    this.getProductList(postArg, this.isInitLoaded, isFacetsTriggered);
  }
  ngOnInit(): void {
    this.sortOptions = this.sharedService.sortOptions;
    this.isInitLoaded = true;
    this.loadInitData(this.isInitLoaded, false);
    this.subscriptions.push(
      this.router.events.subscribe(event => {
        if (event instanceof NavigationEnd) {
          const params = this.activatedRoute.snapshot.params;
          if (params.keyword === this.terms) {
            this.terms = params.keyword + ' ';
            setTimeout(() => {
              this.terms = params.keyword;
            }, 0);
          }else{
            // Call initial function when the keyword got changed
            this.resetFlags();
            this.isRootChanged = true;
            this.isInitLoaded = true;
            this.loadInitData(true, false);
          }
        }
      })
    );

    this.subscriptions.push(
      this.activatedRoute.params.subscribe((params) => {
        this.setRouteParams(params);
      })
    );

    this.currLocale = this.user.locale;
    this.showTaxDisclaimer = this.config.showTaxDisclaimer;
    this.analyticsUserObject = this.sharedService.getAnalyticsUserObject(this.activatedRoute.snapshot.data);
  }

  ngAfterContentChecked(): void {
    this.cdRef.detectChanges();
  }
  setRouteParams(params){
    this.routeParams['category'] = params['category'];
    this.routeParams['subcat'] = params['subcat'];

    if (this.noSearchResults){
      if (document.getElementById('returnedErMsg')?.innerText){
        document.getElementById('returnedErMsg').innerText = '';
      }
    }else{
      if (document.getElementById('returnedMsg')?.innerText){
        document.getElementById('returnedMsg').innerText = '';
      }
    }
  }

  resetFlags() {
    this.result = false;
    this.contentLoading = true;
    this.enableResetFlag = false;
    this.filterProductsData = undefined;
    this.facetsFiltersObj = {};
    this.moreSearchToLoad = false;
  }

  getProductList(param, isInitLoaded, isFacetsTriggered) {
    this.contentLoading = true;
    this.productService.getFilteredProducts(param).subscribe((items) => {
        if (isEmpty(this.filterProductsData) || !this.filterProductsData){
          this.filterProductsData = items;
        }
        this.latestFacetsFilter = items.facetsFilters;
        const allItems = items.products;
        const allItemsTotal = items.totalFound;
        const deepCopyParams = {...this.activatedRoute.snapshot.queryParams};
        delete deepCopyParams.sort;
        // const setFacets(allItems);

        if (allItems && allItems.length > 0) {
          // don't run if there are no products
          this.item = allItems[0]; // TODO: verify this assignment for correctness;
          this.items = this.gridEventHandlerService.cleanData(allItems);
          this.itemTotal = allItemsTotal;
          this.noSearchResults = false;
        } else {
          this.noSearchResults = true;
        }

        let pageName = this.analyticsUserObject.pgName || '';
        pageName = pageName.replace('<search_term>', this.terms ? this.terms : '');
        const analyticsUserObj = {
          pgName: pageName.toLowerCase(),
          pgType: this.analyticsUserObject.pgType || '',
          pgSectionType: this.analyticsUserObject.pgSectionType || ''
        };

        if (isInitLoaded) {
          this.ensightenService.broadcastEvent(analyticsUserObj, this.items);
        }

        this.matomoService.broadcast(AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.CATEGORY_SEARCH, {
          payload: {
            searchTerm: this.terms,
            results: (allItems && allItems.length > 0) ? allItems : []
          }
        });

        this.moreSearchToLoad = false;
        if (this.items.length >= 12 && this.items.length !== this.itemTotal && (!this.filterProductsData.facetsFilters || Object.keys(param.facetsFilters).length > 0 || isFacetsTriggered)) {
        // Call load more if no facetFilters available
          this.getMoreSearchItems(allItems.length);
        // if facet filters available on refresh , then loadMore will call from Facets Emitter function
      }
        if (isFacetsTriggered || (isInitLoaded && allItems.length < 12)){
        this.isInitLoaded = false;
      }

        this.result = true;
        this.contentLoading = false;
      },
    (error) => {
      this.isInitLoaded = true;
      if (error.status === 401 || error.status === 0) {
        // sessionMgmt.showTimeout();
      } else {
        this.contentLoading = false;
      }
    });
  }

  loadMoreSearchItems(): void {
    const prevItems = this.items;
    if (this.moreSearchItems && this.moreSearchItems.length > 0) {
      this.items = prevItems.concat(this.moreSearchItems);

      // SEARCH analytics object
      let pageName = this.analyticsUserObject.pgName || '';
      pageName = pageName.replace('<search_term>', this.terms ? this.terms : '');
      const analyticsUserObj = {
        pgName: pageName.toLowerCase(),
        pgType: this.analyticsUserObject.pgType || '',
        pgSectionType: this.analyticsUserObject.pgSectionType || ''
      };
      this.ensightenService.broadcastEvent(analyticsUserObj, this.items);
    }

    if (this.moreSearchItems && (this.moreSearchItems.length < 12 || this.items.length === this.itemTotal)) {
      this.moreSearchToLoad = false;
    } else {
      this.getMoreSearchItems(this.items.length);
    }
}

  getMoreSearchItems(num): void {
    const param = {
      ...(this.facetsFiltersObj && {facetsFilters: this.facetsFiltersObj}),
      keyword: this.terms,
      pageSize: 12,
      resultOffSet: num,
      order: this.sortBy.orderBy,
      sort: this.sortBy.sortBy,
      withVariations: true
    };
    this.moreSearchToLoad = true;
    this.subscriptions.push(
      this.productService.getFilteredProducts(param).subscribe(
        (items) => {
          const moreItems = items.products;
          this.moreSearchItems = this.gridEventHandlerService.cleanData(moreItems);
        },
        (error) => {
          if (error.status === 401 || error.status === 0) {
            // sessionMgmt.showTimeout();
          } else {
            this.moreSearchItems = [];
            this.moreSearchToLoad = false;
          }
        }
      )
    );
  }

  enableReset(event): void {
    this.enableResetFlag = event;
    this.contentLoading = true;
  }

  filteredItems(data): void {
    this.items = this.gridEventHandlerService.cleanData(data.products);
    this.itemTotal = data.totalFound;
    window.scrollTo({ left: 0, top: 0, behavior: 'smooth' });
    this.resetFiltersObj = false;
    this.contentLoading = false;
    // SEARCH analytics object
    let pageName = this.analyticsUserObject.pgName || '';
    pageName = pageName.replace('<search_term>', this.terms ? this.terms : '');
    const analyticsUserObj = {
      pgName: pageName.toLowerCase(),
      pgType: this.analyticsUserObject.pgType || '',
      pgSectionType: this.analyticsUserObject.pgSectionType || ''
    };
    this.ensightenService.broadcastEvent(analyticsUserObj, this.items);
  }

  filterStickyTopOffset() {
    this.facetsScrollObj = this.gridEventHandlerService.filterStickyTopOffset(this.facetsScrollObj);

    return this.facetsScrollObj.filterBarTop;
  }

  triggerApi(facetsFiltersObj) {
    const params = this.activatedRoute.snapshot.params;
    if (this.terms !== params['keyword']){
      // this.terms !== params['keyword']  => Occured when filters applied and keyword changed
      return;
    }
    if (this.isRootChanged){
      this.isRootChanged = false;
    }
    this.facetsFiltersObj = facetsFiltersObj;

    if (this.isInitLoaded && Object.keys(this.facetsFiltersObj).length === 0){
      // load the second call and activate the scroll
      if (this.items.length >= 12) {
        this.getMoreSearchItems(12);
        this.isInitLoaded = false;
      }else{
        this.moreSearchToLoad = false;
      }
      // After call done make it this.initLoaded = false
    }else{
      this.loadInitData(true, true);
    }
  }
  triggerScroll(){
    this.facetsScroll(false);
  }
  getGrid(){
    const  grid = document.getElementById('grid');
    return  {
      offsetHeight : grid.offsetHeight,
      scrollHeight : grid.scrollHeight
    };
  }
  getFacetsContainerFullView(){
    const facet = document.getElementsByTagName('app-facets-filters-component')[0];
    if (!facet){
      return '';
    }
    return  {
      offsetHeight : facet['offsetHeight']
    };
  }
  getFacetsContainerView(){
    const facet = document.getElementsByClassName('facets-filters-template-container')[0];
    if (!facet){
      return '';
    }
    return  {
      offsetHeight : facet['offsetHeight']
    };
  }

  facetsScroll = (event): void => {
    this.facetsScrollObj = this.gridEventHandlerService.facetsScroll(event, this.facetsScrollObj);
  };

  ngOnDestroy(): void {
    this.subscriptions.forEach((subscription) => subscription.unsubscribe());
    window.removeEventListener('scroll', this.facetsScroll, true);
  }
}
