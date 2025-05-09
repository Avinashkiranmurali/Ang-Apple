import { Injectable } from '@angular/core';
import { BaseService } from '../../services/base.service';
import { Offer } from '@app/models/offer';
import { User } from '@app/models/user';
import { UserStoreService } from '@app/state/user-store.service';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { AuthenticationService } from '@app/auth/authentication.service';
import { KeyStoneSyncService } from '../../services/key-stone-sync.service';
import { EncryptDecryptService } from '@app/services/encrypt-decrypt.service';

@Injectable({
  providedIn: 'root'
})

export class PricingService extends BaseService {

  user: User;

  constructor(
    private userStore: UserStoreService,
    private http: HttpClient,
    private authenticationService: AuthenticationService,
    private keyStoneSyncService: KeyStoneSyncService ,
    private encryptDecryptService: EncryptDecryptService
  ) {
    super();
    this.user = this.userStore.user;
  }

  checkDiscounts(offers: Offer): { [key: string]: boolean } {
    const isDiscounted = {
      discounted: false,
      point: false,
      cash: false,
      fullPointDiscounted: false,
      fullCashDiscounted: false
    };

    if (!offers) {
      return isDiscounted;
    }

    const display = offers.displayPrice;
    const original = offers.unpromotedDisplayPrice;

    if (display && original && (display.amount !== original.amount || display.points !== original.points)) {
      isDiscounted.discounted = true;
      isDiscounted.point = (display.points !== original.points);
      isDiscounted.cash = (display.amount !== original.amount);
      isDiscounted.fullPointDiscounted = (display.points === 0);
      isDiscounted.fullCashDiscounted = (display.amount === 0);
    }
    return isDiscounted;
  }

  getPricingOption(): { [key: string]: any } {
    const bundledPricingOption = this.user.program.bundledPricingOption ? this.user.program.bundledPricingOption : null;
    const PricingOption: { [key: string]: any } = {};

    switch (bundledPricingOption) {
      case 'UNBUNDLED_DETAIL_PAGE_BUNDLED_CHECKOUT':
        PricingOption.option = 'unbundledDetails';
        PricingOption.isUnbundled = false;
        break;
      case 'BASE_PRICE_SHOPPING_UNBUNDLED_CHECKOUT':
        PricingOption.option = 'unbundledCheckout';
        PricingOption.isUnbundled = true;
        break;
      default:
        PricingOption.option = 'bundled';
        PricingOption.isUnbundled = false;
    }

    return PricingOption;
  }

  placeOrder(params) {
    const url = this.baseUrl + 'order/' + 'placeOrder';
    return this.http.post<any>(url, params, this.httpOptionsWithResponse).pipe(
      map((response) => {
        // const authToken = response.headers.get('xsrf-token');
        if (response.headers.get('xsrf-token')) {
          sessionStorage.setItem('currentToken', this.encryptDecryptService.encrypt(JSON.stringify(response.headers.get('xsrf-token'))));
          this.authenticationService.currentTokenValue = response.headers.get('xsrf-token');
        }
        const orderSuccessData: { [key: string]: any } = response;
        const orderSuccessEvent = new CustomEvent('orderSuccess', {
            detail: {
                b2Id: orderSuccessData.b2sOrderId,
                clientId: orderSuccessData.varOrderId
            }
        });
        window.dispatchEvent(orderSuccessEvent);
        if (this.keyStoneSyncService.isKeyStoneSync('balanceUpdate')) {
          this.keyStoneSyncService.PointsBalanceSyncEventDispatch().subscribe(data => {
            // just triggered
          });
        }
        return response;
      }),
      catchError((error: HttpErrorResponse) => {
        this.handleError(error);
        return throwError(error);
      })
    );
  }
}
