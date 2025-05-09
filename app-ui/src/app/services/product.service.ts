import { Injectable } from '@angular/core';
import { BaseService } from '@app/services/base.service';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { Observable, throwError } from 'rxjs';
import { FilterProducts } from '@app/models/filter-products';
import { Cart } from '@app/models/cart';
import { ProductsWithConfiguration } from '@app/models/products-with-configuration';
import { MatomoService } from '@app/analytics/matomo/matomo.service';

@Injectable({
  providedIn: 'root'
})

export class ProductService extends BaseService {

  constructor(
    private http: HttpClient,
    private matomoService: MatomoService
  ) {
    super();
  }

  /**
   * Get Filtered Products
   *
   * @summary Retrieve products and filters
   * @param params
   * @returns {Observable<FilterProducts>}
   */
  getFilteredProducts(params): Observable<FilterProducts> {
    const url = this.baseUrl + 'filterProducts';

    return this.http.post<FilterProducts>(url, params, this.httpOptions)
      .pipe(
        map((response) => response),
        catchError((error: HttpErrorResponse) => {
          this.handleError(error);
          this.matomoService.sendErrorToAnalyticService();
          return throwError(error);
        })
      );
  }

  /***********************Products with Options**********************************
   @description Get products with options.
   @param {string} string of query string parameters to add to the service call
   @returns {object} options array and product arrays
   *************************************************************************/
  getProductsWopts(params): Observable<ProductsWithConfiguration | Array<any>> {
    // get the params and add them to the URL
    const urlWparams = '/apple-gr/service/productsWithConfiguration' + params;

    const products = this.http.get<ProductsWithConfiguration | Array<any>>(urlWparams, this.httpOptions).
    pipe(
      map((data) => {
        // TODO backend is sending blank arrays... this catches that for now
        if (Array.isArray(data) && data.length === 0) {
          // TODO:
          // $rootScope.errorMsg = 'No products were returned for: ' + params;
          // errorLogService(400, $rootScope.errorMsg);
        }
        return data;
      }),
      catchError((error: HttpErrorResponse) => {
        this.handleError(error);
        return throwError(error);
        // TODO:
        // if (error.status !== 401 || error.status !== 0) {
        //   // log to server-side
        //   $rootScope.errorMsg = 'Error: products with options REST service failed to GET @params:' + params + ', product data';
        //   errorLogService(status, $rootScope.errorMsg);
        // }
      })
    );
    return products;
  }
}
