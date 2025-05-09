import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { GiftPromoService } from '@app/services/gift-promo.service';
import { GiftPromoComponent } from './gift-promo.component';
import { CartService } from '@app/services/cart.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';
import { RouterTestingModule } from '@angular/router/testing';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { of } from 'rxjs';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ActivatedRoute, Router } from '@angular/router';
import { TemplateStoreService } from '@app/state/template-store.service';
import { ModalsService } from '@app/components/modals/modals.service';
import { PageTitleComponent } from '@app/modules/shared/page-title/page-title.component';
import { SharedService } from '@app/modules/shared/shared.service';
import { UserStoreService } from '@app/state/user-store.service';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { AplImgSizePipe } from '@app/pipes/apl-img-size.pipe';

describe('GiftPromoComponent', () => {
  let component: GiftPromoComponent;
  let giftPromoService: GiftPromoService;
  let httpTestingController: HttpTestingController;
  const item = require('assets/mock/gift-products.json');
  let cartService: CartService;
  let router: Router;
  let fixture: ComponentFixture<GiftPromoComponent>;
  const programData = require('assets/mock/program.json');
  const mockUser = require('assets/mock/user.json');
  mockUser['program'] = programData;
  const userData = {
    user: mockUser,
    program: programData,
    config: programData['config'],
    get: () => of(mockUser)
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        GiftPromoComponent,
        PageTitleComponent
      ],
      imports: [
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        RouterTestingModule
      ],
      providers: [
        { provide: ParsePsidPipe },
        { provide: SharedService, useValue: {
          openEngraveModalDialog: () => {}
        }},
        { provide: CurrencyPipe, useValue: {
            program: of(programData),
            transform: () => of({}) }
        },
        { provide: NgbActiveModal },
        { provide: Router, useValue: {
            navigate: () => {} }
        },
        { provide: ActivatedRoute, useValue: {
            routeParams: of({ cartItemId: 12345, psid: '30001MXG22LL/A' }),
            params: of({cartItemId: 12345, qualifyingPsid: '30001MXG22LL/A'}),
            queryParams: of({hasRelatedProduct: true})
        }
        },
        { provide: TemplateStoreService, useValue: {
            buttonColor: () => of({}) }
        },
        { provide: ModalsService, useValue: {
            openAnonModalComponent: () => {},
            openSuggestAddressModalComponent: () => {},
            openBrowseOnlyComponent: () => {} }
        },
        { provide: UserStoreService, useValue: userData},
        { provide: MatomoService, useValue: {
          broadcast: () => {}
        }},
        AplImgSizePipe
      ]
    })
      .compileComponents();
    // Inject the http test controller
    httpTestingController = TestBed.inject(HttpTestingController);
    giftPromoService = TestBed.inject(GiftPromoService);
    cartService = TestBed.inject(CartService);
    router = TestBed.inject(Router);
    spyOn(router, 'navigate');
    giftPromoService.user = userData.user;
    giftPromoService.user.program = programData;
    giftPromoService.config = programData['config'];
    giftPromoService.config['loginRequired'] = false;
    giftPromoService.user['browseOnly'] = false;
  }));

  afterEach(() => {
    // After every test, assert that there are no more pending requests.
    httpTestingController.verify();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GiftPromoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });


  it('should create', () => {
    // Run some expectations
    const psid = '30001MXG22LL/A';
    const mockGiftProductResponse = require('assets/mock/gift-products.json');
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/' + 'giftItem?qualifyingPsid=' + psid.replace(/-/g, '/'));
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    // Respond with the fake data when called
    req.flush(mockGiftProductResponse);
    expect(mockGiftProductResponse.length).toBe(3);
    expect(component).toBeTruthy();
  });

  it('get additemgift call', waitForAsync(() => {
    spyOn(component, 'addGiftItem').and.callThrough();
    const psid = '30001MXG22LL/A';
    const fakeResponse = require('assets/mock/cart.json')['cartItems'];
    const mockGiftProductResponse = require('assets/mock/gift-products.json');
    spyOn(giftPromoService, 'giftItemModify').and.returnValue(of(fakeResponse));
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/' + 'giftItem?qualifyingPsid=' + psid.replace(/-/g, '/'));
     // Respond with the fake data when called
    req.flush(mockGiftProductResponse);
    item[0]['isEngravable'] = true;
    component.addGiftItem(item[0]);
    expect(component.addGiftItem).toHaveBeenCalled();
  }));
  it('get additemgift call', waitForAsync(() => {
    spyOn(component, 'addGiftItem').and.callThrough();
    const psid = '30001MXG22LL/A';
    const mockGiftProductResponse = require('assets/mock/gift-products.json');
    const fakeResponse = require('assets/mock/cart.json')['cartItems'];
    spyOn(giftPromoService, 'giftItemModify').and.returnValue(of(fakeResponse));
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/' + 'giftItem?qualifyingPsid=' + psid.replace(/-/g, '/'));
     // Respond with the fake data when called
    req.flush(mockGiftProductResponse);
    item[0]['isEngravable'] = false;
    component.addGiftItem(item[0]);
    expect(component.addGiftItem).toHaveBeenCalled();
  }));
  it('get parseitem call', waitForAsync(() => {
    spyOn(component, 'parseName').and.callThrough();
    const psid = '30001MXG22LL/A';
    const name = 'Apple Pencil (1st generation)';
    const mockGiftProductResponse = require('assets/mock/gift-products.json');
    const req = httpTestingController.expectOne('/apple-gr/service/' + 'giftItem?qualifyingPsid=' + psid.replace(/-/g, '/'));
    // Respond with the fake data when called
    req.flush(mockGiftProductResponse);
    component.parseName(psid);
    expect(component.parseName).toHaveBeenCalled();
  }));
  it('get parseitem call', waitForAsync(() => {
    spyOn(component, 'parseName').and.callThrough();
    const psid = '30001MXG22LL/A';
    const name = 'Apple';
    const mockGiftProductResponse = require('assets/mock/gift-products.json');
    const req = httpTestingController.expectOne('/apple-gr/service/' + 'giftItem?qualifyingPsid=' + psid.replace(/-/g, '/'));
    // Respond with the fake data when called
    req.flush(mockGiftProductResponse);
    component.parseName(name);
    expect(component.parseName).toHaveBeenCalled();
  }));
  it('should test for 401 error', waitForAsync(() => {
    const errorMsg = 'deliberate 401 error';
    const psid = '30001MXG22LL/A';
    const mockGiftProductResponse = require('assets/mock/gift-products.json');
    const req = httpTestingController.expectOne('/apple-gr/service/' + 'giftItem?qualifyingPsid=' + psid.replace(/-/g, '/'));
    // Respond with the fake data when called
    req.flush(mockGiftProductResponse);
    component.addGiftItem(item);
    const errreq = httpTestingController.expectOne(cartService.baseUrl + 'cart/modify/' + 12345);
    // Respond with mock error
    errreq.flush(errorMsg, { status: 401, statusText: 'Not Found' });
  }));
  it('should test for 404 error', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    const psid = '30001MXG22LL/A';
    const mockGiftProductResponse = require('assets/mock/gift-products.json');
    const req = httpTestingController.expectOne('/apple-gr/service/' + 'giftItem?qualifyingPsid=' + psid.replace(/-/g, '/'));
    // Respond with the fake data when called
    req.flush(mockGiftProductResponse);
    component.addGiftItem(item);
    const errreq = httpTestingController.expectOne(cartService.baseUrl + 'cart/modify/' + 12345);
    // Respond with mock error
    errreq.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

  it('should call router.navigate when cancelSelection is called', () => {
    const psid = '30001MXG22LL/A';
    const mockGiftProductResponse = require('assets/mock/gift-products.json');
    const req = httpTestingController.expectOne('/apple-gr/service/' + 'giftItem?qualifyingPsid=' + psid.replace(/-/g, '/'));
    // Respond with the fake data when called
    req.flush(mockGiftProductResponse);
    component.routeQueryParams.hasRelatedProduct = 'true';
    component.cancelSelection();
    expect(router.navigate).toHaveBeenCalled();
  });

  it('should call router.navigate when cancelSelection is called with routeQueryParams.hasRelatedProduct value undefined', () => {
    const psid = '30001MXG22LL/A';
    const mockGiftProductResponse = require('assets/mock/gift-products.json');
    const req = httpTestingController.expectOne('/apple-gr/service/' + 'giftItem?qualifyingPsid=' + psid.replace(/-/g, '/'));
    // Respond with the fake data when called
    req.flush(mockGiftProductResponse);
    component.routeQueryParams.hasRelatedProduct = undefined;
    component.cancelSelection();
    expect(router.navigate).toHaveBeenCalled();
  });
});
