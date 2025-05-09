import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from '@angular/core/testing';
import { StoreComponent } from './store.component';
import { CategoryService } from '@app/services/category.service';
import { TemplateService } from '@app/services/template.service';
import { UserStoreService } from '@app/state/user-store.service';
import { NavStoreService } from '@app/state/nav-store.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BehaviorSubject, of } from 'rxjs';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { TemplateStoreService } from '@app/state/template-store.service';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { NgbActiveModal, NgbPopoverModule } from '@ng-bootstrap/ng-bootstrap';
import { ActivatedRoute, ActivatedRouteSnapshot, ActivationEnd, NavigationEnd, Router } from '@angular/router';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';
import { HeaderComponent } from '@app/modules/header/header.component';
import { FooterComponent } from '@app/modules/footer/footer.component';
import { NavComponent } from '@app/components/nav/nav.component';
import { HeaderDirective } from '@app/modules/header/header.directive';
import { FooterDirective } from '@app/modules/footer/footer.directive';
import { UAFooterComponent } from '@app/components/vars/ua/footer/ua-footer.component';
import { SubNavComponent } from '@app/components/sub-nav/sub-nav.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {CommonModule, DecimalPipe, formatNumber} from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { InterpolatePipe } from '@app/pipes/interpolate.pipe';
import { NotificationBannerComponent } from '@app/components/notification-banner/notification-banner.component';
import { LogoComponent } from '@app/components/vars/default/header/logo/logo.component';
import { WelcomeMsgComponent } from '@app/components/vars/default/header/welcome-msg/welcome-msg.component';
import { By } from '@angular/platform-browser';
import { FooterDisclaimerService } from '@app/modules/footer/footer-disclaimer.service';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { Idle } from '@ng-idle/core';
import { Keepalive } from '@ng-idle/keepalive';
import { IdleService } from '@app/services/idle.service';
import { NgIdleKeepaliveModule } from '@ng-idle/keepalive';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

