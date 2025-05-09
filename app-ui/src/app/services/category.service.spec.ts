import { TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CategoryService } from './category.service';
import { Category } from '@app/models/category';
import { HttpErrorResponse } from '@angular/common/http';

describe('CategoryService', () => {
  let categoryService: CategoryService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ],
      providers: [
        { provide: CategoryService },
      ]
    });

    // Inject the http test controller
    httpTestingController = TestBed.inject(HttpTestingController);
    categoryService = TestBed.inject(CategoryService);
  });

  afterEach(() => {
    // After every test, assert that there are no more pending requests.
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(categoryService).toBeTruthy();
  });

  it('should return Categories', waitForAsync(() => {
    // Fake response data
    const fakeResponse = require('assets/mock/categories.json');

    // Setup a request using the fakeResponse data
    categoryService.getNav().subscribe(
      (categories: Array<Category>) => expect(categories).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );

    // Expect a call to this URL
    const req = httpTestingController.expectOne(categoryService.baseUrl + 'categories');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(fakeResponse);

    // Run some expectations
    expect(fakeResponse.length).toBe(7);
    expect(fakeResponse[0].i18nName).toBe('Mac');
    expect(fakeResponse[1].i18nName).toBe('iPad');
    expect(fakeResponse[2].i18nName).toBe('iPhone');
    expect(fakeResponse[0].subCategories[0].name).toBe('MacBook Air');
    expect(fakeResponse[0].subCategories[0].active).toBe(true);
  }));

  it('should test for 404 error - getNav', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    categoryService.getNav().subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne(categoryService.baseUrl + 'categories');
    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));
});
