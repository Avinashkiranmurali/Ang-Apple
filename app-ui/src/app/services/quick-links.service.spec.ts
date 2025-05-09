import { TestBed, waitForAsync } from '@angular/core/testing';

import { QuickLinksService } from './quick-links.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { QuickLink } from '@app/models/quick-link';
import { HttpErrorResponse } from '@angular/common/http';

describe('QuickLinksService', () => {
  let service: QuickLinksService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ],
    });

    // Inject the http test controller
    httpTestingController = TestBed.inject(HttpTestingController);
    service = TestBed.inject(QuickLinksService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return Quick Links', waitForAsync(() => {
    // Fake response data
    const fakeResponse = require('assets/mock/quickLinks.json');

    // Setup a request using the fakeResponse data
    service.getQuickLinks().subscribe(
      (data: Array<QuickLink>) => expect(data).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );

    // Expect a call to this URL
    const req = httpTestingController.expectOne(service.baseUrl + 'quicklinks');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(fakeResponse);
  }));

  it('should test for 404 error', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';

    service.getQuickLinks().subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne(service.baseUrl + 'quicklinks');

    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));
});
