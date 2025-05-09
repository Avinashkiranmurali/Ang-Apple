import { ComponentFixture, inject, TestBed, waitForAsync } from '@angular/core/testing';
import { ShippingAddressComponent } from './shipping-address.component';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CartService } from '@app/services/cart.service';
import { of } from 'rxjs/internal/observable/of';
import { ShippingAddressFormDirective } from '@app/modules/shipping-address/shipping-address-form.directive';
import { ShippingAddressService } from './shipping-address.service';
import { UserStoreService } from '@app/state/user-store.service';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { ModalsService } from '@app/components/modals/modals.service';
import { TemplateStoreService } from '@app/state/template-store.service';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { DecimalPipe, KeyValuePipe } from '@angular/common';
import { AddressFormEnComponent } from '@app/modules/shipping-address/address-form-en/address-form-en.component';
import { AddressFormSgComponent } from '@app/modules/shipping-address/address-form-sg/address-form-sg.component';
import { AddressFormTwComponent } from '@app/modules/shipping-address/address-form-tw/address-form-tw.component';
import { AddressFormAuComponent } from '@app/modules/shipping-address/address-form-au/address-form-au.component';
import { AddressFormAeComponent } from '@app/modules/shipping-address/address-form-ae/address-form-ae.component';
import { AddressFormHkComponent } from '@app/modules/shipping-address/address-form-hk/address-form-hk.component';
import { AddressFormMyComponent } from '@app/modules/shipping-address/address-form-my/address-form-my.component';
import { AddressFormThComponent } from '@app/modules/shipping-address/address-form-th/address-form-th.component';
import { AddressFormMxComponent } from '@app/modules/shipping-address/address-form-mx/address-form-mx.component';
import { AddressFormPhComponent } from '@app/modules/shipping-address/address-form-ph/address-form-ph.component';
import { User } from '@app/models/user';
import { DataMaskingModule } from '@bakkt/data-masking';
import { SharedService } from '@app/modules/shared/shared.service';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';
import { TransitionService } from '@app/transition/transition.service';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

