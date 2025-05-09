import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { Observable, throwError } from 'rxjs';
import { NavStoreService } from '../state/nav-store.service';
import { BaseService } from './base.service';
import { Category } from '@app/models/category';

@Injectable({
  providedIn: 'root'
})

export class CategoryService extends BaseService{

  constructor(
    private http: HttpClient,
    private navStoreService: NavStoreService
  ) {
    super();
  }

  /**
   * Get Nav
   *
   * @summary Get list of Categories
   * @returns {Observable<Array<Category>>}
   */
  getNav(): Observable<Array<Category>> {
    const url = this.baseUrl + 'categories';

    return this.http.get<Array<Category>>(url, this.httpOptions)
      .pipe(
        map((response) => {
          this.navStoreService.addMainNav(response);
          return response;
        }),
        catchError((error: HttpErrorResponse) => {
          // general and service level error actions
          this.handleError(error);
          return throwError(error);
        })
      );
  }

}
