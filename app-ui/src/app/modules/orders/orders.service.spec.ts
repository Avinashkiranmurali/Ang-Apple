import { TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { OrderDetail } from '@app/models/order-detail';
import { OrdersService } from '@app/modules/orders/orders.service';

describe('ordersService', () => {
  let ordersService: OrdersService;
  let httpTestingController: HttpTestingController;

  // Fake response data
  const fakeResponse = require('assets/mock/order-history.json');
  const fakeResponseDetails = require('assets/mock/order-history-details.json');
  const orderId = 2100118073; // orderId found in above test json


  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ],
      providers: [
        { provide: OrdersService },
      ]
    });

    // Inject the http test controller
    httpTestingController = TestBed.inject(HttpTestingController);
    ordersService = TestBed.inject(OrdersService);
  });

  afterEach(() => {
    // After every test, assert that there are no more pending requests.
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(ordersService).toBeTruthy();
  });

  it('should return Order History', waitForAsync(() => {
    // Setup a request using the fakeResponse data
    ordersService.getOrderHistory(60).subscribe(
      (orderHistory: Array<OrderDetail>) => expect(orderHistory).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );

    // Expect a call to this URL
    const req = httpTestingController.expectOne(ordersService.baseUrl + 'order/orderHistory?days=60');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(fakeResponse);

    // Run some expectations
    expect(fakeResponse.length).toBe(1);
    expect(fakeResponse[0].orderId).toBe(2100118073);
    expect(fakeResponse[0].orderTotal.amount).toBe(999);
    expect(fakeResponse[0].orderTotal.points).toBe(435564);
    expect(fakeResponse[0].paymentInfo.awardsUsed).toBe(435564);
  }));

  it('should return Order History Details', waitForAsync(() => {
    // Setup a request using the fakeResponse data
    ordersService.getOrderDetails(orderId).subscribe(
      (data: OrderDetail) => expect(data).toEqual(fakeResponseDetails, 'should return fakeResponse'), fail
    );

    // Expect a call to this URL
    const req = httpTestingController.expectOne(ordersService.baseUrl + `order/orderHistoryDetails/${orderId}`);

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(fakeResponseDetails);

    // Run some expectations
    expect(fakeResponseDetails.orderId).toBe(orderId);
    expect(fakeResponseDetails.orderTotal.amount).toBe(999);
    expect(fakeResponseDetails.paymentInfo.awardsUsed).toBe(435564);
    expect(fakeResponseDetails.lineItems[0].sku).toBe('MWTK2LL/A');
  }));

  it('should be OK returning no data', waitForAsync(() => {
    ordersService.getOrderHistory(60).subscribe(
      (orderHistory: Array<OrderDetail>) => expect(orderHistory.length).toEqual(0, 'should be empty'),
      fail
    );

    const req = httpTestingController.expectOne(ordersService.baseUrl + 'order/orderHistory?days=60');
    req.flush([]); // Respond with empty
  }));

  it('should test for 404 error when call OrderHistory', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';

    ordersService.getOrderHistory(60).subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne(ordersService.baseUrl + 'order/orderHistory?days=60');

    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

  it('should test for 404 error when call OrderDetails', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';

    ordersService.getOrderDetails(orderId).subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne(ordersService.baseUrl + `order/orderHistoryDetails/${orderId}`);

    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

});
