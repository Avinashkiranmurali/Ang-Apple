import { Injectable, OnDestroy } from '@angular/core';
import { throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})

export class BaseService implements OnDestroy {

  baseUrl: string;
  orderUrl: string;
  mockUrl: string;
  httpOptions: object = {
    observe: 'body'
  };
  httpOptionsWithResponse = {
    observe: 'response' as const
  };
  subscriptions: any = {};
  constructor() {
    this.baseUrl = '/apple-gr/service/';
    this.orderUrl = this.baseUrl + 'order/';
    this.mockUrl = '/apple-gr/assets/mock/';
  }

  /**
   *
   * @param error
   */
  public handleError(error) {
    return throwError(error);
  }

  /**
   *
   */
  ngOnDestroy(): void {
    Object.keys(this.subscriptions).forEach(key => this.subscriptions[key].unsubscribe());
  }
}
