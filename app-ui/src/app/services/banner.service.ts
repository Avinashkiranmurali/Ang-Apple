import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { Observable, throwError } from 'rxjs';
import { Banner } from '@app/models/banner';
import { BaseService } from './base.service';
import { BannerStoreService } from '../state/banner-store.service';
import { Banners } from '@app/models/banners';

@Injectable({
  providedIn: 'root'
})

export class BannerService extends BaseService {

  constructor(
    private http: HttpClient,
    private bannerStoreService: BannerStoreService
  ) {
    super();
  }

  /**
   * Get Banners
   *
   * @summary Get the list of banners
   * @returns {Observable<Banner>}
   */
  getBanners(): Observable<Array<Banner>> {
    const url = this.baseUrl + 'banner/template';
    return this.http.get<Array<Banner>>(url, this.httpOptions)
      .pipe(
        map((response) => {
          this.bannerStoreService.addBanner(response);
          return response;
        }),
        catchError((error: HttpErrorResponse) => {
          this.handleError(error);
          return throwError(error);
        })
      );
  }

  /**
   * Get StoreLandingBanners
   *
   * @summary Get the list of StoreLandingBanners
   * @returns {Observable<Banners>}
   */
  getStoreLandingBanners(): Observable<Banners> {
    const url = this.baseUrl + 'banner/config';
    return this.http.get<Banners>(url, this.httpOptions)
      .pipe(
        map((response) => {
          this.bannerStoreService.addBanners(response);
          return response;
        }),
        catchError((error: HttpErrorResponse) => {
          this.handleError(error);
          return throwError(error);
        })
      );
  }

}
