import { Injectable } from '@angular/core';
import {Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Router} from '@angular/router';
import { EMPTY, Observable } from 'rxjs';
import { UserService } from '@app/services/user.service';
import { catchError } from 'rxjs/operators';
import { AppConstants } from '@app/constants/app.constants';

@Injectable({ providedIn: 'root' })
export class UserResolver implements Resolve<object> {
  constructor(private userService: UserService,
              private router: Router) {}

  resolve(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<any>|Promise<any>|any {
    return this.userService.getUser()
      .pipe(catchError(() => {
        this.router.navigate(['/login-error']);
        return EMPTY;
      }));
  }
}
