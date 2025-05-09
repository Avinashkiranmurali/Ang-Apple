import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthenticationService } from './authentication.service';
import { XsrfService } from '@app/init/xsrf.service';

@Injectable({
  providedIn: 'root'
})

export class AuthGuard implements CanActivate {

  /**
   *
   * @param router
   * @param authenticationService
   */
  constructor(
    private router: Router,
    private authenticationService: AuthenticationService,
    private xsrfService: XsrfService,
  ) { }

  /**
   * Navigate to Error Page
   */
  navigateToErrorPage() {
    this.router.navigate(['/login-error']);
  }

  /**
   *
   * @param route
   * @param state
   */
  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.xsrfService.init().then(() => {
      const currentToken = this.authenticationService.currentTokenValue;
      if (currentToken) {
        // logged in so return true
        return Promise.resolve(true);
      }
      this.navigateToErrorPage();
      return Promise.resolve(false);
    }).catch(() => {
      this.navigateToErrorPage();
      return Promise.resolve(false);
    });
  }

}
