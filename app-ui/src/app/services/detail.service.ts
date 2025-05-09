import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BaseService } from '@app/services/base.service';
import { catchError, map } from 'rxjs/operators';
import { Observable, throwError } from 'rxjs';
import { Product } from '@app/models/product';
import { Program } from '@app/models/program';
import { Config } from '@app/models/config';
import { User } from '@app/models/user';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { AppConstants } from '@app/constants/app.constants';

@Injectable({
  providedIn: 'root'
})
export class DetailService extends BaseService {
  config: Config;
  user: User;
  program: Program;

  constructor(
    private http: HttpClient,
    private matomoService: MatomoService
  ) {
    super();
  }

  /**
   * Get Details
   *
   * @summary Retrieve a product details
   * @param psid
   * @returns {Observable<Product>}
   */
  getDetails(params: string): Observable<Product> {
    const url = this.baseUrl + 'products/' + params;

    return this.http.get<Product>(url, this.httpOptions)
      .pipe(
        map((response) => {
          const product = { ...response, productName: response.name, productId: response.sku, product: response.psid };

          this.matomoService.broadcast(AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.PRODUCT_VIEW, {
            payload: product
          });

          response.isEligibleForGift = response.addOns.availableGiftItems.length > 0;
          response.isMultiGiftAvailable = response.addOns.availableGiftItems.length > 1;
          return response;
        }),
        catchError((error: HttpErrorResponse) => {
          this.handleError(error);
          return throwError(error);
        })
      );
  }
}
