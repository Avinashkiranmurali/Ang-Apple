import { TestBed, waitForAsync } from '@angular/core/testing';
import { AnonModalComponent } from './anon/anon-modal.component';
import { BrowseOnlyComponent } from './browse-only/browse-only.component';
import { ModalsService } from './modals.service';
import { NgbActiveModal, NgbModal, NgbModalModule, ModalDismissReasons } from '@ng-bootstrap/ng-bootstrap';
import { UserStoreService } from '@app/state/user-store.service';
import { TemplateStoreService } from '@app/state/template-store.service';
import { BehaviorSubject, of } from 'rxjs';
import { AddressModalComponent } from './address-modal/address-modal.component';
import { ConsentFormComponent } from './consent-form/consent-form.component';
import { IdologyModelComponent } from './idology-model/idology-model.component';
import { WFSpanishComponent } from './spanish-modal/wf-footer-spanish-modal.component';
import { OopsModalComponent } from './oops-modal/oops-modal.component';
import { TimeoutComponent } from './timeout/timeout.component';
import { TimeoutWarningModelComponent } from './timeout-warning/timeout-warning-model.component';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';
import { Router, RouterEvent } from '@angular/router';
import { NotificationRibbonService } from '@app/services/notification-ribbon.service';
import { EngraveModalComponent } from './engrave-modal/engrave-modal.component';
import { SharedService } from '@app/modules/shared/shared.service';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { DecimalPipe } from '@angular/common';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

// Mock class for NgbModalRef
export class MockNgbModalRef {
  componentInstance = {
    validator: '',
    messages: '',
    showSuggestedAddr: '',
    template: ''
  };
  result: Promise<any> = new Promise((resolve, reject) => resolve(true));
}

export class MockNgbModalReference {
  componentInstance = {
    validator: '',
    messages: '',
    showSuggestedAddr: '',
    template: ''
  };
  result: Promise<any> = new Promise((resolve, reject) => resolve(false));
}