describe('StoreComponent', () => {
  let component: StoreComponent;
  let fixture: ComponentFixture<StoreComponent>;
  let userStoreService: UserStoreService;
  let templateStoreService: TemplateStoreService;
  const configData = require('assets/mock/configData.json');
  const programData = require('assets/mock/program.json');
  const mockUser = require('assets/mock/user.json');
  mockUser['program'] = programData;
  const userData = {
    user: mockUser,
    program: programData,
    config: programData['config'],
    get: () => of(mockUser)
  };
  const mainNavData = require('assets/mock/categories.json');
  const routerEvent$ = new BehaviorSubject<any>(null);
  let router: Router;
  document.head.innerHTML += '<link id="favIcon" rel="icon" type="image/x-icon" href="favicon.ico">';

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        StoreComponent,
        UAFooterComponent,
        NavComponent,
        SubNavComponent,
        NotificationBannerComponent,
        LogoComponent,
        WelcomeMsgComponent,
        FooterComponent,
        FooterDirective,
        HeaderDirective,
        HeaderComponent,
        OrderByPipe,
        CurrencyFormatPipe,
        InterpolatePipe
      ],
      imports: [
        NgbPopoverModule,
        CommonModule,
        BrowserAnimationsModule,
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot(),
        NgIdleKeepaliveModule.forRoot()
      ],
      providers: [
        EnsightenService,
        InterpolatePipe,
        { provide: NavStoreService },
        { provide: CategoryService },
        { provide: TemplateService },
        { provide: NgbActiveModal },
        { provide: ActivatedRoute, useValue: {
          params: of({category: 'ipad', subcat: 'ipad-accessories', addCat: 'ipad-accessories-apple-pencil', psid: '30001MXG22LL/A' }),
          snapshot: {},
          fragment: of(''),
          data: of(mainNavData) }
        },
        FooterDisclaimerService,
        { provide: MatomoService, useValue: {
          broadcast: () => {},
          initConfig: () => {}
        }},
        Idle,
        Keepalive,
        IdleService,
        CurrencyPipe,
        DecimalPipe,
        CurrencyFormatPipe
      ]
    })
    .compileComponents();
    templateStoreService = TestBed.inject(TemplateStoreService);
    userStoreService = TestBed.inject(UserStoreService);
    userStoreService.detailsname = 'TestingModule';
    userStoreService.addUser(userData.user);
    userStoreService.addProgram(userData.program);
    userData.config['imageServerUrl'] = 'https://als-static.bridge2rewards.com/dev2';
    userData.config.unAuthorizedPages = 'Store';
    userStoreService.config = userData.config;
    templateStoreService.template = configData['configData'];
    router = TestBed.inject(Router);
    router.navigate = jasmine.createSpy('navigate');
    (router as any).events = routerEvent$.asObservable();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StoreComponent);
    component = fixture.componentInstance;
    component.config['imageServerUrl'] = 'https://als-static.bridge2rewards.com/dev2';
    component.pageData = {
      pageName: 'PDP',
      theme: 'main-store'
    };
    component.pageName = 'Store';
    component.enableIframeResizer = true;
    component.mainNav = mainNavData;
    component.messages = require('assets/mock/messages.json');
    fixture.detectChanges();
  });

  it('should create with event of ActivationEnd', () => {
    const snapshot: ActivatedRouteSnapshot = Object.assign({});
    (snapshot as any).firstChild = Object.assign({});
    snapshot['firstChild'].data = {
      pageName: 'TESTPAGE',
      theme: 'main-store'
    };
    (snapshot as any).pathFromRoot = [Object.assign({})];
    snapshot.pathFromRoot[0]['url'] = [{path: 'testUrl', parameters: {}, parameterMap: null}];
    (snapshot as any).params = {
      category: 'watch',
      subcat: ''
    };
    routerEvent$.next(new ActivationEnd(snapshot));
    expect(component).toBeTruthy();
  });

  it('should create with event of NavigationEnd', () => {
    routerEvent$.next(new NavigationEnd(1, '/store/curated/ipad/ipad-accessories', '/store/curated/ipad/ipad-accessories'));
    component.config.fullCatalog = false;
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should trigger ngOnInit', () => {
    routerEvent$.next(new NavigationEnd(1, '/store/curated/ipad/ipad-accessories', '/store/curated/ipad/ipad-accessories'));
    component.config.fullCatalog = true;
    fixture.detectChanges();
    component.ngOnInit();
  });

  it('should trigger dispatch setPointsBalance event', fakeAsync(() => {
    routerEvent$.next(new NavigationEnd(1, '/store/curated/ipad/ipad-accessories', '/store/curated/ipad/ipad-accessories'));
    component.config.fullCatalog = true;
    component.enableIframeResizer = true;
    fixture.detectChanges();
    const ev = new CustomEvent('setPointsBalance', {
      detail: {
        formatted: formatNumber(99999999, 'en_US'),
        integer: 99999999
      }
    });
    component.ngOnInit();
    tick(3500);
    spyOn(window, 'dispatchEvent').and.callThrough();
    window.dispatchEvent(ev);
    expect(window.dispatchEvent).toHaveBeenCalledWith(ev);
  }));

  it('should trigger dispatch setSSO event', fakeAsync(() => {
    routerEvent$.next(new NavigationEnd(1, '/store/curated/ipad/ipad-accessories', '/store/curated/ipad/ipad-accessories'));
    component.config.fullCatalog = true;
    component.enableIframeResizer = true;
    fixture.detectChanges();
    const ssoEvent = new CustomEvent('setSSO', {
      detail: {
        persistentId: 12345
      }
    });
    component.ngOnInit();
    tick(3500);
    spyOn(window, 'dispatchEvent').and.callThrough();
    window.dispatchEvent(ssoEvent);
    expect(window.dispatchEvent).toHaveBeenCalledWith(ssoEvent);
  }));

  it('should trigger loadBannerStyles', () => {
    spyOn(component, 'loadBannerStyles').and.callThrough();
    component.loadBannerStyles();
    expect(component.loadBannerStyles).toHaveBeenCalled();
  });

  it('should trigger enableCustomRibbon', () => {
    component.notificationRibbonEnabledForPages.push('TestPage');
    spyOn(component, 'enableCustomRibbon').and.callThrough();
    component.enableCustomRibbon('TestPage');
    expect(component.enableCustomRibbon).toHaveBeenCalled();
  });

  it('should trigger skipMainContent', waitForAsync(() => {
    spyOn(component, 'skipMainContent').and.callThrough();
    const dummyElement = fixture.debugElement.query(By.css('#mainContent')).nativeElement;
    document.getElementById = jasmine.createSpy().and.returnValue(dummyElement);
    component.skipMainContent();
    expect(component.skipMainContent).toHaveBeenCalled();
  }));

  it('should call onActivate', fakeAsync(async () => {
    spyOn(component, 'onActivate').withArgs({}).and.callThrough();
    component.onActivate({});
    tick(100);
    expect(component.onActivate).toHaveBeenCalled();
  }));

  it('should trigger getChild', () => {
    spyOn(component, 'getChild').and.callThrough();
    const activatedRoute: ActivatedRoute = Object.assign({});
    (activatedRoute as any).firstChild = {
      params: of({category: 'ipad', subcat: 'ipad-accessories'})
    };
    component.getChild(activatedRoute);
    expect(component.getChild).toHaveBeenCalled();
  });

  it('should call setTitle method for testUrl', () => {
    spyOn(component, 'setTitle').and.callThrough();
    component.params = {
      category: 'ipad',
      subcat: 'ipad-accessories',
      addCat: 'ipad-accessories-apple-pencil',
      psid: ''
    };
    fixture.detectChanges();
    const currentData = {
      brcrumb: 'WebShop',
      appendTitle: 'uaShop',
      pageName: 'PDP'
    };
    component.setTitle(currentData, 'testUrl');
    expect(component.setTitle).toHaveBeenCalled();
  });

  it('should call setTitle method for webshop', () => {
    spyOn(component, 'setTitle').and.callThrough();
    component.params = {
      category: 'ipad',
      subcat: 'ipad-accessories',
      addCat: 'ipad-accessories-apple-pencil',
      psid: '30001MXG22LL/A'
    };
    fixture.detectChanges();
    const currentData = {
      brcrumb: 'WebShop',
      appendTitle: 'uaShop',
      pageName: 'webshop'
    };
    component.setTitle(currentData, '/webshop');
    expect(component.setTitle).toHaveBeenCalled();
  });

  it('should call setTitle method for store', () => {
    spyOn(component, 'setTitle').and.callThrough();
    component.params = {
      category: 'ipad',
      subcat: 'ipad-accessories',
      addCat: 'ipad-accessories-apple-pencil',
      psid: '30001MXG22LL/A'
    };
    component.mainNav = mainNavData['mainNav'];
    fixture.detectChanges();
    const currentData = {
      brcrumb: 'PDP',
      appendTitle: '',
      pageName: 'PDP'
    };
    component.setTitle(currentData, '/store');
    expect(component.setTitle).toHaveBeenCalled();
  });

  it('should call setPageTitle method', () => {
    spyOn(component, 'setPageTitle').and.callThrough();
    const stParams = { catname: 'ipad', subcatname: 'ipad-accessories', addcatname: 'ipad-accessories-apple-pencil', detailsname: 'Store' };
    const currentData = { brcrumb: 'Store', appendTitle: '', pageName: 'Store', configuredTitle: true };
    component.setPageTitle('breadcrumbStore', '', currentData, stParams);
    expect(component.setPageTitle).toHaveBeenCalled();
  });

});
