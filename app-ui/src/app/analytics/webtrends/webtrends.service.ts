import { DOCUMENT } from '@angular/common';
import { Inject, Injectable } from '@angular/core';
import { Router, RouterEvent, NavigationEnd } from '@angular/router';
import { BaseService } from '@app/services/base.service';
import { UserStoreService } from '@app/state/user-store.service';
import { Webtrends } from './webtrends';

@Injectable({
  providedIn: 'root'
})
export class WebtrendsService extends BaseService {

  webtrendsInstance: Webtrends;

  constructor(
    private userStoreService: UserStoreService,
    private router: Router,
    @Inject(DOCUMENT) private document: Document
  ) {
    super();
  }

  loadInitialScript() {
    this.loadElements();
    this.loadWebtrendsScript().onload = () => {
      this.webtrendsInstance = new window.WebTrends();
      this.router.events.subscribe(
        (event: RouterEvent) => {
          if (event instanceof NavigationEnd) {
            const tempUrl = this.router.url;
            this.webtrendsInstance.dcsMultiTrack('DCS.dcsuri', tempUrl);
          }
        });
      this.triggerMethods();
    };
  }

  loadWebtrendsScript(): HTMLScriptElement {
    const body = this.document.getElementsByTagName('body')[0];
    const scriptElememt = this.document.createElement('script');
    scriptElememt.src = this.userStoreService.config.webtrendsEndPoint;
    scriptElememt.type = 'text/javascript';
    body.appendChild(scriptElememt);
    return scriptElememt;
  }

  triggerMethods() {
    this.webtrendsInstance.dcsGetId();
    this.webtrendsInstance.dcsCustom = () => {
      // Add custom parameters here.
      // webtrendsInstance.DCSext.param_name=param_value;
    };
    this.webtrendsInstance.dcsCollect();
  }

  loadElements() {
    const body = this.document.getElementsByTagName('body')[0];
    const divElememt = this.document.createElement('div');
    divElememt.setAttribute('wt-dcs-pg-load', '');
    body.appendChild(divElememt);
  }
}
