import { TestBed, waitForAsync } from '@angular/core/testing';
import { XsrfService } from './xsrf.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthenticationService } from '@app/auth/authentication.service';
import { RouterTestingModule } from '@angular/router/testing';
import { AppConstants } from '@app/constants/app.constants';
import { Router } from '@angular/router';

describe('XsrfService', () => {
  let service: XsrfService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [
        {provide: AuthenticationService},
        { provide: Router, useValue: {
            navigate: jasmine.createSpy('navigate') }
        },
        ]
    });
    service = TestBed.inject(XsrfService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('get XSRF method for authentication response', waitForAsync(() => {
    service.init();
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/getXSRFToken');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush('OK', { headers: {'X-XSRF-TOKEN': 'b82780ff-e016-4c82-8f7'}});
  }));

  it('get XSRF method for authentication  failure response', waitForAsync(() => {
    service.init();
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/getXSRFToken');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush('Failure', {status: 404, statusText: 'Not found', headers: {'X-XSRF-TOKEN': 'b82780ff-e016-4c82-8f7'}});
  }));

});
