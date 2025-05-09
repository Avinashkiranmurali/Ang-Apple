import { TestBed, waitForAsync } from '@angular/core/testing';
import { AuthenticationService } from './authentication.service';
import { FormsModule } from '@angular/forms';
import { HttpClientModule, HttpErrorResponse } from '@angular/common/http';
import { RouterTestingModule } from '@angular/router/testing';
import { AuthGuard } from './auth.guard';
import { Router } from '@angular/router';
import { HttpTestingController, HttpClientTestingModule } from '@angular/common/http/testing';

describe('AuthenticationService', () => {
  let authService: AuthenticationService;
  let httpTestingController: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        HttpClientModule,
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        AuthGuard,
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate') }
        }
      ]
    });
    authService = TestBed.inject(AuthenticationService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    sessionStorage.removeItem('currentToken');
  });

  it('should be created', () => {
    const service: AuthenticationService = TestBed.inject(AuthenticationService);
    expect(service).toBeTruthy();
  });

  it('get XSRF method for authentication response with token', waitForAsync(() => {
    const mockResponse = 'OK';
    // Setup a request using the giftProducts data
    authService.getXSRF().subscribe(
        (res) => expect(res).toEqual(res, 'should return mockResponse'), fail
    );
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/getXSRFToken');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush('OK', { headers: {'X-XSRF-TOKEN': 'b82780ff-e016-4c82-8f7'}});
  }));

  it('get XSRF method for authentication response without token', waitForAsync(() => {
    const mockResponse = 'OK';
    // Setup a request using the giftProducts data
    authService.getXSRF().subscribe(
        (res) => expect(res).toEqual(res, 'should return mockResponse'), fail
    );
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/getXSRFToken');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush('OK', { headers: {'X-XSRF-TOKEN': ''}});
  }));

  it('should test for 404 error - getXSRF', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    authService.getXSRF().subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      });
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/getXSRFToken');
    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
  }));

});
