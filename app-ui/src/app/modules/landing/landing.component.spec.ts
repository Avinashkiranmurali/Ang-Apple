import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { BehaviorSubject, of, throwError } from 'rxjs';
import { ActivatedRoute, Router, RouterEvent } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { UserStoreService } from '@app/state/user-store.service';
import { BannerStoreService } from '@app/state/banner-store.service';
import { NavStoreService } from '@app/state/nav-store.service';
import { LandingComponent } from '@app/modules/landing/landing.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { FamilyBannerComponent } from './banners/family-banner/family-banner.component';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { FilterBannerPipe } from '@app/pipes/filter-banner.pipe';
import { InterpolatePipe } from '@app/pipes/interpolate.pipe';
import { Category } from '@app/models/category';
import { BannerService } from '@app/services/banner.service';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { DecimalPipe } from '@angular/common';

describe('LandingComponent - Store landing', () => {
  let component: LandingComponent;
  let fixture: ComponentFixture<LandingComponent>;
  let userStoreService: UserStoreService;
  let navStoreService: NavStoreService;
  let bannerStoreService: BannerStoreService;
  const routerEvent$ = new BehaviorSubject<RouterEvent>(null);
  let router: Router;
  const mainNavData = require('assets/mock/categories.json');
  const bannerData = require('assets/mock/banner.json');
  const programData = require('assets/mock/program.json');
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        LandingComponent,
        FamilyBannerComponent,
        OrderByPipe,
        FilterBannerPipe,
        InterpolatePipe
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        NgbActiveModal,
        BannerService,
        { provide: ActivatedRoute, useValue: {
            params: of({category: 'mac', subcat: 'macbook-pro', addcat: '', psid: ''}),
            data: of({})
          }
        },
        CurrencyPipe,
        CurrencyFormatPipe,
        DecimalPipe
      ]
    })
    .compileComponents();
    userStoreService = TestBed.inject(UserStoreService);
    navStoreService = TestBed.inject(NavStoreService);
    bannerStoreService = TestBed.inject(BannerStoreService);
    userStoreService.addUser(userData.user);
    userStoreService.addProgram(userData.program);
    userStoreService.addConfig(userData.config);
    mainNavData[0]['subCategories'][1]['detailUrl'] = 'test/Url';
    navStoreService.addMainNav(mainNavData);
    bannerStoreService.addBanner(bannerData);
    router = TestBed.inject(Router);
    (router as any).events = routerEvent$.asObservable();
    router.navigate = jasmine.createSpy('navigate');
    (router as any).snapshot = {};
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LandingComponent);
    component = fixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
    component.config['imageServerUrl'] = 'https://als-static.bridge2rewards.com/dev2';
    component.mainNav = mainNavData;
    component.bannerData = bannerData[0];
    component.bannerData['banners'] = component.buildBannerConfig();
    component.category = 'mac';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  /* it('should create with navigationEnd', () => {
    routerEvent$.next(new NavigationEnd(1, '/store/browse/mac/macbook-pro', '/store/browse/mac/macbook-pro'));
    expect(component).toBeTruthy();
  }); */

  it('ngOnInit should be called - success banner response', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    spyOnProperty(component['bannerStoreService'], 'banner').and.returnValue([]);
    fixture.detectChanges();
    spyOn((component as any).bannerService, 'getBanners').and.returnValue(of(bannerData));
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('ngOnInit should be called - failure banner response', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    spyOnProperty(component['bannerStoreService'], 'banner').and.returnValue([]);
    fixture.detectChanges();
    // Fake response data
    const errorResponse = {
      status: 404,
      statusText: 'Not Found'
    };
    spyOn((component as any).bannerService, 'getBanners').and.returnValue(throwError(errorResponse));
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should call displayProgramBanner method', () => {
    spyOn(component, 'displayProgramBanner').and.callThrough();
    component.displayProgramBanner('true', null);
    expect(component.displayProgramBanner).toHaveBeenCalled();
  });

  it('should call displayProductBanner method', () => {
    spyOn(component, 'displayProductBanner').and.callThrough();
    component.displayProductBanner();
    expect(component.displayProductBanner).toHaveBeenCalled();
  });

  it('should call getSubNav method', () => {
    component.mainNav = mainNavData;
    fixture.detectChanges();
    spyOn(component, 'getSubNav').and.callThrough();
    component.getSubNav();
    expect(component.getSubNav).toHaveBeenCalled();
  });

  it('should call getSubNav method for undefined category', () => {
    component.mainNav = mainNavData;
    component.category = undefined;
    fixture.detectChanges();
    spyOn(component, 'getSubNav').and.callThrough();
    component.getSubNav();
    expect(component.getSubNav).toHaveBeenCalled();
  });

  it('should call buildBannerLink method', () => {
    component.messages = {
      macbookProParams: '{\"category\": \"mac\",\"subcat\": \"macbook-pro\"}',
      macbookProState: '/store/browse',
      macbookProTitle: 'MacBook Pro'
    };
    spyOn(component, 'buildBannerLink').and.callThrough();
    const category: Category = Object.assign({});
    category['detailUrl'] = 'mac/macbook-pro';
    category['state'] = 'macbookProState';
    category['params'] = 'macbookProParams';
    component.buildBannerLink(category);
    expect(component.buildBannerLink).toHaveBeenCalled();
  });

  it('should call buildBannerLink method - else check', () => {
    component.messages = {
      macbookProParams: '{\"category\": \"mac\",\"subcat\": \"macbook-pro\"}',
      macbookProState: '/store/browse',
      macbookProTitle: 'MacBook Pro'
    };
    spyOn(component, 'buildBannerLink').and.callThrough();
    const category: Category = Object.assign({});
    category['detailUrl'] = 'mac/macbook-pro/macbook-pro';
    category['state'] = 'macbookProState';
    category['params'] = 'macbookProParams';
    component.buildBannerLink(category);
    expect(component.buildBannerLink).toHaveBeenCalled();
  });

  it('should call buildBannerLink method - when subcat doesnot exists', () => {
    component.messages = {
      macbookProParams: '{\"category\": \"mac\"}',
      macbookProState: '/store/browse',
      macbookProTitle: 'MacBook Pro'
    };
    spyOn(component, 'buildBannerLink').and.callThrough();
    const category: Category = Object.assign({});
    category['detailUrl'] = 'mac';
    category['state'] = 'macbookProState';
    category['params'] = 'macbookProParams';
    component.buildBannerLink(category);
    expect(component.buildBannerLink).toHaveBeenCalled();
  });

  it('should call buildBannerLink method - no category detailUrl', () => {
    component.messages = {
      macbookProParams: '{\"category\": \"mac\",\"subcat\": \"macbook-pro\"}',
      macbookProState: '/store/browse',
      macbookProTitle: 'MacBook Pro'
    };
    spyOn(component, 'buildBannerLink').and.callThrough();
    const category: Category = Object.assign({});
    category['detailUrl'] = null;
    category['state'] = 'macbookProState';
    category['params'] = 'macbookProParams';
    component.buildBannerLink(category);
    expect(component.buildBannerLink).toHaveBeenCalled();
  });

  it('should call buildMultiBannerData method', () => {
    spyOn(component, 'buildMultiBannerData').and.callThrough();
    const list = ['macbook-air', 'mac-mini', 'macbook-pro', 'iphone-iphone-12', 'iphone-iphone-12-mini'];
    component.buildMultiBannerData(list, bannerData[0].bannerTemplateObj,  '');
    expect(component.buildMultiBannerData).toHaveBeenCalled();
  });

  it('should call postBanners method', () => {
    component.bannerData = [];
    component.bannerData['banners'] = [];
    component.category = '';
    component.mainNav = [];
    spyOn(component, 'postBanners').and.callThrough();
    component.postBanners();
    expect(component.postBanners).toHaveBeenCalled();
  });

  it('should call postBanners method - with bannerData dispayOrder details', () => {
    component.bannerData['banners'][0]['subcat'][0]['displayOrderBy'] = '2';
    spyOn(component, 'postBanners').and.callThrough();
    component.postBanners();
    expect(component.postBanners).toHaveBeenCalled();
  });

  it('should call initMultiBanner method', () => {
    component.bannerData = [];
    spyOn(component, 'initMultiBanner').and.callThrough();
    component.initMultiBanner();
    expect(component.initMultiBanner).toHaveBeenCalled();
  });

  it('should call initMultiBanner method with multiBannerData', () => {
    component.messages['multiProductBannerList'] = 'newproducts,npiproducts,promo';
    component.messages['promo-multiProductBannerList'] = 'macbook-pro,iphone-iphone-12-mini,macbook-air,iphone-iphone-12,mac-mini';
    spyOn(component, 'initMultiBanner').and.callThrough();
    component.initMultiBanner();
    expect(component.initMultiBanner).toHaveBeenCalled();
  });

  it('should call initMultiBanner method with marketingBannerData', () => {
    component.messages['marketingBannerList'] = 'banner1,banner2';
    spyOn(component, 'initMultiBanner').and.callThrough();
    component.initMultiBanner();
    expect(component.initMultiBanner).toHaveBeenCalled();
  });

  it('should call buildMultiBannerActiveGroup method', () => {
    component.bannerData = [];
    spyOn(component, 'buildMultiBannerActiveGroup').and.callThrough();
    const group = [''];
    component.buildMultiBannerActiveGroup(group);
    expect(component.buildMultiBannerActiveGroup).toHaveBeenCalled();
  });

  it('should call getBanners method', () => {
    const storeLandingBannerMock = require('assets/mock/store-landing-banners.json');
    spyOn(component, 'getBanners').and.callThrough();
    spyOn(component['bannerService'], 'getBanners').and.returnValue(of(storeLandingBannerMock));
    component.getBanners();
    expect(component.getBanners).toHaveBeenCalled();
  });

  it('should define getBanners data', () => {
    const storeLandingBannerMock = require('assets/mock/store-landing-banners.json');
    component['bannerStoreService'].banners = storeLandingBannerMock;
    fixture.detectChanges();
    component.ngOnInit();
    expect(component).toBeTruthy();
  });

  it('should call getBanners method - fails', () => {
    spyOn(component, 'getBanners').and.callThrough();
    const errorResponse = {
      status: 404,
      statusText: 'Not Found'
    };
    spyOn((component as any).bannerService, 'getBanners').and.returnValue(throwError(errorResponse));
    component.getBanners();
    expect(component.getBanners).toHaveBeenCalled();
  });

});
