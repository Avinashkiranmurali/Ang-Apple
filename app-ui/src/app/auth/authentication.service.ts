import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpResponse } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { Router } from '@angular/router';
import { BaseService } from '@app/services/base.service';
import { EncryptDecryptService } from '@app/services/encrypt-decrypt.service';

@Injectable({
  providedIn: 'root'
})

export class AuthenticationService extends BaseService {

  httpOptions: object = {
    headers: new HttpHeaders({
      'Content-Type':  'application/json; charset=UTF-8'
    }),
    observe: 'response',
    responseType: 'text' as 'json'
  };

  api = '/api/';

  private currentTokenSubject: BehaviorSubject<string>;
  public currentToken: Observable<string>;
  public storedToken: string;

  /**
   *
   * @param http
   * @param router
   * @param encryptDecryptService
   */

  constructor(
    private http: HttpClient,
    private router: Router,
    private encryptDecryptService: EncryptDecryptService
  ) {
    super();

    this.storedToken = sessionStorage.getItem('currentToken') || '';

    if (this.storedToken) {
      this.storedToken = this.encryptDecryptService.decrypt(this.storedToken);
    }

    this.currentTokenSubject = new BehaviorSubject<string>(this.storedToken);
    this.currentToken = this.currentTokenSubject.asObservable();
  }

  /**
   *
   */
  public get currentTokenValue(): string {
    return this.currentTokenSubject.value;
  }
  public set currentTokenValue(val: string) {
    this.currentTokenSubject.next(val);
  }

  getXSRF() {
    const url = this.baseUrl + 'getXSRFToken';

    return this.http.get<any>(url, this.httpOptions)
      .pipe(
        map((response) => {
          // const authToken = response.headers.get('X-XSRF-TOKEN');
          if (response.headers.get('X-XSRF-TOKEN')) {
            sessionStorage.setItem('currentToken', this.encryptDecryptService.encrypt(JSON.stringify(response.headers.get('X-XSRF-TOKEN'))));
            this.currentTokenSubject.next(response.headers.get('X-XSRF-TOKEN'));
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
