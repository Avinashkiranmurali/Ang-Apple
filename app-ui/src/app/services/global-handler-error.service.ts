import { ErrorHandler, Injectable, Injector } from '@angular/core';
import { LocationStrategy, PathLocationStrategy } from '@angular/common';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { LoggerService } from './logger.service';
import { throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import * as StackTrace from 'stacktrace-js';

@Injectable({
  providedIn: 'root'
})
export class GlobalHandlerErrorService implements ErrorHandler {

  constructor(
    private injector: Injector,
    private http: HttpClient) { }

  handleError(error) {
    const loggingService = this.injector.get(LoggerService);
    const location = this.injector.get(LocationStrategy);
    const message = error.message ? error.message : error.toString();
    const url = location instanceof PathLocationStrategy ? location.path() : '';
    const httpOptions = {
      observe: 'body' as 'body',
      headers: new HttpHeaders().set('content-type', 'application/json')
    };

    loggingService.logError(error);

    StackTrace.fromError(error).then(stackframes => {
      const stackString = stackframes
        .splice(0, 20)
        .map((sf) => sf.toString());

      // log on the server
      const payload = JSON.stringify({
        errorUrl: url,
        errorMessage: message,
        stackTrace: stackString
      });

      const errorLog = this.http.post('/apple-gr/log/errors.json', payload, httpOptions)
      .pipe(
        map((response) => response),
        catchError((errorResponse: HttpErrorResponse) => {
          loggingService.logWarning('Error logging failed');
          loggingService.logMessage(errorResponse);
          return throwError(errorResponse);
        })
      );
      errorLog.subscribe(() => {
        // logic goes here...
      });
    });
  }
}
