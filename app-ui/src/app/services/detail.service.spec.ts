import { TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { DetailService } from './detail.service';
import { Product } from '@app/models/product';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterTestingModule } from '@angular/router/testing';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Router } from '@angular/router';
import { UserStoreService } from '@app/state/user-store.service';

describe('DetailService', () => {
  let detailService: DetailService;
  let httpTestingController: HttpTestingController;
  const programData = require('assets/mock/program.json');
  const userStoreData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };
  userStoreData['user']['program'] = programData;
  userStoreData['user']['matomoTrackerURL'] = 'test/url';
  userStoreData['config']['imageServerUrl'] = '/imageserver';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ],
      providers: [
        { provide: DetailService },
        { provide: NgbActiveModal },
        { provide: MatomoService, useValue: {
          broadcast: () => {}
        } },
        { provide: UserStoreService, useValue: userStoreData },
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate') }
        }
      ]
    });

    // Inject the http test controller
    httpTestingController = TestBed.inject(HttpTestingController);
    detailService = TestBed.inject(DetailService);
  });

  afterEach(() => {
    // After every test, assert that there are no more pending requests.
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(detailService).toBeTruthy();
  });

  it('should return Product Details', waitForAsync(() => {
    // Fake response data
    const fakeResponse = require('assets/mock/product-detail.json');

    // Setup a request using the fakeResponse data
    detailService.getDetails('30001MXG22LL/A').subscribe(
      (details: Product) => expect(details).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );

    // Expect a call to this URL
    const req = httpTestingController.expectOne(detailService.baseUrl + 'products/30001MXG22LL/A');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(fakeResponse);

    // Run some expectations
    expect(fakeResponse.psid).toBe('30001MXG22LL/A');
    expect(fakeResponse.shortDescription).toBe('12.9-inch iPad Pro Wi‑Fi + Cellular 1TB - Space Gray');
    expect(fakeResponse.categories[0].i18nName).toBe('iPad Pro');
    expect(fakeResponse.options[0].name).toBe('model');
  }));

  it('should test for 404 error', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    detailService.getDetails('30001MXG22LL/A').subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne(detailService.baseUrl + 'products/30001MXG22LL/A');

    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));
});
