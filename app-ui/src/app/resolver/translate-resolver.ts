import { Injectable } from '@angular/core';
import {Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Router} from '@angular/router';
import { EMPTY, Observable } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { catchError } from 'rxjs/operators';
import { AppConstants } from '@app/constants/app.constants';

@Injectable({ providedIn: 'root' })
export class TranslateResolver implements Resolve<object> {
  constructor( public translate: TranslateService,
               private router: Router) {}

  resolve(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<any>|Promise<any>|any {
    return this.translate.use('en')
      .pipe(catchError(() => {
        this.router.navigate(['/login-error']);
        return EMPTY;
    }));
  }
}
