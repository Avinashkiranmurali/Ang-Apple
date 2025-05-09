import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AddressModalComponent } from './address-modal.component';
import { TranslateModule } from '@ngx-translate/core';
import { UserStoreService } from '@app/state/user-store.service';
import { CartService } from '@app/services/cart.service';
import { TemplateStoreService } from '@app/state/template-store.service';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

describe('AddressModalComponent', () => {
  let component: AddressModalComponent;
  let fixture: ComponentFixture<AddressModalComponent>;
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;
  let templateStoreService: TemplateStoreService;
  let cartService: CartService;
  const config = require('assets/mock/configData.json');
  const programData = require('assets/mock/program.json');
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ AddressModalComponent ],
      providers: [
        { provide: NgbActiveModal, UserStoreService, CartService },
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate') }
        },
        { provide: UserStoreService, useValue: userData },
        { provide: MatomoService, useValue: {
          sendErrorToAnalyticService: () => {}
        }},
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot(),
        FormsModule
      ]
    })
    .compileComponents();
    httpClient = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);
    cartService = TestBed.inject(CartService);
    templateStoreService = TestBed.inject(TemplateStoreService);
    templateStoreService.addTemplate(config['configData']);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AddressModalComponent);
    component = fixture.componentInstance;
    const ADDRESS_MOCK = require('assets/mock/address.json');
    component.buttonColor = '';
    component.originalAddress = ADDRESS_MOCK[0];
    component.suggestedAddress = ADDRESS_MOCK[1];
    component.warningMessage = ADDRESS_MOCK[1].errorMessage;
    component.showSuggestedAddr = true;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call goToAddressFormPage method', () => {
    spyOn(component, 'goToAddressFormPage').and.callThrough();
    component.goToAddressFormPage();
    expect(component.goToAddressFormPage).toHaveBeenCalled();
  });

  it('should call useAddress method - suggested address option', () => {
    spyOn(component, 'useAddress').and.callThrough();
    component.useAddress('suggested');
    expect(component.useAddress).toHaveBeenCalled();
  });

  it('should call useAddress method - original address option', () => {
    spyOn(component, 'useAddress').and.callThrough();
    component.useAddress('original');
    expect(component.useAddress).toHaveBeenCalled();
  });

  it('should call verifySuggestionToCheckoutFlow method - original address option', () => {
    spyOn(component, 'verifySuggestionToCheckoutFlow').and.callThrough();
    const mockModifyAddressData = require('assets/mock/address.json')[1];
    component.config['fullCatalog'] = false;
    cartService['cartData'] = require('assets/mock/cart.json');
    fixture.detectChanges();
    component.verifySuggestionToCheckoutFlow(mockModifyAddressData);
    expect(component.verifySuggestionToCheckoutFlow).toHaveBeenCalled();
  });

  it('should call verifySuggestionToCheckoutFlow method - suggested address option', () => {
    spyOn(component, 'verifySuggestionToCheckoutFlow').and.callThrough();
    const mockModifyAddressData = require('assets/mock/address.json')[1];
    component.config['fullCatalog'] = true;
    cartService['cartData'] = require('assets/mock/cart.json');
    fixture.detectChanges();
    component.verifySuggestionToCheckoutFlow(mockModifyAddressData);
    expect(component.verifySuggestionToCheckoutFlow).toHaveBeenCalled();
  });


  it('should call modifyShippingAddress method with fullCatalog = false', waitForAsync(() => {
    component.config['fullCatalog'] = true;
    cartService['cartData'] = {};
    fixture.detectChanges();
    component.useAddress('suggested');
    const mockModifyAddressData = require('assets/mock/address.json')[1];
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(mockModifyAddressData);
  }));

  it('should test for 401 error', waitForAsync(() => {
    const errorMsg = 'deliberate 401 error';
    component.useAddress('original');
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');
    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');
    // Respond with mock error
    req.flush(errorMsg, { status: 401, statusText: 'Sessiontimeout' });
  }));

  it('should test for 404 error', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    component.useAddress('original');
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');
    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');
    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'not found' });
  }));

  it('should define ngOnInit method', () => {
    component.showSuggestedAddr = false;
    component.suggestedAddress = undefined;
    fixture.detectChanges();
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should define ngOnInit method', () => {
    const ADDRESS_MOCK = require('assets/mock/address.json');
    component.suggestedAddress = ADDRESS_MOCK[1];
    component.showSuggestedAddr = true;
    component.suggestedAddress.cartTotalModified = true;
    fixture.detectChanges();
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should define ngOnInit method', () => {
    component.showSuggestedAddr = true;
    component.suggestedAddress = undefined;
    fixture.detectChanges();
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  afterAll(() => {
    const configData = require('assets/mock/configData.json');
    config['configData'].templates.buttonColor = '';
    templateStoreService.addTemplate(configData['configData']);
  });

  it('should create', () => {
    config['configData'].templates.buttonColor = '#fff';
    templateStoreService.addTemplate(config['configData']);
    expect(component).toBeTruthy();
  });

});
