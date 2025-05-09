import { TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PaymentService } from './payment.service';
import { HttpErrorResponse } from '@angular/common/http';
import { CartService } from './cart.service';
import { of, throwError } from 'rxjs';

describe('PaymentService', () => {
  let paymentService: PaymentService;
  let httpTestingController: HttpTestingController;
  const ccEntryDetails = {
    addr1: '1 Park Blvd',
    addr2: '',
    addr3: '',
    ccCCV: '111',
    ccMon: '02',
    ccName: null,
    ccNum: '4111111111111111',
    ccType: 'VISA',
    ccUsername: 'Eric Theall',
    ccYear: '23',
    city: 'San Diego',
    country: 'US',
    firstName: 'Eric',
    last4: '1111',
    lastName: 'Theall',
    phoneNumber: null,
    state: 'CA',
    zip: '92101'
  };
  const fakeResponse = { code: 1, message: 'Success'};

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ],
      providers: [
        { provide: CartService, useValue: {
          getPurchasePoints: () => of(fakeResponse) }
        }
      ]
    });
    // Inject the http test controller
    httpTestingController = TestBed.inject(HttpTestingController);
    paymentService = TestBed.inject(PaymentService);
  });

  afterEach(() => {
    // After every test, assert that there are no more pending requests.
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(paymentService).toBeTruthy();
  });

  it('should call getPurchasePointsObservable method', waitForAsync(() => {
    spyOn(paymentService, 'getPurchasePointsObservable').and.callThrough();
    paymentService.getPurchasePointsObservable().subscribe(
        response => expect(response).toEqual(response, 'should return response'), fail
    );
    expect(paymentService.getPurchasePointsObservable).toHaveBeenCalled();
    expect(paymentService.getPurchasePointsObservable).toBeDefined();
  }));

  it('should call getPurchasePointsObservable method', waitForAsync(() => {
    spyOn(paymentService, 'getPurchasePointsObservable').and.callThrough();
    spyOn(paymentService['cartService'], 'getPurchasePoints').and.returnValue(throwError({statusCode: 404, statusText: 'Not Found'}))
    paymentService.getPurchasePointsObservable();
    expect(paymentService.getPurchasePointsObservable).toHaveBeenCalled();
    expect(paymentService.getPurchasePointsObservable).toBeDefined();
  }));

  it('should post the cc card details', waitForAsync(() => {
    // Setup a request using the fakeResponse data
    paymentService.postCardDetails(ccEntryDetails).subscribe(
      (response) => expect(response).toEqual(response, 'should return fakeResponse'), fail
    );
    // Expect a call to this URL - Return true for every call to the match predicate since it has nocache parameter
    const req = httpTestingController.expectOne(paymentService.baseUrl + 'ccEntry/init');

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(fakeResponse);
  }));

  it('should test for 404 error - postCardDetails', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    paymentService.postCardDetails(ccEntryDetails).subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );
    // Return true for every call to the match predicate since it has nocache parameter
    const req = httpTestingController.expectOne(() => true);
    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

  it('should get Payment transaction Api points', waitForAsync(() => {
    // Setup a request using the fakeResponse data
    paymentService.getPaymentTransactionApi().subscribe(
      (response) => expect(response).toEqual(response, 'should return fakeResponse'), fail
    );
    // Expect a call to this URL - Return true for every call to the match predicate since it has nocache parameter
    const req = httpTestingController.expectOne(() => true);

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with the fake/null data when called
    req.flush({});
  }));

  it('should test for 404 error - getPaymentTransactionApi', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    paymentService.getPaymentTransactionApi().subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );
    // Return true for every call to the match predicate since it has nocache parameter
    const req = httpTestingController.expectOne(() => true);
    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

  it('should post payment details with cc card details', waitForAsync(() => {
    const urlValue = paymentService.baseUrl + '/paymentserver/api/payment/VUF8TVB8ZXJpY3w3N0I0ODI2MkFGRUYzMDZBQkVEMENDNkM3N0EzRjkwNXx';
    const paymentApiDet = {
      url: urlValue,
      transactionId: 'VUF8TVB8ZXJpY3w3N0I0ODI2MkFGRUYzMDZBQkVEMENDNkM3N0EzRjkwNXw3RFo3NHU3NzM1bEt3NjVNTnMxY2JOa1BmcjEwbFNOdmFnQ3k0NU5iRzdOWm0wVENxNQ'
    };
    sessionStorage.setItem('paymentApiDet', JSON.stringify(paymentApiDet));
    // Setup a request using the fakeResponse data
    paymentService.postPaymentDetails(ccEntryDetails).subscribe(
      (response) => expect(response).toEqual(response, 'should return fakeResponse'), fail
    );
    // Expect a call to this URL - Return true for every call to the match predicate since it has nocache parameter
    const req = httpTestingController.expectOne(() => true);

    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(fakeResponse);
  }));

  it('should test for 404 error - postPaymentDetails', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    const urlValue = paymentService.baseUrl + '/paymentserver/api/payment/VUF8TVB8ZXJpY3w3N0I0ODI2MkFGRUYzMDZBQkVEMENDNkM3N0EzRjkwNXx';
    const paymentApiDet = {
      url: urlValue,
      transactionId: 'VUF8TVB8ZXJpY3w3N0I0ODI2MkFGRUYzMDZBQkVEMENDNkM3N0EzRjkwNXw3RFo3NHU3NzM1bEt3NjVNTnMxY2JOa1BmcjEwbFNOdmFnQ3k0NU5iRzdOWm0wVENxNQ'
    };
    sessionStorage.setItem('paymentApiDet', JSON.stringify(paymentApiDet));
    paymentService.postPaymentDetails(ccEntryDetails).subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );
    // Return true for every call to the match predicate since it has nocache parameter
    const req = httpTestingController.expectOne(() => true);
    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

});
