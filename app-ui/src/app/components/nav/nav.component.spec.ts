import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { NavComponent } from './nav.component';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { NavStoreService } from '@app/state/nav-store.service';
import { NgbModule, NgbPopover, NgbPopoverModule } from '@ng-bootstrap/ng-bootstrap';
import { BehaviorSubject, of } from 'rxjs';
import { TemplateStoreService } from '@app/state/template-store.service';
import { ModalsService } from '@app/components/modals/modals.service';
import { ActivatedRoute, NavigationEnd, Router, RouterEvent } from '@angular/router';
import { MainNavComponent } from '@app/components/main-nav/main-nav.component';
import { SubNavComponent } from '@app/components/sub-nav/sub-nav.component';
import { SearchBoxComponent } from '@app/components/search-box/search-box.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { CustomHoverLinkDirective } from '@app/directives/custom-hover-link.directive';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { UserDropdownComponent } from '../user-dropdown/user-dropdown.component';
import { SafePipe } from '@app/pipes/safe.pipe';
import { By } from '@angular/platform-browser';
import { Component } from '@angular/core';
import { AplImgSizePipe } from '@app/pipes/apl-img-size.pipe';

// Simple test component that will not in the actual app
@Component({
  template:
  `<header role="banner" class="header-container">
    <div class="wrapper-header"></div>
     <section  class="wrapper-nav remove-overlay-srch">
        <a href="javascript:void(0)" aria-expanded="false" aria-label="Bag" placement="bottom" class="popup-cta btn-cart-popup nav-icon g-icons icon-ShoppingCartIcon cart-with-items"
          role="button" #bagMenu="ngbPopover"  triggers="manual"  [ngbPopover]="popContent" [autoClose]="'outside'">
        <span class="g-icons nav-hover-element path1"></span>
        <span class="g-icons path2" style="color: rgb(122, 185, 249);"></span>
        </a>
        <ng-container>
          <li class="main nav-animation" appCustomHoverLink>
            <a class="g-icons nav-icon active" routerLinkActive="active">
              <span class="hideFromDOM">{{ link.i18nName }}</span>
            </a>
          </li>
        </ng-container>
      <ng-template #popContent>
        <div class="popover-content" id="bag-content">
        </div>
      </ng-template>
     </section>
        <div id="subnav-container" style="background: rgb(247, 248, 249);">
          <div class="contain subnav-container">
            <div class="navbar-wrap"></div>
          </div>
      </div>
    </header>`
})
class TestComponent {}

