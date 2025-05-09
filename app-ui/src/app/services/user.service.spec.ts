import { TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { UserService } from './user.service';

describe('UserService', () => {
  let userService: UserService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ],
      providers: [
        { provide: UserService },
      ]
    });

    // Inject the http test controller
    httpTestingController = TestBed.inject(HttpTestingController);
    userService = TestBed.inject(UserService);
  });

  afterEach(() => {
    // After every test, assert that there are no more pending requests.
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(userService).toBeTruthy();
  });

  it('should return User data', waitForAsync(() => {
    // Fake response data
    const fakeResponse = require('assets/mock/user.json');

    // Setup a request using the fakeResponse data
    userService.getUser().subscribe(
      data => expect(data).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );

    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/customer/user.json');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(fakeResponse);

    // Run some expectations
    expect(fakeResponse.userid).toBe('alex');
    expect(fakeResponse.varid).toBe('Delta');
    expect(fakeResponse.points).toBe(98032602);
    expect(fakeResponse.city).toBe('Alpharetta');
    expect(fakeResponse.programId).toBe('b2s_qa_only');
  }));

  it('should test for 404 error', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';

    userService.getUser().subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne('/apple-gr/customer/user.json');

    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));
});
