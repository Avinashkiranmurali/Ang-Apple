import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { EMPTY, Observable } from 'rxjs';
import { SessionService } from '@app/services/session.service';
import { catchError } from 'rxjs/operators';
import { AppConstants } from '@app/constants/app.constants';

@Injectable({ providedIn: 'root' })
export class SessionResolver implements Resolve<object> {
  constructor(private sessionService: SessionService) { }

  resolve(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<any> | Promise<any> | any {
    return this.sessionService.getSession();
      // .pipe(catchError(() => {
      //   window.location.href = AppConstants.ERROR_URL;
      //   return EMPTY;
      // }));
  }
}