describe('NavComponent', () => {
  let component: NavComponent;
  let fixture: ComponentFixture<NavComponent>;
  let testFixture: ComponentFixture<TestComponent>;
  let httpTestingController: HttpTestingController;
  let navStore: NavStoreService;
  let router: Router;
  let templateStoreService: TemplateStoreService;
  const mainNavData = require('assets/mock/categories.json');
  const routerEvent$ = new BehaviorSubject<RouterEvent>(null);
  const configData = require('assets/mock/configData.json');
  const programData = require('assets/mock/program.json');
  programData.config['navBarColors'] = {
    activeTextColor: '#fff'
  };
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config'],
    enableNotificationBanner: () => of({})
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        NavComponent,
        TestComponent,
        MainNavComponent,
        SubNavComponent,
        SearchBoxComponent,
        CustomHoverLinkDirective,
        UserDropdownComponent,
        SafePipe
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        NgbModule,
        NgbPopoverModule,
        BrowserAnimationsModule,
        FormsModule,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: MessagesStoreService },
        { provide: UserStoreService, useValue : userData },
        { provide: ModalsService, useValue: {
          openAnonModalComponent: () => ({}),
          openBrowseOnlyComponent: () => ({})  }
        },
        { provide: ActivatedRoute, useValue: {
          params: of({category: 'ipad', subcat: 'ipad-accessories' }),
          snapshot: {},
          firstChild: {
            params: of({category: 'ipad', subcat: 'ipad-accessories'}) }
          }
        },
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe,
        AplImgSizePipe
      ]
    })
    .compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    (router as any).events = routerEvent$.asObservable();
    navStore = TestBed.inject(NavStoreService);
    navStore.addMainNav(mainNavData);
    templateStoreService = TestBed.inject(TemplateStoreService);
    router.navigate = jasmine.createSpy('navigate');
    templateStoreService.addTemplate(configData['configData']);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NavComponent);
    testFixture = TestBed.createComponent(TestComponent);
    component = fixture.componentInstance;
    component.isTablet = true;
    component.mainNav = mainNavData;
    component.currentSlug = 'ipad';
    component.config.displayTwoLineIcon = true;
    component.showSubNavBar = false;
    component.cartItems = require('assets/mock/cart.json')['cartItems'];
    component['cartService'].setUpdateCartObj(require('assets/mock/cart.json'));
    const cartTotal = component['sharedService'].getPriceData(require('assets/mock/cart.json')['cartTotal'])
    spyOn(component['sharedService'], 'getPriceData').and.returnValue(cartTotal);
    fixture.detectChanges();
  });

  it('should create', () => {
    routerEvent$.next(new NavigationEnd(1, '/store/curated/ipad/ipad-accessories', '/store/curated/ipad/ipad-accessories'));
    expect(component).toBeTruthy();
  });

  it('should create else block', () => {
    component['route'].firstChild.params = of({category: null, subcat: 'ipad-accessories' });
    fixture.detectChanges();
    routerEvent$.next(new NavigationEnd(1, '/store/curated/ipad/ipad-accessories', '/store/curated/ipad/ipad-accessories'));
    expect(component).toBeTruthy();
  });

  it('ngOnInit should be called - Main Nav', () => {
    component.showSubNavBar = true;
    spyOnProperty(router, 'url').and.returnValue('/store/curated/ipad/ipad-accessories');
    component.cartService.setCartUpdateMessage('item added successfully');
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.mainNav.length).toEqual(7);
    expect(component.mainNav[0]['slug']).toEqual('mac');
  });

  it('ngOnInit should be called - when no cart update message exists', () => {
    component.showSubNavBar = true;
    spyOnProperty(router, 'url').and.returnValue('/store/curated/ipad/ipad-accessories');
    component.cartService.setCartUpdateMessage(undefined);
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.mainNav.length).toEqual(7);
    expect(component.mainNav[0]['slug']).toEqual('mac');
  });

  it('ngOnInit should be called - Main Nav else block', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    component.showSubNavBar = true;
    spyOnProperty(router, 'url').and.returnValue('/store/curated/test/test-accessories');

    fixture.detectChanges();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should call openModal method and return false', () => {
    expect(component.openModal()).toBeFalsy();
  });

  it('should call showHideSearchBar method', () => {
    spyOn(component, 'showHideSearchBar').and.callThrough();
    component.showHideSearchBar();
    expect(component.showHideSearchBar).toHaveBeenCalled();
  });

  it('should call toggleQuickLinks method', () => {
    spyOn(component, 'toggleQuickLinks').and.callThrough();
    component.toggleQuickLinks(true);
    expect(component.toggleQuickLinks).toHaveBeenCalled();
  });

  it('should call mobile navigation', () => {
    spyOn(component, 'mobileNavClose').and.callThrough();
    component.isTablet = false;
    component.config.displayMobileHeader = true;
    document.body.innerHTML += '<div class="new-nav-mobile"><div class="nav-items enabled"></div></div>';
    fixture.detectChanges();
    component.mobileNavClose();
    expect(component.mobileNavClose).toHaveBeenCalled();
  });

  it('should call mobile navigation to open', () => {
    component.isTablet = true;
    component.config.showFamilyNav = true;
    fixture.detectChanges();
    spyOn(component, 'mobileNavOpen').and.callThrough();
    const element = document.querySelectorAll('.main.nav-animation');
    element[0].children[0].setAttribute('class', 'active g-icons nav-icon icon-ipad');
    fixture.detectChanges();
    component.mobileNavOpen();
    expect(component.mobileNavOpen).toHaveBeenCalled();
  });

  it('should build submenu', () => {
    component.currentSlug = 'mac';
    component.initialSubnav(component.mainNav);
    expect(component.subNav.length).toEqual(6);
    expect(component.subNav[0].i18nName).toEqual('MacBook Air');
  });

  it('should change subNav menu', () => {
    component.changeSubnav(3);
    expect(component.subNav.length).toEqual(7);
    expect(component.subNav[0].i18nName).toEqual('Apple Watch Series 6');

    component.changeSubnav(2);
    expect(component.subNav.length).toEqual(8);
    expect(component.subNav[0].i18nName).toEqual('iPhone 12 Pro');
  });

  it('should call focus search icon navigation', () => {
    component.config.showNavBarSearch = true;
    fixture.detectChanges();
    spyOn(component, 'focusSearchIcon').and.callThrough();
    component.focusSearchIcon();
    expect(component.focusSearchIcon).toHaveBeenCalled();
  });

  it('should call mobile navigation toggle', waitForAsync(() => {
    spyOn(component, 'mobileNavToggle').and.callThrough();
    document.body.innerHTML += '<div class="new-nav-mobile"><div class="nav-items"></div></div>';
    component.isMobile = true;
    fixture.detectChanges();
    const element = fixture.debugElement.query(By.directive(NgbPopover));
    spyOn(element.references['mobileNav'], 'isOpen').and.callFake(() => false);
    component.mobileNavToggle(element.references['mobileNav']);
    expect(component.mobileNavToggle).toHaveBeenCalled();
  }));

  it('should call mobileToggle method', () => {
    spyOn(component, 'mobileNavToggle').and.callThrough();
    document.body.innerHTML += '<div class="new-nav-mobile"><div class="nav-items"></div></div>';
    component.isMobile = true;
    fixture.detectChanges();
    const element = fixture.debugElement.query(By.directive(NgbPopover));
    spyOn(element.references['mobileNav'], 'isOpen').and.callFake(() => true);
    component.mobileNavToggle(element.references['mobileNav']);
    expect(component.mobileNavToggle).toHaveBeenCalled();
  });

  it('should call getTemplateUrl', () => {
    spyOn(component, 'getTemplateURL').and.callThrough();
    component.getTemplateURL('apple-gr/vars/default/templates/apple-gr-templates/tile.html');
    expect(component.getTemplateURL).toHaveBeenCalled();
  });

  it('should call getTemplateUrl', () => {
    spyOn(component, 'getTemplateURL').and.callThrough();
    component.getTemplateURL('');
    expect(component.getTemplateURL).toHaveBeenCalled();
  });

  it('should call getCart', () => {
    const cartDataResponse = require('assets/mock/cart.json');
    component.config.loginRequired = true;
    spyOn(component['cartService'], 'getCart').and.returnValue(of(cartDataResponse));
    spyOn(component, 'getCart').and.callThrough();
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  });

  it('should call getCart - loginRequired is false', () => {
    const cartDataResponse = require('assets/mock/cart.json');
    component.config.loginRequired = false;
    spyOn(component['cartService'], 'getCart').and.returnValue(of(cartDataResponse));
    spyOn(component, 'getCart').and.callThrough();
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  });

  it('should call setShoppingCartItemsMessage method for 4 cart items', () => {
    component.cartItems = require('assets/mock/cart.json')['cartItems'];
    spyOn(component, 'setShoppingCartItemsMessage').and.callThrough();
    component.setShoppingCartItemsMessage();
    expect(component.setShoppingCartItemsMessage).toHaveBeenCalled();
  });

  it('close popover with false boolean value', () => {
    expect(component.closePopover()).toBeFalsy();
  });

  it('open popover with popupTabEvent func call', () => {
    component.popupTabEvent();
    expect(component.isOpen).toBeTruthy();
  });

  it('should call goToCart method', () => {
    component.user.browseOnly = true;
    fixture.detectChanges();
    spyOn(component, 'goToCart').and.callThrough();
    component.goToCart(null);
    expect(component.goToCart).toHaveBeenCalled();
  });

  it('should call window resize method when resize event dispatch', () => {
    spyOn(component, 'onResize').and.callThrough();
    component.isOpen = true;
    window.dispatchEvent(new Event('resize'));
    fixture.detectChanges();
    expect(component.onResize).toHaveBeenCalled();
  });

  it('should call window resize method when resize event dispatch', () => {
    spyOn(component, 'onResize').and.callThrough();
    component.isOpen = false;
    window.dispatchEvent(new Event('resize'));
    fixture.detectChanges();
    expect(component.onResize).toHaveBeenCalled();
  });

  it('should call getCart', () => {
    const cartDataResponse = require('assets/mock/cart.json');
    spyOn(component, 'getCart').and.callThrough();
    spyOn(component.cartService, 'getCart').and.returnValue(of(cartDataResponse));
    component.persistCartAbandonNotification = false;
    component.isCartAbandonNotificationEnabled = true;
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  });

  it('should call getQuickLinks method', () => {
    spyOn(component, 'getQuickLinks').and.callThrough();
    const quickLinks = require('assets/mock/quickLinks.json');
    spyOn(component['quickLinkService'], 'getQuickLinks').and.returnValue(of(quickLinks));
    component.getQuickLinks();
    expect(component.getQuickLinks).toHaveBeenCalled();
    expect(component.quickLinks).toBeDefined();
  });

  it('should call goToCart method', () => {
    component.user.browseOnly = false;
    const directive = testFixture.debugElement.query(By.directive(NgbPopover));
    component.bagMenu = directive.references['bagMenu'];
    fixture.detectChanges();
    spyOn(component, 'goToCart').and.callThrough();
    component.goToCart(component.bagMenu);
    expect(component.goToCart).toHaveBeenCalled();
  });

  it('should call closeBagMenu method', () => {
    component.persistCartAbandonNotification = true;
    fixture.detectChanges();
    spyOn(component, 'closeBagMenu').and.callThrough();
    const directive = testFixture.debugElement.query(By.directive(NgbPopover));
    component.bagMenu = directive.references['bagMenu'];
    component.closeBagMenu(directive.references['bagMenu']);
    expect(component.closeBagMenu).toHaveBeenCalled();
  });

  it('should call setPositionForBagPopup for mobile view', () => {
    spyOn(component, 'setPositionForBagPopup').and.callThrough();
    spyOnProperty(window, 'innerWidth').and.returnValue(720);
    component.isMobile = true;
    fixture.detectChanges();
    component.setPositionForBagPopup();
    expect(component.setPositionForBagPopup).toHaveBeenCalled();
  });

  it('should call setPositionForBagPopup - when reading clientWidth', () => {
    spyOn(component, 'setPositionForBagPopup').and.callThrough();
    spyOnProperty(window, 'innerWidth').and.returnValue(null);
    component.isMobile = true;
    fixture.detectChanges();
    component.setPositionForBagPopup();
    expect(component.setPositionForBagPopup).toHaveBeenCalled();
  });

  it('should call setPositionForBagPopup for desktop view', () => {
    spyOn(component, 'setPositionForBagPopup').and.callThrough();
    component.isMobile = false;
    fixture.detectChanges();
    component.setPositionForBagPopup();
    expect(component.setPositionForBagPopup).toHaveBeenCalled();
  });

});
