import { Router } from '@angular/router';
import { Injectable } from '@angular/core';
import { LoaderService } from './loader.service';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
@Injectable({
  providedIn: 'root'
})

export class LoaderInterceptorService implements HttpInterceptor {
  private requests: HttpRequest<any>[] = [];

  constructor(private loaderService: LoaderService, private router: Router) { }

  removeRequest(req: HttpRequest<any>) {
    const i = this.requests.indexOf(req);
    if (i >= 0) {
      this.requests.splice(i, 1);
    }

    // This short timeout on the loading spinner is to provide enough time
    // for a screen reader to announce "application busy"
    setTimeout(() => {
      this.loaderService.isLoading.next(this.requests.length > 0);
    }, 200);
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    const allowedSpinnerArray = ['getXSRFToken', 'messages', 'program', 'categories', 'configData', 'quicklinks', 'banner', 'categorySlug', '/cart/add', 'modifyCartAddress', 'withEngraveConfig', 'orderHistory', 'withRelatedProduct'];

    allowedSpinnerArray.forEach(key => {
      if (req.url.indexOf(key) !== -1 && (this.router.url.indexOf('store/cart') === -1 || req.url.indexOf('withEngraveConfig') !== -1)) {
        this.requests.push(req);
        this.loaderService.isLoading.next(true);
      }
    });

    return new Observable(observer => {
      const subscription = next.handle(req)
        .subscribe(
          event => {
            if (event instanceof HttpResponse) {
              this.removeRequest(req);
              observer.next(event);
            }
          },
          err => {
            this.removeRequest(req);
            observer.error(err);
          },
          () => {
            this.removeRequest(req);
            observer.complete();
          });
      // remove request from queue when cancelled
      return () => {
        this.removeRequest(req);
        subscription.unsubscribe();
      };
    });
  }
}
