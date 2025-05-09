import { Injectable } from '@angular/core';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { UserStoreService } from '../state/user-store.service';
import { BaseService } from './base.service';
import { LocaleService } from '@app/services/locale.service';
import { User } from '@app/models/user';

@Injectable({
  providedIn: 'root'
})

export class UserService extends BaseService {

  constructor(
    private http: HttpClient,
    private userStore: UserStoreService,
    private localeService: LocaleService
  ) {
    super();
  }

  /**
   * Get User
   *
   * @returns {Observable<User>}
   */
  getUser(): Observable<User> {
    const url = '/apple-gr/customer/user.json';
    return this.http.get<User>(url, this.httpOptions)
      .pipe(
        map((response) => {
          this.userStore.addUser(response);
          this.localeService.registerLocale(response.locale);
          return response;
        }),
        catchError((error: HttpErrorResponse) => {
          this.handleError(error);
          return throwError(error);
        })
      );
  }
}
