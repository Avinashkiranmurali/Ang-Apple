import { Injectable } from '@angular/core';
import { AuthenticationService } from '../auth/authentication.service';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class XsrfService {

  constructor(
    private authenticationService: AuthenticationService,
    private router: Router
  ) { }

  init(): Promise<any> {
    return this.authenticationService.getXSRF()
      .toPromise()
      .then((data: any) => Promise.resolve())
      .catch((err: any) => {
        Promise.resolve();
        this.router.navigate(['/login-error']);
      }
    );
  }
}
