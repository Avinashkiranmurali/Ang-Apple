import { TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { BannerService } from './banner.service';
import { Banner } from '@app/models/banner';
import { Banners } from '@app/models/banners';

describe('BannerService', () => {
  let bannerService: BannerService;
  let httpTestingController: HttpTestingController;

  // Set fake response data
  const fakeResponse = require('assets/mock/banner.json');

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ],
      providers: [
        { provide: BannerService },
      ]
    });

    // Inject the http test controller
    httpTestingController = TestBed.inject(HttpTestingController);
    bannerService = TestBed.inject(BannerService);
  });

  afterEach(() => {
    // After every test, assert that there are no more pending requests.
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(bannerService).toBeTruthy();
  });

  it('should return Banners', waitForAsync(() => {
    // Setup a request using the fakeResponse data
    bannerService.getBanners().subscribe(
      (banners: Array<Banner>) => expect(banners).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );

    // Expect a call to this URL
    const req = httpTestingController.expectOne(bannerService.baseUrl + 'banner/template');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(fakeResponse);

    // Run some expectations
    expect(fakeResponse.length).toBe(1);
    expect(fakeResponse[0].template.type).toBe('stacks');
    expect(fakeResponse[0].programBanners.multiProductBanner.multiProductBannerList).toBe('List');
    expect(fakeResponse[0].programBanners.productBanner.activeProducts).toBe('ActiveProducts');
    expect(fakeResponse[0].bannerTemplateObj.imageUrl).toBe('ImageUrl');
  }));

  it('should be OK returning no banners', waitForAsync(() => {
    bannerService.getBanners().subscribe(
      (banners: Array<Banner>) => expect(banners.length).toEqual(0, 'should be empty'),
      fail
    );

    const req = httpTestingController.expectOne(bannerService.baseUrl + 'banner/template');
    req.flush([]); // Respond with no banners
  }));

  it('should return banners (called multiple times)', waitForAsync(() => {
    bannerService.getBanners().subscribe();
    bannerService.getBanners().subscribe();
    bannerService.getBanners().subscribe(
      (banners: Array<Banner>) => expect(banners).toEqual(fakeResponse, 'should return fakeResponse'),
      fail
    );

    const requests = httpTestingController.match(bannerService.baseUrl + 'banner/template');
    expect(requests.length).toEqual(3, 'calls to getBanners()');

    // Respond to each request with different mock banner results
    requests[0].flush([]); // return with empty data
    requests[1].flush([{ id: 1, name: 'Some Banner' }]); // return that specific object
    requests[2].flush(fakeResponse); // return the fakeResponse data
  }));

  it('should test for 404 error', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';

    bannerService.getBanners().subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne(bannerService.baseUrl + 'banner/template');

    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

  it('should return productFamilyBanners', waitForAsync(() => {
    // Setup a request using the fakeResponse data
    const mockResponse = require('assets/mock/store-landing-banners.json');
    bannerService.getStoreLandingBanners().subscribe(
      (banners: Banners) => expect(banners).toEqual(mockResponse, 'should return fakeResponse'), fail
    );

    // Expect a call to this URL
    const req = httpTestingController.expectOne(() => true);
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    // Respond with the fake data when called
    req.flush(mockResponse);
    // Run some expectations
    expect(fakeResponse).toBeDefined();
  }));

  it('should test for 404 error - productFamilyBanner Service', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';

    bannerService.getStoreLandingBanners().subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne(() => true);
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

});
