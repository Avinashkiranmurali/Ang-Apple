import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed, fakeAsync, tick, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { AddressService } from './address.service';
import { of } from 'rxjs';
import { ModalsService } from '@app/components/modals/modals.service';
import { TemplateStoreService } from '@app/state/template-store.service';
import { Address } from '@app/models/address';
import { Router } from '@angular/router';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';

describe('AddressService', () => {
  let addressService: AddressService;
  let httpTestingController: HttpTestingController;
  const cartData = require('assets/mock/cart.json');
  const programData = require('assets/mock/program.json');
  const fakeResponse = require('assets/mock/cart.json')['cartItems'];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule,
        HttpClientTestingModule],
        providers: [
          { provide: TemplateStoreService, useValue: {
              buttonColor: () => of({})
            }
          },
          { provide: ModalsService, useValue: {
            openOopsModalComponent: () => of({}),
            openSuggestAddressModalComponent: () => of({}) }
          },
          { provide: Router, useValue: {
            navigate: jasmine.createSpy('navigate') }
          },
          CurrencyPipe,
          CurrencyFormatPipe,
          DecimalPipe
        ]
    });
    addressService = TestBed.inject(AddressService);
    httpTestingController = TestBed.inject(HttpTestingController);
    addressService.config = programData['config'];
  });

  it('should be created', () => {
    expect(addressService).toBeTruthy();
  });

  it('should call decodeAddress method', () => {
    spyOn(addressService, 'decodeAddress').and.callThrough();
    addressService.decodeAddress(cartData['shippingAddress']);
    expect(addressService.decodeAddress).toHaveBeenCalled();
  });

  it('should call modifyShippingAddress method with transition', waitForAsync(() => {
    spyOn(addressService, 'modifyShippingAddress').and.callThrough();
    addressService.modifyShippingAddress(cartData['shippingAddress'], false);
    expect(addressService.modifyShippingAddress).toHaveBeenCalled();

    const mockModifyAddressData = {
      firstName: '1',
      middleName: '',
      lastName: '1',
      businessName: 'Unit Testing',
      address1: '5900 Windward Pkwy',
      address2: 'Ste 450',
      address3: 'Street',
      subCity: null,
      city: 'Alpharetta',
      state: 'GA',
      zip5: '30005',
      zip4: '5479',
      country: 'US',
      phoneNumber: '1',
      faxNumber: '',
      email: 'msankaradoss@bridge2solutions.com',
      errorMessage: {
        firstName: 'First and Last name must be alpha characters, each with 1 characters minimum.',
        phoneNumber: 'Enter a valid phone number'
      },
      warningMessage: {},
      ignoreSuggestedAddress: 'false',
      cartTotalModified: false,
      selectedAddressId: 0,
      addressModified: 'Y',
      validAddress: false
    };
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(mockModifyAddressData);
  }));

  it('should call modifyShippingAddress method with transition and warning message', waitForAsync(() => {
    spyOn(addressService, 'modifyShippingAddress').and.callThrough();
    addressService.modifyShippingAddress(cartData['shippingAddress'], false);
    expect(addressService.modifyShippingAddress).toHaveBeenCalled();

    const mockModifyAddressData = {
      firstName: '1',
      middleName: '',
      lastName: '1',
      businessName: 'Unit Testing',
      address1: '5900 Windward Pkwy',
      address2: 'Ste 450',
      address3: 'Street',
      subCity: null,
      city: 'Alpharetta',
      state: 'GA',
      zip5: '30005',
      zip4: '5479',
      country: 'US',
      phoneNumber: '1',
      faxNumber: '',
      email: 'msankaradoss@bridge2solutions.com',
      errorMessage: {},
      warningMessage: {
        firstName: 'First and Last name must be alpha characters, each with 1 characters minimum.',
        phoneNumber: 'Enter a valid phone number'},
      ignoreSuggestedAddress: 'false',
      cartTotalModified: false,
      selectedAddressId: 0,
      addressModified: 'Y',
      validAddress: false
    };
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(mockModifyAddressData);
  }));

  it('should call modifyShippingAddress method with transition and warning message', waitForAsync(() => {
    spyOn(addressService, 'modifyShippingAddress').and.callThrough();
    addressService.modifyShippingAddress(cartData['shippingAddress'], false);
    expect(addressService.modifyShippingAddress).toHaveBeenCalled();

    const mockModifyAddressData = {
      firstName: '1',
      middleName: '',
      lastName: '1',
      businessName: 'Unit Testing',
      address1: '5900 Windward Pkwy',
      address2: 'Ste 450',
      address3: 'Street',
      subCity: null,
      city: 'Alpharetta',
      state: 'GA',
      zip5: '30005',
      zip4: '5479',
      country: 'US',
      phoneNumber: '1',
      faxNumber: '',
      email: 'msankaradoss@bridge2solutions.com',
      errorMessage: {},
      warningMessage: {
        firstName: 'First and Last name must be alpha characters, each with 1 characters minimum.',
        phoneNumber: 'Enter a valid phone number'},
      ignoreSuggestedAddress: 'false',
      cartTotalModified: false,
      selectedAddressId: 0,
      addressModified: null,
      validAddress: false
    };
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(mockModifyAddressData);
  }));

  it('should call modifyShippingAddress method with error messages', waitForAsync(() => {
    spyOn(addressService, 'modifyShippingAddress').and.callThrough();
    addressService.modifyShippingAddress(cartData['shippingAddress'], false);
    expect(addressService.modifyShippingAddress).toHaveBeenCalled();

    const mockResponse = require('assets/mock/address.json')[1];
    mockResponse.errorMessage = {
      firstName: 'First and Last name must be alpha characters, each with 1 characters minimum.',
      phoneNumber: 'Enter a valid phone number'
    };
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(mockResponse);
  }));

  it('should call modifyShippingAddress method without error messages', waitForAsync(() => {
    spyOn(addressService, 'modifyShippingAddress').and.callThrough();
    addressService.modifyShippingAddress(cartData['shippingAddress'], false);
    expect(addressService.modifyShippingAddress).toHaveBeenCalled();

    const mockResponse = require('assets/mock/address.json')[1];
    mockResponse.errorMessage = {};
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(mockResponse);
  }));

  it('should call modifyShippingAddress method without transition', () => {
    spyOn(addressService, 'modifyShippingAddress').and.callThrough();
    addressService.modifyShippingAddress('', false);
    expect(addressService.modifyShippingAddress).toHaveBeenCalled();
  });

  it('should test for  501 error', waitForAsync(() => {
    const errorMsg = 'deliberate 501 error';
    addressService.modifyShippingAddress(cartData['shippingAddress'], false);
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');
    // Respond with mock error
    req.flush(errorMsg, { status: 501, statusText: 'Address error' });
  }));

  it('should call modifyShippingAddress method without errormessages and skipPaymentOption = true', fakeAsync(() => {
    addressService.config['fullCatalog'] = true;
    tick(100);
    addressService.modifyShippingAddress(cartData['shippingAddress'], true);

    const response = require('assets/mock/address.json')[1];
    response.errorMessage = {};
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(response);
  }));

  it('should call modifyShippingAddress method without errormessages and fullCatalog = true', fakeAsync(() => {
    addressService.config['fullCatalog'] = true;
    tick(100);
    addressService.modifyShippingAddress(cartData['shippingAddress'], false);

    const response = require('assets/mock/address.json')[1];
    response.errorMessage = {};
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(response);
  }));

  it('should call modifyShippingAddress method with fullCatalog = false', fakeAsync(() => {
    addressService.config['fullCatalog'] = false;
    tick(100);
    addressService.modifyShippingAddress(cartData['shippingAddress'], false);

    const response = require('assets/mock/address.json')[1];
    response.errorMessage = {};
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(response);
  }));

  it('should test for 401 error', waitForAsync(() => {
    const errorMsg = 'deliberate 401 error';
    addressService.modifyShippingAddress(cartData['shippingAddress'], false);
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');
    // Respond with mock error
    req.flush(errorMsg, { status: 401, statusText: 'Sessiontimeout' });
  }));

  it('should test for 404 error', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    addressService.modifyShippingAddress(cartData['shippingAddress'], false);
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');
    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Sessiontimeout' });
  }));

  it('should call modifyShippingAddress method returnValue is null', waitForAsync(() => {
    const modifyCartAddress: Address = Object.assign({});
    addressService.modifyShippingAddress(cartData['shippingAddress'], true);
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');
    // Respond with the fake data when called
    req.flush(modifyCartAddress);
  }));

  it('should call prepareErrorWarningObj method', () => {
    spyOn(addressService['sharedService'], 'prepareErrorWarningObj').and.callThrough();
    const errorObject = {
      firstName: 'First and Last name must be alpha characters, each with 1 characters minimum.',
      phoneNumber: 'Enter a valid phone number',
      'firstName,lastName' : 'Enter valid data'
    };
    addressService['sharedService'].prepareErrorWarningObj(errorObject);
    expect(addressService['sharedService'].prepareErrorWarningObj).toHaveBeenCalled();
  });

  it('should get the states', waitForAsync(() => {
    // Setup a request using the fakeResponse data
    addressService.getStates().subscribe(
      (response) => expect(response).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );
    // Expect a call to this URL
    const req = httpTestingController.expectOne(addressService.baseUrl + '/address/getStates');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(fakeResponse);

    // Run some expectations
    expect(fakeResponse.length).toBe(4);
    expect(fakeResponse[0].id).toBe(230624);
    expect(fakeResponse[0].productName).toBe('Apple TV HD 32GB');
    expect(fakeResponse[0].productDetail.categories[0].slug).toBe('apple-tv-apple-tv');
    expect(fakeResponse[1].id).toBe(230623);
  }));

  it('should test for 404 error', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    addressService.getStates().subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne(addressService.baseUrl + '/address/getStates');

    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

  it('should get the cities', waitForAsync(() => {
    // Setup a request using the fakeResponse data
    addressService.getCities().subscribe(
      (response) => expect(response).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );
    // Expect a call to this URL
    const req = httpTestingController.expectOne(addressService.baseUrl + '/address/cities');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(fakeResponse);

    // Run some expectations
    expect(fakeResponse.length).toBe(4);
    expect(fakeResponse[0].id).toBe(230624);
    expect(fakeResponse[0].productName).toBe('Apple TV HD 32GB');
    expect(fakeResponse[0].productDetail.categories[0].slug).toBe('apple-tv-apple-tv');
    expect(fakeResponse[1].id).toBe(230623);
  }));

  it('should test for 404 error', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    addressService.getCities().subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne(addressService.baseUrl + '/address/cities');
    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

  it('should get the states - getStateProvince', waitForAsync(() => {
    const mockStateResponse = require('assets/mock/getStates.json');
    // Setup a request using the fakeResponse data
    addressService.getStateProvince().subscribe(
      (response) => expect(response).toEqual(mockStateResponse, 'should return fakeResponse'), fail
    );
    // Expect a call to this URL
    const req = httpTestingController.expectOne(addressService.baseUrl + 'address/getStates');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(mockStateResponse);
  }));

  it('should test for 404 error - getStateProvince', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    addressService.getStateProvince().subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne(addressService.baseUrl + 'address/getStates');

    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

  it('should call modifyAddress method - success response', waitForAsync(() => {
    const cartData = require('assets/mock/cart.json');
    // Setup a request using the fakeResponse data
    addressService.modifyAddress(cartData['shippingAddress']).subscribe(
      (response) => expect(response).toBeTruthy(), fail
    );

    const mockModifyAddressData = require('assets/mock/address.json')[1];
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(mockModifyAddressData);
  }));

  it('should test for 401 error - modifyAddress', waitForAsync(() => {
    const errorMsg = 'deliberate 401 error';
    const cartData = require('assets/mock/cart.json');
    addressService.modifyAddress(cartData['shippingAddress']).subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(401, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/address/modifyCartAddress');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');
    // Respond with mock error
    req.flush(errorMsg, { status: 401, statusText: 'Sessiontimeout' });
  }));

  it('should call modifyShippingAddress when skipPaymentOption is true', () => {
    const address : Address = {
      firstName: 'KINDRA',
      middleName: '',
      lastName: 'CONNOR',
      businessName: 'test',
      address1: '5900 Windward Pkwy',
      address2: 'Ste 450',
      address3: 'street',
      subCity: null,
      city: 'Alpharetta',
      state: 'GA',
      zip5: 30005,
      zip4: '',
      country: 'US',
      phoneNumber: '(814) 384-7122',
      faxNumber: '',
      email: 'msankaradoss@bridge2solutions.com',
      errorMessage: null,
      warningMessage: null,
      ignoreSuggestedAddress: false,
      cartTotalModified: false,
      selectedAddressId: 0,
      addressModified: null,
      validAddress: true
    };
    spyOn(addressService, 'modifyShippingAddress').and.callThrough();
    spyOn(addressService, 'modifyAddress').and.callFake(() => of(address));
    spyOn(addressService, 'decodeAddress').and.callFake(() => address);
    addressService.modifyShippingAddress(cartData['shippingAddress'], false);
    addressService['config']['fullCatalog'] = true;
    addressService.modifyShippingAddress(cartData['shippingAddress'], true);
    addressService.modifyShippingAddress(cartData['shippingAddress'], false);
    expect(addressService.modifyShippingAddress).toHaveBeenCalled();
  });

});
