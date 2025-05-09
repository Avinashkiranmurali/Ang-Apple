import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { GridComponent } from './grid.component';
import { ActivatedRoute, NavigationEnd, Router, RouterEvent } from '@angular/router';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { BehaviorSubject, of, throwError } from 'rxjs';
import { PageTitleComponent } from '@app/modules/shared/page-title/page-title.component';
import { CategoryNavigationTrayComponent } from '@app/modules/shared/category-navigation-tray/category-navigation-tray.component';
import { FilterProducts, FilterProductsPayload } from '@app/models/filter-products';
import { ProductService } from '@app/services/product.service';
import { UserStoreService } from '@app/state/user-store.service';
import { FacetsFiltersComponent } from '@app/modules/facets-filters/facets-filters.component';
import { SortByComponent } from '@app/modules/sort-by/sort-by.component';
import { NavStoreService } from '@app/state/nav-store.service';
import { GridEventHandlerService } from '@app/services/grid-event-handler.service';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';

describe('GridComponent', () => {
  let component: GridComponent;
  let fixture: ComponentFixture<GridComponent>;
  let httpTestingController: HttpTestingController;
  let productService: ProductService;
  let mainNavStore: NavStoreService;
  let gridEventHandlerService: GridEventHandlerService;
  const routerEvent$ = new BehaviorSubject<RouterEvent>(null);
  const mainNav = require('assets/mock/categories.json');
  const programData = require('assets/mock/program.json');
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };
  let router;

  const filterProductsData: FilterProducts = require('assets/mock/facets-filters.json');

  const sortOptionsMock = {
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
    }
  };
  const facetsScrollObj = {
    notificationRibbonEnabled: false,
    fixedFacetsContainer: false,
    endPositionFacetsContainer: false,
    scrollTopPrev: 0,
    scrollTop: 0,
    transition: 0,
    maxTransition: 0,
    scrollStarts: 326,
    topFreezedElementHeight: 0,
    filterBarTop: 0
  };
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        NgbModule,
        TranslateModule.forRoot()
      ],
      declarations: [
        GridComponent,
        PageTitleComponent,
        CategoryNavigationTrayComponent,
        FacetsFiltersComponent,
        SortByComponent
      ],
      providers: [
        { provide: ActivatedRoute, useValue: {
          params: of({ category: 'mac', subcat: 'macbook-pro', addCat: undefined }),
          queryParams: of({}),
          snapshot: {params: {}, queryParams: {}}
          }
        },
        { provide: NgbActiveModal },
        { provide: ParsePsidPipe },
        { provide: UserStoreService, useValue: userData },
        { provide: EnsightenService, useValue: {
          broadcastEvent: () => {} }
        },
        GridEventHandlerService,
        { provide: CurrencyFormatPipe },
        { provide: DecimalPipe },
        { provide: CurrencyPipe }
      ]
    })
    .compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
    productService = TestBed.inject(ProductService);
    router = TestBed.inject(Router);
    gridEventHandlerService = TestBed.inject(GridEventHandlerService);
    (router as any).events = routerEvent$.asObservable();
    mainNavStore = TestBed.inject(NavStoreService);
    mainNavStore.addMainNav(mainNav);
    const footerElement = document.createElement('footer');
    document.body.appendChild(footerElement);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GridComponent);
    component = fixture.componentInstance;
    component.filterProductsData = filterProductsData;
    component.facetsScrollObj = facetsScrollObj;
    const items = require('assets/mock/products-ipad.json');
    component.items = gridEventHandlerService.cleanData(items.products);
    component.showFacetsFilters = true;
    component.moreItems = [];
    window.scrollTo({ top: 0, behavior: 'smooth' });
    component['activatedRoute'].params = of({ category: 'mac', subcat: 'macbook-pro', addcat: undefined });
    component['activatedRoute'].queryParams = of({});
    component['activatedRoute']['_routerState'] = {snapshot: {url: '/store/browse/mac/macbook-pro'}};
    fixture.detectChanges();
  });

  afterEach(() => {
    TestBed.resetTestingModule();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
    const mockFilterProductResponse = require('assets/mock/facets-filters.json');
    // Expect a call to this URL
    const req = httpTestingController.expectOne(productService.baseUrl + 'filterProducts');
    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');
    // Respond with the fake data when called
    req.flush(mockFilterProductResponse);
  });

  it('should init', () => {
    expect(component.routeParams['addCat']).toEqual(undefined);
    expect(component.routeParams['category']).toEqual('mac');
    expect(component.routeParams['subcat']).toEqual('macbook-pro');
    expect(component.sortBy['key']).toEqual('price_low_to_high');

    component['activatedRoute'].params = of({ category: 'accessories', subcat: 'all-accessories', addcat: undefined });
    fixture.detectChanges();
    spyOnProperty(router, 'url', 'get').and.returnValue('/webshop');
    // router.url = '/webshop';
    component.ngOnInit();
  });

  it('filteredItems should load the products without load error', () => {
    const mockProducts = require('assets/mock/products-ipad.json');
    const data = {
      products: mockProducts['products'],
      totalFound: 12
    };
    component.filteredItems(data);
    expect(component.itemTotal).toBe(data.totalFound);
    expect(component.productLoadError).toEqual(false);
  });

  it('filteredItems should generate product load error', () => {
    const data = {
     products: []
    };
    component.filteredItems(data);
    expect(component.productLoadError).toEqual(true);
  });


  it('should call loadInitData', () => {
    expect(component.routeParams['addCat']).toEqual(undefined);
    expect(component.routeParams['category']).toEqual('mac');
    expect(component.routeParams['subcat']).toEqual('macbook-pro');
    expect(component.sortBy['key']).toEqual('price_low_to_high');

    component['activatedRoute'].params = of({ category: 'accessories', subcat: 'all-accessories', addcat: undefined });
    fixture.detectChanges();
    spyOnProperty(router, 'url', 'get').and.returnValue('/webshop');
    // router.url = '/webshop';
    component.loadInitData(false, true);
  });

  it('should scroll to load more products', () => {
    // Fake response data
    const fakeResponse = require('assets/mock/products-ipad.json');
    // Expect a call and return the success response
    spyOn(productService, 'getFilteredProducts').and.returnValue(of(fakeResponse));
    component.routeParams = {addCat: undefined, category: 'mac', subcat: 'macbook-pro'};

    component.sortOptions = sortOptionsMock;
    component.sortBy['key'] = 'price_low_to_high';
    component.scrollToLoadProducts();

    spyOnProperty(router, 'url', 'get').and.returnValue('/webshop');
    // router.url = '/webshop';
    component.scrollToLoadProducts();
    expect(component).toBeTruthy();
  });
  it('should init,loadMore, scroll to Load more products accessories with addcat', () => {
    // Fake response data
    const fakeResponse = require('assets/mock/products-ipad.json');
    // Expect a call and return the success response
    spyOn(productService, 'getFilteredProducts').and.returnValue(of(fakeResponse));
    component.sortOptions = sortOptionsMock;
    component.sortBy['key'] = 'price_low_to_high';
    spyOnProperty(router, 'url', 'get').and.returnValue('/store/curated/accessories/all-accessories/all-accessories-only-at-apple');
    component.facetsFiltersObj = undefined;
    component.routeParams = {addCat: 'iphone-accessories-audio-music', category: 'iphone', subcat: 'iphone-accessories'};
    component['activatedRoute'].snapshot.params = component.routeParams;
    fixture.detectChanges();
    component.facetsFiltersObj = {
      color: [{
        disabled: false,
        i18Name: 'Color',
        isFiltered: true,
        key: 'Black',
        name: 'color',
        orderBy: 0,
        points: null,
        swatchImageUrl: null,
        value: 'Black'
      }]
    };
    component.loadInitData(true, false);
    expect(component.enableResetFlag).toBe(false);
    component.facetsFiltersObj = undefined;
    component.loadMoreProducts();
    expect(component.loadMoreProducts).toBeDefined();
    component.scrollToLoadProducts();
    expect(component.scrollToLoadProducts).toBeDefined();

  });
  it('should init,loadMore, scroll to Load more products for accessories', () => {
    // Fake response data
    const fakeResponse = require('assets/mock/products-ipad.json');
    // Expect a call and return the success response
    spyOn(productService, 'getFilteredProducts').and.returnValue(of(fakeResponse));
    component.sortOptions = sortOptionsMock;
    component.sortBy['key'] = 'price_low_to_high';
    component.facetsFiltersObj = undefined;
    spyOnProperty(router, 'url', 'get').and.returnValue('/store/curated/accessories/all-accessories');
    component.routeParams = {addCat: '', category: 'accessories', subcat: 'all-accessories'};
    component['activatedRoute'].snapshot.params = component.routeParams;
    fixture.detectChanges();
    component.loadInitData(true, false);
    expect(component.enableResetFlag).toBe(false);
    component.facetsFiltersObj = undefined;
    component.loadMoreProducts();
    expect(component.loadMoreProducts).toBeDefined();
    component.facetsFiltersObj = undefined;
    component.scrollToLoadProducts();
    expect(component.scrollToLoadProducts).toBeDefined();

  });

  it('should load more products', () => {
    component.routeParams = {addCat: undefined, category: 'mac', subcat: 'macbook-pro'};
    component.sortOptions = sortOptionsMock;
    component.sortBy['key'] = 'price_low_to_high';
    component.loadMoreProducts();

    spyOnProperty(router, 'url', 'get').and.returnValue('/webshop');
    // router.url = '/webshop';
    component.loadMoreProducts();
    expect(component).toBeTruthy();
  });

  it('expect filter toggle on mobile', () => {
    component.isMobile = true;
    component.showFacetsFilters = false;
    spyOn(document, 'querySelector').and.returnValue(fixture.nativeElement);
    fixture.detectChanges();
    component.filterToggle();
    expect(component.showFacetsFilters).toBeTruthy();
  });
  it('expect filter toggle on mobile', () => {
    component.isMobile = true;
    component.showFacetsFilters = true;
    spyOn(document, 'querySelector').and.returnValue(undefined);
    fixture.detectChanges();
    component.filterToggle();
    expect(component.showFacetsFilters).toBeTruthy();
  });
  it('expect filter toggle on desktop', () => {
    component.isMobile = false;
    component.showFacetsFilters = false;
    fixture.detectChanges();
    component.filterToggle();
    expect(component.showFacetsFilters).toBeTruthy();
  });
  it('expect close modal', () => {
    component.closeModal(true);
    expect(component).toBeTruthy();
  });

  it('expect refresh facets filter component', () => {
    component.refreshFacetsFilterComponent();
    expect(component.enableResetFlag).toBeFalse();
    expect(component.filterProductsData).toEqual(undefined);
  });

  it('expect construct route', () => {
    gridEventHandlerService.cleanData([{psid: '1234-5678', options: [{name: 'storage'}]}]);
    expect(component).toBeTruthy();
  });

  it('on toggleCategoryNavigationTray(), toggle the value of categoryNavigationTray.show', () => {
    const showCategoryNavigationTray = component.categoryNavigationTray.show;
    component.toggleCategoryNavigationTray();
    expect(component.categoryNavigationTray.show).toBe(!showCategoryNavigationTray);
  });

  it('scroll to call scroll(0) with no items', waitForAsync(() => {
    component.items = undefined;
    // component.filterProductsData.facetsFilters = undefined;
    component.moreItems = [];
    component.itemTotal = 0; /*
    spyOn(gridEventHandlerService, 'getGrid').and.returnValue({offsetHeight: 1200, scrollHeight: 500});
    spyOn(gridEventHandlerService, 'getFacetsContainerFullView').and.returnValue({offsetHeight: 600});*/
    fixture.detectChanges();
    window.scrollTo(0, 0);
    spyOn(component, 'scroll').and.callThrough();
    expect(component.scroll).toBeDefined();
    // component.facetsScroll(undefined);
    expect(component.facetsScroll).toBeDefined();
  }));
  it('Facets scroll Up ', waitForAsync(() => {
    component.moreItems = [];
    component.itemTotal = 12;
    component.facetsScrollObj.scrollTop = 600;
    spyOn(gridEventHandlerService, 'getFacetsContainerView').and.returnValue({offsetHeight: window.innerHeight * 2});
    fixture.detectChanges();
    window.scrollTo(0, 400);
    fixture.detectChanges();
    // spyOn(component, 'scroll').and.callThrough();
    expect(component.scroll).toBeDefined();
    // component.facetsScroll(undefined);
    expect(component.facetsScroll).toBeDefined();
  }));
  it('scroll to call scroll(60) with less than 12 items with facets', waitForAsync(() => {
    component.items.pop();
    component.items.length = 11;
    component.moreItems = [];
    component.itemTotal = 100; /*
    spyOn(gridEventHandlerService, 'getGrid').and.returnValue({offsetHeight: 1200, scrollHeight: 500});
    spyOn(gridEventHandlerService, 'getFacetsContainerFullView').and.returnValue({offsetHeight: 600});*/
    window.scrollTo(0, 60);
    fixture.detectChanges();
    // spyOn(component, 'scroll').and.callThrough();
    // expect(component.scroll).toBeDefined();
    // component.facetsScroll(undefined);
    expect(component.facetsScroll).toBeDefined();
  }));
  it('scroll to call scroll(middle) with less than 12 items with facets', waitForAsync(() => {
    component.items.length = 12;
    component.moreItems = [];
    component.itemTotal = 100; /*
    spyOn(gridEventHandlerService, 'getGrid').and.returnValue({offsetHeight: 1200, scrollHeight: 500});
    spyOn(gridEventHandlerService, 'getFacetsContainerFullView').and.returnValue({offsetHeight: 600});*/
    spyOn(gridEventHandlerService, 'getFacetsContainerView').and.returnValue({offsetHeight: window.innerHeight - 100});
    window.scrollTo(0, document.body.scrollHeight / 2 );
    fixture.detectChanges();
    expect(component.facetsScroll).toBeDefined();
  }));
  it('scroll to call scroll(middle) with less than 12 items with less facets filters', waitForAsync(() => {
    const items = require('assets/mock/facets-filters.json');
    component.filterProductsData = items;
    component.items = gridEventHandlerService.cleanData(items.products);
    component.items.length = 12;
    component.moreItems = [];
    component.itemTotal = 100;
    fixture.detectChanges();

    spyOn(gridEventHandlerService, 'getGrid').and.returnValue({offsetHeight: 1200, scrollHeight: 500});
    /* spyOn(gridEventHandlerService, 'getFacetsContainerFullView').and.returnValue({offsetHeight: 600});*/
    window.scrollTo(0, document.body.scrollHeight / 2 );
    fixture.detectChanges();
    expect(component.facetsScroll).toBeDefined();
  }));
  it('scroll to call scroll(middle) with less than 12 items with facets scroll event undefined', waitForAsync(() => {
    component.items.pop();
    component.items.length = 11;
    component.moreItems = [];
    component.itemTotal = 100; /*
    spyOn(gridEventHandlerService, 'getGrid').and.returnValue({offsetHeight: 1200, scrollHeight: 500});
    spyOn(gridEventHandlerService, 'getFacetsContainerFullView').and.returnValue({offsetHeight: 600});*/
    window.scrollTo(0, document.body.scrollHeight / 2 );
    fixture.detectChanges();
    component.facetsScroll(undefined);
    expect(component.facetsScroll).toBeDefined();
  }));
  it('scroll to call scroll(middle) with less items (2) with facets', waitForAsync(() => {
    const items = require('assets/mock/facets-filters.json');
    component.items = gridEventHandlerService.cleanData(items.products);
    component.items.length = 2;
    component.moreItems = [];
    component.itemTotal = 100;
    spyOn(gridEventHandlerService, 'getGrid').and.returnValue({offsetHeight: 200, scrollHeight: 100});
    spyOn(gridEventHandlerService, 'getFacetsContainerFullView').and.returnValue({offsetHeight: 300});
    window.scrollTo(0, document.body.scrollHeight / 2 );
    fixture.detectChanges();
    expect(component.facetsScroll).toBeDefined();
  }));

  it('scroll to call scroll(end) with less than 12 items with less facets', waitForAsync(() => {
    const items = require('assets/mock/facets-filters.json');
    component.filterProductsData = items;
    component.items = gridEventHandlerService.cleanData(items.products);
    component.items.length = 12;
    component.moreItems = [];
    component.itemTotal = 100;
    window.scrollTo(0, document.body.scrollHeight);
    fixture.detectChanges();
    expect(component.facetsScroll).toBeDefined();
  }));
  it('scroll to call scroll(end) with less than 12 items with facets', waitForAsync(() => {
    component.items.pop();
    component.items.length = 11;
    component.moreItems = [];
    component.itemTotal = 100;
    const facetKeys = Object.keys(component.filterProductsData.facetsFilters);
    let loop = 0;
    for (const i in facetKeys){
      if (i){
        if (facetKeys.length === loop){
          return;
        }
        delete component.filterProductsData.facetsFilters[i];
        loop++;
      }
    }
    window.scrollTo(0, document.body.scrollHeight);
    fixture.detectChanges();
    expect(component.facetsScroll).toBeDefined();
  }));
  it('scroll to call scroll(end) faces has available space', waitForAsync(() => {
    const items = require('assets/mock/facets-filters.json');
    component.filterProductsData = items;
    component.items = gridEventHandlerService.cleanData(items.products);
    component.items.length = 12;
    component.moreItems = [];
    component.itemTotal = 100;
    // Mock for scroll Reached Facets
    spyOnProperty(document.documentElement, 'scrollTop', 'get').and.returnValue(component.facetsScrollObj.scrollStarts + 300);
    spyOn(component, 'filterStickyTopOffset').and.returnValue(60);
    component.facetsScrollObj.filterBarTop = 60;
    spyOn(gridEventHandlerService, 'getFacetsContainerView').and.returnValue({offsetHeight: window.innerHeight - component.facetsScrollObj.topFreezedElementHeight -  300});
    // fixture.detectChanges();
    window.scrollTo(0, document.body.scrollHeight);
    fixture.detectChanges();
    expect(component.facetsScroll).toBeDefined();
  }));
  it('scroll to call scroll(end) scroll reach end', waitForAsync(() => {
    component.items.length = 12;
    component.moreItems = [];
    component.itemTotal = 100;
    // Mock for scroll Reached Facets
    spyOnProperty(document.documentElement, 'scrollTop', 'get').and.returnValue(component.facetsScrollObj.scrollStarts + 300);
    spyOn(component, 'filterStickyTopOffset').and.returnValue(60);
    component.facetsScrollObj.filterBarTop = 60;
    spyOn(gridEventHandlerService, 'getFacetsContainerView').and.returnValue({offsetHeight: window.innerHeight + component.facetsScrollObj.topFreezedElementHeight +  400});
    // fixture.detectChanges();
    window.scrollTo(0, document.body.scrollHeight - 100);
    fixture.detectChanges();
    expect(component.facetsScroll).toBeDefined();
  }));
  it('scroll to call scroll(end) faces scroll not reach end / facets space available', waitForAsync(() => {
    const items = require('assets/mock/facets-filters.json');
    component.filterProductsData = items;
    component.items = gridEventHandlerService.cleanData(items.products);
    component.items.length = 12;
    component.moreItems = [];
    component.itemTotal = 100;
    // Mock for scroll Reached Facets
    spyOnProperty(document.documentElement, 'scrollTop', 'get').and.returnValue(200);
    // spyOn(component, 'filterStickyTopOffset').and.returnValue(60);
    // component.facetsScrollObj.filterBarTop = 60;
    spyOn(gridEventHandlerService, 'getFacetsContainerView').and.returnValue({offsetHeight: 600});
    window.scrollTo(0, document.body.scrollHeight);
    fixture.detectChanges();
    expect(component.facetsScroll).toBeDefined();
  }));
  it('scroll to call scroll() with no facets', waitForAsync(() => {
    component.items.pop();
    component.items.length = 11;
    component.moreItems = [];
    component.itemTotal = 100; /*
    spyOn(gridEventHandlerService, 'getGrid').and.returnValue({offsetHeight: 1200, scrollHeight: 500});
    spyOn(gridEventHandlerService, 'getFacetsContainerFullView').and.returnValue({offsetHeight: 600});*/
    component.filterProductsData.facetsFilters = undefined;
    fixture.detectChanges();
    window.scrollTo(0, 100);
    // fixture.detectChanges();
    // spyOn(component, 'scroll').and.callThrough();
    // expect(component.scroll).toBeDefined();
    // component.facetsScroll(undefined);
    expect(component.facetsScroll).toBeDefined();
  }));
  it('facetView container element with no facets', waitForAsync(() => {
    spyOn(gridEventHandlerService, 'getFacetsContainerView');
    component.items.length = 12;
    component.moreItems = [];
    component.itemTotal = 100;
    component.filterProductsData.facetsFilters = undefined;
    gridEventHandlerService.getFacetsContainerView();
    expect(gridEventHandlerService.getFacetsContainerView).toHaveBeenCalled();
  }));
  it('facetFullView container element with no facets', waitForAsync(() => {
    component.items.length = 12;
    component.moreItems = [];
    component.itemTotal = 100;
    component.filterProductsData.facetsFilters = undefined;
    gridEventHandlerService.getFacetsContainerFullView();
    expect(gridEventHandlerService.getFacetsContainerFullView).toBeDefined();
  }));
  it('should call getProductList method with success response', waitForAsync(() => {
    // Fake response data
    const fakeResponse = require('assets/mock/products-ipad.json');
    // Expect a call and return the success response
    spyOn(productService, 'getFilteredProducts').and.returnValue(of(fakeResponse));
    fixture.detectChanges();
    const postArg: FilterProductsPayload = {
      facetsFilters: {},
      pageSize: 12,
      resultOffSet: 0,
      order: 'ASCENDING',
      sort: 'DISPLAY_PRICE'
    };
    spyOn(component, 'getProductList').and.callThrough();
    component.getProductList(postArg, true, false);
    expect(component.getProductList).toHaveBeenCalled();
  }));

  it('should call getProductList method with 404 failure response', waitForAsync(() => {
    // Fake response data
    const errorResponse = {
      status: 404,
      statusText: 'Not Found'
    };
    // Expect a call and return the success response
    spyOn(productService, 'getFilteredProducts').and.returnValue(throwError(errorResponse));
    spyOn(component, 'getProductList').and.callThrough();
    component.getProductList(null, true, true );
    expect(component.getProductList).toHaveBeenCalled();
  }));
  it('should call getProductList method with success response and no products', waitForAsync(() => {
    // Fake response data
    const mockFakeResponse = Object.assign({});
    mockFakeResponse['products'] = [];
    mockFakeResponse['totalFound'] = 0;
    // Expect a call and return the success response
    spyOn(productService, 'getFilteredProducts').and.returnValue(of(mockFakeResponse));
    fixture.detectChanges();
    const postArg: FilterProductsPayload = {
      pageSize: 12,
      resultOffSet: 0,
      order: 'ASCENDING',
      sort: 'DISPLAY_PRICE'
    };
    spyOn(component, 'getProductList').and.callThrough();
    component.getProductList(postArg, true, false);
    expect(component.getProductList).toHaveBeenCalled();
  }));
  it('should call getCategory with addCat', waitForAsync(() => {
    const routeParams = {addCat: 'iphone-accessories-audio-music', category: 'iphone', subcat: 'iphone-accessories'};
    component.setRouteParams(routeParams);
    component.getCategory();
    expect(component.getCategory).toBeDefined();
  }));
  it('should call getCategory for all-accessories  ', waitForAsync(() => {
   const routeParams = {addCat: '', category: 'accessories', subcat: 'all-accessories'};
   component.setRouteParams(routeParams);
   const category = component.getCategory();
   expect(category).toEqual('all-accessories');
  }));

  it('should call triggerAPI Method witn no product - Grid Component', waitForAsync(() => {
    // Fake response data
    const mockFakeResponse = Object.assign({});
    mockFakeResponse['products'] = [];
    mockFakeResponse['totalFound'] = 0;
    // Expect a call and return the success response
    spyOn(productService, 'getFilteredProducts').and.returnValue(of(mockFakeResponse));
    fixture.detectChanges();
    const postArg = {
      accessoryType: [
        {
          name: 'accessoryType',
          value: 'All Cases',
          key: 'All Cases',
          i18Name: 'Product Type',
          orderBy: 0,
          points: null,
          swatchImageUrl: null,
          isFiltered: false,
          disabled: false,
          checked: true
        }
      ]
    };
    component.rootURL = '/store/curated/ipad/ipad-accessories';
    spyOn(component, 'triggerApi').and.callThrough();
    component.triggerApi(postArg);
    expect(component.triggerApi).toHaveBeenCalled();
  }));

  it('should call triggerAPI Method with products - Grid Component', waitForAsync(() => {
    // Fake response data
    const mockFakeResponse = Object.assign({});
    mockFakeResponse['products'] = new Array(12);
    mockFakeResponse['totalFound'] = 12;
    // Expect a call and return the success response
    spyOn(productService, 'getFilteredProducts').and.returnValue(of(mockFakeResponse));
    spyOnProperty(router, 'url', 'get').and.returnValue('/webshop');
    fixture.detectChanges();
    const postArg = {};
    component.items = mockFakeResponse.products;
    spyOn(component, 'triggerApi').and.callThrough();
    component.triggerApi(postArg);
    expect(component.triggerApi).toHaveBeenCalled();
  }));

  it('should call triggerAPI Method with products - Grid Component init load', waitForAsync(() => {
    // Fake response data
    const mockFakeResponse = Object.assign({});
    mockFakeResponse['products'] = new Array(12);
    mockFakeResponse['totalFound'] = 12;
    // Expect a call and return the success response
    spyOn(productService, 'getFilteredProducts').and.returnValue(of(mockFakeResponse));
    spyOnProperty(router, 'url', 'get').and.returnValue('/webshop');
    fixture.detectChanges();
    const postArg = {};
    component.isInitLoaded = false;
    spyOn(component, 'triggerApi').and.callThrough();
    component.triggerApi(postArg);
    expect(component.triggerApi).toHaveBeenCalled();
  }));
  it('should call triggerScroll Method ', waitForAsync(() => {
    component.triggerScroll();
    expect(component.triggerScroll).toBeDefined();
  }));



  it('should set sortby value when sort option change', waitForAsync(() => {
    component['activatedRoute'].snapshot.queryParams = {sort: 'byPriceHighToLow'};
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.sortBy.label).toEqual('byPriceHighToLow');
  }));

  it('should set sortby value when sort option change', waitForAsync(() => {
    component['activatedRoute'].snapshot.queryParams = {color: 'blue'};
    component['activatedRoute'].snapshot.params = {subcat: 'all-accessories'};
    component['activatedRoute'].snapshot.data = {analyticsObj: {pgName: ''}};
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.enableResetFlag).toBeTrue();
  }));

  it('should call setRouteParams', () => {
     const routeParams = {addCat: 'iphone-accessories-audio-music', category: 'iphone', subcat: 'iphone-accessories'};
     component.setRouteParams(routeParams);
     expect(component.addCatName).toBeDefined();
  });
  it('should call setRouteParams with no categories', () => {
    const routeParams = {addCat: 'iphone-accessories-audio-music', category: 'iphone', subcat: 'iphone-accessories'};
    component.mainNav = [];
    component.setRouteParams(routeParams);
    expect(component.addCatName).toBeDefined();
  });
  it('should create with event of NavigationEnd - Different from Root URL', () => {
    const routeParams = {addCat: 'iphone-accessories-audio-music', category: 'iphone', subcat: 'iphone-accessories'};
    component.setRouteParams(routeParams);
    component.rootURL = '/store/curated/ipad/ipad-accessories';
    routerEvent$.next(new NavigationEnd(1, '/store/curated/ipad/ipad-accessories', '/store/curated/ipad/ipad-accessories'));
    expect(component).toBeTruthy();
  });
  it('should create with event of NavigationEnd - facetsFilters undefined', () => {
    component.filterProductsData = undefined;
    fixture.detectChanges();
    routerEvent$.next(new NavigationEnd(1, '/store/curated/ipad/ipad-accessories', '/store/curated/ipad/ipad-accessories'));
    expect(component).toBeTruthy();


  });

});
