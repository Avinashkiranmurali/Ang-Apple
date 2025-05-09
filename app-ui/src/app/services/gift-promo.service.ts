import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { Observable, throwError, of } from 'rxjs';
import { BaseService } from './base.service';
import { ModalsService } from '@app/components/modals/modals.service';
import { Config } from '@app/models/config';
import { User } from '@app/models/user';
import { UserStoreService } from '@app/state/user-store.service';
import { CartService } from './cart.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';
import { AddressService } from './address.service';
import { AddToCartResponse, Cart } from '@app/models/cart';
import { Product } from '@app/models/product';
import { IAccounts, IProfile } from '@app/models/nav-menu.interface';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { Address } from '@app/models/address';
import { TransitionService } from '@app/transition/transition.service';
import { SharedService } from '../modules/shared/shared.service';
import { HeapService } from '@app/analytics/heap/heap.service';

@Injectable({
    providedIn: 'root'
})

export class GiftPromoService extends BaseService {

    config: Config;
    user: User;
    checkoutAddress: Address;
    public profileP: Promise<void | IProfile>;

  constructor(
      private http: HttpClient,
      private modalService: ModalsService,
      private userStore: UserStoreService,
      private cartService: CartService,
      private parsePsidPipe: ParsePsidPipe,
      private route: Router,
      private addressService: AddressService,
      private matomoService: MatomoService,
      private heapService: HeapService,
      private transitionService: TransitionService,
      private sharedService: SharedService
    ) {
        super();
        this.config = this.userStore.config;
        this.user = this.userStore.user;
    }

    /**
     * Get GiftPromoService
     *
     * @returns Observable<{[key: string]: string}[]>
     */
    getGiftPromoProducts(psid): Observable<{[key: string]: string}[]> {
        const url = this.baseUrl + 'giftItem?qualifyingPsid=' + psid.replace(/-/g, '/');

        return this.http.get<Array<{[key: string]: string}>>(url, this.httpOptions)
            .pipe(
                map((response) => response),
                catchError((error: HttpErrorResponse) => {
                    // general and service level error actions
                    this.handleError(error);
                    return throwError(error);
                })
            );
    }

  giftItemModify(params, itemId: number): Observable<any> {
    this.transitionService.openTransition();
    const url = this.baseUrl + 'cart/modify/' + itemId;
    const httpOptions = {
      observe: 'response' as const
    };

    return this.http.post<Cart>(url, params, httpOptions)
      .pipe(
        map((data) => {
          this.transitionService.closeTransition();
          data.body['cartItems'] = this.sharedService.giftAttributeUpdates(data.body['cartItems']);
          return data;
        }),
        catchError((error: HttpErrorResponse) => {
          this.transitionService.closeTransition();
          return this.handleError(error);
        })
      );

    }

    getUserDevice(): string {
      let userDevice = 'desktop';

      const width = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;

      const height = window.innerHeight || document.documentElement.clientHeight || document.body.clientWidth;

      if (width < 720) {
          userDevice = 'mobile';
          if (width === 600 && height === 1024) {
              userDevice = 'tablet';
          }
      } else if (width >= 1028) {
          userDevice = 'desktop';
      } else {
          userDevice = 'tablet';
      }

      return userDevice;
  }

    getChaseProfileData(chaseBaseUrl:string): Observable<IProfile> {
      console.log('------------');
      console.log('chaseBaseUrl',chaseBaseUrl)
      console.log('------------');
    //   const mockData: IProfile =  {
    //     "acctIndex": "1009012",
    //     "loyaltyVersion": "SAPPHIRE",
    //     "loyaltyVersionCashPerPoint": ".010",
    //     "promotionalDiscount": 20,
    //     "loyaltyCard": {
    //         "productCode": "0419",
    //         "altText": "Chase Sapphire® card",
    //         "smallImage": "/content/services/structured-image/image.small.png/UR/CardArt/Product/06653_Sapphire_700x442.png",
    //         "mediumImage": "/content/services/structured-image/image.medium.png/UR/CardArt/Product/06653_Sapphire_700x442.png",
    //         "originalImage": "/content/dam/structured-images/UR/CardArt/Product/06653_Sapphire_700x442.png",
    //         "standardImage": "/content/services/structured-image/image.standard.png/UR/CardArt/Product/06653_Sapphire_700x442.png",
    //         "retinaImage": "/content/services/structured-image/image.retina.png/UR/CardArt/Product/06653_Sapphire_700x442.png"
    //     },
    //     "loyaltyCardsNotInContext": {},
    //     "profileData": {
    //         "loyaltyAccount": {
    //             "productType": "00080",
    //             "productNumber": "5412345678910245",
    //             "accountIndex": "1009012",
    //             "loyaltyVersion": "SAPPHIRE",
    //             "accountName": "Chase Sapphire_12 (...0245)",
    //             "rewardsProductCode": "0419",
    //             "rewardsBalance": {
    //                 "amount": 999999,
    //                 "currency": "PTS"
    //             }
    //         },
    //         "otherLoyaltyAccounts": [],
    //         "cigProfileId": "10008112",
    //         "customerName": "Bonita",
    //         "favoriteCount": 0,
    //         "reservationCount": 0,
    //         "conversionRate": 100
    //     },
    //     "promotionalDisclaimer": "",
    //     "calculatorEnabled": true,
    //     "analyticsWindow": {
    //         "jp_rpc": "0419",
    //         "jp_aoc": "00419"
    //     }
    // }
    //   return of(mockData)
    //   .pipe(
    //     catchError((error: any) => {
    //       console.error(error);
    //       throw error;
    //     })
    //   );
      return this.http.get<IProfile>(`${chaseBaseUrl}cardMemberInContext`, { withCredentials: true })
      .pipe(
        catchError((error: any) => {
          console.error(error);
          throw error;
        })
      );
    }
}
