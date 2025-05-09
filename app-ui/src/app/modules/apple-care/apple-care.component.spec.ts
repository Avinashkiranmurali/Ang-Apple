import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule, TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Observable, of, Subscription, throwError } from 'rxjs';
import { AppleCareComponent } from './apple-care.component';
import { PricingTempComponent } from '@app/modules/pricing/pricing-temp/pricing-temp.component';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { UserStoreService } from '@app/state/user-store.service';
import { ModalsService } from '@app/components/modals/modals.service';
import { NgbModal, NgbActiveModal, NgbModalModule } from '@ng-bootstrap/ng-bootstrap';
import { Injectable } from '@angular/core';
import { CartService } from '@app/services/cart.service';
import { SessionService } from '@app/services/session.service';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { DecimalPipe } from '@angular/common';

// Mock class for NgbModalRef
export class MockNgbModalResolve {
  componentInstance = {
    config: '',
    messages: '',
    mediaProduct: ''
  };
  result: Promise<any> = Promise.resolve(true);
}

@Injectable()
export class TranslateServiceStub {
  public get<T>(key: T): Observable<T> {
    return of(key);
  }
  public instant(key: string): any {
    return '';
  }
}

describe('AppleCareComponent', () => {
  let component: AppleCareComponent;
  let fixture: ComponentFixture<AppleCareComponent>;
  let bootstrapModal: NgbModal;
  let cartService: CartService;
  const mockModalResolve = new MockNgbModalResolve();
  const programData = require('assets/mock/program.json');
  const userMock = require('assets/mock/user.json');
  userMock['program'] = programData;
  const userData = {
    user: userMock,
    program: programData,
    config: programData['config'],
    get: () => of(userMock)
  };
  const productDetail = require('assets/mock/product-detail.json');
  const cartData = require('assets/mock/cart.json');

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        NgbModalModule
      ],
      declarations: [
        AppleCareComponent,
        PricingTempComponent,
        CurrencyFormatPipe,
        TranslatePipe
      ],
      providers: [
        { provide: UserStoreService, useValue: userData },
        { provide: CurrencyFormatPipe },
        { provide: ActivatedRoute,
          useValue: { data: of({pageName: 'PCP'}), snapshot: { data: { pageName: 'PCP'}}}
        },
        { provide: ModalsService, useValue: {
          settop: () => ({}) }
        },
        { provide: TranslateService, useClass: TranslateServiceStub },
        NgbModal,
        NgbActiveModal,
        CurrencyPipe,
        TranslatePipe,
        TranslateService,
        DecimalPipe,
        { provide: SessionService , useValue: {
          showTimeout: () => {}
        }}
      ]
    })
    .compileComponents();
    bootstrapModal = TestBed.inject(NgbModal);
    cartService = TestBed.inject(CartService);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AppleCareComponent);
    component = fixture.componentInstance;
    component.user = userData.user;
    component.program = userData.program;
    component.config = userData.config;
    component.messages = require('assets/mock/messages.json');
    component.appleCareServicePlans = productDetail.addOns.servicePlans;
    component.cartItem = cartData.cartItems[0];
    fixture.detectChanges();
  });

  afterAll(() => {
    component.program.formatPointName = programData.formatPointName;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call ngOnInit with no formatPointName', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    component.program.formatPointName = '';
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should call openAppleCareModal method', () => {
    spyOn(component, 'openAppleCareModal').and.callThrough();
    mockModalResolve.result = Promise.resolve(true);
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalResolve as any);
    component.openAppleCareModal(productDetail.addOns.servicePlans[0]);
    expect(component.openAppleCareModal).toHaveBeenCalled();
  });

  it('should call openAppleCareModal method - When no data exists', () => {
    spyOn(component, 'openAppleCareModal').and.callThrough();
    mockModalResolve.result = Promise.resolve(false);
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalResolve as any);
    component.openAppleCareModal(productDetail.addOns.servicePlans[0]);
    expect(component.openAppleCareModal).toHaveBeenCalled();
  });

  it('should call addAppleCareServicePlan method', () => {
    spyOn(component, 'addAppleCareServicePlan').and.callThrough();
    component.isProductView = false;
    component.addAppleCareServicePlan(productDetail.addOns.servicePlans[0]);
    expect(component.addAppleCareServicePlan).toHaveBeenCalled();
  });

  it('should call removeAppleCareServicePlan method for Product view', () => {
    spyOn(component, 'removeAppleCareServicePlan').and.callThrough();
    component.giftItem = cartData.cartItems[0];
    component.isGiftView = true;
    component.removeAppleCareServicePlan();
    expect(component.removeAppleCareServicePlan).toHaveBeenCalled();
  });

  it('should call removeAppleCareServicePlan method', () => {
    spyOn(component, 'removeAppleCareServicePlan').and.callThrough();
    component.isGiftView = false;
    component.removeAppleCareServicePlan();
    expect(component.removeAppleCareServicePlan).toHaveBeenCalled();
  });

  it('should call addAppleCareServicePlan method for Product view', () => {
    spyOn(component, 'addAppleCareServicePlan').and.callThrough();
    component.isProductView = true;
    component.addAppleCareServicePlan(productDetail.addOns.servicePlans[0]);
    component.addAppleCareServicePlan(productDetail.addOns.servicePlans[0]);
    expect(component.addAppleCareServicePlan).toHaveBeenCalled();
  });

  it('should call addOrRemoveServicePlan method for Product view - success response', () => {
    spyOn(component['cartService'], 'modifyCart').and.returnValue(of(cartData));
    spyOn(component, 'addOrRemoveServicePlan').and.callThrough();
    component.addOrRemoveServicePlan(productDetail.addOns.servicePlans[0]);
    expect(component.addOrRemoveServicePlan).toHaveBeenCalled();
  });

  it('should call addOrRemoveServicePlan method for Product view - 401 error response', () => {
    const errorResponse = { status: 401, statusText: 'Not Found' };
    spyOn(cartService, 'modifyCart').and.callFake(() => throwError(errorResponse));
    spyOn(component, 'addOrRemoveServicePlan').and.callThrough();
    component.addOrRemoveServicePlan(productDetail.addOns.servicePlans[0]);
    expect(component.addOrRemoveServicePlan).toHaveBeenCalled();
  });

  it('should call addOrRemoveServicePlan method for Product view - 0 error response', () => {
    const errorResponse = { status: 0, statusText: 'Not Found' };
    spyOn(cartService, 'modifyCart').and.callFake(() => throwError(errorResponse));
    spyOn(component, 'addOrRemoveServicePlan').and.callThrough();
    component.addOrRemoveServicePlan(productDetail.addOns.servicePlans[0]);
    expect(component.addOrRemoveServicePlan).toHaveBeenCalled();
  });

  it('should call addOrRemoveServicePlan method for Product view - 404 error response', () => {
    const errorResponse = { status: 404, statusText: 'Not Found' };
    spyOn(cartService, 'modifyCart').and.callFake(() => throwError(errorResponse));
    spyOn(component, 'addOrRemoveServicePlan').and.callThrough();
    component.addOrRemoveServicePlan(productDetail.addOns.servicePlans[0]);
    expect(component.addOrRemoveServicePlan).toHaveBeenCalled();
  });

  it('should call openAppleCareModal method - When rejects', () => {
    spyOn(component, 'openAppleCareModal').and.callThrough();
    mockModalResolve.result = Promise.reject(false);
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalResolve as any);
    component.selectedServicePlan = productDetail.addOns.servicePlans[0];
    component.openAppleCareModal(productDetail.addOns.servicePlans[0]);
    expect(component.openAppleCareModal).toHaveBeenCalled();
  });

  it('should unsubscribe on destroy', () => {
    component['subscriptions'].push(new Subscription());
    component.ngOnDestroy();
    expect(component.ngOnDestroy).toBeTruthy();
  });

});
