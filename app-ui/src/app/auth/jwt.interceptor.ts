import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthenticationService } from './authentication.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(private authenticationService: AuthenticationService) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // add authorization header with jwt token if available
    const currentToken = this.authenticationService.currentTokenValue;
    if (!request.withCredentials && this.isHeaderNeeded(request.url) && currentToken) {
      request = request.clone({
        headers : request.headers.set('X-XSRF-TOKEN', `${currentToken}`)
      });
      /*request = request.clone({
        headers : request.headers.set( 'Authorization', `${currentToken}`)
      });*/

    }
    request = request.clone({
      headers : request.headers.set('Content-Type',  'application/json; charset=UTF-8')
    });
    return next.handle(request);
  }

  /**
   * Function used to avoid setting xsrf token in request header
   *
   * @param url
   * b2r/api/participant/balance - Custom header update will trigger preflight call, we need to restrict it
   */
  isHeaderNeeded(url: string) {
    const XSRFTokenRestrictionURLs = ['/paymentserver/api/', 'apple-gr/assets/mock/'];
    for (const path of XSRFTokenRestrictionURLs){
      if (url.indexOf(path) >= 0) {
          return false;
        }
    }
    return true;
  }
}
