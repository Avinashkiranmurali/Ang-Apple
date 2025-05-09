import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { Observable, throwError } from 'rxjs';
import { BaseService } from '@app/services/base.service';
import { QuickLink } from '@app/models/quick-link';

@Injectable({
  providedIn: 'root'
})
export class QuickLinksService extends BaseService{

  constructor(private http: HttpClient) {
    super();
  }

  /**
   * Get Quick Links
   *
   * @summary Retrieve List of Quick Links
   * @returns {Observable<Array<QuickLink>>}
   */
  getQuickLinks(): Observable<Array<QuickLink>> {
    const url = this.baseUrl + 'quicklinks';

    return this.http.get<QuickLink[]>(url, this.httpOptions)
      .pipe(
        map((response) => response),
        catchError((error: HttpErrorResponse) => {
          this.handleError(error);
          return throwError(error);
        })
      );
  }
}
