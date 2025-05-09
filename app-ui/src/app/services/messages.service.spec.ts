import { TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { MessagesService } from './messages.service';
import { Messages } from '@app/models/messages';
import { HttpErrorResponse } from '@angular/common/http';

describe('MessagesService', () => {
  let messagesService: MessagesService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ],
      providers: [
        { provide: MessagesService },
      ]
    });

    // Inject the http test controller
    httpTestingController = TestBed.inject(HttpTestingController);
    messagesService = TestBed.inject(MessagesService);
  });

  afterEach(() => {
    // After every test, assert that there are no more pending requests.
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(messagesService).toBeTruthy();
  });

  it('should return Messages', waitForAsync(() => {
    // Fake response data
    const fakeResponse = require('assets/mock/messages.json');

    // Setup a request using the fakeResponse data
    messagesService.getMessages().subscribe(
      (messages: Messages) => expect(messages).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );

    // Expect a call to this URL
    const req = httpTestingController.expectOne(messagesService.baseUrl + 'messages');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(fakeResponse);

    // Run some expectations
    expect(fakeResponse.productBannerActiveProducts).toBe('macbook-pro');
    expect(fakeResponse.macFamilyState).toBe('/store/browse');
    expect(fakeResponse.macFamilyImageTxt).toBe('Mac');
    expect(fakeResponse.macbookAirOrderBy).toBe('2');
  }));

  it('should test for 404 error - message service', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    messagesService.getMessages().subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      });
    // Expect a call to this URL
    const req = httpTestingController.expectOne(messagesService.baseUrl + 'messages');
    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
  }));
});
