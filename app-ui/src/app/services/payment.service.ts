import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, map, switchMap } from 'rxjs/operators';
import { Observable, Subject, throwError } from 'rxjs';
import { BaseService } from './base.service';
import { CartService } from '@app/services/cart.service';

@Injectable({
    providedIn: 'root'
})

export class PaymentService extends BaseService {

    constructor(
        private http: HttpClient,
        private cartService: CartService
    ) {
        super();
    }

    /**
     * Post CardDetails
     *
     * @returns {Observable<object>}
     */
    postCardDetails(ccEntryDetails): Observable<object> {
        const url = this.baseUrl + 'ccEntry/init';

        return this.http.post<object>(url, ccEntryDetails, this.httpOptions)
            .pipe(
                map((response) => response),
                catchError((error: HttpErrorResponse) => {
                    // general and service level error actions
                    this.handleError(error);
                    return throwError(error);
                })
            );
    }

    /**
     * We create a reactive observer Subject that takes a number
     * It waits a time before sending after a change
     * It makes sure the number is different
     * It unsubscribes from the previous observable converted from a promise when it changes
     * It maps the response to the data object
     * It performs actions for the state on the response
     * It catches any errors and logs them while still returning the error for use in subscribers
     *
     * @returns {*|worker|D}
     */
    getPurchasePointsObservable() {
        const observerStream = new Subject<number>();

        return observerStream.pipe(
          map((points) => this.cartService.getPurchasePoints(points)),
          switchMap(innerOb => innerOb),
          catchError((error: HttpErrorResponse) =>
            // general and service level error actions
             this.handleError(error)
          )
        ) as Subject<number>;
    }

    getPaymentTransactionApi() {
      const url = this.baseUrl + 'payment/transaction' + '?nocache=' + new Date().getTime();
      const paymentApi = this.http.post(url, this.httpOptions).pipe(
        map( (response) => response), catchError((error: HttpErrorResponse) => {
          this.handleError(error);
          return throwError(error);
        })
      );
      return paymentApi;
    }

    postPaymentDetails(ccEntryDetails) {
      const sessionPaymentApiDet = sessionStorage.getItem('paymentApiDet');
      const paymentApiDet = JSON.parse(sessionPaymentApiDet);
      const paymentApi = this.http.post(paymentApiDet.url, ccEntryDetails).pipe(
        map( (response) => response), catchError((error: HttpErrorResponse) => {
          this.handleError(error);
          return throwError(error);
        })
      );
      return paymentApi;
    }
}
