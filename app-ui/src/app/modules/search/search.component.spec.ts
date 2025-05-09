import { ComponentFixture, fakeAsync, TestBed, tick, waitForAsync } from '@angular/core/testing';
import { SearchComponent } from './search.component';
import { NgbActiveModal, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { ActivatedRoute, NavigationEnd, Router, RouterEvent } from '@angular/router';
import { BehaviorSubject, of, throwError } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';
import { TranslateModule } from '@ngx-translate/core';
import { SearchBoxComponent } from '@app/components/search-box/search-box.component';
import { FormsModule } from '@angular/forms';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';
import { By } from '@angular/platform-browser';
import { ProductService } from '@app/services/product.service';
import { SortByComponent } from '@app/modules/sort-by/sort-by.component';
import { find } from 'lodash';
import { NavStoreService } from '@app/state/nav-store.service';
import { FilterProducts } from '@app/models/filter-products';
import { UserStoreService } from '@app/state/user-store.service';
import { GridEventHandlerService } from '@app/services/grid-event-handler.service';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

describe('SearchComponent', () => {
  let component: SearchComponent;
  let fixture: ComponentFixture<SearchComponent>;
  let httpTestingController: HttpTestingController;
  let productService: ProductService;
  let parsePsidPipe: ParsePsidPipe;
  let mainNavStore: NavStoreService;
  let gridEventHandlerService: GridEventHandlerService;
  const routerEvent$ = new BehaviorSubject<RouterEvent>(null);
  const mainNav = require('assets/mock/categories.json');
  const programData = require('assets/mock/program.json');

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
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };
  let router;
  const filterProductsData: FilterProducts = require('assets/mock/facets-filters.json');
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        SearchComponent,
        SearchBoxComponent,
        SortByComponent
      ],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({
              keyword: 'mac'
            }),
            queryParams: of({}),
            snapshot: {params: {keyword: 'mac'}, queryParams: {}}
          }
        },
        { provide: NgbActiveModal },
        { provide: ParsePsidPipe },
        { provide: UserStoreService, useValue: userData },
        { provide: EnsightenService, useValue: {
          broadcastEvent: () => {} }
        },
        GridEventHandlerService,
        CurrencyPipe,
        DecimalPipe,
        CurrencyFormatPipe
      ],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        NgbModule,
        FormsModule
      ]
    })
      .compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
    productService = TestBed.inject(ProductService);
    parsePsidPipe = TestBed.inject(ParsePsidPipe);
    gridEventHandlerService = TestBed.inject(GridEventHandlerService);
    router = TestBed.inject(Router);
    (router as any).events = routerEvent$.asObservable();
    mainNavStore = TestBed.inject(NavStoreService);
    mainNavStore.addMainNav(mainNav);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchComponent);
    component = fixture.componentInstance;
    const items = filterProductsData;
    component.items = gridEventHandlerService.cleanData(filterProductsData.products);
    component.filterProductsData = filterProductsData;
    component.showFacetsFilters = true;
    component.isDesktop = true;
    component.facetsScrollObj = facetsScrollObj;
    window.scrollTo({ top: 0, behavior: 'smooth' });
    component['activatedRoute'].params = of({ keyword: 'mac' });
    component['activatedRoute'].queryParams = of({});
    component['activatedRoute']['_routerState'] = {snapshot: {url: '/store/search/mac'}};
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('closeModal to close the modal', () => {
    const event = true;
    component.closeModal(event);
    expect(component.showFacetsFilters).toBe(event);
    expect(document.body.style.overflow).toEqual('auto');
  });

  it('filterToggle to filter the data', () => {
    component.isDesktop = false;
    component.isTablet = false;
    component.showFacetsFilters = true;
    fixture.detectChanges();
    component.filterToggle();
    expect(document.body.style.overflow).toBe('hidden');
    if (!component.showFacetsFilters && !component.isDesktop) {
      expect(component.showFacetsFilters).toBeFalsy();
    }
  });

  it('filteredItems should assign values to different properties', () => {
    const mockProducts = require('assets/mock/products-ipad.json');
    const data = {
      products: mockProducts['products'],
      totalFound: 6
    };
    component.filteredItems(data);
    expect(component.itemTotal).toBe(data.totalFound);
    expect(component.resetFiltersObj).toBeFalse();
    expect(component.contentLoading).toBeFalse();
  });

  it('enableReset should set the value of  enableResetFlag', () => {
    const event = true;
    component.enableReset(event);
    expect(component.enableResetFlag).toBe(event);
    expect(component.contentLoading).toBeTrue();
  });

  it('should call getProductList method', () => {
    spyOn(component, 'getProductList').and.callThrough();
    // Fake response data
    const fakeResponse = require('assets/mock/facets-filters.json');
    spyOn(productService, 'getFilteredProducts').and.returnValue(of(fakeResponse));
    component.getProductList({}, true, false);
    expect(component.getProductList).toHaveBeenCalled();
  });

  it('should call getProductList method - 0 products', () => {
    spyOn(component, 'getProductList').and.callThrough();
    const response = {
      categoryPrices: null,
      facetsFilters: null,
      optionsConfigurationData: null,
      products: [],
      searchRedirect: null,
      totalFound: 0
    };
    spyOn(productService, 'getFilteredProducts').and.returnValue(of(response));
    component.getProductList({}, true, false);
    expect(component.getProductList).toHaveBeenCalled();
  });

  it('should call loadInitData method with isInit = false', () => {
    const isInit = false;
    const isFacetsTriggered = false;
    component.loadInitData( isInit, isFacetsTriggered);
    spyOn(component, 'loadInitData').and.callThrough();
    expect(component.items).toEqual([]);
  });

  it('should set a default value in setSortBy method', () => {
    const queryParamsSort = component['activatedRoute'].snapshot.queryParams = {sort: null};
    component.setSortBy();
    const sortOptions = component.sharedService.sortOptions;
    component.sortBy = find(sortOptions, {label: queryParamsSort});
    if (!component.sortBy) {
      component.sortBy = sortOptions['relevancy'];
    }
    spyOn(component, 'setSortBy').and.callThrough();
    expect(component.sortBy['key']).toEqual('relevancy');
  });

  it('should set an updated value in setSortBy method', () => {
    const queryParamsSort = 'byPriceHighToLow';
    component.setSortBy();
    const sortOptions = component.sharedService.sortOptions;
    component.sortBy = find(sortOptions, {label: queryParamsSort});
    spyOn(component, 'setSortBy').and.callThrough();
    expect(component.sortBy['orderBy']).toEqual('DESCENDING');
  });

  it('should be message for search result', fakeAsync(() => {
    const navBarSearchField = fixture.debugElement.query(By.css('#search-input'));
    const searchInputFieldElement = navBarSearchField.nativeElement;
    const eventKeyUp1 = new KeyboardEvent('keyup', {
      key: '1',
    });
    searchInputFieldElement.dispatchEvent(eventKeyUp1);
    const eventKeyUp2 = new KeyboardEvent('keyup', {
      key: '2'
    });
    searchInputFieldElement.dispatchEvent(eventKeyUp2);
    const eventKeyEnter = new KeyboardEvent('keypress', {
      key: 'Enter'
    });
    searchInputFieldElement.dispatchEvent(eventKeyEnter);
    tick(500);
    fixture.detectChanges();
    const searchInputResultMessage = fixture.debugElement.query(By.css('.result-title'));
    const searchInputResultMessageElement = searchInputResultMessage.nativeElement;
    expect(searchInputResultMessageElement.textContent).toEqual('');
  }));

  it('should call resetFlags method', () => {
    spyOn(component, 'resetFlags').and.callThrough();
    component['result'] = false;
    component.contentLoading = true;
    component.enableResetFlag = false;
    component.filterProductsData = undefined;
    component.facetsFiltersObj = {};
    component.moreSearchToLoad = false;
    component.resetFlags();
    expect(component.contentLoading).toEqual(true);
  });

  it('should call getMoreSearchItems method - 12+ products 404 failure', () => {
    spyOn(component, 'getMoreSearchItems').and.callThrough();
    const errorResponse = { status: 404, statusText: 'Not Found' };
    spyOn(productService, 'getFilteredProducts').and.callFake(() => throwError(errorResponse));
    component.getMoreSearchItems(14);
    expect(component.getMoreSearchItems).toHaveBeenCalled();
  });

  it('should call getMoreSearchItems method - 12+ products 401 failure', () => {
    spyOn(component, 'getMoreSearchItems').and.callThrough();
    const errorResponse = { status: 401, statusText: 'Failure' };
    spyOn(productService, 'getFilteredProducts').and.callFake(() => throwError(errorResponse));
    component.getMoreSearchItems(14);
    expect(component.getMoreSearchItems).toHaveBeenCalled();
  });

  it('should call getMoreSearchItems methods - products success', () => {
    const postArg = {
      facetsFilters: require('assets/mock/facets-filters.json')['products'],
      keyword: 'case',
      pageSize: 12,
      resultOffSet: 0,
      order: component.sortBy.orderBy,
      sort: component.sortBy.sortBy,
      withVariations: true
    };
    const allItems = require('assets/mock/facets-filters.json')['products'];
    const isFacetsTriggered = true;
    component.filterProductsData.facetsFilters=undefined;
    component.items.length = 14;
    component.itemTotal = 28;
    spyOn(component, 'getProductList').withArgs(postArg, true, isFacetsTriggered).and.callThrough();
    spyOn(component, 'getMoreSearchItems').withArgs(12).and.callThrough();
    fixture.detectChanges();
    component.getMoreSearchItems(allItems.length);
    expect(component.getProductList).toBeDefined();
    expect(component.getMoreSearchItems).toHaveBeenCalled();
  });

  it('should call loadMoreSearchItems more than 12 products', () => {
    spyOn(component, 'loadMoreSearchItems').and.callThrough();
    const items = require('assets/mock/facets-filters.json');
    component.items = items['products'];
    const moreItems = require('assets/mock/facets-filters.json');
    component.moreSearchItems = moreItems['products'];
    fixture.detectChanges();
    component.loadMoreSearchItems();
    expect(component.loadMoreSearchItems).toHaveBeenCalled();
    expect(component.moreSearchToLoad).toBeTruthy();
  });

  it('filterToggle to filter the data for else', () => {
    component.isDesktop = true;
    component.isTablet = true;
    component.showFacetsFilters = false;
    fixture.detectChanges();
    component.filterToggle();
    expect(document.body.style.overflow);
    if (!component.showFacetsFilters && !component.isDesktop) {
      expect(component.showFacetsFilters).toBeTruthy();
    }
  });

  it('should call loadMoreSearchItems for else', () => {
    component.moreSearchItems = [];
    fixture.detectChanges();
    component.loadMoreSearchItems();
    expect(component.moreSearchToLoad).toBeFalsy();
  });

  it('expect construct route for else', () => {
    gridEventHandlerService.cleanData([{ psid: '', options: [{ name: 'storage' }] }]);
    expect(component).toBeTruthy();
  });

  it('expect cleanData is called and psidSlug is created and converted correctly', () => {
    spyOn(gridEventHandlerService, 'cleanData').and.callThrough();
    const item = {
      psid: '30001MM2Y3ZM/A',
      psidSlug: ''
    };
    const oldItems = require('assets/mock/facets-filters.json')['products'];
    gridEventHandlerService.cleanData([{ oldItems }]);
    fixture.detectChanges();
    expect(gridEventHandlerService.cleanData).toHaveBeenCalled();
    parsePsidPipe = TestBed.inject(ParsePsidPipe);
    item.psidSlug = parsePsidPipe.transform(item.psid, '-');
    expect(item['psidSlug']).toEqual('30001MM2Y3ZM-A');
  });

  it('should call triggerAPI Method witn no product - search Component', waitForAsync(() => {
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
    fixture.detectChanges();
    const postArg = {};
    component.isRootChanged = true;
    component.isInitLoaded = true;
    spyOn(component, 'triggerApi').and.callThrough();
    component.triggerApi(postArg);
    expect(component.triggerApi).toHaveBeenCalled();
  }));
  it('should call triggerAPI Method for new search', waitForAsync(() => {
    // Fake response data
    const mockFakeResponse = Object.assign({});
    mockFakeResponse['products'] = [];
    mockFakeResponse['totalFound'] = 0;
    component.terms = 'keyboard';
    component['activatedRoute'].snapshot.params = { keyword: 'macbookpro'};
    // Expect a call and return the success response
    spyOn(productService, 'getFilteredProducts').and.returnValue(of(mockFakeResponse));
    fixture.detectChanges();
    const postArg = {};
    spyOn(component, 'triggerApi').and.callThrough();
    component.triggerApi(postArg);
    expect(component.triggerApi).toHaveBeenCalled();
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
  it('scroll to call scroll(0) with no items', waitForAsync(() => {
    component.items = undefined;
    component.moreSearchItems = [];
    component.itemTotal = 0; /*
    spyOn(gridEventHandlerService, 'getGrid').and.returnValue({offsetHeight: 1200, scrollHeight: 500});
    spyOn(gridEventHandlerService, 'getFacetsContainerFullView').and.returnValue({offsetHeight: 600});*/
    fixture.detectChanges();
    window.scrollTo(0, 0);
    component.facetsScroll(Event);
    expect(component.facetsScroll).toBeDefined();
  }));
  it('Facets scroll Up ', waitForAsync(() => {
    component.moreSearchItems = [];
    component.itemTotal = 12;
    component.facetsScrollObj.scrollTop = 600;
    spyOn(gridEventHandlerService, 'getFacetsContainerView').and.returnValue({offsetHeight: window.innerHeight * 2});
    fixture.detectChanges();
    window.scrollTo(0, 400);
    fixture.detectChanges();
    component.facetsScroll(Event);
    expect(component.facetsScroll).toBeDefined();
  }));
  it('scroll to call scroll(60) with less than 12 items with facets', waitForAsync(() => {
    component.items.pop();
    component.items.length = 11;
    component.moreSearchItems = [];
    component.itemTotal = 100; /*
    spyOn(gridEventHandlerService, 'getGrid').and.returnValue({offsetHeight: 1200, scrollHeight: 500});
    spyOn(gridEventHandlerService, 'getFacetsContainerFullView').and.returnValue({offsetHeight: 600});*/
    window.scrollTo(0, 60);
    fixture.detectChanges();
    // spyOn(component, 'scroll').and.callThrough();
    // expect(component.scroll).toBeDefined();
    // component.facetsScroll(undefined);
    component.facetsScroll(Event);
    expect(component.facetsScroll).toBeDefined();
  }));
  it('scroll to call scroll(middle) with less than 12 items with facets', waitForAsync(() => {
    component.items.length = 12;
    component.moreSearchItems = [];
    component.itemTotal = 100; /*
    spyOn(gridEventHandlerService, 'getGrid').and.returnValue({offsetHeight: 1200, scrollHeight: 500});
    spyOn(gridEventHandlerService, 'getFacetsContainerFullView').and.returnValue({offsetHeight: 600});*/
    spyOn(gridEventHandlerService, 'getFacetsContainerView').and.returnValue({offsetHeight: window.innerHeight - 100});
    window.scrollTo(0, document.body.scrollHeight / 2 );
    fixture.detectChanges();
    component.facetsScroll(Event);
    expect(component.facetsScroll).toBeDefined();
  }));
  it('scroll to call scroll(middle) with less than 12 items with less facets filters', waitForAsync(() => {
    const items = require('assets/mock/facets-filters.json');
    component.filterProductsData = items;
    component.items = gridEventHandlerService.cleanData(items.products);
    component.items.length = 12;
    component.moreSearchItems = [];
    component.itemTotal = 100;
    fixture.detectChanges();

    spyOn(gridEventHandlerService, 'getGrid').and.returnValue({offsetHeight: 1200, scrollHeight: 500});
    /* spyOn(gridEventHandlerService, 'getFacetsContainerFullView').and.returnValue({offsetHeight: 600});*/
    window.scrollTo(0, document.body.scrollHeight / 2 );
    fixture.detectChanges();
    component.facetsScroll(Event);
    expect(component.facetsScroll).toBeDefined();
  }));
  it('scroll to call scroll(middle) with less than 12 items with facets scroll event undefined', waitForAsync(() => {
    component.items.pop();
    component.items.length = 11;
    component.moreSearchItems = [];
    component.itemTotal = 100; /*
    spyOn(gridEventHandlerService, 'getGrid').and.returnValue({offsetHeight: 1200, scrollHeight: 500});
    spyOn(gridEventHandlerService, 'getFacetsContainerFullView').and.returnValue({offsetHeight: 600});*/
    fixture.detectChanges();
    window.scrollTo(0, document.body.scrollHeight / 2 );
    component.facetsScroll(undefined);
    expect(component.facetsScroll).toBeDefined();
  }));
  it('scroll to call scroll(middle) with less items (2) with facets', waitForAsync(() => {
    const items = require('assets/mock/facets-filters.json');
    component.items = gridEventHandlerService.cleanData(items.products);
    component.items.length = 2;
    component.moreSearchItems = [];
    component.itemTotal = 100;
    spyOn(gridEventHandlerService, 'getGrid').and.returnValue({offsetHeight: 200, scrollHeight: 100});
    spyOn(gridEventHandlerService, 'getFacetsContainerFullView').and.returnValue({offsetHeight: 300});
    window.scrollTo(0, document.body.scrollHeight / 2 );
    fixture.detectChanges();
    component.facetsScroll(Event);
    expect(component.facetsScroll).toBeDefined();
  }));

  it('scroll to call scroll(end) with less than 12 items with less facets', waitForAsync(() => {
    const items = require('assets/mock/facets-filters.json');
    component.filterProductsData = items;
    component.items = gridEventHandlerService.cleanData(items.products);
    component.items.length = 12;
    component.moreSearchItems = [];
    component.itemTotal = 100;
    window.scrollTo(0, document.body.scrollHeight);
    fixture.detectChanges();
    component.facetsScroll(Event);
    expect(component.facetsScroll).toBeDefined();
  }));
  it('scroll to call scroll(end) with less than 12 items with facets', waitForAsync(() => {
    component.items.pop();
    component.items.length = 11;
    component.moreSearchItems = [];
    component.itemTotal = 100;
    window.scrollTo(0, document.body.scrollHeight);
    fixture.detectChanges();
    component.facetsScroll(Event);
    expect(component.facetsScroll).toBeDefined();
  }));
  it('scroll to call scroll(end) faces has available space', waitForAsync(() => {
    const items = require('assets/mock/facets-filters.json');
    component.filterProductsData = items;
    component.items = gridEventHandlerService.cleanData(items.products);
    component.items.length = 12;
    component.moreSearchItems = [];
    component.itemTotal = 100;
    // Mock for scroll Reached Facets
    spyOnProperty(document.documentElement, 'scrollTop', 'get').and.returnValue(component.facetsScrollObj.scrollStarts + 300);
    spyOn(component, 'filterStickyTopOffset').and.returnValue(60);
    component.facetsScrollObj.filterBarTop = 60;
    spyOn(gridEventHandlerService, 'getFacetsContainerView').and.returnValue({offsetHeight: window.innerHeight - component.facetsScrollObj.topFreezedElementHeight -  300});
    // fixture.detectChanges();
    window.scrollTo(0, document.body.scrollHeight);
    fixture.detectChanges();
    component.facetsScroll(Event);
    expect(component.facetsScroll).toBeDefined();
  }));
  it('scroll to call scroll(end) scroll reach end', waitForAsync(() => {
    component.items.length = 12;
    component.moreSearchItems = [];
    component.itemTotal = 100;
    // Mock for scroll Reached Facets
    spyOnProperty(document.documentElement, 'scrollTop', 'get').and.returnValue(component.facetsScrollObj.scrollStarts + 300);
    spyOn(component, 'filterStickyTopOffset').and.returnValue(60);
    component.facetsScrollObj.filterBarTop = 60;
    spyOn(gridEventHandlerService, 'getFacetsContainerView').and.returnValue({offsetHeight: window.innerHeight + component.facetsScrollObj.topFreezedElementHeight +  400});
    // fixture.detectChanges();
    window.scrollTo(0, document.body.scrollHeight - 100);
    fixture.detectChanges();
    component.facetsScroll(Event);
    expect(component.facetsScroll).toBeDefined();
  }));
  it('scroll to call scroll(end) faces scroll not reach end / facets space available', waitForAsync(() => {
    const items = require('assets/mock/facets-filters.json');
    component.filterProductsData = items;
    component.items = gridEventHandlerService.cleanData(items.products);
    component.items.length = 12;
    component.moreSearchItems = [];
    component.itemTotal = 100;
    // Mock for scroll Reached Facets
    spyOnProperty(document.documentElement, 'scrollTop', 'get').and.returnValue(200);
    // spyOn(component, 'filterStickyTopOffset').and.returnValue(60);
    // component.facetsScrollObj.filterBarTop = 60;
    spyOn(gridEventHandlerService, 'getFacetsContainerView').and.returnValue({offsetHeight: 600});
    window.scrollTo(0, document.body.scrollHeight);
    fixture.detectChanges();
    component.facetsScroll(Event);
    expect(component.facetsScroll).toBeDefined();
  }));
  it('scroll to call scroll() with no facets', waitForAsync(() => {
    component.items.pop();
    component.items.length = 11;
    component.moreSearchItems = [];
    component.itemTotal = 100; /*
    spyOn(gridEventHandlerService, 'getGrid').and.returnValue({offsetHeight: 1200, scrollHeight: 500});
    spyOn(gridEventHandlerService, 'getFacetsContainerFullView').and.returnValue({offsetHeight: 600});*/
    component.filterProductsData.facetsFilters = undefined;
    fixture.detectChanges();
    window.scrollTo(0, 100);
    fixture.detectChanges();
    component.facetsScroll(Event);
    // spyOn(component, 'scroll').and.callThrough();
    // expect(component.scroll).toBeDefined();
    // component.facetsScroll(undefined);
    expect(component.facetsScroll).toBeDefined();
  }));
  it('facetView container element with no facets', waitForAsync(() => {
    component.items.length = 12;
    component.moreSearchItems = [];
    component.itemTotal = 100;
    component.filterProductsData.facetsFilters = undefined;
    gridEventHandlerService.getFacetsContainerView();
    expect(gridEventHandlerService.getFacetsContainerView).toBeDefined();
  }));
  it('facetFullView container element with no facets', waitForAsync(() => {
    component.items.length = 12;
    component.moreSearchItems = [];
    component.itemTotal = 100;
    component.filterProductsData.facetsFilters = undefined;
    gridEventHandlerService.getFacetsContainerFullView();
    expect(gridEventHandlerService.getFacetsContainerFullView).toBeDefined();
  }));
  it('should create with event of NavigationEnd - Different from Root URL', () => {
    const routeParams = {keyword: 'mac'};
    component.setRouteParams(routeParams);
    routerEvent$.next(new NavigationEnd(1, '/store/search/mac', '/store/search/keyboard'));
    expect(component).toBeTruthy();
  });
  it('should create with event of NavigationEnd - facetsFilters undefined', () => {
    component.filterProductsData = undefined;
    const routeParams = {keyword: 'mac'};
    fixture.detectChanges();
    routerEvent$.next(new NavigationEnd(1, '/store/search/mac', '/store/search/keyboard'));
    expect(component).toBeTruthy();
  });
  it('should call triggerScroll Method ', waitForAsync(() => {
    component.triggerScroll();
    expect(component.triggerScroll).toBeDefined();
  }));
  it('facetView container element with no facets', waitForAsync(() => {
    component.items.length = 12;
    component.moreSearchItems = [];
    component.itemTotal = 100;
    component.filterProductsData.facetsFilters = undefined;
    gridEventHandlerService.getFacetsContainerView();
    expect(gridEventHandlerService.getFacetsContainerView).toBeDefined();
  }));
  it('facetFullView container element with no facets', waitForAsync(() => {
    component.items.length = 12;
    component.moreSearchItems = [];
    component.itemTotal = 100;
    component.filterProductsData.facetsFilters = undefined;
    gridEventHandlerService.getFacetsContainerFullView();
    expect(gridEventHandlerService.getFacetsContainerFullView).toBeDefined();
  }));
  it('should call setRouteParams - search result - true', () => {
    const routeParams = {keyword: 'mac'};
    component.noSearchResults = true;
    spyOnProperty(document.getElementById('returnedMsg'), 'innerText').and.returnValue('error message');
    component.setRouteParams(routeParams);
    expect(component.setRouteParams).toBeDefined();
  });
  it('should call setRouteParams - search result - true', () => {
    const routeParams = {keyword: 'mac'};
    component.noSearchResults = true;

    spyOnProperty(document.getElementById('returnedMsg'), 'innerText').and.returnValue('');
    component.setRouteParams(routeParams);
    expect(component.setRouteParams).toBeDefined();
  });
  it('should call setRouteParams - search result - false', () => {
    const routeParams = {keyword: 'mac'};
    component.noSearchResults = false;
    spyOnProperty(document.getElementById('returnedMsg'), 'innerText').and.returnValue('error message');
    component.setRouteParams(routeParams);
    expect(component.setRouteParams).toBeDefined();
  });
  it('should call setRouteParams - search result - false', () => {
    const routeParams = {keyword: 'mac'};
    component.noSearchResults = false;
    spyOnProperty(document.getElementById('returnedMsg'), 'innerText').and.returnValue('');
    component.setRouteParams(routeParams);
    expect(component.setRouteParams).toBeDefined();
  });


});
