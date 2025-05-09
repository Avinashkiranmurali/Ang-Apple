import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})

export class NavStoreService {

  // eslint-disable-next-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match
  private readonly _mainNav = new BehaviorSubject<any>([]);

  readonly mainNav$ = this._mainNav.asObservable();

  constructor() { }

  get mainNav(): Array<any> {
    return this._mainNav.getValue();
  }

  set mainNav(val: Array<any>) {
    this._mainNav.next(val);
  }

  addMainNav(newNav: Array<any>) {
    // this.mainNav = _.merge(this.mainNav, newNav);
    newNav.forEach(function (nav) {
      if(nav.templateType == 'CATEGORY' && nav.subCategories.length == 0) {
        nav.subCategories.push(JSON.parse(JSON.stringify(nav)));
      }
    });
    this.mainNav = newNav;
  }

}
