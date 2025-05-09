import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Carousel } from '@app/models/carousel';
import { BaseService } from '@app/services/base.service';

@Injectable({
  providedIn: 'root'
})
export class CarouselService extends BaseService {

  constructor(private http: HttpClient) {
    super();
  }

  getCarouselData(pageName: string): Observable<Carousel[]> {
    const url = this.baseUrl + 'carousel/' + pageName.toLowerCase();

    return this.http.get<Carousel[]>(url, this.httpOptions)
      .pipe(
        map((response) => response),
        catchError((error: HttpErrorResponse) => {
          this.handleError(error);
          return throwError(error);
        })
      );
  }
}
