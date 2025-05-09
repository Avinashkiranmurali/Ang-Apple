import { Injectable } from '@angular/core';
import { Banners } from '@app/models/banners';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})

export class BannerStoreService {

  // eslint-disable-next-line @typescript-eslint/naming-convention,no-underscore-dangle,id-blacklist,id-match
  private readonly _banner = new BehaviorSubject<Array<any>>([]);
  // eslint-disable-next-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match
  private readonly _banners = new BehaviorSubject<Banners>({} as Banners);

  // Expose the observable$ part of the _messages subject (read only stream)
  readonly banner$ = this._banner.asObservable();
  readonly banners$ = this._banners.asObservable();
  constructor() { }

  get banner(): Array<any> {
    return this._banner.getValue();
  }

  set banner(val: Array<any>) {
    this._banner.next(val);
  }

  addBanner(data: Array<any>) {
    this.banner = data;
  }

  get banners(): Banners {
    return this._banners.getValue();
  }

  set banners(val: Banners) {
    this._banners.next(val);
  }

  addBanners(data: Banners) {
    this.banners = data;
  }
}
