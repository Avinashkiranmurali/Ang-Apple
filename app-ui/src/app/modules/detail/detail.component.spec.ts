import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { DetailComponent } from './detail.component';
import { UserStoreService } from '@app/state/user-store.service';
import { DetailService } from '@app/services/detail.service';
import { TemplateStoreService } from '@app/state/template-store.service';
import { of } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { PageTitleComponent } from '@app/modules/shared/page-title/page-title.component';
import { TranslateModule } from '@ngx-translate/core';
import { TemplateService } from '@app/services/template.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { NgbActiveModal, NgbCollapseModule } from '@ng-bootstrap/ng-bootstrap';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { SessionService } from '@app/services/session.service';
import { GiftPromoService } from '@app/services/gift-promo.service';
import { HeapService } from '@app/analytics/heap/heap.service';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';

describe('DetailComponent', () => {
  let component: DetailComponent;
  let fixture: ComponentFixture<DetailComponent>;
  let httpTestingController: HttpTestingController;
  let templateStoreService: TemplateStoreService;
  let templateService: TemplateService;
  let sharedService: SharedService;
  const productDetail = require('assets/mock/products-watch.json');
  const configData = require('assets/mock/configData.json');
  const programData = require('assets/mock/program.json');
  programData['carouselPages'] = ['pdp'];
  const userMock = require('assets/mock/user.json');
  userMock['program'] = programData;
  const userData = {
    user: userMock,
    program: programData,
    config: programData['config'],
    get: () => of(userMock)
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        DetailComponent,
        PageTitleComponent
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot(),
        NgbCollapseModule
      ],
      providers: [
        ParsePsidPipe,
        CurrencyPipe,
        SharedService,
        DetailService,
        NgbActiveModal,
        SessionService,
        MatomoService,
        { provide: CurrencyFormatPipe },
        { provide: DecimalPipe },
        { provide: UserStoreService, useValue: userData },
        { provide: ActivatedRoute, useValue: {
            params: of({category: 'mac', subcat: 'mackbook-pro', psid: '30001MYDA2LL/A'}),
            snapshot: of({data: {}}),
            data: of({})
          }
        },
        { provide: MatomoService, useValue: {
          broadcast: () => {},
          initConfig: () => {}
        }},
        { provide: HeapService,
          useValue: {
            broadcastEvent: () => {},
            loadInitialScript: () => {}
          }
        }
      ]
    })
    .compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
    templateStoreService = TestBed.inject(TemplateStoreService);
    templateService = TestBed.inject(TemplateService);
    sharedService = TestBed.inject(SharedService);
    templateStoreService.addTemplate(configData['configData']);
    templateService.template = configData['configData'];
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DetailComponent);
    component = fixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
    component.isTablet = false;
    sessionStorage.removeItem('detailDataInfo');
    component.selectedOptions = ['black'];
    component.itemOptions = component['sharedService'].transformOptions(productDetail.optionsConfigurationData, 'apple-watch-bands', false) ;
    const configProducts = component['sharedService'].transformProducts(productDetail.variations);
    const primaryOptValData = component.itemOptions[0].optionData;
    const  primaryValIfo = [];
    for (const primaryOptVal of primaryOptValData) {
      const primaryVal = primaryOptVal.key;
      primaryValIfo[primaryVal] = sharedService.filterProducts(configProducts, primaryVal);
    }
    fixture.detectChanges();
  });

  it('should create with success respones', waitForAsync(() => {
    component.itemOptions = [];
    fixture.detectChanges();
    // Fake response data
    const mockResponse = require('assets/mock/product-detail.json');
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/products/30001MYDA2LL/A?withVariations=false');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    // Respond with the fake data when called
    req.flush(mockResponse);
    expect(component).toBeTruthy();
  }));

  it('should create with failure response', waitForAsync(() => {
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/products/30001MYDA2LL/A?withVariations=false');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    // Respond with mock error
    req.flush('Error not found', { status: 404, statusText: 'Not Found' });
    expect(component).toBeTruthy();
  }));

  it('should create with detail configurations', waitForAsync(() => {
    fixture.detectChanges();
    // Fake response data
    const response = require('assets/mock/products-watch.json');
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/products/30001MYDA2LL/A?withVariations=false');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    // Respond with the fake data when called
    req.flush(response);
    expect(component).toBeTruthy();
  }));

  it('should call getDetailswithPsid method', () => {
    spyOn(component, 'getDetailswithPsid').and.callThrough();
    const variantInfo = {
      psid: 'A',
      withVariationCheck: false
    };
    component.getDetailswithPsid(variantInfo.psid, variantInfo.withVariationCheck);
    expect(component.getDetailswithPsid).toHaveBeenCalled();
  });

  it('should call addToCart method', () => {
    spyOn(component, 'addToCart').and.callThrough();
    const product = require('assets/mock/product-detail.json');
    component.addToCart(product.psid, product);
    expect(component.addToCart).toHaveBeenCalled();
  });

  it('should call setOptionLength method', () => {
    spyOn(component, 'setOptionLength').and.callThrough();
    component.setOptionLength();
    expect(component.setOptionLength).toHaveBeenCalled();
  });

  it('should call selectedAppleCareServicePlan method', () => {
    spyOn(component, 'selectedAppleCareServicePlan').and.callThrough();
    component.selectedAppleCareServicePlan('12345');
    expect(component.selectedAppleCareServicePlan).toHaveBeenCalled();
  });


  it('should call mouseHoverEvent method', () => {
    component.mouseHoverEvent('#fff');
    expect(component.hoverColor).toEqual('#fff');
  });

  it('should call mouseLeaveEvent method', () => {
    component.mouseLeaveEvent();
    expect(component.hoverColor).toEqual('');
  });

  it('should call detailPageData method', () => {
    const object = { templateUrl: 'apple-gr/vars/default/templates/product-detail-template.html' };
    expect(component.detailPageData()).toEqual(object);
  });

  it('should call filterProducts method', () => {
    spyOn(sharedService, 'filterProducts').and.callThrough();
    const productList = require('assets/mock/facets-filters.json');
    sharedService.filterProducts(productList['products'], 'space_gray');
    expect(sharedService.filterProducts).toHaveBeenCalled();
  });

  it('should call changeVariantOption method', () => {
    spyOn(component, 'changeVariantOption').and.callThrough();
    sessionStorage.setItem('detailDataInfo', JSON.stringify(productDetail));
    component.details = productDetail;
    component.selectedVariant = { color: 'product_red' };
    const data = {
      disabled: true,
      hidden: false,
      isDenomination: false,
      name: 'color',
      optionData: [{
        image: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/MXAA2ref_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1611165303000&qlt=95',
        key: 'black',
        optDisable: false,
        optHidden: false,
        points: null,
        tabindex: 0,
        value: 'Black'
      }],
      orderBy: 0,
      title: 'Color'
    };
    component.itemOptions.push(data);
    fixture.detectChanges();
    const optionSet = component.itemOptions[0];
    component.changeVariantOption(0, component.selectedVariant['color'], optionSet);
    expect(component.changeVariantOption).toHaveBeenCalled();
  });

  it('should call setDefaultVariant method', () => {
    productDetail.variations[0]['options'] = [
      { i18Name: 'Language', key: 'en_US', name: 'language', orderBy: 0, points: null, swatchImageUrl: null, value: 'US English' }
    ];
    component.selectedProductVariant = productDetail.variations[0];
    component.selectedVariant = {};
    fixture.detectChanges();
    spyOn(component, 'changeVariantOption').and.callFake(() => {});
    spyOn(component, 'setDefaultVariant').and.callThrough();
    component.setDefaultVariant(productDetail);
    expect(component.setDefaultVariant).toHaveBeenCalled();
  });

  it('should call ngOnDestroy method', () => {
    spyOn(component, 'ngOnDestroy').and.callThrough();
    component.ngOnDestroy();
    expect(component.ngOnDestroy).toHaveBeenCalledTimes(1);
  });

});
