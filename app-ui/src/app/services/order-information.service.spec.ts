import { TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { OrderInformationService } from './order-information.service';
import { OrderStatus } from '@app/models/order-detail';

describe('OrderInformationService', () => {
  let orderInformationService: OrderInformationService;
  let httpTestingController: HttpTestingController;

  // Fake response data
  const fakeResponse = require('assets/mock/orderConfirmation.json');

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ],
      providers: [
        { provide: OrderInformationService },
      ]
    });

    // Inject the http test controller
    httpTestingController = TestBed.inject(HttpTestingController);
    orderInformationService = TestBed.inject(OrderInformationService);
  });

  afterEach(() => {
    // After every test, assert that there are no more pending requests.
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(orderInformationService).toBeTruthy();
  });

  it('should return Order Information', waitForAsync(() => {
    // Setup a request using the fakeResponse data
    orderInformationService.getOrderInformation().subscribe(
      (orderInfo: OrderStatus) => expect(orderInfo).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );

    // Expect a call to this URL
    const req = httpTestingController.expectOne(orderInformationService.orderUrl + 'orderInformation');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(fakeResponse);

    // Run some expectations
    expect(fakeResponse.b2sOrderId).toBe(2100118077);
    expect(fakeResponse.cartTotal.price.points).toBe(348364);
    expect(fakeResponse.cartTotal.totalFees.points).toBe(22372);
    expect(fakeResponse.orderCompleted).toBe(true);
  }));

  it('should test for 404 error', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';

    orderInformationService.getOrderInformation().subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne(orderInformationService.baseUrl + 'order/orderInformation');

    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

  it('should return Purchase Selection Info', waitForAsync(() => {
    // Setup a request using the fakeResponse data
    orderInformationService.getPurchaseSelectionInfo(2100118077).subscribe(
      (orderInfo: OrderStatus) => expect(orderInfo).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );

    // Expect a call to this URL
    const req = httpTestingController.expectOne(orderInformationService.orderUrl + 'getPurchaseSelectionInfo/2100118077');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(fakeResponse);

    // Run some expectations
    expect(fakeResponse.b2sOrderId).toBe(2100118077);
    expect(fakeResponse.cartTotal.price.points).toBe(348364);
    expect(fakeResponse.cartTotal.totalFees.points).toBe(22372);
    expect(fakeResponse.orderCompleted).toBe(true);
  }));

  it('should test for 404 error - getPurchaseSelectionInfo', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    orderInformationService.getPurchaseSelectionInfo(2100118077).subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne(orderInformationService.baseUrl + 'order/getPurchaseSelectionInfo/2100118077');
    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));
});
