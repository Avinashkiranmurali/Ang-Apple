import { Injectable } from '@angular/core';
import { BaseService } from './base.service';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { Observable, throwError } from 'rxjs';
import { OrderStatus } from '@app/models/order-detail';

@Injectable({
  providedIn: 'root'
})
export class OrderInformationService extends BaseService{

  constructor(
    private http: HttpClient
  ) {
    super();
  }

  /**
   * Get Order Information
   *
   * @summary Retrieve Order information
   * @returns {Observable<OrderStatus>}
   */
  getOrderInformation(): Observable<OrderStatus> {

    const url = this.orderUrl + 'orderInformation';

    return this.http.get<OrderStatus>(url, this.httpOptions)
      .pipe(
        map((response) => response),
        catchError((error: HttpErrorResponse) => this.handleError(error))
      );
  }

  getPurchaseSelectionInfo(orderId){
    const url = this.orderUrl + 'getPurchaseSelectionInfo/' + orderId;
    return this.http.get(url, this.httpOptions).pipe(
      map(response => response),
      catchError((error: HttpErrorResponse) => {
        this.handleError(error);
        return throwError(error);
      })
    );

  }

}
