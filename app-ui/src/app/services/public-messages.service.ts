import { Injectable } from '@angular/core';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BaseService } from './base.service';

@Injectable({
  providedIn: 'root'
})
export class PublicMessagesService extends BaseService {
  locale: string;

  constructor(
    private http: HttpClient
    ) {
    super();
  }

  /**
   * Get Public Messages
   *
   * @returns {Observable<{[key: string]: string}>}
   */
  getPublicMessages(locale = 'en_US', codeType = 'maintenance'): Observable<{[key: string]: string}> {
    const url = this.baseUrl + 'publicMessages';
    const params = new HttpParams()
    .set('locale', locale)
    .set('code_type', codeType);
    const httpOptions =  {
      ...this.httpOptions,
      params,
      headers: {
        contentType: 'application/json'
      }
    };

    return this.http.get<any>(url, httpOptions)
      .pipe(
        map((response) => response),
        catchError((error: HttpErrorResponse) => this.handleError(error))
      );
  }
}
