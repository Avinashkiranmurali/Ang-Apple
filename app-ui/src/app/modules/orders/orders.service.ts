import { Injectable } from '@angular/core';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { BaseService } from '@app/services/base.service';
import { OrderDetail } from '@app/models/order-detail';

@Injectable({
  providedIn: 'root'
})
export class OrdersService extends BaseService {

  constructor(
    private http: HttpClient
  ) {
    super();
  }

  /**
   * Get Order History
   *
   * @summary Retrieve the order history
   * @returns {Observable<Array<OrderDetail>>}
   */
  getOrderHistory(days?: number): Observable<Array<OrderDetail>> {
    const urlParam = days ? 'order/orderHistory?days=' + days : 'order/orderHistory';
    const url = this.baseUrl + urlParam;

    return this.http.get<Array<OrderDetail>>(url, this.httpOptions)
      .pipe(
        map((response) => response),
        catchError((error: HttpErrorResponse) => {
          this.handleError(error);
          return throwError(error);
        })
      );
  }

  /**
   * Get Order Details
   *
   * @summary Retrieve the order history details
   * @param orderId
   * @returns {Observable<OrderDetail>}
   */
  getOrderDetails(orderId): Observable<OrderDetail> {
    const url = this.baseUrl + `order/orderHistoryDetails/${orderId}`;

    return this.http.get<OrderDetail>(url, this.httpOptions)
      .pipe(
        map((response) => response),
        catchError((error: HttpErrorResponse) => {
          this.handleError(error);
          return throwError(error);
        })
      );
  }

}