describe('ShippingAddressComponent', () => {
  let component: ShippingAddressComponent;
  let httpTestingController: HttpTestingController;
  let fixture: ComponentFixture<ShippingAddressComponent>;
  let addressFormComponent: AddressFormEnComponent;
  let addressFormFixture: ComponentFixture<AddressFormEnComponent>;

  const fg: FormGroup = new FormGroup({
    name: new FormControl('John'),
    businessName: new FormControl('abc'),
    phoneNumber: new FormControl('1448757'),
    email: new FormControl('abc@bakkt.com'),
    address1: new FormControl('5900 Windward Pkwy'),
    address2: new FormControl('Ste 450'),
    address3: new FormControl('street'),
    city: new FormControl('Alpharetta'),
    state: new FormControl('GA'),
    zip5: new FormControl('30005')
  });
  const cartData = require('assets/mock/cart.json');
  const programData = require('assets/mock/program.json');
  programData.config.ContactInfoLocked = false;
  programData.config.ShipToNameLocked = false;
  programData.config.businessNameLocked = false;
  programData.config.MercAddressLocked = false;
  const mockUser = require('assets/mock/user.json');
  mockUser['program'] = programData;
  const userData = {
    user: mockUser,
    program: programData,
    config: programData['config'],
    get: () => of(mockUser)
  };
  const mockUserCA = mockUser;
  mockUserCA.locale = 'en_CA';
  const userDataCA = {
    user: mockUserCA,
    program: programData,
    config: programData['config'],
    get: () => of(mockUserCA)
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        ShippingAddressComponent,
        ShippingAddressFormDirective,
        AddressFormEnComponent,
        AddressFormSgComponent,
        AddressFormTwComponent,
        AddressFormAuComponent,
        AddressFormAeComponent,
        AddressFormHkComponent,
        AddressFormMyComponent,
        AddressFormThComponent,
        AddressFormMxComponent,
        AddressFormPhComponent
      ],
      providers: [
        { provide: UserStoreService, useValue: userData },
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate') }
        },
        { provide: ModalsService, useValue: {
          openSuggestAddressModalComponent: () => {},
          openOopsModalComponent: () => {} }
        },
        { provide: TemplateStoreService },
        { provide: TransitionService },
        { provide: CartService },
        { provide: KeyValuePipe },
        { provide: ShippingAddressService },
        { provide: ActivatedRoute, useValue: {
          params: of({category: 'ipad', subcat: 'ipad-accessories', addCat: 'ipad-accessories-apple-pencil', psid: '30001MXG22LL/A' }) }
        },
        { provide: SharedService, useValue: {
          verifySkipPaymentOption: () => {} }
        },
        { provide: EnsightenService, useValue: {
          broadcastEvent: () => {} }
        },
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
        FormsModule,
        TranslateModule.forRoot(),
        ReactiveFormsModule,
        DataMaskingModule
      ]
    })
    .compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ShippingAddressComponent);
    addressFormFixture = TestBed.createComponent(AddressFormEnComponent);
    component = fixture.componentInstance;
    addressFormComponent = addressFormFixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
    component.appShippingAddressForm = {};
    component.errorMessage = {};
    component.shipAddress = cartData.shippingAddress;
    component.changeShipAddress = fg;
    component.config.fullCatalog = true;
    component.changeShipAddress.patchValue({
      phoneNumber: '(814) 384-7122',
      address1: '5900 Windward Pkwy',
      address2: 'Ste 450',
      address3: 'street',
      city: 'Taichung City',
      subCity: null,
      state: 'GA',
      zip5: '30005'
    });
    component.setAddressData(component.changeShipAddress.value);
    component.isMultiAddress = true;
    component['hasNoAddress'] = false;
    component.ctnButtonEnabled = false;
    component.overrideShipping = false;
    component.user = userData.user;
    component.config.ContactInfoLockOverrides = 'phoneNumber';
    component.config.MercAddressLockOverrides = '';
    fixture.detectChanges();
  });

  it('shippingAddress should create', () => {
    expect(component).toBeTruthy();
  });

  it('shippingAddress should create for CA locale', () => {
    component.user = userDataCA.user;
    component.addressFormCountry = component.user.locale.split('_')[1].toLowerCase();
    expect(component).toBeTruthy();
  });

  it('shippingAddress should create for US locale', () => {
    mockUser.locale = 'en_US';
    const userData = {
      user: mockUser,
      program: programData,
      config: programData['config'],
      get: () => of(mockUser)
    };
    component.user = userData.user;
    component.addressFormCountry = component.user.locale.split('_')[1].toLowerCase();
    expect(component.addressFormCountry).toEqual('us');
  });

  it('should create instance for non-english locale', () => {
    const mockUserForTWLocale = {
      programid: 'b2s_qa_only',
      userid: 'eric',
      varid: 'TW',
      additionalInfo: {
        countryCode: 'TW',
        languageCode: 'ZHO'
      },
      fullName: 'Eric Theall',
      varId: 'TW',
      userId: 'eric',
      balance: 999999999,
      programId: 'b2s_qa_only',
      locale: 'zh_TW',
      country: 'TW'
    };
    component.user = mockUserForTWLocale as unknown as User;
    component.config.ContactInfoLockOverrides = '';
    component.config.MercAddressLockOverrides = 'state';
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

/*   it('should call ngAfterViewInit method', waitForAsync(() => {
    component['hasNoAddress'] = false;
    component.isMultiAddress = false;
    component.changeShipAddress.patchValue({
      phoneNumber: '(814) 384-7122',
      address1: '5900 Windward Pkwy',
      address2: 'Ste 450',
      address3: 'street',
      city: 'Taichung City',
      subCity: null,
      state: 'GA',
      zip5: '30005'
    });
    fixture.detectChanges();
    const mockCartResponse = require('assets/mock/cart.json');
    mockCartResponse.shippingAddress.errorMessage = {
      firstName: 'First and Last name must be alpha characters, each with 1 characters minimum.',
      phoneNumber: 'Enter a valid phone number',
      state: 'Select state from dropdown'
    };
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/cart');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    req.flush(mockCartResponse);
    const stateResponse = require('assets/mock/getStates.json');
    // Expect a call to this URL
    const getStateReq = httpTestingController.expectOne('/apple-gr/service//address/getStates');
    // Assert that the request is a GET
    expect(getStateReq.request.method).toEqual('GET');
    // Respond with the fake data when called
    getStateReq.flush(stateResponse);
  })); */

  it('should call ngOnInit for fr_fr', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    const mockUserForFRLocale = {
      programid: 'b2s_qa_only',
      userid: 'eric',
      varid: 'RBC',
      additionalInfo: {
        countryCode: 'CA',
        languageCode: 'FR'
      },
      fullName: 'Eric Theall',
      varId: 'RBC',
      userId: 'eric',
      balance: 5457309,
      programId: 'b2s_qa_only',
      locale: 'fr_fr',
      country: 'CA'
    };
    component.user = mockUserForFRLocale as unknown as User;
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should call ngOnInit for ru_ru', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    const mockUserForRULocale = {
      programid: 'b2s_qa_only',
      userid: 'eric',
      varid: 'RU',
      additionalInfo: {
        countryCode: 'RU',
        languageCode: 'RU'
      },
      fullName: 'Eric Theall',
      varId: 'RU',
      userId: 'eric',
      balance: 999999999,
      programId: 'b2s_qa_only',
      locale: 'ru_ru',
      country: 'RU'
    };
    component.user = mockUserForRULocale as unknown as User;
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should call ngOnInit for en_au', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    const mockUserForAULocale = {
      programid: 'b2s_qa_only',
      userid: 'eric',
      varid: 'AU',
      additionalInfo: {
        countryCode: 'AU',
        languageCode: 'ENG'
      },
      fullName: 'Eric Theall',
      varId: 'AU',
      userId: 'eric',
      balance: 7893015,
      programId: 'b2s_qa_only',
      locale: 'en_AU',
      country: 'AU'
    };
    component.user = mockUserForAULocale as unknown as User;
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  /* it('should call ngOnInit for zh_TW', waitForAsync(() => {
    const mockUserForAULocale = {
      programid: 'b2s_qa_only',
      userid: 'eric',
      varid: 'TW',
      additionalInfo: {
        countryCode: 'TW',
        languageCode: 'ZHO'
      },
      fullName: 'Eric Theall',
      varId: 'TW',
      userId: 'eric',
      balance: 7893015,
      programId: 'b2s_qa_only',
      locale: 'zh_TW',
      country: 'TW'
    };
    component.user = mockUserForAULocale as unknown as User;
    fixture.detectChanges();
    const mockCartResponse = require('assets/mock/cart.json');
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/cart');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    req.flush(mockCartResponse);
    const stateResponse = require('assets/mock/getCities.json');
    // Expect a call to this URL
    const getStateReq = httpTestingController.expectOne('/apple-gr/service//address/cities');
    // Assert that the request is a GET
    expect(getStateReq.request.method).toEqual('GET');
    // Respond with the fake data when called
    getStateReq.flush(stateResponse);
  })); */

  it('should call submitAddressChange without error messages for fields', waitForAsync(() => {
    component.changeShipAddress.patchValue({
      phoneNumber: '(814) 384-7122',
      address1: '5900 Windward Pkwy',
      address2: 'Ste 450',
      address3: 'street',
      city: 'Taichung City',
      subCity: null,
      state: 'GA',
      zip5: '30005',
      name: 'KINDRA CONNOR'
    });
    fixture.detectChanges();
    spyOn(component, 'submitAddressChange').and.callThrough();
    component.submitAddressChange();
    expect(component.submitAddressChange).toHaveBeenCalled();

    const mockResponse = require('assets/mock/address.json')[1];
    mockResponse.warningMessage = {};
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');
    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');
    // Respond with the fake data when called
    req.flush(mockResponse);
  }));

  it('should call submitAddressChange with warning messages for fields', waitForAsync(() => {
    component.changeShipAddress.patchValue({
      phoneNumber: '(814) 384-7122',
      address1: '5900 Windward Pkwy',
      address2: 'Ste 450',
      address3: 'street',
      city: 'Taichung City',
      subCity: null,
      state: 'GA',
      zip5: '30005',
      name: 'KINDRA CONNOR'
    });
    fixture.detectChanges();
    spyOn(component, 'submitAddressChange').and.callThrough();
    component.submitAddressChange();
    expect(component.submitAddressChange).toHaveBeenCalled();

    const mockData = require('assets/mock/address.json')[1];
    mockData.warningMessage = { state: 'Select state from dropdown' };
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');
    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');
    // Respond with the fake data when called
    req.flush(mockData);
  }));

  it('should call submitAddressChange - with error response 401', waitForAsync(() => {
    component.changeShipAddress.patchValue({
      phoneNumber: '(814) 384-7122',
      address1: '5900 Windward Pkwy',
      address2: 'Ste 450',
      address3: 'street',
      city: 'Taichung City',
      subCity: null,
      state: 'GA',
      zip5: '30005',
      name: 'KINDRA CONNOR'
    });
    fixture.detectChanges();
    spyOn(component, 'submitAddressChange').and.callThrough();
    component.submitAddressChange();
    expect(component.submitAddressChange).toHaveBeenCalled();

    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');
    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');
    // Respond with the fake data when called
    req.flush('Error', { status: 401, statusText: 'Error Data'});
  }));

  it('should call submitAddressChange - with error response 404', waitForAsync(() => {
    component.changeShipAddress.patchValue({
      phoneNumber: '(814) 384-7122',
      address1: '5900 Windward Pkwy',
      address2: 'Ste 450',
      address3: 'street',
      city: 'Taichung City',
      subCity: null,
      state: 'GA',
      zip5: '30005',
      name: 'KINDRA CONNOR'
    });
    fixture.detectChanges();
    spyOn(component, 'submitAddressChange').and.callThrough();
    component.submitAddressChange();
    expect(component.submitAddressChange).toHaveBeenCalled();

    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');
    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');
    // Respond with the fake data when called
    req.flush('No Found', { status: 404, statusText: 'Not Found - Error Data'});
  }));

  it('should call submitAddressChange - with error response 500', waitForAsync(() => {
    component.changeShipAddress.patchValue({
      phoneNumber: '(814) 384-7122',
      address1: '5900 Windward Pkwy',
      address2: 'Ste 450',
      address3: 'street',
      city: 'Taichung City',
      subCity: null,
      state: 'GA',
      zip5: '30005',
      name: 'KINDRA CONNOR'
    });
    fixture.detectChanges();
    spyOn(component, 'submitAddressChange').and.callThrough();
    component.submitAddressChange();
    expect(component.submitAddressChange).toHaveBeenCalled();

    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');
    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');
    // Respond with the fake data when called
    req.flush('Internal Server Issue', { status: 500, statusText: 'Internal Server Issue'});
  }));

  it('should call submitAddressChange for invalid address fields', () => {
    for ( const formControlKey in component.changeShipAddress.controls ) {
      if ( component.changeShipAddress.controls.hasOwnProperty(formControlKey) ) {
        component.changeShipAddress.controls[formControlKey].enable();
      }
    }
    component.changeShipAddress.patchValue({
      name: '',
      businessName: '',
      phoneNumber: ''
    });
    addressFormComponent.multiAddressSelect(0);
    fixture.detectChanges();
    spyOn(component, 'submitAddressChange').and.callThrough();
    component.submitAddressChange();
    expect(component.submitAddressChange).toHaveBeenCalled();
  });

  it('should call multiAddressSelect', () => {
    spyOn(component, 'multiAddressSelect').and.callThrough();
    addressFormComponent.multiAddressSelect(0);
    component.multiAddressSelect(component.user.addresses[0].addressId);
    expect(component.multiAddressSelect).toHaveBeenCalled();
  });

  it('should call multiAddressSelect', () => {
    spyOn(component, 'multiAddressSelect').and.callThrough();
    component.overrideShipping = true;
    fixture.detectChanges();
    component.multiAddressSelect(component.user.addresses[0].addressId);
    expect(component.multiAddressSelect).toHaveBeenCalled();
  });

  it('should call setCity method', () => {
    const mockCartResponse = require('assets/mock/cart.json');
    spyOn(component, 'setCity').and.callThrough();
    // component.overrideShipping = true;
    fixture.detectChanges();
    component.setCity(mockCartResponse.shippingAddress,{ instance: {} });
    expect(component.setCity).toHaveBeenCalled();
  });

  it('should call getCart method', inject([CartService], (cartService) => {
    component.isMultiAddress = false;
    fixture.detectChanges();
    const mockCartResponse = require('assets/mock/cart.json');
    mockCartResponse.shippingAddress.selectedAddressId = 0;
    spyOn(component, 'setAddressData');
    spyOn(cartService, 'getCart').and.returnValue(of(mockCartResponse));
    component.getCart({ instance: {} });
    expect(component.setAddressData).toHaveBeenCalled();
  }));

  it('should call getCart method for selectedAddressId to hide extra address', waitForAsync(() => {
    component.isMultiAddress = false;
    fixture.detectChanges();
    const mockCartResponse = require('assets/mock/cart.json');
    mockCartResponse.shippingAddress.selectedAddressId = '';
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/cart');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    req.flush(mockCartResponse);
  }));

  it('should call getCart method for 0 address id', waitForAsync(() => {
    component.isMultiAddress = true;
    component.user.addresses.push({addressId: 0} as any);
    fixture.detectChanges();
    const mockCartResponse = require('assets/mock/cart.json');
    mockCartResponse.shippingAddress.selectedAddressId = 0;
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/cart');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    req.flush(mockCartResponse);
  }));

  it('should call getCart method for override shipping address', waitForAsync(() => {
    component.isMultiAddress = true;
    component.overrideShipping = false;
    fixture.detectChanges();
    const mockCartResponse = require('assets/mock/cart.json');
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/cart');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    req.flush(mockCartResponse);
  }));

  it('should call getCart method for override shipping address else check', waitForAsync(() => {
    component.isMultiAddress = true;
    fixture.detectChanges();
    const response = require('assets/mock/cart.json');
    response.shippingAddress.selectedAddressId = null;
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/cart');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    req.flush(response);
  }));

  it('should call setAddressData for empty value', () => {
    spyOn(component, 'setAddressData').and.callThrough();
    component.setAddressData({});
    expect(component.setAddressData).toHaveBeenCalled();
  });

  it('should call setShippingAddressName for reverse name order', () => {
    spyOn(component, 'setShippingAddressName').and.callThrough();
    component.reverseNameOrder = true;
    const shipAddress = {firstName: 'John', lastName: 'Eric'};
    component.setShippingAddressName(shipAddress, 'concat');
    expect(component.setShippingAddressName).toHaveBeenCalled();
  });

  it('should call setShippingAddressName for reverse name order with name', () => {
    spyOn(component, 'setShippingAddressName').and.callThrough();
    component.reverseNameOrder = true;
    const shipAddress = {name: 'Hubert Blaine Wolfeschlegelsteinhausenbergerdorff Sr'};
    component.setShippingAddressName(shipAddress);
    expect(component.setShippingAddressName).toHaveBeenCalled();
  });

  it('should call setShippingAddressName when name space exists', () => {
    spyOn(component, 'setShippingAddressName').and.callThrough();
    const shipAddress = { name: 'Eric' };
    component.setShippingAddressName(shipAddress);
    expect(component.setShippingAddressName).toHaveBeenCalled();
  });

  it('should call setShipAddressForm', () => {
    spyOn(component, 'setShipAddressForm').and.callThrough();
    component.setShipAddressForm(fg);
    expect(component.setShipAddressForm).toHaveBeenCalled();
  });

  it('should call disableButton', () => {
    spyOn(component, 'disableButton').and.callThrough();
    component.isMultiAddress = true;
    component.ctnButtonEnabled = true;
    component.disableButton();
    expect(component.disableButton).toHaveBeenCalled();
  });

  it('should call toNextPage - Navigate to Payment', () => {
    spyOn(component, 'toNextPage').and.callThrough();
    component.toNextPage();
    expect(component.toNextPage).toHaveBeenCalled();
  });

  it('should call toNextPage - Navigate to Checkout', () => {
    component.config.fullCatalog = false;
    fixture.detectChanges();
    spyOn(component, 'toNextPage').and.callThrough();
    component.toNextPage();
    expect(component.toNextPage).toHaveBeenCalled();
  });

  it('should call cancel', () => {
    spyOn(component, 'cancel').and.callThrough();
    component.cancel();
    expect(component.cancel).toHaveBeenCalled();
  });

  it('should call clearErrors', () => {
    component.errorMessage = { state: 'Select state from dropdown' };
    fixture.detectChanges();
    spyOn(component, 'clearErrors').and.callThrough();
    component.clearErrors();
    expect(component.clearErrors).toHaveBeenCalled();
  });

  it('should call updateAddressErrors', () => {
    spyOn(component, 'updateAddressErrors').and.callThrough();
    component.errorMessage = {firstName: 'Eric', lastName: 'John'};
    component.updateAddressErrors();
    expect(component.updateAddressErrors).toHaveBeenCalled();
  });

  it('should call updateAddressErrors', () => {
    spyOn(component, 'updateAddressErrors').and.callThrough();
    component.errorMessage = {firstName: '', lastName: 'Eric'};
    component.updateAddressErrors();
    expect(component.updateAddressErrors).toHaveBeenCalled();
  });

  it('should call updateAddressErrors', () => {
    spyOn(component, 'updateAddressErrors').and.callThrough();
    component.errorMessage = {firstName: 'John', lastName: ''};
    component.updateAddressErrors();
    expect(component.updateAddressErrors).toHaveBeenCalled();
  });

  it('should return false when displayAddressCTA is called with hasNoAddress value true', () => {
    component['hasNoAddress']  = true;
    const bool = component.displayAddressCTA();
    expect(bool).toBeFalsy();
  });

  it('should return true when displayAddressCTA is called', () => {
    component.user.addresses = undefined;
    component.config.MercAddressLocked = true;
    component.config.ContactInfoLocked = true;
    component.config.ShipToNameLocked = true;
    let bool = component.displayAddressCTA();
    expect(bool).toBeTruthy();
    component.config.businessNameLocked = true;
    bool = component.displayAddressCTA();
    expect(bool).toBeTruthy();
    component['contactInfoLockOverrides'] = ['phoneNumber'];
    component['mercAddressLockOverrides'] = ['state'];
    bool = component.displayAddressCTA();
    expect(bool).toBeFalsy();
  });

  it('should return empty string when updateErrorMessage is called', () => {
    component.errorCount = 0;
    component.updateErrorMessage();
    expect(component.errorCountLabel).toBe('');
  });

});
