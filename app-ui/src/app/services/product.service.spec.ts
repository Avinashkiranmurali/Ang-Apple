import { TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { ProductService } from './product.service';
import { FilterProducts } from '@app/models/filter-products';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

describe('ProductService', () => {
  let productService: ProductService;
  let httpTestingController: HttpTestingController;

  // Fake response data
  const fakeResponse = require('assets/mock/facets-filters.json');
  // Fake post request payload for the Apogee brand
  const requestPayload = {
    sort: 'SALES_RANK',
    order: 'DESCENDING',
    categorySlugs: [
      'all-accessories'
    ],
    facetsFilters: {
      brand: [
        {
          name: 'brand',
          value: 'Apogee',
          key: 'Apogee',
          i18Name: 'Brand',
          orderBy: 0,
          points: null,
          swatchImageUrl: null,
          disabled: false,
          isFiltered: true
        }
      ]
    },
    pageSize: 12,
    resultOffSet: 0,
    withVariations: true
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ],
      providers: [
        { provide: ProductService },
        { provide: MatomoService, useValue: {
          sendErrorToAnalyticService: () => {
            {
              // logic goes here...
            }
          }
        }},
        { provide: CurrencyFormatPipe },
        { provide: CurrencyPipe },
        { provide: DecimalPipe }
      ]
    });

    // Inject the http test controller
    httpTestingController = TestBed.inject(HttpTestingController);
    productService = TestBed.inject(ProductService);
  });

  afterEach(() => {
    // After every test, assert that there are no more pending requests.
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(productService).toBeTruthy();
  });

  it('should return Products', waitForAsync(() => {
    // Setup a request using the fakeResponse data
    productService.getFilteredProducts(requestPayload).subscribe(
      (products: FilterProducts) => expect(products).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );

    // Expect a call to this URL
    const req = httpTestingController.expectOne(productService.baseUrl + 'filterProducts');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');

    // Respond with the fake data when called
    req.flush(fakeResponse);

    // Run some expectations
    expect(fakeResponse.products.length).toBe(12);
    expect(fakeResponse.products[0].psid).toBe('30001MWP22AM/A');
    expect(fakeResponse.products[0].brand).toBe('Apple®');
    expect(fakeResponse.products[0].categories[0].slug).toBe('mac-accessories-headphones-speakers');
  }));

  it('should test for 404 error', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';

    productService.getFilteredProducts(requestPayload).subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne(productService.baseUrl + 'filterProducts');

    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

  it('should return ProductsWithOptions', waitForAsync(() => {
    const param = '/?categorySlug=' + 'ipad-pro';
    // Setup a request using the fakeResponse data
    productService.getProductsWopts(param).subscribe(
      (products) => expect(products).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );

    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/productsWithConfiguration' + param);

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
      // console.log('rerer', req);
    req.flush(fakeResponse);

    // Run some expectations
    expect(fakeResponse.products.length).toBe(12);
    expect(fakeResponse.products[0].psid).toBe('30001MWP22AM/A');
    expect(fakeResponse.products[0].brand).toBe('Apple®');
    expect(fakeResponse.products[0].categories[0].slug).toBe('mac-accessories-headphones-speakers');
    expect(fakeResponse.products[0].optionsConfigurationData).toBe(null);
  }));

  it('should return ProductsWithOptions when no products found', waitForAsync(() => {
    const param = '/?categorySlug=' + 'ipad-pro';
    // Setup a request using the fakeResponse data
    productService.getProductsWopts(param).subscribe(
      (products) => expect(products).toEqual([], 'should return fakeResponse'), fail
    );

    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/productsWithConfiguration' + param);

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
      // console.log('rerer', req);
    req.flush([]);
    expect(productService.getProductsWopts).toBeDefined();
  }));

  it('should test for 404 error', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    productService.getProductsWopts('').subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne('/apple-gr/service/productsWithConfiguration');

    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));
});
