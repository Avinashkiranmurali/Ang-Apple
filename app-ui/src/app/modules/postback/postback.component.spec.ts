import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormsModule, NgForm } from '@angular/forms';
import { DomSanitizer } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { PurchaseSelectionInfo } from '@app/models/postback-order-conformaction';
import { CartService } from '@app/services/cart.service';
import { OrderInformationService } from '@app/services/order-information.service';
import { SessionService } from '@app/services/session.service';
import { UserStoreService } from '@app/state/user-store.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { PostbackComponent } from './postback.component';
import { TransitionService } from '@app/transition/transition.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

describe('PostbackComponent', () => {
  let component: PostbackComponent;
  let sharedService: SharedService;
  let fixture: ComponentFixture<PostbackComponent>;
  let httpTestingController: HttpTestingController;
  const fakeResponse = require('assets/mock/orderConfirmation.json');
  const programData = require('assets/mock/program.json');
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        PostbackComponent
      ],
      imports: [
        TranslateModule.forRoot(),
        RouterTestingModule,
        FormsModule,
        HttpClientTestingModule
      ],
      providers: [
        NgForm,
        NgbActiveModal,
        OrderInformationService,
        CartService,
        DomSanitizer,
        TransitionService,
        { provide: SessionService },
        { provide: HttpClient },
        { provide: UserStoreService, useValue: userData },
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate') }
        },
        CurrencyPipe,
        DecimalPipe,
        CurrencyFormatPipe
      ]
    })
    .compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
    sharedService = TestBed.inject(SharedService);
    fixture = TestBed.createComponent(PostbackComponent);
    component = fixture.componentInstance;
  }));

  beforeEach(() => {
    component.config = userData.config;
    component.config['postBackType'] = '';
    fixture.detectChanges();
  });

  afterEach(() => {
    const fakeDataResponse = require('assets/mock/orderConfirmation.json');
    fakeDataResponse.b2sOrderId = 2100118077;
    TestBed.resetTestingModule();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call returnZero method', () => {
    spyOn(sharedService, 'returnZero').and.callThrough();
    sharedService.returnZero();
    expect(sharedService.returnZero).toHaveBeenCalled();
  });

  it('should call postBackControl for success response', waitForAsync(() => {
    const fakeDataResponse = require('assets/mock/orderConfirmation.json');
    fakeDataResponse.b2sOrderId = 2100118077;
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/order/orderInformation');
    expect(req.request.method).toEqual('GET');
    req.flush(fakeDataResponse);
    expect(fakeDataResponse.b2sOrderId).toBe(2100118077);
    expect(fakeDataResponse.cartTotal.price.points).toBe(348364);
  }));

  it('should call postBackControl for success response null b2sOrderId', waitForAsync(() => {
    const fakeDataResponse = require('assets/mock/orderConfirmation.json');
    fakeDataResponse.b2sOrderId = null;
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/order/orderInformation');
    expect(req.request.method).toEqual('GET');
    req.flush(fakeDataResponse);
    expect(fakeDataResponse.cartTotal.price.points).toBe(348364);
  }));

  it('should call postBackControl for failure response', waitForAsync(() => {
    // Expect a call to this URL
    const errorMsg = 'deliberate 404 error';
    const req = httpTestingController.expectOne('/apple-gr/service/order/orderInformation');
    expect(req.request.method).toEqual('GET');
    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

  it('should create for failure jwt postUrl', waitForAsync(() => {
    spyOn(component, 'postInfo').and.callThrough();
    component.postInfo(2100118077);
    // Expect a call to this URL
    const errorMsg = 'deliberate 404 error';
    const purchaseReq = httpTestingController.expectOne('/apple-gr/service/order/getPurchaseSelectionInfo/2100118077');
    purchaseReq.flush(errorMsg, { status: 404, statusText: 'Not Found' });
    expect(component.postInfo).toHaveBeenCalled();
  }));

  it('should create for postData success for postBackType', waitForAsync(() => {
    component.config.postBackType = 'api';
    fixture.detectChanges();
    const mockData: PurchaseSelectionInfo = {
      jwt: 'testData',
      payrollProviderRedirect: null,
      purchasePostUrl: 'javascript:void(0)',
      method: null
    };
    spyOn(component, 'postInfo').and.callThrough();
    component.postInfo(2100118077);
    // Expect a call to this URL
    const purchaseReq = httpTestingController.expectOne('/apple-gr/service/order/getPurchaseSelectionInfo/2100118077');
    expect(purchaseReq.request.method).toEqual('GET');
    purchaseReq.flush(mockData);
    expect(component.postInfo).toHaveBeenCalled();
  }));

  it('should create for postData success for postBackType', waitForAsync(() => {
    component.config.postBackType = 'api';
    fixture.detectChanges();
    const mockData: PurchaseSelectionInfo = {
      jwt: 'testData',
      payrollProviderRedirect: null,
      purchasePostUrl: null,
      method: null
    };
    spyOn(component, 'postInfo').and.callThrough();
    component.postInfo(2100118077);
    // Expect a call to this URL
    const purchaseReq = httpTestingController.expectOne('/apple-gr/service/order/getPurchaseSelectionInfo/2100118077');
    expect(purchaseReq.request.method).toEqual('GET');
    purchaseReq.flush(mockData);
    expect(component.postInfo).toHaveBeenCalled();
  }));

  it('should create for postData jwt', waitForAsync(() => {
    component.config.postBackType = '';
    fixture.detectChanges();
    document.body.innerHTML += '<form><input type="text" value="test"/><button type="submit" /></form>';
    spyOn(document.querySelector('form'), 'submit').and.callFake(() => null);
    spyOn(component, 'postInfo').and.callThrough();
    component.postInfo(2100118077);
    const mockData: PurchaseSelectionInfo = {
      jwt: 'testData',
      payrollProviderRedirect: null,
      purchasePostUrl: 'javascript:void(0)',
      method: null
    };
    // Expect a call to this URL
    const purchaseReq = httpTestingController.expectOne('/apple-gr/service/order/getPurchaseSelectionInfo/2100118077');
    expect(purchaseReq.request.method).toEqual('GET');
    purchaseReq.flush(mockData);
    expect(component.postInfo).toHaveBeenCalled();
  }));

  it('should create for postData jwt else check', waitForAsync(() => {
    component.config.postBackType = '';
    fixture.detectChanges();
    document.body.innerHTML += '<form><input type="text" value="test"/><button type="submit" /></form>';
    spyOn(document.querySelector('form'), 'submit').and.callFake(() => null);
    spyOn(component, 'postInfo').and.callThrough();
    component.postInfo(2100118077);
    const mockData: PurchaseSelectionInfo = {
      jwt: '',
      payrollProviderRedirect: null,
      purchasePostUrl: 'javascript:void(0)',
      method: null
    };
    mockData['itemDescription'] = 'Apple Watch Series 3 GPS, 38mm Space Gray Aluminum Case with Black Sport Band';
    // Expect a call to this URL
    const purchaseReq = httpTestingController.expectOne('/apple-gr/service/order/getPurchaseSelectionInfo/2100118077');
    expect(purchaseReq.request.method).toEqual('GET');
    purchaseReq.flush(mockData);
    expect(component.postInfo).toHaveBeenCalled();
  }));
});