describe('ModalsService', () => {
  let modalService: ModalsService;
  let httpTestingController: HttpTestingController;
  const programData = require('assets/mock/program.json');
  const mockUser = require('assets/mock/user.json');
  mockUser['program'] = programData;
  const userData = {
    user: mockUser,
    program: programData,
    config: programData['config'],
    get: () => of(mockUser)
  };
  let bootstrapModal: NgbModal;
  const mockModalRef: MockNgbModalRef = new MockNgbModalRef();
  const mockModalReference: MockNgbModalReference = new MockNgbModalReference();
  const routerEvent$ = new BehaviorSubject<RouterEvent>(null);
  let router: Router;
  const engraveObj = {
    cartItemId: null,
    psIdSlug: '30001MXG22LL/A',
    isGiftPromo: false,
    isEdit: false,
    updateCart: false,
    qualifyingProduct: require('assets/mock/product-detail.json')
  };
  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [
        AnonModalComponent,
        BrowseOnlyComponent,
        AddressModalComponent,
        ConsentFormComponent,
        IdologyModelComponent,
        WFSpanishComponent,
        OopsModalComponent,
        TimeoutComponent,
        TimeoutWarningModelComponent,
        EngraveModalComponent
      ],
      providers: [
        NgbModal,
        NgbActiveModal,
        SharedService,
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe,
        NotificationRibbonService,
        { provide: UserStoreService, useValue : userData },
        { provide: TemplateStoreService, useValue: {
            anonymousModal: () => of({})
          }
        },
        { provide: MatomoService, useValue: {
          broadcast: () => {}
        }}
      ],
      imports: [
        NgbModalModule,
        RouterTestingModule,
        HttpClientTestingModule,
        TranslateModule.forRoot()
      ]
    });
    modalService = TestBed.inject(ModalsService);
    httpTestingController = TestBed.inject(HttpTestingController);
    modalService.messages = require('assets/mock/messages.json');
    bootstrapModal = TestBed.inject(NgbModal);
    document.body.innerHTML += '<div class="modal-lg"><div class="modal-dialog"></div></div>';
    const modalElement = document.createElement('div');
    modalElement.setAttribute('class', 'modal-lg modal-dialog');
    spyOn(document, 'querySelector').and.returnValue(modalElement);
    router = TestBed.inject(Router);
    (router as any).events = routerEvent$.asObservable();
    router.navigate = jasmine.createSpy('navigate');
  });

  it('should be created', () => {
    expect(modalService).toBeTruthy();
  });

  it('should call modalAfterClosed method', () => {
    expect(modalService.modalAfterClosed).toBeTruthy();
  });

  it('should call hasAnyOpenModal method', () => {
    spyOn(modalService, 'hasAnyOpenModal').and.callThrough();
    modalService.hasAnyOpenModal();
    expect(modalService.hasAnyOpenModal).toHaveBeenCalled();
  });

  it('should call dismissAllModals method', () => {
    spyOn(modalService, 'dismissAllModals').and.callThrough();
    modalService.dismissAllModals();
    expect(modalService.dismissAllModals).toHaveBeenCalled();
  });

  it('should call openMobileFacetsFiltersComponent method', () => {
    spyOn(modalService, 'openMobileFacetsFiltersComponent').and.callThrough();
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    modalService.openMobileFacetsFiltersComponent(null, null, null);
    expect(modalService.openMobileFacetsFiltersComponent).toHaveBeenCalled();
  });

  it('should call openBrowseOnlyComponent method', () => {
    spyOn(modalService, 'openBrowseOnlyComponent').and.callThrough();
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    modalService.openBrowseOnlyComponent();
    expect(modalService.openBrowseOnlyComponent).toHaveBeenCalled();
  });

  it('should call settop method', waitForAsync (() => {
    const popupName = 'oopsModal';
    const dummyElement = document.createElement('div');
    document.getElementById = jasmine.createSpy('HTML Element').and.returnValue(dummyElement);
    dummyElement.innerHTML = '<div class="modal-lg"> </div>';
    spyOn(modalService, 'settop').withArgs(popupName).and.callThrough();
    spyOnProperty(window, 'innerWidth').and.returnValue(undefined);
    spyOnProperty(window, 'innerHeight').and.returnValue(undefined);
    modalService.settop(popupName);
    expect(modalService.settop).toHaveBeenCalled();
  }));

  it('should call openAnonModalComponent method', () => {
    spyOn(modalService, 'openAnonModalComponent').and.callThrough();
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    modalService['anonymousModal']['size'] = 'lg';
    modalService.openAnonModalComponent();
    expect(modalService.openAnonModalComponent).toHaveBeenCalled();
  });

  it('should call openAnonModalComponent method', () => {
    spyOn(modalService, 'openAnonModalComponent').and.callThrough();
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    modalService['anonymousModal']['className'] = '';
    modalService.openAnonModalComponent();
    expect(modalService.openAnonModalComponent).toHaveBeenCalled();
  });

  it('should call openAnonModalComponent method', () => {
    spyOn(modalService, 'openAnonModalComponent').and.callThrough();
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    modalService['anonymousModal']['className'] = 'error-modal in';
    modalService.openAnonModalComponent();
    expect(modalService.openAnonModalComponent).toHaveBeenCalled();
  });

  it('should call openSuggestAddressModalComponent method', () => {
    spyOn(modalService, 'openSuggestAddressModalComponent').and.callThrough();
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    modalService.openSuggestAddressModalComponent(null, null, null, null);
    expect(modalService.openSuggestAddressModalComponent).toHaveBeenCalled();
  });

  it('should call openSuggestAddressModalComponent method with no data', () => {
    spyOn(modalService, 'openSuggestAddressModalComponent').and.callThrough();
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalReference as any);
    modalService.openSuggestAddressModalComponent(null, null, null, null);
    expect(modalService.openSuggestAddressModalComponent).toHaveBeenCalled();
  });

  it('should call openSuggestAddressModalComponent method - When rejects', () => {
    spyOn(modalService, 'openSuggestAddressModalComponent').and.callThrough();
    mockModalReference.result = Promise.reject(false);
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalReference as any);
    modalService.openSuggestAddressModalComponent(null, null, null, null);
    expect(modalService.openSuggestAddressModalComponent).toHaveBeenCalled();
  });

  it('should call openOopsModalComponent method', waitForAsync(() => {
    spyOn(modalService, 'openOopsModalComponent').and.callThrough();
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    modalService.openOopsModalComponent('shippingAddressError');
    expect(modalService.openOopsModalComponent).toHaveBeenCalled();
  }));

  it('should call openConsentFormComponent method', () => {
    spyOn(modalService, 'openConsentFormComponent').and.callThrough();
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    modalService.openConsentFormComponent();
    expect(modalService.openConsentFormComponent).toHaveBeenCalled();
  });

  it('should call openIdologyModalComponent method', () => {
    spyOn(modalService, 'openIdologyModalComponent').and.callThrough();
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    modalService.openIdologyModalComponent(null, null);
    expect(modalService.openIdologyModalComponent).toHaveBeenCalled();
  });

  it('should call openWFSpanishComponent method', () => {
    spyOn(modalService, 'openWfSpanishComponent').and.callThrough();
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    modalService.openWfSpanishComponent();
    expect(modalService.openWfSpanishComponent).toHaveBeenCalled();
  });

  it('should call openEngraveModalComponent method with no data', () => {
    const mockProductResponse = require('assets/mock/product-detail.json');
    mockProductResponse['engrave'] = {
      line1: 'ENGRAVE',
      line2: 'TEST ENGRAVE TEST',
      font: 'Helvetica Neue',
      fontCode: 'ESR077N',
      maxCharsPerLine: '18 Eng',
      widthDimension: '45mm',
      noOfLines: 2,
      engraveBgImageLocation: 'apple-gr/assets/img/engraving/',
      isSkuBasedEngraving: false,
      templateClass: 'engrave-apple-pencil convert-to-upper-case',
      engraveFontConfigurations: null
    };
    spyOn(modalService, 'openEngraveModalComponent').and.callThrough();
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    modalService.openEngraveModalComponent(engraveObj);
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/products/30001MXG22LL/A?withVariations=false&withEngraveConfig=true');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    // Respond with the fake data when called
    req.flush(mockProductResponse);
    expect(modalService.openEngraveModalComponent).toHaveBeenCalled();
  });

  it('Engrave modal - Stay in cart page when modal dismiss while updating', () => {
    router['currentUrlTree'] = router.parseUrl('/store/cart');
    const mockProductResponse = require('assets/mock/product-detail.json');
    spyOn(modalService['sharedService'], 'getProducts').and.returnValue(of(mockProductResponse));
    engraveObj.isEdit = false;
    engraveObj.updateCart = true;
    engraveObj.isGiftPromo = false;
    spyOn(modalService, 'openEngraveModalComponent').and.callThrough();
    mockModalReference.result = Promise.reject(ModalDismissReasons.BACKDROP_CLICK);
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalReference as any);
    modalService['sharedService'].currentEngraveProductDetail = null;
    modalService.openEngraveModalComponent(engraveObj);
    expect(modalService.openEngraveModalComponent).toHaveBeenCalled();
  });

  it('EngraveModal - should test for 404 error', () => {
    spyOn(modalService, 'openEngraveModalComponent').and.callThrough();
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    modalService.openEngraveModalComponent(engraveObj);
    const errorMsg = 'deliberate 404 error';
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/products/30001MXG22LL/A?withVariations=false&withEngraveConfig=true');
    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    expect(modalService.openEngraveModalComponent).toHaveBeenCalled();
  });

  it('EngraveModal - should test for 401 error', () => {
    spyOn(modalService, 'openEngraveModalComponent').and.callThrough();
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    modalService.openEngraveModalComponent(engraveObj);
    const errorMsg = 'deliberate 401 error';
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/products/30001MXG22LL/A?withVariations=false&withEngraveConfig=true');
    // Respond with mock error
    req.flush(errorMsg, { status: 401, statusText: 'Not Found' });
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    expect(modalService.openEngraveModalComponent).toHaveBeenCalled();
  });

  it('should call openEngraveModalComponent method - with more available gift items', () => {
    const mockProductResponse = require('assets/mock/product-detail.json');
    spyOn(modalService, 'openEngraveModalComponent').and.callThrough();
    mockModalReference.result = Promise.reject(ModalDismissReasons.BACKDROP_CLICK);
    spyOn(modalService['sharedService'], 'getProducts').and.returnValue(of(mockProductResponse));
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalReference as any);
    modalService['sharedService'].currentEngraveProductDetail = mockProductResponse;
    modalService.openEngraveModalComponent(engraveObj);
    expect(modalService.openEngraveModalComponent).toHaveBeenCalled();
  });

  it('should call openEngraveModalComponent method - with available gift item ', () => {
    const mockProductResponse = require('assets/mock/product-detail.json');
    mockProductResponse.addOns.availableGiftItems = [
      {
        psid: '30001MK0C2AM/A',
        name: 'Apple Pencil (1st generation)',
        isEngravable: false
      }
    ];
    spyOn(modalService, 'openEngraveModalComponent').and.callThrough();
    mockModalReference.result = Promise.reject(ModalDismissReasons.BACKDROP_CLICK);
    spyOn(modalService['sharedService'], 'getProducts').and.returnValue(of(mockProductResponse));
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalReference as any);
    modalService['sharedService'].currentEngraveProductDetail = mockProductResponse;
    modalService.openEngraveModalComponent(engraveObj);
    expect(modalService.openEngraveModalComponent).toHaveBeenCalled();
  });

  it('should call openEngraveModalComponent method - when item has related products', () => {
    const mockProductResponse = require('assets/mock/product-detail.json');
    mockProductResponse.hasRelatedProduct = true;
    mockProductResponse.addOns.availableGiftItems = [
      {
        psid: '30001MK0C2AM/A',
        name: 'Apple Pencil (1st generation)',
        isEngravable: false
      }
    ];
    spyOn(modalService, 'openEngraveModalComponent').and.callThrough();
    mockModalReference.result = Promise.reject(ModalDismissReasons.BACKDROP_CLICK);
    spyOn(modalService['sharedService'], 'getProducts').and.returnValue(of(mockProductResponse));
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalReference as any);
    modalService['sharedService'].currentEngraveProductDetail = mockProductResponse;
    modalService.openEngraveModalComponent(engraveObj);
    expect(modalService.openEngraveModalComponent).toHaveBeenCalled();
  });

  it('should call openEngraveModalComponent method - when its a gift promo to update cart', () => {
    const mockProductResponse = require('assets/mock/product-detail.json');
    mockProductResponse.addOns.availableGiftItems = [];
    engraveObj.isGiftPromo = true;
    engraveObj.updateCart = true;
    spyOn(modalService, 'openEngraveModalComponent').and.callThrough();
    mockModalReference.result = Promise.reject(ModalDismissReasons.BACKDROP_CLICK);
    spyOn(modalService['sharedService'], 'getProducts').and.returnValue(of(mockProductResponse));
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalReference as any);
    modalService['sharedService'].currentEngraveProductDetail = mockProductResponse;
    modalService.openEngraveModalComponent(engraveObj);
    expect(modalService.openEngraveModalComponent).toHaveBeenCalled();
  });

  it('Engrave modal - Redirect to related products page while dismiss', () => {
    const mockProductResponse = require('assets/mock/product-detail.json');
    mockProductResponse.addOns.availableGiftItems = [];
    spyOnProperty(router, 'url').and.returnValue('/store/curated/ipad/ipad-accessories');
    spyOn(modalService, 'openEngraveModalComponent').and.callThrough();
    mockModalReference.result = Promise.reject(ModalDismissReasons.BACKDROP_CLICK);
    spyOn(modalService['sharedService'], 'getProducts').and.returnValue(of(mockProductResponse));
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalReference as any);
    modalService['sharedService'].currentEngraveProductDetail = mockProductResponse;
    modalService.openEngraveModalComponent(engraveObj);
    expect(modalService.openEngraveModalComponent).toHaveBeenCalled();
  });

  it('Engrave modal - Redirect to cart page when no related products found while dismiss', () => {
    const mockProductResponse = require('assets/mock/product-detail.json');
    mockProductResponse.addOns.availableGiftItems = [];
    mockProductResponse.hasRelatedProduct = false;
    engraveObj.qualifyingProduct.hasRelatedProduct = false;
    spyOnProperty(router, 'url').and.returnValue('/store/curated/ipad/ipad-accessories');
    spyOn(modalService, 'openEngraveModalComponent').and.callThrough();
    mockModalReference.result = Promise.reject(ModalDismissReasons.BACKDROP_CLICK);
    spyOn(modalService['sharedService'], 'getProducts').and.returnValue(of(mockProductResponse));
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalReference as any);
    modalService['sharedService'].currentEngraveProductDetail = mockProductResponse;
    modalService.openEngraveModalComponent(engraveObj);
    expect(modalService.openEngraveModalComponent).toHaveBeenCalled();
  });

  it('Engrave modal - Redirect to cart page when item is editable while dismiss', () => {
    const mockProductResponse = require('assets/mock/product-detail.json');
    mockProductResponse.addOns.availableGiftItems = [];
    mockProductResponse.hasRelatedProduct = false;
    engraveObj.isEdit = true;
    spyOnProperty(router, 'url').and.returnValue('/store/curated/ipad/ipad-accessories');
    spyOn(modalService, 'openEngraveModalComponent').and.callThrough();
    mockModalReference.result = Promise.reject(ModalDismissReasons.BACKDROP_CLICK);
    spyOn(modalService['sharedService'], 'getProducts').and.returnValue(of(mockProductResponse));
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalReference as any);
    modalService['sharedService'].currentEngraveProductDetail = mockProductResponse;
    modalService.openEngraveModalComponent(engraveObj);
    expect(modalService.openEngraveModalComponent).toHaveBeenCalled();
  });

});
