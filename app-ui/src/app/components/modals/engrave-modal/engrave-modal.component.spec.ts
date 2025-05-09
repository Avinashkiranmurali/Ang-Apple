import { ComponentFixture, fakeAsync, inject, TestBed, tick, waitForAsync } from '@angular/core/testing';
import { ActivatedRoute, ActivatedRouteSnapshot, ActivationEnd, Router, RouterModule } from '@angular/router';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';
import { SharedService } from '@app/modules/shared/shared.service';
import { UserStoreService } from '@app/state/user-store.service';
import { BehaviorSubject, of, throwError } from 'rxjs';
import { CartService } from '@app/services/cart.service';
import { SafePipe } from '@app/pipes/safe.pipe';
import { FormsModule } from '@angular/forms';
import { EngraveModalComponent } from './engrave-modal.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { DecimalPipe } from '@angular/common';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { GiftPromoService } from '@app/services/gift-promo.service';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';
import { NotificationRibbonService } from '@app/services/notification-ribbon.service';

describe('EngraveModalComponent', () => {
  let component: EngraveModalComponent;
  let fixture: ComponentFixture<EngraveModalComponent>;
  let httpTestingController: HttpTestingController;
  let cartService: CartService;
  const relatedProductDetail = require('assets/mock/product-detail.json');
  const programData = require('assets/mock/program.json');
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config'],
    get: () => of(userData)
  };
  const detailData = require('assets/mock/product-detail.json');
  detailData.engrave = {
    engraveBgImageLocation: 'apple-gr/assets/img/engraving/',
    engraveFontConfigurations: [],
    font: 'Helvetica Neue',
    fontCode: 'ESR075N',
    isSkuBasedEngraving: false,
    line1: '',
    line2: '',
    maxCharsPerLine: '18 Eng',
    noOfLines: 2,
    templateClass: '',
    widthDimension: '45mm',
    isDefaultPreviewEnabled: false,
    isUpperCaseEnabled: false,
    previewUrl: 'https://www.apple.com/shop/preview/engrave/{{itemSKU}}?{{engraveText}}s=2&f=mixed'
  };

  const routerEvent$ = new BehaviorSubject<ActivationEnd>(null);
  let router: Router;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        EngraveModalComponent,
        SafePipe,
      ],
      imports: [
        FormsModule,
        HttpClientTestingModule,
        RouterModule.forRoot([], { relativeLinkResolution: 'legacy' }),
        TranslateModule.forRoot()
      ],
      providers: [
        SharedService,
        GiftPromoService,
        ParsePsidPipe,
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe,
        { provide: NgbActiveModal },
        { provide: UserStoreService, useValue: userData },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({
              progId: '776269',
              psid: '30001MXG22LL/A'
            })
          }
        },
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate') }
        },
        NotificationRibbonService
      ]
    })
    .compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
    cartService = TestBed.inject(CartService);
    router = TestBed.inject(Router);
    (router as any).events = routerEvent$.asObservable();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EngraveModalComponent);
    component = fixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
    component.config['imageServerUrl'] = 'https://als-static.bridge2rewards.com/dev2';
    component.psIdSlug = '30001MXG22LL/A';
    component.configItemSku = 'MXG22LL/A';
    component.cartItemId = '776269';
    component.detailData = detailData;
    component.emptyFieldError = false;
    component.qualifyingProduct = relatedProductDetail;
    component['sharedService'].currentEngraveProductDetail = detailData;
    fixture.detectChanges();
  });

  it('should create', () => {
    const snapshot: ActivatedRouteSnapshot = Object.assign({});
    snapshot.data = {
      gift: true
    };
    routerEvent$.next(new ActivationEnd(snapshot));
    expect(component).toBeTruthy();
  });

  it('should call convertToUpperCase method - TO UPPERCASE', () => {
    expect(component.convertToUpperCase('test', true)).toEqual('TEST');
  });

  it('should call convertToUpperCase method - NOT TO UPPERCASE', () => {
    expect(component.convertToUpperCase(null, false)).toEqual('');
  });

  it('should call getEngraveImage method', () => {
    spyOn(component, 'getEngraveImage').and.callThrough();
    component.engrave = {
      line1: 'ENGRAVE',
      line2: 'TEST ENGRAVE TEST'
    };
    fixture.detectChanges();
    component.shortLocale = 'en';
    component.getEngraveImage();
    expect(component.getEngraveImage).toHaveBeenCalled();
  });

  it('should call getEngraveImage method when no engrave data exists', () => {
    spyOn(component, 'getEngraveImage').and.callThrough();
    component.engrave = {
      line1: '',
      line2: ''
    };
    component.isDefaultPreviewEnabled = true;
    component.noOfLines = 2;
    fixture.detectChanges();
    component.getEngraveImage();
    expect(component.getEngraveImage).toHaveBeenCalled();
  });

  it('should call getEngraveImage method when no engrave data exists and noOfLines is 0', () => {
    spyOn(component, 'getEngraveImage').and.callThrough();
    component.engrave = {
      line1: '',
      line2: ''
    };
    component.isDefaultPreviewEnabled = true;
    component.noOfLines = 0;
    fixture.detectChanges();
    component.getEngraveImage();
    expect(component.getEngraveImage).toHaveBeenCalled();
  });

  it('should call getEngraveImage method when no engrave data with single line', () => {
    spyOn(component, 'getEngraveImage').and.callThrough();
    component.engrave = {
      line1: '',
      line2: ''
    };
    component.isDefaultPreviewEnabled = true;
    component.isUpperCaseEnabled = true;
    component.noOfLines = 1;
    fixture.detectChanges();
    component.getEngraveImage();
    expect(component.getEngraveImage).toHaveBeenCalled();
  });

  it('should call getEngraveImage method for not engrave data with single line and templateClass', () => {
    spyOn(component, 'getEngraveImage').and.callThrough();
    component.engrave = {
      line1: '',
      line2: ''
    };
    component.isDefaultPreviewEnabled = true;
    component.noOfLines = 1;
    fixture.detectChanges();
    component.getEngraveImage();
    expect(component.getEngraveImage).toHaveBeenCalled();
  });

  it('should call updateEngraving method for success validation', () => {
    spyOn(component, 'updateEngraving').and.callThrough();
    component.engrave = {
      line1: 'ENGRAVE',
      line2: 'TEST ENGRAVE TEST'
    };
    fixture.detectChanges();
    component.updateEngraving();
    expect(component.updateEngraving).toHaveBeenCalled();
  });

  it('should call engraveLine2KeyUp method for when engrave second line alone presents', () => {
    spyOn(component, 'engraveLine2KeyUp').and.callThrough();
    component.engrave = {
      line1: '',
      line2: 'test'
    };
    fixture.detectChanges();
    component.engraveLine2KeyUp();
    expect(component.engraveLine2KeyUp).toHaveBeenCalled();
  });

  it('should call engraveLine2KeyUp method for when engrave data not exists but with emptyErrorField', () => {
    spyOn(component, 'engraveLine2KeyUp').and.callThrough();
    component.emptyFieldError = true;
    component.engrave = {
      line1: '',
      line2: ''
    };
    fixture.detectChanges();
    component.engraveLine2KeyUp();
    expect(component.engraveLine2KeyUp).toHaveBeenCalled();
  });

  it('should call engraveLine1KeyUp method for invalid engrave line one validation', () => {
    spyOn(component, 'engraveLine1KeyUp').and.callThrough();
    component.engrave = {
      line1: '<ENGRAVE>',
      line2: ''
    };
    fixture.detectChanges();
    component.engraveLine2KeyUp();
    component.engraveLine1KeyUp();
    expect(component.engraveLine1KeyUp).toHaveBeenCalled();
  });

  it('should call engraveLine1KeyUp method for valid engrave line one', () => {
    spyOn(component, 'engraveLine1KeyUp').and.callThrough();
    component.engrave = {
      line1: 'ENGRAVE',
      line2: '<TEST>'
    };
    fixture.detectChanges();
    component.engraveLine2KeyUp();
    component.engraveLine1KeyUp();
    expect(component.engraveLine1KeyUp).toHaveBeenCalled();
  });

  it('should call engraveLine2KeyUp method for invalid engrave line two validation', () => {
    spyOn(component, 'engraveLine2KeyUp').and.callThrough();
    component.engrave = {
      line1: '',
      line2: '<ENGRAVE>'
    };
    fixture.detectChanges();
    component.engraveLine1KeyUp();
    component.engraveLine2KeyUp();
    expect(component.engraveLine2KeyUp).toHaveBeenCalled();
  });

  it('should call engraveLine2KeyUp method for undefined engrave line one validation', () => {
    spyOn(component, 'engraveLine2KeyUp').and.callThrough();
    component.engrave = {
      line1: undefined,
      line2: 'ENGRAVE'
    };
    fixture.detectChanges();
    component.engraveLine2KeyUp();
    expect(component.engraveLine2KeyUp).toHaveBeenCalled();
  });

  it('should call updateEngrav method for undefined engrave line two validation', () => {
    spyOn(component, 'updateEngraving').and.callThrough();
    component.engrave = {
      line1: 'Engrave',
      line2: undefined
    };
    fixture.detectChanges();
    component.updateEngraving();
    expect(component.updateEngraving).toHaveBeenCalled();
  });

  it('should call updateEngraving method when only second line data exists', () => {
    spyOn(component, 'updateEngraving').and.callThrough();
    component.engrave = {
      line1: '',
      line2: '<TEST>'
    };
    fixture.detectChanges();
    component.updateEngraving();
    expect(component.updateEngraving).toHaveBeenCalled();
  });

  it('should call updateEngraving via modify cart service api success', () => {
    spyOn(component, 'updateEngraving').and.callThrough();
    component.engrave = {
      line1: 'ENGRAVE',
      line2: 'TEST ENGRAVE TEST'
    };
    component.isGiftPromo = true;
    fixture.detectChanges();
    component.updateEngraving();

    const fakeResponse = require('assets/mock/cart.json')['cartItems'];
    // Expect a call to this URL
    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart/modify/' + 776269);
    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');
    // Respond with the fake data when called
    req.flush(fakeResponse);
    expect(component.updateEngraving).toHaveBeenCalled();
  });

  it('should call updateEngraving via modify cart service api success - for Edit scenario', () => {
    spyOn(component, 'updateEngraving').and.callThrough();
    component.engrave = {
      line1: 'ENGRAVE',
      line2: 'TEST ENGRAVE TEST'
    };
    component.isGiftPromo = true;
    component.isEdit = true;
    component.qualifyingProduct = relatedProductDetail;
    fixture.detectChanges();
    component.updateEngraving();

    const fakeResponse = require('assets/mock/cart.json')['cartItems'];
    // Expect a call to this URL
    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart/modify/' + 776269);
    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');
    // Respond with the fake data when called
    req.flush(fakeResponse);
    expect(component.updateEngraving).toHaveBeenCalled();
  });

  it('should call updateEngraving via modify cart service api with 401 error', () => {
    spyOn(component, 'updateEngraving').and.callThrough();
    component.engrave = {
      line1: 'ENGRAVE',
      line2: 'TEST ENGRAVE TEST'
    };
    component.isGiftPromo = false;
    fixture.detectChanges();
    component.updateEngraving();
    const errorMsg = 'deliberate 401 error';

    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart/modify/' + 776269);
    // Respond with mock error
    req.flush(errorMsg, { status: 401, statusText: 'Not Found' });
    expect(component.updateEngraving).toHaveBeenCalled();
  });

  it('should call updateEngraving via modify cart service api with 500 error', () => {
    spyOn(component, 'updateEngraving').and.callThrough();
    component.engrave = {
      line1: 'ENGRAVE',
      line2: 'TEST ENGRAVE TEST'
    };
    component.isGiftPromo = false;
    fixture.detectChanges();
    component.updateEngraving();
    const errorMsg = 'deliberate 500 error';

    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart/modify/' + 776269);
    // Respond with mock error
    req.flush(errorMsg, { status: 500, statusText: 'Internal server not Found' });
    expect(component.updateEngraving).toHaveBeenCalled();
  });

  it('should call updateEngraving via modify cart service api with 400 error for first engrave line', () => {
    spyOn(component, 'updateEngraving').and.callThrough();
    component.engrave = {
      line1: 'ENGRAVE',
      line2: 'TEST ENGRAVE TEST'
    };
    component.isGiftPromo = false;
    fixture.detectChanges();
    component.updateEngraving();
    const errorMsg = {
      error: {
        inputField: 'line1'
      }
    };

    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart/modify/' + 776269);
    // Respond with mock error
    req.flush(errorMsg, { status: 400, statusText: 'Internal Issue Found' });
    expect(component.updateEngraving).toHaveBeenCalled();
  });

  it('should call updateEngraving via modify cart service api with 400 error for second engrave line', () => {
    spyOn(component, 'updateEngraving').and.callThrough();
    component.engrave = {
      line1: 'ENGRAVE',
      line2: 'TEST ENGRAVE TEST'
    };
    component.isGiftPromo = false;
    fixture.detectChanges();
    component.updateEngraving();
    const errorMsg = {
      error: {
        inputField: 'line2'
      }
    };

    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart/modify/' + 776269);
    // Respond with mock error
    req.flush(errorMsg, { status: 400, statusText: 'Internal Issue Found' });
    expect(component.updateEngraving).toHaveBeenCalled();
  });

  it('should call updateEngraving via modify cart service api with 400 error for unknown engrave line', () => {
    spyOn(component, 'updateEngraving').and.callThrough();
    component.engrave = {
      line1: 'ENGRAVE',
      line2: 'TEST ENGRAVE TEST'
    };
    component.isGiftPromo = false;
    fixture.detectChanges();
    component.updateEngraving();
    const errorMsg = {
      error: {
        inputField: 'line3'
      }
    };
    const req = httpTestingController.expectOne(cartService.baseUrl + 'cart/modify/' + 776269);
    // Respond with mock error
    req.flush(errorMsg, { status: 400, statusText: 'Internal Issue Found' });
    expect(component.updateEngraving).toHaveBeenCalled();
  });

  it('should call getLevelsFromCategories method', () => {
    const data = {
      accessoryItem: true,
      categories: [
        {
          imageUrl: '',
          i18nName: 'iPad Pro',
          slug: 'ipad-pro',
          name: 'iPad Pro',
          templateType: 'CONFIGURABLE',
          defaultImage: 'apple-gr/assets/img/customizable/ipad-pro.jpg',
          summaryIconImage: '',
          displayOrder: 1,
          engraveBgImageLocation: 'apple-gr/assets/img/engraving/',
          subCategories: [],
          parents: [
            {
              imageUrl: '',
              i18nName: 'iPad',
              slug: 'ipad',
              name: 'iPad',
              templateType: 'LANDING',
              defaultImage: '',
              summaryIconImage: '',
              displayOrder: 2,
              engraveBgImageLocation: null,
              subCategories: [],
              parents: [],
              products: [],
              images: {},
              psid: null,
              depth: 0,
              new: false,
              configurable: true,
              active: true
            }
          ],
          products: [],
          images: {},
          psid: null,
          depth: 1,
          new: false,
          configurable: true,
          active: true
        }
      ]
    };
    spyOn(component, 'getLevelsFromCategories').and.callThrough();
    component.getLevelsFromCategories(data);
    expect(component.getLevelsFromCategories).toHaveBeenCalled();
  });

  it('calling validateText method when whitelist is empty', () => {
    spyOn(component, 'validateText').and.callThrough();
    component.messages = {
      allowedCharacters: ''
    };
    component.validateText('<test>');
    expect(component.validateText).toHaveBeenCalled();
  });

  it('should call loadEngraving method when categories is not configurable', () => {
    spyOn(component, 'loadEngraving').and.callThrough();
    spyOn(component, 'getItemAdditionalInfo').and.callFake(() => {});
    spyOn(component['sharedService'], 'getProductBackImage').and.callFake(() => {});
    component.detailData = {
      name: 'iPad Pro',
      slug: 'ipad-pro',
      categories: [{
        templateType: 'CATEGORYLIST',
        slug: 'ipad-pro'
      }]
    };
    component.productLevels = {
      category: { slug: 'ipad', name: 'iPad' },
      subcat: { slug: 'ipad-pro', name: 'iPad Pro' }
    };
    component.engraveData = { isPreview: true, isSkuBasedEngraving: true };
    component.loadEngraving();
    expect(component.loadEngraving).toHaveBeenCalled();
  });

  it('should call loadEngraving method when categories slug is for accessories', () => {
    spyOn(component, 'loadEngraving').and.callThrough();
    spyOn(component, 'getItemAdditionalInfo').and.callFake(() => {});
    spyOn(component['sharedService'], 'getProductBackImage').and.callFake(() => {});
    component.cartItemId = '';
    component.detailData = {
      name: 'AirPods with Wireless Charging Case',
      categories: [{
        templateType: 'CATEGORYLIST'
      }]
    };
    component.productLevels = {
      category: { slug: 'all-accessories-headphones-speakers', name: 'Headphones & Speakers' },
      subcat: { slug: 'all-accessories-headphones-speakers', name: 'Headphones & Speakers' }
    };
    component.engraveData = { isPreview: true, isSkuBasedEngraving: true };
    component.loadEngraving();
    expect(component.loadEngraving).toHaveBeenCalled();
  });

  it('should call loadEngraving method when categories is configurable', () => {
    spyOn(component, 'loadEngraving').and.callThrough();
    spyOn(component, 'getItemAdditionalInfo').and.callFake(() => {});
    spyOn(component['sharedService'], 'getProductBackImage').and.callFake(() => {});
    component.detailData = {
      name: 'iPad Pro',
      categories: [{
        templateType: 'CONFIGURABLE',
        slug: 'ipad-pro'
      }]
    };
    component.productLevels = {
      category: { slug: 'ipad', name: 'iPad' },
      subcat: { slug: 'ipad-pro', name: 'iPad Pro' }
    };
    component.engraveData = { isPreview: true, isSkuBasedEngraving: true };
    component.cartItemId = null;
    component.loadEngraving();
    expect(component.loadEngraving).toHaveBeenCalled();
  });

  it('should call loadEngraving method when categories is not available', () => {
    spyOn(component, 'loadEngraving').and.callThrough();
    spyOn(component, 'getItemAdditionalInfo').and.callFake(() => {});
    spyOn(component['sharedService'], 'getProductBackImage').and.callFake(() => {});
    component.user.locale = 'en_TH';
    component.detailData = {
      name: 'all-accessories-mice-keyboards',
      categories: [{
        templateType: 'CONFIGURABLE',
      }]
    };
    component.productLevels = {
      category: { slug: 'all-accessories-mice-keyboards', name: 'Apple Penicl' },
      subcat: { slug: 'all-accessories-mice-keyboards', name: 'Apple Pencil' }
    };
    component.engraveData = { preview: true, isSkuBasedEngraving: true };
    component.cartItemId = null;
    component.loadEngraving();
    component.user.locale = 'en_US';
    expect(component.loadEngraving).toHaveBeenCalled();
  });

  it('should call getItemAdditionalInfo method with detailData', () => {
    spyOn(component, 'getItemAdditionalInfo').and.callThrough();
    const data = {
      offers: [
        { appleSku: 'MXG22LL/A' }
      ],
      upc: 190199455498
    };
    component.getItemAdditionalInfo('init', null);
    component.getItemAdditionalInfo('', data);
    expect(component.getItemAdditionalInfo).toHaveBeenCalled();
  });

  it('should call cancel - to close the Engrave modal', () => {
    component.isEdit = false;
    component.qualifyingProduct = relatedProductDetail;
    expect(component.cancel()).toBeFalsy();
  });

  it('should call cancel for Edit scenario - to close the Engrave modal', () => {
    component.isEdit = true;
    expect(component.cancel()).toBeFalsy();
  });

  it('should call imagePreviewLoad', () => {
    spyOn(component, 'imagePreviewLoad').and.callThrough();
    component.imagePreviewLoad();
    expect(component.imagePreviewLoad).toHaveBeenCalled();
  });

  it('should call initEngrave for Edit Engrave modal', () => {
    spyOn(component, 'initEngrave').and.callThrough();
    component.isEdit = true;
    component.initEngrave();
    expect(component.initEngrave).toHaveBeenCalled();
  });

  it('should call updateErrorMessage for Edit Engrave modal', waitForAsync(() => {
    component.errorCount = 2;
    fixture.detectChanges();
    spyOn(component, 'updateErrorMessage').and.callThrough();
    component.updateErrorMessage();
    expect(component.updateErrorMessage).toHaveBeenCalled();
  }));

  it('should call getProducts service api - loadEngravableGiftItem', inject([SharedService], (sharedService) => {
    spyOn(component, 'initEngrave');
    spyOn(sharedService, 'getProducts').and.returnValue(of(relatedProductDetail));
    component.psIdSlug = '30001MXG22LL/A';
    component.loadEngravableGiftItem();
    expect(component.isGiftPromo ).toBeTruthy();
    expect(component.initEngrave).toHaveBeenCalled();
  }));

  it('should test for 404 error - loadEngravableGiftItem', inject([SharedService, NotificationRibbonService], (sharedService, notificationRibbonService) => {
    const errorResponse = {
      status: 404,
      statusText: 'Not Found'
    };
    spyOn(notificationRibbonService, 'emitChange');
    spyOn(sharedService, 'getProducts').and.returnValue(throwError(errorResponse));
    component.psIdSlug = '30001MXG22LL/A';
    component.loadEngravableGiftItem();
    expect(notificationRibbonService.emitChange).toHaveBeenCalled();
  }));

  it('should not call notificationRibbonService.emitChange when loadEngravableGiftItem is called', inject([SharedService, NotificationRibbonService], (sharedService, notificationRibbonService) => {
    const errorResponse = {
      status: 401,
      statusText: 'Not Found'
    };
    spyOn(notificationRibbonService, 'emitChange');
    spyOn(sharedService, 'getProducts').and.returnValue(throwError(errorResponse));
    component.psIdSlug = '30001MXG22LL/A';
    component.loadEngravableGiftItem();
    expect(notificationRibbonService.emitChange).not.toHaveBeenCalled();
  }));

  it('should test for 401 error - loadEngravableGiftItem', () => {
    spyOn(component, 'loadEngravableGiftItem').and.callThrough();
    component.psIdSlug = '30001MXG22LL/A';
    component.loadEngravableGiftItem();
    const errorResponse = {
      status: 401,
      statusText: 'Not Found'
    };
    spyOn(component['sharedService'], 'getProducts').and.returnValue(throwError(errorResponse));
    expect(component.loadEngravableGiftItem).toHaveBeenCalled();
  });

  it('should call setFocusToInputErrorField', fakeAsync(() => {
    spyOn(component, 'setFocusToInputErrorField').and.callThrough();
    component.emptyFieldError = true;
    fixture.detectChanges();
    component.setFocusToInputErrorField();
    tick(500);
    expect(component.setFocusToInputErrorField).toHaveBeenCalled();
  }));

  it('should call redirectToCartPage when verifyAndNavigateToNextPage is called', () => {
    spyOn(component, 'redirectToCartPage');
    component.isGiftPromo = false;
    component.detailData = {
      addOns: {
        availableGiftItems: [],
      }
    };
    component.verifyAndNavigateToNextPage();
    expect(component.redirectToCartPage).toHaveBeenCalled();
    component.detailData = {
      addOns: {}
    };
    component.verifyAndNavigateToNextPage();
    expect(component.redirectToCartPage).toHaveBeenCalled();
  });

  it('should call cartService.updateGiftItemModifyCart when cancel is called', inject([CartService], (cartService) => {
    spyOn(cartService, 'updateGiftItemModifyCart');
    component.isEdit = false;
    component.isGiftPromo = true;
    component.updateCart = true;
    component.cancel();
    expect(cartService.updateGiftItemModifyCart).toHaveBeenCalled();
  }));

  it('should call sharedService.setUpdatedCartItem when redirectToCartPage is called', inject([SharedService], (sharedService) => {
    spyOn(sharedService, 'setUpdatedCartItem');
    component.isGiftPromo = false;
    component.updateCart = true;
    component.qualifyingProduct = {
      hasRelatedProduct: false,
    };
    component.redirectToCartPage();
    expect(sharedService.setUpdatedCartItem).toHaveBeenCalled();
  }));

});
