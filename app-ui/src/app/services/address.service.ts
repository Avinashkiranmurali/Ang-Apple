import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { ModalsService } from '@app/components/modals/modals.service';
import { Address } from '@app/models/address';
import { BaseService } from './base.service';
import isEqual from 'lodash/isEqual';
import { Config } from '@app/models/config';
import { UserStoreService } from '@app/state/user-store.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AddressService extends BaseService {

  config: Config;

  constructor(
    private router: Router,
    private modalService: ModalsService,
    private http: HttpClient,
    private userStore: UserStoreService,
    private sharedService: SharedService
  ) {
    super();
    this.config = this.userStore.config;
  }

  modifyShippingAddress(addressToUse, skipPaymentOption) {
    this.modifyAddress(addressToUse).subscribe(data => {
      let returnedData = data;
      const errorMessages = this.sharedService.prepareErrorWarningObj(returnedData.errorMessage);
      const hasErrorMsgs = Object.keys(errorMessages).length > 0;
      const warningMessages = this.sharedService.prepareErrorWarningObj(returnedData.warningMessage);
      const hasWarningMsgs = Object.keys(warningMessages).length > 0;
      returnedData = this.decodeAddress(returnedData);
      if (hasErrorMsgs || hasWarningMsgs) {
        const isAddressChanged = (returnedData.addressModified === 'Y');
        const message = (isAddressChanged && !hasWarningMsgs) ? errorMessages : warningMessages;
        this.modalService.openSuggestAddressModalComponent(isAddressChanged, addressToUse, returnedData, message);
      }
      else if (isEqual(returnedData, {})) {
        this.modalService.openOopsModalComponent('shippingAddressError'); // to make a custom text for this scenario
      }
      else if (!this.config.fullCatalog || skipPaymentOption) {
        this.router.navigate(['/store', 'checkout'], {});
      }
      else {
        this.router.navigate(['/store', 'payment']);
      }
    }, err => {
      if (err.status < 500) {
        if (err.status === 401 || err.status === 0) {
          // TO DO SESSION
          // sessionMgmt.showTimeout();
        } else {
          this.router.navigate(['/store', 'shipping-address'], {});
        }
      } else {
        this.modalService.openOopsModalComponent('shippingAddressError');
      }
    }
    );
  }

  getStates(): Observable<{ [key: string]: string }[]> {
    const url = this.baseUrl + '/address/getStates';
    return this.http.get<Array<{ [key: string]: string }>>(url, this.httpOptions).pipe(
      map(response => response),
      catchError((error: HttpErrorResponse) => {
        this.handleError(error);
        return throwError(error);
      })
    );
  }

  getCities(): Observable<{ [key: string]: string }> {
    const url = this.baseUrl + '/address/cities';
    return this.http.get<{ [key: string]: string }>(url, this.httpOptions).pipe(
      map(response => response),
      catchError((error: HttpErrorResponse) => {
        this.handleError(error);
        return throwError(error);
      })
    );
  }

  modifyAddress(changeShipAddress): Observable<Address> {
    const url = this.baseUrl + 'address/modifyCartAddress';
    return this.http.post<Address>(url, {shippingAddress: changeShipAddress}, this.httpOptionsWithResponse).pipe(
      map(response => response.body),
      catchError((error: HttpErrorResponse) => {
        this.handleError(error);
        return throwError(error);
      })
    );
  }

  getStateProvince() {
    const url = this.baseUrl + 'address/' + 'getStates';
    return this.http.get<Array<{ [key: string]: string }>>(url, this.httpOptions).pipe(
      map(response => response),
      catchError((error: HttpErrorResponse) => {
        this.handleError(error);
        return throwError(error);
      })
    );
  }

  decodeAddress(data): Address {
    if (data.firstName) {
      data.firstName = decodeURIComponent(data.firstName);
    }

    if (data.lastName) {
      data.lastName = decodeURIComponent(data.lastName);
    }

    if (data.address1) {
      data.address1 = decodeURIComponent(data.address1);
    }

    if (data.address2) {
      data.address2 = decodeURIComponent(data.address2);
    }

    if (data.address3) {
      data.address3 = decodeURIComponent(data.address3);
    }

    if (data.city) {
      data.city = decodeURIComponent(data.city);
    }

    if (data.businessName) {
      data.businessName = decodeURIComponent(data.businessName);
    }

    if (data.email) {
      data.email = decodeURIComponent(data.email);
    }

    if (data.phoneNumber) {
      data.phoneNumber = decodeURIComponent(data.phoneNumber);
    }
    return data;
  }

}
