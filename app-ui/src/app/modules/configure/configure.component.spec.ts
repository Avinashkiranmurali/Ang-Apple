import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, ActivationEnd, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { PageTitleComponent } from '@app/modules/shared/page-title/page-title.component';
import { PricingTempComponent } from '@app/modules/pricing/pricing-temp/pricing-temp.component';
import { ProductInformationComponent } from '@app/modules/shared/product-information/product-information.component';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';
import { SafePipe } from '@app/pipes/safe.pipe';
import { SharedService } from '@app/modules/shared/shared.service';
import { NavStoreService } from '@app/state/nav-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateModule } from '@ngx-translate/core';
import { of, ReplaySubject } from 'rxjs';
import { ConfigureComponent } from './configure.component';
import { ConfigOptionDirective } from '@app/directives/config-option.directive';
import { TemplateService } from '@app/services/template.service';
import { NgbActiveModal, NgbCollapseModule } from '@ng-bootstrap/ng-bootstrap';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ProductService } from '@app/services/product.service';
import { GiftPromoService } from '@app/services/gift-promo.service';
import { ProductsWithConfiguration } from '@app/models/products-with-configuration';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { AplImgSizePipe } from '@app/pipes/apl-img-size.pipe';

describe('ConfigureComponent', () => {
  let component: ConfigureComponent;
  let fixture: ComponentFixture<ConfigureComponent>;
  let httpTestingController: HttpTestingController;
  let templateService: TemplateService;
  let sharedService: SharedService;
  let productService: ProductService;
  let mainNavStore: NavStoreService;
  const mainNav = require('assets/mock/categories.json');
  const programData = require('assets/mock/program.json');
  programData['carouselPages'] = ['pcp'];
  const productItems = require('assets/mock/products-ipad.json');
  const template = require('assets/mock/configData.json');
  const userMock = require('assets/mock/user.json');
  userMock['program'] = programData;
  const userData = {
    user: userMock,
    program: programData,
    config: programData['config'],
    get: () => of(userMock)
  };
  const eventSubject = new ReplaySubject<ActivationEnd>(1);
  const mockRouter = {
    events: eventSubject.asObservable(),
    url: '/testUrl',
    navigate: jasmine.createSpy('navigate')
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        ConfigureComponent,
        PageTitleComponent,
        PricingTempComponent,
        ProductInformationComponent,
        SafePipe,
        ConfigOptionDirective
      ],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        FormsModule,
        BrowserAnimationsModule,
        NgbCollapseModule
      ],
      providers: [
        NgbActiveModal,
        ParsePsidPipe,
        CurrencyPipe,
        SafePipe,
        ConfigOptionDirective,
        TemplateService,
        { provide: UserStoreService, useValue: userData },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({
              category: 'accessories',
              subcat: 'all-accessories',
              sku: '30001MYN92VC/A'
            }),
            data: of({}),
            snapshot: {
              data: {
                analyticsObj: {
                  pgName: '',
                  pgType: '',
                  pgSectionType: ''
                }
              }
            }
          }
        },
        { provide: Router, useValue: mockRouter },
        { provide: CurrencyFormatPipe },
        { provide: DecimalPipe },
        AplImgSizePipe
      ]
    })
    .compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
    sharedService = TestBed.inject(SharedService);
    templateService = TestBed.inject(TemplateService);
    productService = TestBed.inject(ProductService);
    templateService.template = template['configData'];
    mainNavStore = TestBed.inject(NavStoreService);
    mainNavStore.addMainNav(mainNav);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigureComponent);
    component = fixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
    component.config.imageServerUrl = 'https://als-static.bridge2rewards.com/dev';
    component.mainNav = mainNav;
    component.details = require('assets/mock/product-detail.json');
    component.configProducts = sharedService.transformProducts(productItems['products']);
   /* component.userProductsFilterBySelections = component.configProducts.filter(value => {
      return component.optionsContain(value.options, component.selectedConfigItem);
    });*/
    component.selectedGfOption = {
      item: null
    };
    component.tempOptionsList = [];
    component.itemOptions = sharedService.transformOptions(productItems['optionsConfigurationData'], 'ipad', true);
    const optionList = component.itemOptions;
    const primaryOptValData = optionList[0].optionData;
    const primaryValIfo = [];
    for (const primaryOptVal of primaryOptValData) {
      const primaryVal = primaryOptVal.key;
      primaryValIfo[primaryVal] = component.filterProducts(component.configProducts, primaryVal);
    }
    component.filteredByPrimary = primaryValIfo;
    component.itemToCart = require('assets/mock/cart.json')['cartItems'];
    component.displayImage = 'https://als-static.bridge2rewards.com/dev2/apple-gr/assets/img/customizable/ipads.jpg';
    fixture.detectChanges();
  });

  afterEach(() => {
    TestBed.resetTestingModule();
  });

  it('should create non-giftcard items', () => {
    component.details = null;
    fixture.detectChanges();
    // Fake response data
    const fakeResponse = require('assets/mock/products-ipad.json');
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/productsWithConfiguration' + '/?categorySlug=all-accessories');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    // Respond with the fake data when called
    req.flush(fakeResponse);
    expect(component).toBeTruthy();
  });

  it('should create getProducts -> Failure 404 Not found', () => {
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/productsWithConfiguration' + '/?categorySlug=all-accessories');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    // Respond with mock error
    req.flush('Failure', { status: 404, statusText: 'Not Found' });
    expect(component).toBeTruthy();
  });

  it('should create getProducts -> Failure 401 Not found', () => {
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/productsWithConfiguration' + '/?categorySlug=all-accessories');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    // Respond with mock error
    req.flush('Failure', { status: 401, statusText: 'API Failed' });
    expect(component).toBeTruthy();
  });

  it('should call productPreSelection method', () => {
    spyOn(component, 'productPreSelection').and.callThrough();
    component.productPreSelection(component.configProducts[0]);
    expect(component.productPreSelection).toHaveBeenCalled();
  });

  it('should call getProductDetail method', waitForAsync(() => {
    spyOn(component, 'getProductDetail').and.callThrough();
    component.getProductDetail('', '/' + component.details.psid);
    expect(component.getProductDetail).toHaveBeenCalled();
    // Fake response data
    const mockResponse = require('assets/mock/products-ipad.json');
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/productsWithConfiguration/?categorySlug=all-accessories');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    // Respond with the fake data when called
    req.flush(mockResponse);
    // Fake response data
    const fakeResponse = require('assets/mock/product-detail.json');
    spyOn(component['detailService'], 'getDetails').and.returnValue(of(fakeResponse));
    // Run some expectations
    expect(fakeResponse.psid).toBe('30001MXG22LL/A');
    expect(component.getProductDetail).toHaveBeenCalled();
  }));

  it('should call getProductDetail method - 401 Failure scenario', waitForAsync(() => {
    spyOn(component, 'getProductDetail').and.callThrough();
    component.getProductDetail('', '/' + component.details.psid);
    expect(component.getProductDetail).toHaveBeenCalled();
    // Fake response data
    const mockResponse = require('assets/mock/products-ipad.json');
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/productsWithConfiguration/?categorySlug=all-accessories');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    // Respond with the fake data when called
    req.flush(mockResponse);
    // Expect a call to this URL
    const productReq = httpTestingController.expectOne('/apple-gr/service/products/30001MXG22LL/A?withVariations=false');
    // Assert that the request is a GET
    expect(productReq.request.method).toEqual('GET');
    // Respond with mock error
    productReq.flush('Failure', { status: 401, statusText: 'Not Found' });
    expect(component.getProductDetail).toHaveBeenCalled();
  }));

  it('should call getProductDetail method - 0 Failure scenario', waitForAsync(() => {
    spyOn(component, 'getProductDetail').and.callThrough();
    component.getProductDetail('init', '/' + component.details.psid);
    expect(component.getProductDetail).toHaveBeenCalled();
    // Fake response data
    const mockResponse = require('assets/mock/products-ipad.json');
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/productsWithConfiguration/?categorySlug=all-accessories');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    // Respond with the fake data when called
    req.flush(mockResponse);
    // Expect a call to this URL
    const productReq = httpTestingController.expectOne('/apple-gr/service/products/30001MXG22LL/A');
    // Assert that the request is a GET
    expect(productReq.request.method).toEqual('GET');
    // Respond with mock error
    productReq.flush('Failure', { status: 0, statusText: 'Not Found' });
    expect(component.getProductDetail).toHaveBeenCalled();
  }));

  it('should call getProductDetail method - 500 Failure scenario', waitForAsync(() => {
    spyOn(component, 'getProductDetail').and.callThrough();
    component.getProductDetail('', '/' + component.details.psid);
    expect(component.getProductDetail).toHaveBeenCalled();
    // Fake response data
    const mockResponse = require('assets/mock/products-ipad.json');
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/productsWithConfiguration/?categorySlug=all-accessories');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    // Respond with the fake data when called
    req.flush(mockResponse);
    // Expect a call to this URL
    const productReq = httpTestingController.expectOne('/apple-gr/service/products/30001MXG22LL/A?withVariations=false');
    // Assert that the request is a GET
    expect(productReq.request.method).toEqual('GET');
    // Respond with mock error
    productReq.flush('Failure', { status: 500, statusText: 'Server error' });
    expect(component.getProductDetail).toHaveBeenCalled();
  }));

  it('should call addToCart method', () => {
    spyOn(component, 'addToCart').and.callThrough();
    component.addToCart('30001MXG22LL/A', component.details);
    expect(component.addToCart).toHaveBeenCalled();
  });

  it('should call isProductExist method', () => {
    spyOn(component, 'isProductExist').and.callThrough();
    expect(component.isProductExist(component.configProducts, '30001MXG22LL/A')).toBeFalsy();
    expect(component.isProductExist).toHaveBeenCalled();
  });

  it('should call productPreSelection method', () => {
    fixture.detectChanges();
    spyOn(component, 'productPreSelection').and.callThrough();
    component.productPreSelection(component.details);
    expect(component.productPreSelection).toHaveBeenCalled();
  });

  it('should call configSummaryText method', () => {
    spyOn(component, 'configSummaryText').and.callThrough();
    const selectedConfigItem = {
      color: 'Space Gray',
      communication: 'Wi-Fi + Cellular',
      storage: '1 TB'
    };
    component.configSummaryText(selectedConfigItem);

    const configItem = {
      color: 'Space Gray',
      communication: 'Wi-Fi + Cellular',
      storage: '1 TB',
      model: 'Accessories'
    };
    component.configSummaryText(configItem);
    expect(component.configSummaryText).toHaveBeenCalled();
  });

  it('should call filterProductsFromUserSelections method - selectedoption is undefined', () => {
    component.selectedConfigItem = {
      color: 'Space Gray',
      communication: 'Wi-Fi + Cellular',
      storage: '1 TB'
    };
    component.selectedGfOption = {
      item: {
        name: 'iphone 12',
        psid: '123456',
        images: null,
        options: [],
        offers: []
      }
    };
    fixture.detectChanges();
    spyOn(component, 'filterProductsFromUserSelections').and.callThrough();
    component.filterProductsFromUserSelections(0, null, '1 TB', null, false);

    fixture.detectChanges();
    component.filterProductsFromUserSelections(0, null, '1 TB', null, false);
    expect(component.filterProductsFromUserSelections).toHaveBeenCalled();
  });

  it('should call returnSelectedProduct method', () => {
    spyOn(component, 'returnSelectedProduct').and.callThrough();
    component.selectedGfOption = {
      item: {
        name: '12.9-inch iPad Pro Wi‑Fi + Cellular 1TB - Space Gray',
        psid: '30001MXG22LL/A',
        images: null,
        options: [{
          name: 'model',
          value: '12.9-inch',
          key: '12_9inch',
          i18Name: 'Model',
          orderBy: 0,
          points: null,
          swatchImageUrl: null
        },
        {
          name: 'color',
          value: 'Space Gray',
          key: 'space_gray',
          i18Name: 'Color',
          orderBy: 0,
          points: null,
          swatchImageUrl: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-pro-12-select-cell-spacegray-202003_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1583552336142&qlt=95'
        },
        {
          name: 'communication',
          value: 'Wi-Fi + Cellular',
          key: 'nocarrier',
          i18Name: 'Communication',
          orderBy: 0,
          points: null,
          swatchImageUrl: null
        }],
        offers: []
      }
    };
    fixture.detectChanges();
    component.returnSelectedProduct(component.details, 'ipad', { color: 'Space Gray' });
    expect(component.returnSelectedProduct).toHaveBeenCalled();
    // else check
    const itemOptions = sharedService.transformOptions(productItems['optionsConfigurationData'], 'ipad', true);
    component.itemOptions = [itemOptions[0]];
    fixture.detectChanges();
    component.returnSelectedProduct(component.details, 'ipad', {color: 'Space Gray'});
    expect(component.returnSelectedProduct).toHaveBeenCalled();
  });

  it('should call filterProductsFromUserSelections', () => {
    spyOn(component, 'filterProductsFromUserSelections').and.callThrough();
    spyOn(component, 'filterProductsFromDropdown').and.callThrough();
    component.optionsLength = 4;
    component.userProductsFilterBySelections = component.filterProducts(component.configProducts, 'silver');
    component.configProducts = productItems['products'];
    component.tempOptionsList = component.filterProducts(component.configProducts, 'silver');
    component.selectedGfOption = {
      item: {
        name: '10.2-inch iPad Wi-Fi + Cellular 128GB - Silver',
        psid: '30001MYN82VC/A',
        options: [
          {
            name: 'storage',
            value: '128GB',
            key: '128gb',
            i18Name: 'Storage',
            orderBy: 0,
            points: null,
            swatchImageUrl: null
          },
          {
            name: 'color',
            value: 'silver',
            key: 'silver',
            i18Name: 'Color',
            orderBy: 0,
            points: null,
            swatchImageUrl: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-2020-hero-silver-cell-select_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1598915066000&qlt=95'
          }
        ],
        offers: [],
        images: {
          thumbnail: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-2020-hero-silver-cell-select?wid=1200&hei=1200&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1598915066000&wid=30&hei=30',
          small: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-2020-hero-silver-cell-select?wid=1200&hei=1200&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1598915066000&wid=75&hei=75',
          medium: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-2020-hero-silver-cell-select?wid=1200&hei=1200&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1598915066000&wid=150&hei=150',
          large: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-2020-hero-silver-cell-select?wid=1200&hei=1200&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1598915066000'
        }
      }
    };
    component.selectedConfigItem = {
      color: 'Silver'
    };
    component.selectedGfOption = {
      item: {
        name: 'iphone 12',
        psid: '123456',
        images: null,
        options: [],
        offers: []
      }
    };
    fixture.detectChanges();
    component.filterProductsFromDropdown(0, 'silver');
    expect(component.filterProductsFromDropdown).toHaveBeenCalled();
    component.filterProductsFromUserSelections(0, null, null, null, true);
    expect(component.filterProductsFromUserSelections).toHaveBeenCalled();
  });

  it('should call validateSetOptions method for silver color - else if check', () => {
    component.configProducts = productItems['products'];
    const option2 = component.filterProducts(component.configProducts, '128gb');
    option2.splice(0, 3);
    component.itemOptions[2]['disabled'] = false;
    component.itemOptions[1]['disabled'] = false;
    expect(component.displayImage).toBeDefined();
  });

  it('should call getItemAdditionalInfo method', () => {
    spyOn(component, 'getItemAdditionalInfo').and.callThrough();
    const detail = require('assets/mock/product-detail.json');
    detail.offers[0].appleSku = 'MXG22LL/A';
    component.getItemAdditionalInfo('', detail);

    const data = require('assets/mock/product-detail.json');
    data.offers[0].appleSku = '';
    component.getItemAdditionalInfo('', data);
    expect(component.getItemAdditionalInfo).toHaveBeenCalled();
  });

  it('should call getProductImage method', () => {
    spyOn(component, 'getProductImage').and.callThrough();
    const mockValue = require('assets/mock/product-detail.json');
    mockValue.images.large = 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-pro-12-select-cell-spacegray-202003';
    component.getProductImage(mockValue);
    expect(component.getProductImage).toHaveBeenCalled();
  });

  it('should call detailPageData method', () => {
    spyOn(component, 'detailPageData').and.callThrough();
    component.detailPageData();
    expect(component.detailPageData).toHaveBeenCalled();
  });

  it('should call getProducts method - null object as response', () => {
    spyOn(component, 'getProducts').and.callThrough();
    spyOn(productService, 'getProductsWopts').and.returnValue(of({}));
    component.getProducts();
    expect(component.getProducts).toHaveBeenCalled();
  });

  it('should call getProducts method - no data exists', () => {
    spyOn(component, 'getProducts').and.callThrough();
    const response: ProductsWithConfiguration = {
      products: productItems['products'],
      optionsConfigurationData: productItems['optionsConfigurationData']
    };
    spyOn(productService, 'getProductsWopts').and.returnValue(of(response));
    component.getProducts();
    expect(component.getProducts).toHaveBeenCalled();
  });

  it('should call getItemAdditionalInfo - for else check', () => {
    spyOn(component, 'getItemAdditionalInfo').and.callThrough();
    spyOn(component, 'productConfigurationData').and.callFake(() => false);
    component.getItemAdditionalInfo('init', null);
    expect(component.getItemAdditionalInfo).toHaveBeenCalled();
  });

  it('should call getProductDetail method - with pricing modal', () => {
    const fakeData = require('assets/mock/product-detail.json');
    fakeData['additionalInfo'] = {
      PricingModel: { paymentValue: 200, repaymentTerm: 3 }
    };
    spyOn(component['detailService'], 'getDetails').and.returnValue(of(fakeData));
    spyOn(component, 'getProductDetail').and.callThrough();
    component.getProductDetail('', '/' + component.details.psid);
    expect(component.getProductDetail).toHaveBeenCalled();
  });

  it('should call getProductDetail method - without pricing modal', () => {
    const fakeData = require('assets/mock/product-detail.json');
    fakeData['additionalInfo'] = {
      PricingModel: null
    };
    spyOn(component['detailService'], 'getDetails').and.returnValue(of(fakeData));
    spyOn(component, 'getProductDetail').and.callThrough();
    component.getProductDetail('', '/' + component.details.psid);
    expect(component.getProductDetail).toHaveBeenCalled();
  });

  it('should call findMinPriceProduct method - without displayPrice modal', () => {
    const product1 = require('assets/mock/product-detail.json');
    const product2 = { offers: [ { displayPrice: null }]};
    spyOn(component, 'findMinPriceProduct').and.callThrough();
    component.findMinPriceProduct(product1, product2);
    expect(component.findMinPriceProduct).toHaveBeenCalled();
  });

  it('should call filterProductsFromUserSelections method', () => {
    component.configProducts = productItems['products'];
    component.userProductsFilterBySelections = component.filterProducts(component.configProducts, 'silver');
    component.tempOptionsList = component.filterProducts(component.configProducts, 'silver');
    component.selectedConfigItem = {
      color: 'Silver'
    };
    component.selectedGfOption = { item: null };
    component.optionsLength = 4;
    fixture.detectChanges();
    spyOn(component, 'filterProductsFromUserSelections').and.callThrough();
    component.filterProductsFromUserSelections(0, null, null, null, true);
    expect(component.filterProductsFromUserSelections).toHaveBeenCalled();
  });

  it('should call detailPageData method', () => {
    spyOn(component, 'detailPageData').and.callThrough();
    expect(component.detailPageData()).toBeDefined();
    expect(component.detailPageData).toHaveBeenCalled();
  });

});
