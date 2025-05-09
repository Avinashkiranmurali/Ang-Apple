import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from '@angular/core/testing';
import { CheckoutButtonsComponent } from './checkout-buttons.component';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { NgbActiveModal, NgbModal, NgbModalModule } from '@ng-bootstrap/ng-bootstrap';
import { TemplateStoreService } from '@app/state/template-store.service';
import { TranslateModule, TranslatePipe } from '@ngx-translate/core';
import { IdologyLibService, IdologyLibModule } from '@bakkt/idology-lib';
import { ModalsService } from '@app/components/modals/modals.service';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { TransitionService } from '@app/transition/transition.service';
import { Program } from '@app/models/program';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { TemplateService } from '@app/services/template.service';
import { UserStoreService } from '@app/state/user-store.service';
import { TimeoutComponent } from '@app/components/modals/timeout/timeout.component';
import { SharedService } from '@app/modules/shared/shared.service';
import { AppConstants } from '@app/constants/app.constants';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import * as events from 'events';

export class MockNgbModalRef {
  componentInstance = {
    validator: '',
    messages: '',
    showSuggestedAddr: '',
    template: ''
  };
  result: Promise<any> = new Promise((resolve, reject) => resolve(true));
}


describe('CheckoutButtonsComponent', () => {
  let component: CheckoutButtonsComponent;
  let fixture: ComponentFixture<CheckoutButtonsComponent>;
  let templateStoreService: TemplateStoreService;
  let idologyService: IdologyLibService;
  let pricingService: PricingService;
  let httpTestingController: HttpTestingController;
  let transitionService: TransitionService;
  let sharedService: SharedService;
  let userStoreService: UserStoreService;
  let modalService: ModalsService;
  const configData = require('assets/mock/configData.json');
  const programData: Program = require('assets/mock/program.json');
  let bootstrapModal: NgbModal;
  const mockModalRef: MockNgbModalRef = new MockNgbModalRef();

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        CheckoutButtonsComponent,
        TimeoutComponent
      ],
      imports: [
        TranslateModule.forRoot(),
        HttpClientTestingModule,
        RouterTestingModule,
        IdologyLibModule
      ],
      providers: [
        NgbActiveModal,
        NgbModalModule,
        TemplateService,
        SharedService,
        TranslatePipe,
        { provide: Router, useValue: {
            navigate: jasmine.createSpy('navigate') }
        },
        { provide: CurrencyFormatPipe },
        { provide: DecimalPipe },
        { provide: CurrencyPipe }
      ]
    })
    .compileComponents();
    bootstrapModal = TestBed.inject(NgbModal);
    userStoreService = TestBed.inject(UserStoreService);
    sharedService = TestBed.inject(SharedService);
    userStoreService.user = require('assets/mock/user.json');
    userStoreService.program = programData;
    userStoreService.config = programData['config'];
    templateStoreService = TestBed.inject(TemplateStoreService);
    idologyService = TestBed.inject(IdologyLibService);
    pricingService = TestBed.inject(PricingService);
    httpTestingController = TestBed.inject(HttpTestingController);
    transitionService = TestBed.inject(TransitionService);
    modalService = TestBed.inject(ModalsService);
    templateStoreService.addTemplate(configData['configData']);
    spyOn(modalService, 'settop').and.callFake(() => {});
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CheckoutButtonsComponent);
    component = fixture.componentInstance;
    component.config = programData.config;
    modalService['triggerafterclosed'].next(true);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('amountZero should return 0', () => {
    expect(component.amountZero()).toBe(0);
  });

  it('submitOrder with payment as parameter', () => {
    const data = {
      transactionId: ''
    };
    sessionStorage.setItem('paymentApiDet', JSON.stringify(data));
    spyOn(component, 'submitOrder').and.callThrough();
    component.submitOrder('payment');

    const placeOrder = require('assets/mock/orderConfirmation.json');
    const req = httpTestingController.expectOne(pricingService.baseUrl + 'order/placeOrder');

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(placeOrder, { headers: {'xsrf-token': 'b82780ff-e016-4c82-8f7'}});
    expect(component.submitOrder).toHaveBeenCalledWith('payment');
  });

  it('placeOrder with noPayment as parameter with the btn click',  fakeAsync(() => {
    component.config = {
      'idologyEnabled': true
    };
    const par = 'noPayment';
    tick(100);
    spyOn(component, 'placeOrder').withArgs(par, 'click').and.callThrough();
    tick(100);
    component.placeOrder(par, 'click');
    spyOn(component, 'initializeIdology').withArgs(par).and.callThrough();
    tick(100);
    component.initializeIdology(par);
    expect(component.initializeIdology).toHaveBeenCalledWith('noPayment');
  }));

  it('placeOrder with payroll as parameter', () => {
    component.config.idologyEnabled = false;
    component.placeOrder('payroll', '');
    expect(component.placeOrder).toBeDefined();
  });

  it('placeOrder with points and `` as parameters', () => {
    component.config.idologyEnabled = false;
    spyOn(component, 'submitOrder').and.callThrough();
    component.placeOrder('points', '');
    expect(component.submitOrder).toHaveBeenCalledWith('points');
  });

  it('transferToPayroll', () => {
    spyOn(component, 'transferToPayroll').and.callThrough();
    component.transferToPayroll();
    expect(component.transferToPayroll).toHaveBeenCalled();
  });

  it('isPayroll to return true', () => {
    component.config.paymentType = AppConstants.paymentType.pd_fixed;
    component.config.epp = true;
    spyOn(sharedService, 'isPayrollType').and.callThrough();
    sharedService.isPayrollType();
    expect(sharedService.isPayrollType).toHaveBeenCalled();
  });

  it('isPayroll to return false', () => {
    component.config.paymentType = AppConstants.paymentType.no_pay;
    component.config.epp = false;
    spyOn(sharedService, 'isPayrollType').and.callThrough();
    sharedService.isPayrollType();
    expect(sharedService.isPayrollType).toHaveBeenCalled();
  });

  it('checkTimedoutModal', () => {
    spyOn(component, 'checkTimedoutModal').and.callThrough();
    component.checkTimedoutModal();
    const placeOrder = require('assets/mock/orderConfirmation.json');
    const req = httpTestingController.expectOne(() => true);

    // Assert that the request is a POST
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(placeOrder, { headers: {'xsrf-token': 'b82780ff-e016-4c82-8f7'}});
    expect(component.checkTimedoutModal).toHaveBeenCalled();
  });

  it('should call checkTimedoutModal for timeout session', () => {
    spyOn(component, 'checkTimedoutModal').and.callThrough();
    component.checkTimedoutModal();
    const data = {
      timedOut: {
        timedOutUrl : null
      }
    };
    const req = httpTestingController.expectOne(() => true);

    // Assert that the request is a POST
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(data, { headers: {'xsrf-token': 'b82780ff-e016-4c82-8f7'}});
    expect(component.checkTimedoutModal).toHaveBeenCalled();
  });

  it('initializeIdology with cash_only as parameter', () => {
    const idology = require('assets/mock/orderConfirmation.json');
    spyOn(idologyService, 'validatePerson').and.returnValue(of(idology));
    component.initializeIdology('cash_only');
    expect(component.initializeIdology).toBeDefined();
  });

  it('initializeIdology with payroll as parameter', () => {
    const idology = require('assets/mock/orderConfirmation.json');
    spyOn(idologyService, 'validatePerson').and.returnValue(of(idology));
    component.initializeIdology('payroll');
    expect(component.initializeIdology).toBeDefined();
  });

  it('initializeIdology with cash_only and idology response', () => {
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    fixture.detectChanges();
    const idology = require('assets/mock/idology.json');
    spyOn(idologyService, 'validatePerson').and.returnValue(of(idology));
    component.initializeIdology('cash_only');
    expect(component.initializeIdology).toBeDefined();
  });

  it('initializeIdology with cash_only and idology response - else check', () => {
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    (modalService as any).triggerAfterClosed$ = of(false);
    fixture.detectChanges();
    const idology = require('assets/mock/idology.json');
    spyOn(idologyService, 'validatePerson').and.returnValue(of(idology));
    component.initializeIdology('cash_only');
    expect(component.initializeIdology).toBeDefined();
  });

  it('initializeIdology with payroll and idology response', () => {
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    fixture.detectChanges();
    const idology = require('assets/mock/idology.json');
    spyOn(idologyService, 'validatePerson').and.returnValue(of(idology));
    component.initializeIdology('payroll');
    expect(component.initializeIdology).toBeDefined();
  });

  it(' should call changeItemSelection', () => {
    component.changeItemSelection();
    expect(component.changeItemSelection).toBeDefined();
  });

  it('initializeIdology with payroll as parameter - failure response', () => {
    spyOn(idologyService, 'validatePerson').and.returnValue(throwError({status: 401}));
    component.initializeIdology('payroll');
    expect(component.initializeIdology).toThrow();
  });

  it('submitOrder with payroll as parameter - 404 failure response', () => {
    spyOn(modalService, 'openOopsModalComponent').and.callFake(() => {});
    spyOn(pricingService, 'placeOrder').and.returnValue(throwError({status: 404, error: {errorCode : 4}}));
    component.submitOrder('payroll');
    expect(component.initializeIdology).toThrow();
  });

  it('submitOrder with payroll as parameter - 401 failure response', () => {
    spyOn(modalService, 'openOopsModalComponent').and.callFake(() => {});
    spyOn(pricingService, 'placeOrder').and.returnValue(throwError({status: 401, error: {errorCode : 4}}));
    component.submitOrder('payroll');
    expect(component.initializeIdology).toThrow();
  });

  it('initializeIdology with cash-only as parameter - failure response', () => {
    spyOn(idologyService, 'validatePerson').and.returnValue(throwError({status: 401}));
    component.initializeIdology('cash_only');
    expect(component.initializeIdology).toThrow();
  });

  it('initializeIdology with cash-only as parameter - failure response', () => {
    spyOn(idologyService, 'validatePerson').and.returnValue(throwError({status: 401}));
    component.initializeIdology('cash_only');
    expect(component.initializeIdology).toThrow();
  });

  it('should modify gift items and showVarOrderId is true', () => {
    spyOn(component, 'submitOrder').and.callThrough();
    component.config.showVarOrderId = true;
    component.submitOrder('payroll');
    const placeOrder = require('assets/mock/orderConfirmation.json');
    const req = httpTestingController.expectOne(pricingService.baseUrl + 'order/placeOrder');

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(placeOrder, { headers: {'xsrf-token': 'b82780ff-e016-4c82-8f7'}});
    expect(component.submitOrder).toHaveBeenCalled();
  });

  it('should modify gift items and showVarOrderId is false', waitForAsync(() => {
    spyOn(component, 'submitOrder').and.callThrough();
    component.config.showVarOrderId = false;
    component.submitOrder('payroll');
    const placeOrder = require('assets/mock/orderConfirmation.json');
    const req = httpTestingController.expectOne(pricingService.baseUrl + 'order/placeOrder');

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(placeOrder, { headers: {'xsrf-token': 'b82780ff-e016-4c82-8f7'}});
    expect(component.submitOrder).toHaveBeenCalled();
  }));

  it('should modify gift items and isPageAccessible is false', waitForAsync(() => {
    spyOn(component, 'submitOrder').and.callThrough();
    component.isPageAccessible = false;
    component.submitOrder('payroll');
    const placeOrder = require('assets/mock/orderConfirmation.json');
    const req = httpTestingController.expectOne(pricingService.baseUrl + 'order/placeOrder');

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(placeOrder, { headers: {'xsrf-token': 'b82780ff-e016-4c82-8f7'}});
    expect(component.submitOrder).toHaveBeenCalled();
  }));

  it('should modify gift items and unAuthorizedPages as true', () => {
    spyOn(component, 'submitOrder').and.callThrough();
    component.config.unAuthorizedPages = true;
    component.config.unAuthorizedPages = ['CONFIRMATION'];
    component.submitOrder('payroll');
    const placeOrder = require('assets/mock/orderConfirmation.json');
    const req = httpTestingController.expectOne(pricingService.baseUrl + 'order/placeOrder');

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(placeOrder, { headers: {'xsrf-token': 'b82780ff-e016-4c82-8f7'}});
    component.config.unAuthorizedPages = false;
    component.config.unAuthorizedPages = [];
    expect(component.submitOrder).toHaveBeenCalled();
  });

  it('submitOrder with payroll as parameter - failure response', () => {
    spyOn(modalService, 'openOopsModalComponent').and.callFake(() => {});
    spyOn(pricingService, 'placeOrder').and.returnValue(throwError({status: 404, error: {}}));
    component.submitOrder('payroll');
    expect(component.initializeIdology).toThrow();
  });

  it('submitOrder with payroll as parameter and promotionUseExceeded in error', () => {
    spyOn(modalService, 'openOopsModalComponent').and.callFake(() => {});
    spyOn(pricingService, 'placeOrder').and.returnValue(throwError({status: 404, error: {promotionUseExceeded: true}}));
    component.submitOrder('payroll');
    expect(component.initializeIdology).toThrow();
  });

  it('should call getTransitionType - payment', () => {
    spyOn(component, 'getTransitionType').and.callThrough();
    expect(component.getTransitionType('payment')).toBe('processing-cc');
    expect(component.getTransitionType).toHaveBeenCalled();
  });

});
