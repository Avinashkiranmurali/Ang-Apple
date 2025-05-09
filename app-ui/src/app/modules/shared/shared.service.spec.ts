import { TestBed, waitForAsync } from '@angular/core/testing';
import { SharedService } from './shared.service';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { UserStoreService } from '@app/state/user-store.service';
import { Subject } from 'rxjs';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { DecimalPipe, ViewportScroller } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

describe('SharedService', () => {
  let sharedService: SharedService;
  let httpTestingController: HttpTestingController;
  const mockProductResponse = require('assets/mock/products-ipad.json');
  const programData = require('assets/mock/program.json');
  programData['config']['carouselPages'] = ['pdp', 'bag'];
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: Router, useValue: {
            navigate: jasmine.createSpy('navigate')
          }
        },
        {provide: UserStoreService, useValue: userData},
        { provide: CurrencyFormatPipe },
        { provide: CurrencyPipe },
        { provide: DecimalPipe }
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot()
      ]
    });
    httpTestingController = TestBed.inject(HttpTestingController);
    sharedService = TestBed.inject(SharedService);
    sharedService.triggerLogoutSyncEvent$ = new Subject();
    sharedService.messages = require('assets/mock/messages.json');
    sharedService.program = programData;
    sharedService.config = userData.config;
    const fakeSessionUrl = require('assets/mock/validSession.json');
    sessionStorage.setItem('sessionURLs', JSON.stringify(fakeSessionUrl));
  });

  afterAll(() => {
    sharedService.userStore.program = userData.program;
  });

  it('should be created', () => {
    expect(sharedService).toBeTruthy();
  });

  it('should call parseSku method', () => {
    spyOn(sharedService, 'parseSku').and.callThrough();
    const prodItem = require('assets/mock/product-detail.json');
    sharedService.parseSku(prodItem);
    expect(sharedService.parseSku).toHaveBeenCalled();
  });

  it('should call setProperty method', () => {
    spyOn(sharedService, 'setProperty').and.callThrough();
    sharedService.setProperty('catname', 'Mac');
    sharedService.setProperty('subcatname', 'iMac');
    sharedService.setProperty('addcatname', '');
    sharedService.setProperty('detailsname', 'Mac');
    sharedService.setProperty('', 'Mac');
    expect(sharedService.setProperty).toHaveBeenCalled();
  });

  it('should call getProperty method', () => {
    spyOn(sharedService, 'getProperty').and.callThrough();
    sharedService.getProperty('catname');
    expect(sharedService.getProperty).toHaveBeenCalled();
  });

  it('should call getProperty value for null method', () => {
    spyOn(sharedService, 'getProperty').and.callThrough();
    sharedService.getProperty('');
    expect(sharedService.getProperty).toHaveBeenCalled();
  });

  it('should call getOptionsDisplay method and return false', () => {
    spyOn(sharedService, 'getOptionsDisplay').and.callThrough();
    const prodConfig = require('assets/mock/configData.json');
    sharedService.getOptionsDisplay(prodConfig['configData']['productConfiguration'], 'mac', 'macbook');
    expect(sharedService.getOptionsDisplay).toHaveBeenCalled();
  });

  it('should call getOptionsDisplay method and return true', () => {
    spyOn(sharedService, 'getOptionsDisplay').and.callThrough();
    const prodConfig = require('assets/mock/configData.json');
    sharedService.getOptionsDisplay(prodConfig['configData']['productConfiguration'], 'mac', 'imac');
    expect(sharedService.getOptionsDisplay).toHaveBeenCalled();
  });

  it('should call getOptionsDisplay method for non property value', () => {
    spyOn(sharedService, 'getOptionsDisplay').and.callThrough();
    sharedService.getOptionsDisplay('', 'mac', 'macbook');
    expect(sharedService.getOptionsDisplay).toHaveBeenCalled();
  });

  it('should call getOptionsDisplay method for non property value', () => {
    spyOn(sharedService, 'getOptionsDisplay').and.callThrough();
    const prodConfig = require('assets/mock/configData.json');
    sharedService.getOptionsDisplay(prodConfig['configData']['productConfiguration'], 'ipad', '');
    expect(sharedService.getOptionsDisplay).toHaveBeenCalled();
  });

  it('should return Products', waitForAsync(() => {
    // Setup a request using the mockProductResponse data
    sharedService.getProducts('WithConfiguration/?categorySlug=ipads').subscribe(
      (products) => expect(products).toEqual(mockProductResponse, 'should return mockProductResponse'), fail
    );

    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/products' + 'WithConfiguration/?categorySlug=ipads');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(mockProductResponse);

    // Run some expectations
    expect(mockProductResponse.products.length).toBe(12);
    expect(mockProductResponse.products[0].psid).toBe('30001MYN92VC/A');
    expect(mockProductResponse.products[0].brand).toBe('Apple®');
    expect(mockProductResponse.products[0].categories[0].slug).toBe('ipads');
  }));

  it('should return no products', waitForAsync(() => {
    const noProductResponse = [];
    // Setup a request using the mockProductResponse data
    sharedService.getProducts('WithConfiguration/?categorySlug=ipads').subscribe(
      (products) => expect(products).toEqual(noProductResponse, 'should return mockProductResponse'), fail
    );

    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/products' + 'WithConfiguration/?categorySlug=ipads');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(noProductResponse);

    // Run some expectations
    expect(noProductResponse.length).toBe(0);
  }));

  it('should test for 404 error', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    sharedService.getProducts('WithConfiguration/?categorySlug=ipads').subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      });
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/products' + 'WithConfiguration/?categorySlug=ipads');

    // Respond with mock error
    req.flush(errorMsg, {status: 404, statusText: 'Not Found'});

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
  }));

  it('should call transformProducts methods', () => {
    spyOn(sharedService, 'transformProducts').and.callThrough();
    sharedService.transformProducts(mockProductResponse['products']);
    expect(sharedService.transformProducts).toHaveBeenCalled();
  });

  it('should call transformOptions methods', () => {
    spyOn(sharedService, 'transformOptions').and.callThrough();
    sharedService.transformOptions(mockProductResponse['optionsConfigurationData'], '', true);
    expect(sharedService.transformOptions).toHaveBeenCalled();
  });

  it('should call transformOptions methods - only one option present and else check', () => {
    spyOn(sharedService, 'transformOptions').and.callThrough();
    const data = {};
    data['storage'] = [
      {
        name: 'storage',
        value: '32GB',
        key: '32gb',
        i18Name: '',
        orderBy: 0,
        points: null,
        swatchImageUrl: null
      }
    ];
    sharedService.transformOptions(data, '', true);
    expect(sharedService.transformOptions).toHaveBeenCalled();
  });

  it('should call transformOptions methods - for watch caseColor/bandSize', () => {
    spyOn(sharedService, 'transformOptions').and.callThrough();
    const data = {};
    data['caseColor'] = [
      {
        name: 'caseColor',
        value: 'Black',
        key: 'black',
        i18Name: '',
        orderBy: 0,
        points: null,
        swatchImageUrl: null
      },
      {
        name: 'caseColor',
        value: 'Ginger',
        key: 'ginger',
        i18Name: '',
        orderBy: 0,
        points: null,
        swatchImageUrl: null
      }
    ];
    sharedService.transformOptions(data, '', false);
    expect(sharedService.transformOptions).toHaveBeenCalled();
  });

  it('should call psidSlugConvert methods', () => {
    spyOn(sharedService, 'psidSlugConvert').and.callThrough();
    sharedService.psidSlugConvert('30001MYN52VC/A');
    expect(sharedService.psidSlugConvert).toHaveBeenCalled();
  });

  it('should call constructRouterUrl methods', () => {
    spyOn(sharedService, 'constructRouterUrl').and.callThrough();
    const products = require('assets/mock/cart.json')['cartItems'];
    sharedService.constructRouterUrl(products[0]['productDetail']);
    expect(sharedService.constructRouterUrl).toHaveBeenCalled();
  });

  it('should call constructRouterUrl methods for configurable', () => {
    spyOn(sharedService, 'constructRouterUrl').and.callThrough();
    const productDetail = require('assets/mock/product-detail.json');
    sharedService.constructRouterUrl(productDetail);
    expect(sharedService.constructRouterUrl).toHaveBeenCalled();
  });

  it('should call constructRouterUrl methods for addCart value', () => {
    spyOn(sharedService, 'constructRouterUrl').and.callThrough();
    const products = require('assets/mock/cart.json');
    const data = {
      imageUrl: '',
      i18nName: 'Accessories',
      slug: 'accessories',
      name: 'Accessories',
      templateType: '',
      defaultImage: '',
      summaryIconImage: '',
      displayOrder: 7,
      engraveBgImageLocation: null,
      subCategories: [],
      parents: [],
      products: [],
      images: {},
      psid: null,
      depth: 0,
      new: false,
      configurable: false,
      active: true
    };
    products['cartItems'][0]['productDetail']['categories'][0]['parents'][0]['parents'].push(data);
    sharedService.constructRouterUrl(products['cartItems'][0]['productDetail']);
    expect(sharedService.constructRouterUrl).toHaveBeenCalled();
  });

  it('should call verifySkipPaymentOption methods and return false', waitForAsync(() => {
    spyOn(sharedService, 'verifySkipPaymentOption').and.callThrough();
    const cartData = require('assets/mock/cart.json');
    const program = require('assets/mock/program.json');
    program['redemptionOptions'] = {};
    sharedService.userStore.program = program;
    sharedService.userStore.config = program.config;
    sharedService.userStore.config.paymentTemplate = '';
    cartData.cost = 0;
    sharedService.verifySkipPaymentOption(cartData);
    expect(sharedService.verifySkipPaymentOption).toHaveBeenCalled();
  }));

  it('should call verifySkipPaymentOption method with redemptions - pointsonly', waitForAsync(() => {
    spyOn(sharedService, 'verifySkipPaymentOption').and.callThrough();
    const cartData = require('assets/mock/cart.json');
    const programDataMock = require('assets/mock/program.json');
    const data = {
      pointsonly: [
        {
          id: 1751,
          varId: 'Delta',
          programId: 'b2s_qa_only',
          paymentOption: 'pointsonly',
          limitType: 'percentage',
          paymentMinLimit: 50,
          paymentMaxLimit: 0,
          orderBy: 1,
          paymentProvider: null,
          lastUpdatedBy: 'Appl_user',
          lastUpdatedDate: 1526570221080,
          active: true
        }
      ]
    };
    programDataMock['redemptionOptions'] = data;
    sharedService.userStore.program = programDataMock;
    sharedService.verifySkipPaymentOption(cartData);
    expect(sharedService.verifySkipPaymentOption).toHaveBeenCalled();

    const mockCartData = require('assets/mock/cart.json');
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service//cart/483734');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('PUT');

    // Respond with the fake data when called
    req.flush(mockCartData);
  }));

  it('should call verifySkipPaymentOption method with redemptions - cashonly', waitForAsync(() => {
    spyOn(sharedService, 'verifySkipPaymentOption').and.callThrough();
    const cartMockData = {
      cost: 0,
      id: 483734
    };
    const programDataMock = require('assets/mock/program.json');
    const data = {
      cashonly: [
        {
          id: 1751,
          varId: 'Delta',
          programId: 'b2s_qa_only',
          paymentOption: 'cashonly',
          limitType: 'percentage',
          paymentMinLimit: 50,
          paymentMaxLimit: 0,
          orderBy: 1,
          paymentProvider: null,
          lastUpdatedBy: 'Appl_user',
          lastUpdatedDate: 1526570221080,
          active: true
        }
      ]
    };
    programDataMock['redemptionOptions'] = data;
    sharedService.userStore.program = programDataMock;
    sharedService.verifySkipPaymentOption(cartMockData);
    expect(sharedService.verifySkipPaymentOption).toHaveBeenCalled();
  }));

  it('should call verifySkipPaymentOption method with redemptions - with cash', waitForAsync(() => {
    spyOn(sharedService, 'verifySkipPaymentOption').and.callThrough();
    const cartMockData = {
      cost: 23,
      id: 483734
    };
    const programDataMock = require('assets/mock/program.json');
    const data = {
      cashonly: [
        {
          id: 1751,
          varId: 'Delta',
          programId: 'b2s_qa_only',
          paymentOption: 'cashonly',
          limitType: 'percentage',
          paymentMinLimit: 50,
          paymentMaxLimit: 0,
          orderBy: 1,
          paymentProvider: null,
          lastUpdatedBy: 'Appl_user',
          lastUpdatedDate: 1526570221080,
          active: true
        }
      ]
    };
    programDataMock['redemptionOptions'] = data;
    sharedService.userStore.program = programDataMock;
    sharedService.verifySkipPaymentOption(cartMockData);
    expect(sharedService.verifySkipPaymentOption).toHaveBeenCalled();
  }));

  it('should call sessionTypeAction method - navigateToStore', () => {
    spyOn(sharedService, 'sessionTypeAction').and.callThrough();
    sharedService.sessionTypeAction('navigateToStore');
    expect(sharedService.sessionTypeAction).toHaveBeenCalled();
  });

  it('should call sessionTypeAction method - navigateTo', () => {
    spyOn(sharedService, 'sessionTypeAction').and.callThrough();
    spyOn(sharedService, 'navBackAction').and.callFake(() => {});
    sharedService.sessionTypeAction('navigateTo');
    expect(sharedService.sessionTypeAction).toHaveBeenCalled();
  });

  it('should call signOutInit method - signOutInit case', () => {
    spyOn(sharedService, 'signOutInit').and.callThrough();
    sharedService.signOutInit();
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/signOut');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush('OK');
    expect(sharedService.signOutInit).toHaveBeenCalled();
  });

  it('should test for 404 error - signOutInit', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    spyOn(sharedService, 'signOutInit').and.callThrough();
    sharedService.signOutInit();
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/signOut');

    // Respond with mock error
    req.flush(errorMsg, {status: 404, statusText: 'Not Found'});

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    expect(sharedService.signOutInit).toHaveBeenCalled();
  }));

  it('should call sessionTypeAction method - navigateBackUrl', () => {
    const storedUrls = {
      navigateBackUrl: 'javascript:void(0)',
      signOutUrl: 'javascript:void(0)',
      timeOutUrl: 'javascript:void(0)',
      homeLinkUrl: 'javascript:void(0)'
    };
    sessionStorage.setItem('sessionURLs', JSON.stringify(storedUrls));
    spyOn(sharedService, 'sessionTypeAction').and.callThrough();
    sharedService.sessionTypeAction('navigateBackUrl');
    expect(sharedService.sessionTypeAction).toHaveBeenCalled();
  });

  it('should call sessionTypeAction method without navigateBackUrl', () => {
    const storedUrls = {
      navigateBackUrl: '',
      signOutUrl: 'javascript:void(0)',
      timeOutUrl: 'javascript:void(0)',
      homeLinkUrl: 'javascript:void(0)'
    };
    sessionStorage.setItem('sessionURLs', JSON.stringify(storedUrls));
    spyOn(sharedService, 'sessionTypeAction').and.callThrough();
    sharedService.sessionTypeAction('navigateBackUrl');
    expect(sharedService.sessionTypeAction).toHaveBeenCalled();
  });

  it('should call sessionTypeAction method with signinUrl', () => {
    const storedUrls = {
      signOutUrl: 'javascript:void(0)'
    };
    sessionStorage.setItem('sessionURLs', JSON.stringify(storedUrls));
    sharedService.userStore.program.config.signinUrl = 'javascript:void(0)';
    spyOn(sharedService, 'sessionTypeAction').and.callThrough();
    sharedService.sessionTypeAction('signIn');
    expect(sharedService.sessionTypeAction).toHaveBeenCalled();
  });

  it('should call sessionTypeAction method without signinUrl', () => {
    const storedUrls = {
      signOutUrl: 'javascript:void(0)'
    };
    sessionStorage.setItem('sessionURLs', JSON.stringify(storedUrls));
    sharedService.userStore.program.config.signinUrl = null;
    spyOn(sharedService, 'sessionTypeAction').and.callThrough();
    sharedService.sessionTypeAction('signIn');
    expect(sharedService.sessionTypeAction).toHaveBeenCalled();
  });


  it('should call sessionTypeAction method - navigateToHome', () => {
    const storedUrls = {
      homeLinkUrl: 'javascript:void(0)'
    };
    sessionStorage.setItem('sessionURLs', JSON.stringify(storedUrls));
    spyOn(sharedService, 'sessionTypeAction').and.callThrough();
    sharedService.sessionTypeAction('navigateToHome');
    expect(sharedService.sessionTypeAction).toHaveBeenCalled();
  });

  it('should call sessionTypeAction method - signOutPost', () => {
    spyOn(sharedService, 'sessionTypeAction').and.callThrough();
    sharedService.sessionTypeAction('signOutPost');
    expect(sharedService.sessionTypeAction).toHaveBeenCalled();
  });

  it('should call sessionTypeAction method - timeOutUrl', () => {
    const storedUrls = {
      timeOutUrl: 'javascript:void(0)'
    };
    sessionStorage.setItem('sessionURLs', JSON.stringify(storedUrls));
    spyOn(sharedService, 'sessionTypeAction').and.callThrough();
    sharedService.sessionTypeAction('timeOutUrl');
    expect(sharedService.sessionTypeAction).toHaveBeenCalled();
  });

  it('should call sessionTypeAction method - timeOutUrl', () => {
    const storedUrls = {
      timeOutUrl: 'javascript:void(0)'
    };
    sessionStorage.setItem('sessionURLs', JSON.stringify(storedUrls));
    spyOn(sharedService, 'sessionTypeAction').and.callThrough();
    sharedService.sessionTypeAction('timeOutUrl');
    expect(sharedService.sessionTypeAction).toHaveBeenCalled();
  });

  it('should call sessionTypeAction method - signOut', () => {
    const storedUrls = {
      signOutUrl: 'javascript:void(0)'
    };
    sessionStorage.setItem('sessionURLs', JSON.stringify(storedUrls));
    spyOn(sharedService, 'sessionTypeAction').and.callThrough();
    sharedService.sessionTypeAction('signOut');
    expect(sharedService.sessionTypeAction).toHaveBeenCalled();
  });

  it('should call sessionTypeAction method - navigateBack url is not exists', () => {
    const storedUrls = {
      navigateBackUrl: ''
    };
    sessionStorage.setItem('sessionURLs', JSON.stringify(storedUrls));
    spyOn(sharedService, 'sessionTypeAction').and.callThrough();
    sharedService.sessionTypeAction('navigateBack');
    expect(sharedService.sessionTypeAction).toHaveBeenCalled();
  });

  it('should call sessionTypeAction method - navigateBack', () => {
    const storedUrls = {
      navigateBackUrl: 'javascript:void(0)'
    };
    sessionStorage.setItem('sessionURLs', JSON.stringify(storedUrls));
    spyOn(sharedService, 'sessionTypeAction').and.callThrough();
    sharedService.sessionTypeAction('navigateBack');
    expect(sharedService.sessionTypeAction).toHaveBeenCalled();
  });

  it('should call sessionTypeAction method - navigateBack with navFlag entry', waitForAsync(() => {
    sharedService.userStore.user.navflag = 'enable2WayTesting';
    const storedUrls = {
      navigateBackUrl: 'javascript:void(0)'
    };
    sessionStorage.setItem('sessionURLs', JSON.stringify(storedUrls));
    spyOn(sharedService, 'sessionTypeAction').and.callThrough();
    sharedService.sessionTypeAction('navigateBack');
    expect(sharedService.sessionTypeAction).toHaveBeenCalled();
  }));

  it('should call sessionTypeAction method - no navigateBack', waitForAsync(() => {
    const storedUrls = {
      navigateBackUrl: null
    };
    sessionStorage.setItem('sessionURLs', JSON.stringify(storedUrls));
    spyOn(sharedService, 'sessionTypeAction').and.callThrough();
    sharedService.sessionTypeAction('navigateBack');
    expect(sharedService.sessionTypeAction).toHaveBeenCalled();
  }));

  it('should call getProductBackImage method for engraveImageUrl', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    const engraveImageUrl = 'apple-gr/assets/img/customizable/ipad-pro.jpg';
    expect(sharedService.getProductBackImage(mockData, engraveImageUrl, null)).toEqual(engraveImageUrl);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for no sizeVal', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      {
        name: 'model',
        value: '12.9-inch',
        key: '13_1inch',
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
      }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for model 11 inch', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      {
        name: 'model',
        value: '11 inch',
        key: '11inch',
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
      }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for no communicationVal', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      {
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
        key: '',
        i18Name: 'Communication',
        orderBy: 0,
        points: null,
        swatchImageUrl: null
      }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for all available data', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      {
        name: 'model',
        value: '10.5 inch',
        key: '10_5inch',
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
      }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for engraveImageUrl', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    const engraveImageUrl = 'apple-gr/assets/img/customizable/ipad-pro.jpg';
    expect(sharedService.getProductBackImage(mockData, engraveImageUrl, null)).toEqual(engraveImageUrl);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for no sizeVal', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      {
        name: 'model',
        value: '12.9-inch',
        key: '13_1inch',
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
      }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for model 11 inch', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      {
        name: 'model',
        value: '11 inch',
        key: '11inch',
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
      }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for no communicationVal', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      {
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
        key: '',
        i18Name: 'Communication',
        orderBy: 0,
        points: null,
        swatchImageUrl: null
      }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for all available data', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      {
        name: 'model',
        value: '10.5 inch',
        key: '10_5inch',
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
      }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call showSessionTimeOut methods', () => {
    spyOn(sharedService, 'showSessionTimeOut').and.callThrough();
    sharedService.showSessionTimeOut(false);
    expect(sharedService.showSessionTimeOut).toHaveBeenCalled();
  });

  it('should call openEngraveModalDialog methods', () => {
    spyOn(sharedService, 'openEngraveModalDialog').and.callThrough();
    sharedService.openEngraveModalDialog({});
    expect(sharedService.openEngraveModalDialog).toHaveBeenCalled();
  });

  it('should call convertToRemUnit methods', () => {
    spyOn(sharedService, 'convertToRemUnit').and.callThrough();
    sharedService.convertToRemUnit(18);
    expect(sharedService.convertToRemUnit).toHaveBeenCalled();
  });

  it('should call returnZero', () => {
    spyOn(sharedService, 'returnZero').and.callThrough();
    sharedService.returnZero();
    expect(sharedService.returnZero).toHaveBeenCalled();
  });

  it('getPricingTemp to return values based on params', () => {
    expect(sharedService.getPricingTemp('pd_cash', 'full_')).toBe('full_' + 'pd_cash' + '-template.htm');
  });

  it('getPricingTemp to return values based on params', () => {
    expect(sharedService.getPricingTemp('pd_cash', 'dual_')).toBe('dual_' + 'points_and_currency-template.htm');
  });

  it('getPricingTemp to return values based on params', () => {
    expect(sharedService.getPricingTemp('cash_only', 'dual')).toBe('cash-template.htm');
  });

  it('getPricingTemp to return values based on params', () => {
    expect(sharedService.getPricingTemp('cash_only', 'qty_')).toBe('qty_' + 'cash-template.htm');
  });

  it('getPricingTemp to return values based on params', () => {
    expect(sharedService.getPricingTemp('points_only', 'dual')).toBe('rewards-template.htm');
  });

  it('getPricingTemp to return values based on params', () => {
    expect(sharedService.getPricingTemp('points_only', 'qty_')).toBe('qty_' + 'rewards-template.htm');
  });

  it('getPricingTemp to return values based on params', () => {
    expect(sharedService.getPricingTemp('no_pay', 'qty_')).toBe('');
  });

  it('getPricingTemp to return values based on params', () => {
    expect(sharedService.getPricingTemp('no_payy', 'qty_')).toBe('qty_' + 'no_payy' + '-template.htm');
  });

  it('getPricingTemp to return values based on params for no-pricing.htm', () => {
    expect(sharedService.getPricingTemp('no_payy', 'qty', 'test', {fullPointDiscounted: true, fullCashDiscounted: true}, true)).toBe('no-pricing.htm');
  });

  it('getPricingTemp to return values based on params for no-pricing.htm', () => {
    expect(sharedService.getPricingTemp('test', 'qty', 'test', {fullPointDiscounted: true, fullCashDiscounted: true}, false)).toBe('qtyfull-discounted.htm');
  });

  it('getPricingTemp to return values based on params', () => {
    expect(sharedService.getPricingTemp('no_payy', 'qty')).toBe( 'no_payy' + '-template.htm');
  });

  it('should call getProductBackImage method for engraveImageUrl', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    const engraveImageUrl = 'apple-gr/assets/img/customizable/ipad-pro.jpg';
    expect(sharedService.getProductBackImage(mockData, engraveImageUrl, null)).toEqual(engraveImageUrl);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for no sizeVal', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      { name: 'model', value: '12.9-inch', key: '13_1inch', i18Name: 'Model', orderBy: 0, points: null, swatchImageUrl: null },
      {
        name: 'color',
        value: 'Space Gray',
        key: 'space_gray',
        i18Name: 'Color',
        orderBy: 0,
        points: null,
        swatchImageUrl: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-pro-12-select-cell-spacegray-202003_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1583552336142&qlt=95'
      },
      { name: 'communication', value: 'Wi-Fi + Cellular', key: 'nocarrier', i18Name: 'Communication', orderBy: 0, points: null, swatchImageUrl: null }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for model 11 inch', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      { name: 'model', value: '11 inch', key: '11inch', i18Name: 'Model', orderBy: 0, points: null, swatchImageUrl: null },
      {
        name: 'color',
        value: 'Space Gray',
        key: 'space_gray',
        i18Name: 'Color',
        orderBy: 0,
        points: null,
        swatchImageUrl: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-pro-12-select-cell-spacegray-202003_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1583552336142&qlt=95'
      },
      { name: 'communication', value: 'Wi-Fi + Cellular', key: 'nocarrier', i18Name: 'Communication', orderBy: 0, points: null, swatchImageUrl: null }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for no communicationVal', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      { name: 'model', value: '12.9-inch', key: '12_9inch', i18Name: 'Model', orderBy: 0, points: null, swatchImageUrl: null },
      {
        name: 'color',
        value: 'Space Gray',
        key: 'space_gray',
        i18Name: 'Color',
        orderBy: 0,
        points: null,
        swatchImageUrl: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-pro-12-select-cell-spacegray-202003_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1583552336142&qlt=95'
      },
      { name: 'communication', value: 'Wi-Fi + Cellular', key: '', i18Name: 'Communication', orderBy: 0, points: null, swatchImageUrl: null }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for all available data', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      { name: 'model', value: '10.5 inch', key: '10_5inch', i18Name: 'Model', orderBy: 0, points: null, swatchImageUrl: null },
      {
        name: 'color',
        value: 'Space Gray',
        key: 'space_gray',
        i18Name: 'Color',
        orderBy: 0,
        points: null,
        swatchImageUrl: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-pro-12-select-cell-spacegray-202003_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1583552336142&qlt=95'
      },
      { name: 'communication', value: 'Wi-Fi + Cellular', key: 'nocarrier', i18Name: 'Communication', orderBy: 0, points: null, swatchImageUrl: null }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for engraveImageUrl', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    const engraveImageUrl = 'apple-gr/assets/img/customizable/ipad-pro.jpg';
    expect(sharedService.getProductBackImage(mockData, engraveImageUrl, null)).toEqual(engraveImageUrl);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for no sizeVal', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      { name: 'model', value: '12.9-inch', key: '13_1inch', i18Name: 'Model', orderBy: 0, points: null, swatchImageUrl: null },
      {
        name: 'color',
        value: 'Space Gray',
        key: 'space_gray',
        i18Name: 'Color',
        orderBy: 0,
        points: null,
        swatchImageUrl: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-pro-12-select-cell-spacegray-202003_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1583552336142&qlt=95'
      },
      { name: 'communication', value: 'Wi-Fi + Cellular', key: 'nocarrier', i18Name: 'Communication', orderBy: 0, points: null, swatchImageUrl: null }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for model 11 inch', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      { name: 'model', value: '11 inch', key: '11inch', i18Name: 'Model', orderBy: 0, points: null, swatchImageUrl: null },
      {
        name: 'color',
        value: 'Space Gray',
        key: 'space_gray',
        i18Name: 'Color',
        orderBy: 0,
        points: null,
        swatchImageUrl: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-pro-12-select-cell-spacegray-202003_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1583552336142&qlt=95'
      },
      { name: 'communication', value: 'Wi-Fi + Cellular', key: 'nocarrier', i18Name: 'Communication', orderBy: 0, points: null, swatchImageUrl: null }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for no communicationVal', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      { name: 'model', value: '12.9-inch', key: '12_9inch', i18Name: 'Model', orderBy: 0, points: null, swatchImageUrl: null },
      {
        name: 'color',
        value: 'Space Gray',
        key: 'space_gray',
        i18Name: 'Color',
        orderBy: 0,
        points: null,
        swatchImageUrl: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-pro-12-select-cell-spacegray-202003_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1583552336142&qlt=95'
      },
      { name: 'communication', value: 'Wi-Fi + Cellular', key: '', i18Name: 'Communication', orderBy: 0, points: null, swatchImageUrl: null }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for all available data', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      { name: 'model', value: '10.5 inch', key: '10_5inch', i18Name: 'Model', orderBy: 0, points: null, swatchImageUrl: null },
      {
        name: 'color',
        value: 'Space Gray',
        key: 'space_gray',
        i18Name: 'Color',
        orderBy: 0,
        points: null,
        swatchImageUrl: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-pro-12-select-cell-spacegray-202003_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1583552336142&qlt=95'
      },
      { name: 'communication', value: 'Wi-Fi + Cellular', key: 'nocarrier', i18Name: 'Communication', orderBy: 0, points: null, swatchImageUrl: null }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call showSessionTimeOut methods', () => {
    spyOn(sharedService, 'showSessionTimeOut').and.callThrough();
    sharedService.showSessionTimeOut(false);
    expect(sharedService.showSessionTimeOut).toHaveBeenCalled();
  });

  it('should call openEngraveModalDialog methods', () => {
    spyOn(sharedService, 'openEngraveModalDialog').and.callThrough();
    sharedService.openEngraveModalDialog(false);
    expect(sharedService.openEngraveModalDialog).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for engraveImageUrl', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    const engraveImageUrl = 'apple-gr/assets/img/customizable/ipad-pro.jpg';
    expect(sharedService.getProductBackImage(mockData, engraveImageUrl, null)).toEqual(engraveImageUrl);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for no sizeVal', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      { name: 'model', value: '12.9-inch', key: '13_1inch', i18Name: 'Model', orderBy: 0, points: null, swatchImageUrl: null },
      {
        name: 'color',
        value: 'Space Gray',
        key: 'space_gray',
        i18Name: 'Color',
        orderBy: 0,
        points: null,
        swatchImageUrl: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-pro-12-select-cell-spacegray-202003_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1583552336142&qlt=95'
      },
      { name: 'communication', value: 'Wi-Fi + Cellular', key: 'nocarrier', i18Name: 'Communication', orderBy: 0, points: null, swatchImageUrl: null }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for model 11 inch', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      { name: 'model', value: '11 inch', key: '11inch', i18Name: 'Model', orderBy: 0, points: null, swatchImageUrl: null },
      {
        name: 'color',
        value: 'Space Gray',
        key: 'space_gray',
        i18Name: 'Color',
        orderBy: 0,
        points: null,
        swatchImageUrl: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-pro-12-select-cell-spacegray-202003_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1583552336142&qlt=95'
      },
      { name: 'communication', value: 'Wi-Fi + Cellular', key: 'nocarrier', i18Name: 'Communication', orderBy: 0, points: null, swatchImageUrl: null }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for no communicationVal', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      { name: 'model', value: '12.9-inch', key: '12_9inch', i18Name: 'Model', orderBy: 0, points: null, swatchImageUrl: null },
      {
        name: 'color',
        value: 'Space Gray',
        key: 'space_gray',
        i18Name: 'Color',
        orderBy: 0,
        points: null,
        swatchImageUrl: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-pro-12-select-cell-spacegray-202003_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1583552336142&qlt=95'
      },
      { name: 'communication', value: 'Wi-Fi + Cellular', key: '', i18Name: 'Communication', orderBy: 0, points: null, swatchImageUrl: null }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for all available data', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      { name: 'model', value: '10.5 inch', key: '10_5inch', i18Name: 'Model', orderBy: 0, points: null, swatchImageUrl: null },
      {
        name: 'color',
        value: 'Space Gray',
        key: 'space_gray',
        i18Name: 'Color',
        orderBy: 0,
        points: null,
        swatchImageUrl: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-pro-12-select-cell-spacegray-202003_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1583552336142&qlt=95'
      },
      { name: 'communication', value: 'Wi-Fi + Cellular', key: 'nocarrier', i18Name: 'Communication', orderBy: 0, points: null, swatchImageUrl: null }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for engraveImageUrl', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    const engraveImageUrl = 'apple-gr/assets/img/customizable/ipad-pro.jpg';
    expect(sharedService.getProductBackImage(mockData, engraveImageUrl, null)).toEqual(engraveImageUrl);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for no sizeVal', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      { name: 'model', value: '12.9-inch', key: '13_1inch', i18Name: 'Model', orderBy: 0, points: null, swatchImageUrl: null },
      {
        name: 'color',
        value: 'Space Gray',
        key: 'space_gray',
        i18Name: 'Color',
        orderBy: 0,
        points: null,
        swatchImageUrl: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-pro-12-select-cell-spacegray-202003_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1583552336142&qlt=95'
      },
      { name: 'communication', value: 'Wi-Fi + Cellular', key: 'nocarrier', i18Name: 'Communication', orderBy: 0, points: null, swatchImageUrl: null }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for model 11 inch', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      { name: 'model', value: '11 inch', key: '11inch', i18Name: 'Model', orderBy: 0, points: null, swatchImageUrl: null },
      {
        name: 'color',
        value: 'Space Gray',
        key: 'space_gray',
        i18Name: 'Color',
        orderBy: 0,
        points: null,
        swatchImageUrl: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-pro-12-select-cell-spacegray-202003_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1583552336142&qlt=95'
      },
      { name: 'communication', value: 'Wi-Fi + Cellular', key: 'nocarrier', i18Name: 'Communication', orderBy: 0, points: null, swatchImageUrl: null }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for no communicationVal', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      { name: 'model', value: '12.9-inch', key: '12_9inch', i18Name: 'Model', orderBy: 0, points: null, swatchImageUrl: null },
      {
        name: 'color',
        value: 'Space Gray',
        key: 'space_gray',
        i18Name: 'Color',
        orderBy: 0,
        points: null,
        swatchImageUrl: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-pro-12-select-cell-spacegray-202003_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1583552336142&qlt=95'
      },
      { name: 'communication', value: 'Wi-Fi + Cellular', key: '', i18Name: 'Communication', orderBy: 0, points: null, swatchImageUrl: null }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call getProductBackImage method for all available data', () => {
    spyOn(sharedService, 'getProductBackImage').and.callThrough();
    const mockData = require('assets/mock/product-detail.json');
    let product = mockData.categories[0].slug;
    product = product.replace(/-/g, '');
    const baseUrlEngrave = 'apple-gr/assets/img/engraving/';
    mockData['options'] = [
      { name: 'model', value: '10.5 inch', key: '10_5inch', i18Name: 'Model', orderBy: 0, points: null, swatchImageUrl: null },
      {
        name: 'color',
        value: 'Space Gray',
        key: 'space_gray',
        i18Name: 'Color',
        orderBy: 0,
        points: null,
        swatchImageUrl: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-pro-12-select-cell-spacegray-202003_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1583552336142&qlt=95'
      },
      { name: 'communication', value: 'Wi-Fi + Cellular', key: 'nocarrier', i18Name: 'Communication', orderBy: 0, points: null, swatchImageUrl: null }
    ];
    sharedService.getProductBackImage(mockData, null, product, baseUrlEngrave);
    expect(sharedService.getProductBackImage).toHaveBeenCalled();
  });

  it('should call showSessionTimeOut methods', () => {
    spyOn(sharedService, 'showSessionTimeOut').and.callThrough();
    sharedService.showSessionTimeOut(false);
    expect(sharedService.showSessionTimeOut).toHaveBeenCalled();
  });

  it('should return splitPayOption.paymentMinLimit equal to 50 when getSplitPayLimitType is called', waitForAsync(() => {
    const programDataMock = require('assets/mock/program.json');
    const data = {
      splitpay: [
        {
          id: 1751,
          varId: 'Delta',
          programId: 'b2s_qa_only',
          paymentOption: 'pointsonly',
          limitType: 'percentage',
          paymentMinLimit: 50,
          paymentMaxLimit: 0,
          orderBy: 1,
          paymentProvider: null,
          lastUpdatedBy: 'Appl_user',
          lastUpdatedDate: 1526570221080,
          active: true
        }
      ]
    };
    programDataMock['redemptionOptions'] = data;
    sharedService.userStore.program = programDataMock;
    const splitPayOption = sharedService.getSplitPayLimitType('percentage');
    expect(splitPayOption.paymentMinLimit).toEqual(50);
  }));

  it('should call scrollToProductDetails method', () => {
    spyOn(sharedService, 'scrollToProductDetails').and.callThrough();
    const viewportScroller = ViewportScroller;
    sharedService.scrollToProductDetails('app-product-info', {prodInfoSection: {collapsed: true, toggle: () => true}}, viewportScroller );
    expect(sharedService.scrollToProductDetails).toHaveBeenCalled();
  });

  it('should get getPaymentMaxLimit method', () => {
    spyOn(sharedService, 'getPaymentMaxLimit').and.callThrough();
    sharedService.getPaymentMaxLimit(userData.program);
    expect(sharedService.getPaymentMaxLimit).toHaveBeenCalled();
  });

  it('should check redemption value when redemption options is one', () => {
    const program = require('assets/mock/program.json');
    program.redemptionOptions =  { splitpay: [
      {
        id: 966,
        varId: 'Delta',
        programId: 'b2s_qa_only',
        paymentOption: 'splitpay',
        limitType: 'percentage',
        paymentMinLimit: 0,
        paymentMaxLimit: 50,
        orderBy: 2,
        paymentProvider: null,
        lastUpdatedBy: 'Appl_user',
        lastUpdatedDate: 1527177099217,
        active: true
      }
    ] };
    sharedService['userStore'].program = program;
    expect(sharedService.isCashOnlyRedemption()).toBeFalsy();
    expect(sharedService.isPointsOnlyRewards()).toBeFalsy();
    expect(sharedService.isRewardsRedemption()).toBeTruthy();
    sharedService['userStore'].program = programData.redemptionOptions;
  });

  it('should call getTranslateParams method for showDecimal config', () => {
    spyOn(sharedService, 'getTranslateParams').and.callThrough();
    sharedService['userStore'].config.showDecimal = true;
    spyOn(sharedService['currencyPipe'], 'transform').and.callFake(() => state.cart.cartTotal.price.amount);
    const cartData = require('assets/mock/cart.json');
    const state = {
      cart: {
        cartTotal: cartData.cartTotal,
        redemptionPaymentLimit: cartData.redemptionPaymentLimit
      }
    };
    sharedService.getTranslateParams({}, {}, state);
    expect(sharedService.getTranslateParams).toHaveBeenCalled();
  });

  it('should call getTranslateParams method', () => {
    spyOn(sharedService, 'getTranslateParams').and.callThrough();
    sharedService['userStore'].config.showDecimal = false;
    spyOn(sharedService['currencyPipe'], 'transform').and.callFake(() => state.cart.cartTotal.price.amount);
    const cartData = require('assets/mock/cart.json');
    cartData.redemptionPaymentLimit['cartMaxLimit'] = {
      amount: 0,
      currencyCode: 'USD',
      points: 445600
    };
    const state = {
      cart: {
        cartTotal: cartData.cartTotal,
        redemptionPaymentLimit: cartData.redemptionPaymentLimit
      }
    };
    sharedService.getTranslateParams({}, {}, state);
    expect(sharedService.getTranslateParams).toHaveBeenCalled();
  });

  it('should check if carousel is disabled', () => {
    expect(sharedService.isCarouselEnabled(null)).toBeFalsy();
  });

  it('should execute setter and getter of selectedVariant', () => {
    sharedService.setSelectedVariant('test');
    expect(sharedService.getSelectedVariant()).toBe('test');
  });

  it('should call getAnalyticsUserObject method', () => {
    spyOn(sharedService, 'getAnalyticsUserObject').and.callThrough();
    const routeData = {
      analyticsObj: {
        pgName: 'pageName',
        pgType: 'pageType',
        pgSectionType: 'sectionType'
      }
    };
    sharedService.getAnalyticsUserObject(routeData, 10000);
    expect(sharedService.analyticsUserObject).toBeDefined();
    expect(sharedService.getAnalyticsUserObject).toHaveBeenCalled();
  });

  it('should call verifySkipPaymentOption method', () => {
    const programDataMock = require('assets/mock/program.json');
    const data = {
      splitpay: [
        {
          id: 1751,
          varId: 'Delta',
          programId: 'b2s_qa_only',
          paymentOption: 'pointsonly',
          limitType: 'percentage',
          paymentMinLimit: 50,
          paymentMaxLimit: 0,
          orderBy: 1,
          paymentProvider: null,
          lastUpdatedBy: 'Appl_user',
          lastUpdatedDate: 1526570221080,
          active: true
        }
      ],
      pointsonly: [
        {
          id: 965,
          varId: 'Delta',
          programId: 'b2s_qa_only',
          paymentOption: 'pointsonly',
          limitType: 'percentage',
          paymentMinLimit: 50,
          paymentMaxLimit: 0,
          orderBy: 1,
          paymentProvider: null,
          lastUpdatedBy: 'Appl_user',
          lastUpdatedDate: 1527177099217,
          active: true
        }
      ]
    };
    programDataMock['redemptionOptions'] = data;
    sharedService.userStore.program = programDataMock;
    const cartData = require('assets/mock/cart.json');
    cartData.cost = 0;
    spyOn(sharedService, 'isPointsFixed').and.callFake(() => true);
    expect(sharedService.verifySkipPaymentOption(cartData)).toBeTruthy();
  });

});
