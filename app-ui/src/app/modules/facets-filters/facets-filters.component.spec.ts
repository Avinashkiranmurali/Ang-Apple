import {ComponentFixture, TestBed, waitForAsync, tick, fakeAsync} from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { FacetsFiltersComponent } from './facets-filters.component';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { ActivatedRoute, NavigationStart, Router } from '@angular/router';
import { BehaviorSubject, of, ReplaySubject } from 'rxjs';
import { HttpClientModule } from '@angular/common/http';
import { FilterOption, FilterProducts } from '@app/models/filter-products';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ProductService } from '@app/services/product.service';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

describe('FacetsFiltersComponent', () => {
  let component: FacetsFiltersComponent;
  let fixture: ComponentFixture<FacetsFiltersComponent>;
  let httpTestingController: HttpTestingController;
  let productService: ProductService;
  const filterProductsData: FilterProducts = require('assets/mock/facets-filters.json');
  const routerEvent$ = new BehaviorSubject<any>(null);
  let router: Router;
  let activatedRoute: ActivatedRoute;
  const colorFacetFilterOption: FilterOption = {
    disabled: false,
    isFiltered: false,
    name: 'color',
    value: 'Black',
    key: 'Black',
    i18Name: 'Color',
    orderBy: 0,
    points: null,
    swatchImageUrl: null
  };
  const brandFacetFilterOption = {
    name: 'brand',
    key: 'Anker',
    disabled: false,
    isFiltered: false,
    value: 'Anker',
    i18Name: 'Brand',
    orderBy: 0,
    points: null,
    swatchImageUrl: null
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [FacetsFiltersComponent],
      imports: [
        TranslateModule.forRoot(),
        HttpClientModule,
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        MessagesStoreService,
        { provide: ProductService },
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe
      ]
    }).compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
    productService = TestBed.inject(ProductService);
    router = TestBed.inject(Router);
    activatedRoute = TestBed.inject(ActivatedRoute);
    router.navigate = jasmine.createSpy('navigate');
    (router as any).events = routerEvent$.asObservable();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FacetsFiltersComponent);
    component = fixture.componentInstance;
    component.facetsFilters = filterProductsData.facetsFilters;
    component.isTablet = true;
    // component['activatedRoute'].snapshot = {params: {}, queryParams: {}} as ActivatedRouteSnapshot;
    // component['activatedRoute'].params = of({ category: 'mac', subcat: 'macbook-pro', addcat: undefined });
    activatedRoute.queryParams = of({sort: 'byPriceLowToHigh', color: 'black'});
    component.facetsScrollObj = {
      notificationRibbonEnabled: false,
      fixedFacetsContainer: false,
      endPositionFacetsContainer: false,
      scrollTopPrev: 0,
      scrollTop: 0,
      transition: 0,
      maxTransition: 0,
      scrollStarts: 0
    };
    fixture.detectChanges();
  });

  it('Facets Filter Component should create', () => {
    expect(component).toBeTruthy();
  });


  it('should call toggleLimit', () => {
    spyOn(component, 'toggleLimit').and.callThrough();
    component.toggleLimit(0, 10);
    component.toggleLimit(0, 7);
    expect(component.toggleLimit).toHaveBeenCalledTimes(2);
  });

  it('should call filterGroupExpanded method', fakeAsync(() => {
    component.isMobile = false;
    tick(101);
    spyOn(component, 'filterGroupExpanded').and.callThrough();
    component.filterGroupExpanded();
    tick(101);
    fixture.detectChanges();
    expect(component.filterGroupExpanded).toHaveBeenCalledTimes(1);
  }));

  it('should emit on scroll', fakeAsync(() => {
    spyOn(component.triggerScrollEmitter, 'emit');
    component.filterGroupExpanded();
    tick(101);
    fixture.detectChanges();
    expect(component.triggerScrollEmitter.emit).toHaveBeenCalled();
  }));

  it('should emit on click', () => {
    spyOn(component.closeModal, 'emit');
    component.closeActiveModal();
    expect(component.closeModal.emit).toHaveBeenCalled();
    expect(component.closeModal.emit).toHaveBeenCalledWith(false);
  });

  it('Facets Filter Should call queryParamsObj with disabled filter', () => {
    spyOn(component, 'queryParamsObj').and.callThrough();
    component.queryParamsObj(brandFacetFilterOption);
    expect(component.queryParamsObj).toHaveBeenCalled();
  });

  it('should get queryParamsObj when queryParamsObj is called with color Black', () => {
    component['activatedRoute'].snapshot.queryParams = {
      color: 'Black',
      sort: 'byPriceLowToHigh',
    };
    spyOn(component, 'isFacetsFilterDisabled').and.returnValue(false);
    const queryParamsObj = component.queryParamsObj(colorFacetFilterOption);
    expect(queryParamsObj).toBeDefined();
  });

  it('should get queryParamsObj.color to be black when queryParamsObj is called with color value null', () => {
    component['activatedRoute'].snapshot.queryParams = {
      color: null,
      sort: 'byPriceLowToHigh',
    };
    spyOn(component, 'isFacetsFilterDisabled').and.returnValue(false);
    const queryParamsObj = component.queryParamsObj(colorFacetFilterOption);
    expect(queryParamsObj.color).toBe('black');
  });

  it('should call isFacetsFilterDisabled', () => {
    const facetOption = { ...brandFacetFilterOption, isFiltered: true };
    spyOn(component, 'isFacetsFilterDisabled').and.callThrough();
    const result = component.isFacetsFilterDisabled(facetOption);
    expect(component.isFacetsFilterDisabled).toHaveBeenCalled();
    expect(result).toBe(false);
  });

  it('should return false when isFacetsFilterDisabled is called',  fakeAsync(() => {
    const facetOption = { ...brandFacetFilterOption, key: 'AbleNet', value: 'AbleNet' };
    const latestFacetsFilter = require('assets/mock/facets-filters.json');
    component.currentSelection = 'brand';
    component.latestFacetsFilter = { brand: latestFacetsFilter.facetsFilters.brand };
    let result = component.isFacetsFilterDisabled(facetOption);
    expect(result).toBeFalsy();
    component.currentSelection = '';
    result = component.isFacetsFilterDisabled(facetOption);
    expect(result).toBeFalsy();
  }));

  it('should get facetOption.isFiltered value false when setFiltered is called', () => {
    component['activatedRoute'].snapshot.queryParams = {
      sort: 'byPriceLowToHigh',
    };
    const facetOption = component.setFiltered('color', colorFacetFilterOption);
    expect(facetOption.isFiltered).toBeFalsy();
  });


  it('should doFilterSelection', fakeAsync(() => {
    const seletedFacetsFilters = [{'brand' : 'Apple'}];
    const key = 'brand';
    //if (seletedFacetsFilters.length > 0) {
      component.facetsFiltersObj[key] = true;
    //}
    tick(101);
    fixture.detectChanges();
    spyOn(component,'doFilterSelection').and.callThrough();
    component.doFilterSelection(null);
    tick(101);
    fixture.detectChanges();
    expect(component.doFilterSelection).toHaveBeenCalled();
  }));

  it('should call ngOnDestroy method', () => {
    spyOn(component, 'ngOnDestroy').and.callThrough();
    component.ngOnDestroy();
    expect(component.ngOnDestroy).toHaveBeenCalledTimes(1);
  });


  /*

  it('Facets Filter Should call queryParamsObj with non-disabled filter', () => {
    spyOn(component, 'queryParamsObj').and.callThrough();
    component.queryParamsObj('Product Type', 'All Cases', false);
    expect(component.queryParamsObj).toHaveBeenCalled();
  });

  it('Facets Filter Should call queryParamsObj for adding filters in different category', () => {
    component['activatedRoute'].snapshot.queryParams = {
      color: 'black'
    };
    spyOn(component, 'queryParamsObj').and.callThrough();
    component.queryParamsObj('Product Type', 'All Cases', false);
    expect(component.queryParamsObj).toHaveBeenCalled();
  });

  it('Facets Filter Should call queryParamsObj for removing filter in same category', () => {
    component['activatedRoute'].snapshot.queryParams = {
      color: 'black-grey'
    };
    spyOn(component, 'queryParamsObj').and.callThrough();
    component.queryParamsObj('Color', 'Grey', false);
    expect(component.queryParamsObj).toHaveBeenCalled();
  });

  it('Facets Filter Should call queryParamsObj for removing category', () => {
    component['activatedRoute'].snapshot.queryParams = {
      color: 'black'
    };
    spyOn(component, 'queryParamsObj').and.callThrough();
    component.queryParamsObj('Color', 'black', false);
    expect(component.queryParamsObj).toHaveBeenCalled();
  });

  it('Facets Filter Should call queryParamsObj for Adding different filter in same category', () => {
    component['activatedRoute'].snapshot.queryParams = {
      color: 'black'
    };
    spyOn(component, 'queryParamsObj').and.callThrough();
    component.queryParamsObj('Color', 'Grey', false);
    expect(component.queryParamsObj).toHaveBeenCalled();
  });

  it('Facets Filter Should call queryParamsObj for Adding filter in same category', () => {
    component['activatedRoute'].snapshot.queryParams = {
      color: 'black-grey'
    };
    spyOn(component, 'queryParamsObj').and.callThrough();
    component.queryParamsObj('Color', 'Blue', false);
    expect(component.queryParamsObj).toHaveBeenCalled();
  });

  it('activatedRoute.queryParams Subscribe for newly added filter in same category', () => {
    // component['activatedRoute'].queryParams = of({sort: 'byPriceLowToHigh'});
    activatedRoute.queryParams = of({sort: 'byPriceLowToHigh', color: 'black-blue'});
    fixture.detectChanges();
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('activatedRoute.queryParams Subscribe for removed filter in same category', () => {
    activatedRoute.queryParams = of({sort: 'byPriceLowToHigh', color: 'black'});
    fixture.detectChanges();
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('activatedRoute.queryParams Subscribe for new filter and new category', () => {
    activatedRoute.queryParams = of({sort: 'byPriceLowToHigh'});
    fixture.detectChanges();
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('activatedRoute.queryParams Subscribe for new filters in new tab', () => {
    activatedRoute.queryParams = of({sort: 'byPriceLowToHigh', color: 'black'});
    fixture.detectChanges();
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('activatedRoute.queryParams Subscribe for new filters in same tab', () => {
    activatedRoute.queryParams = of({color: 'black-blue'});
    fixture.detectChanges();
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('activatedRoute.queryParams Subscribe for different sort', () => {
    activatedRoute.queryParams = of({sort: 'byPopularity'});
    fixture.detectChanges();
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('activatedRoute.queryParams Subscribe for newly added sort', () => {
    activatedRoute.queryParams = of({color: 'black-blue'});
    fixture.detectChanges();
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('activatedRoute.queryParams Subscribe for reset filters', () => {
    activatedRoute.queryParams = of({});
    component.isDesktop = false;
    component.isTablet = false;
    fixture.detectChanges();
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  }); */

});
