import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, Router, RouterStateSnapshot} from '@angular/router';
import { CategoryService } from '@app/services/category.service';
import { EMPTY, Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AppConstants } from '@app/constants/app.constants';

@Injectable({ providedIn: 'root' })
export class CategoryResolver implements Resolve<object> {
  constructor(private categoryService: CategoryService,
              private router: Router) {}

  resolve(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<any>|Promise<any>|any {
    return this.categoryService.getNav()
      .pipe(catchError(() => {
        this.router.navigate(['/login-error']);
        return EMPTY;
      }));
  }
}
