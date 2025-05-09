import { TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CarouselService } from './carousel.service';
import { Carousel } from '@app/models/carousel';
import { HttpErrorResponse } from '@angular/common/http';

describe('CarouselService', () => {
  let carouselService: CarouselService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule
      ]
    });
    carouselService = TestBed.inject(CarouselService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(carouselService).toBeTruthy();
  });

  it('should return CarouselData', waitForAsync(() => {
    // Fake response data
    const fakeResponse = require('assets/mock/carousel.json');
    // Setup a request using the fakeResponse data
    carouselService.getCarouselData('pdp').subscribe(
      (data: Carousel[]) => expect(data).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );
    // Expect a call to this URL
    const req = httpTestingController.expectOne(() => true);
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    // Respond with the fake data when called
    req.flush(fakeResponse);
    // Run some expectations
    expect(fakeResponse.length).toBe(3);
  }));

  it('should test for 404 error - getCarouselData', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    carouselService.getCarouselData('bag').subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );
    const req = httpTestingController.expectOne(() => true);
    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

});
