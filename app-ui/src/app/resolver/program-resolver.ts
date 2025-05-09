import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, Router, RouterStateSnapshot} from '@angular/router';
import { ProgramService } from '@app/services/program.service';
import { EMPTY, Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AppConstants } from '@app/constants/app.constants';

@Injectable({ providedIn: 'root' })
export class ProgramResolver implements Resolve<object> {
  constructor(private programService: ProgramService,
              private router: Router) {}

  resolve(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<any>|Promise<any>|any {
    return this.programService.getProgram()
      .pipe(catchError(() => {
        this.router.navigate(['/login-error']);
        return EMPTY;
      }));
  }
}
