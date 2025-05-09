import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { UserStoreService } from '@app/state/user-store.service';
import { catchError, map } from 'rxjs/operators';
import { Observable, throwError } from 'rxjs';
import { BaseService } from '@app/services/base.service';
import { Program } from '@app/models/program';
import { AnalyticsService } from '@app/analytics/analytics.service';

@Injectable({
  providedIn: 'root'
})

export class ProgramService extends BaseService {

  constructor(
    private http: HttpClient,
    private userStore: UserStoreService,
    private analyticsService: AnalyticsService
  ) {
    super();
  }

  /**
   * Get Program
   *
   * Retrieve a program
   *
   * @returns {Observable<Program>}
   */
  getProgram(): Observable<Program> {
    const url = this.baseUrl + 'program';

    return this.http.get<Program>(url, this.httpOptions)
      .pipe(
        map((response: Program) => {
          this.userStore.addProgram(response);
          this.analyticsService.loadAnalyticScripts();
          return response;
        }),
        catchError((error: HttpErrorResponse) => {
          this.handleError(error);
          return throwError(error);
        })
      );
  }

}
