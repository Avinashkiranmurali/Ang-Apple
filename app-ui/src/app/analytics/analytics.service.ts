import { Injectable } from '@angular/core';
import { BaseService } from '@app/services/base.service';
import { UserStoreService } from '@app/state/user-store.service';
import { User } from '@app/models/user';
import { EnsightenService } from './ensighten/ensighten.service';
import { MatomoService } from './matomo/matomo.service';
import { WebtrendsService } from './webtrends/webtrends.service';
import { TealiumService } from './tealium/tealium.service';
import { AppConstants } from '@app/constants/app.constants';
import { HeapService } from './heap/heap.service';

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService extends BaseService {

  user: User;

  constructor(
    private userStoreService: UserStoreService,
    private ensightenService: EnsightenService,
    private matomoService: MatomoService,
    private webtrendsService: WebtrendsService,
    private tealiumService: TealiumService,
    private heapService: HeapService
  ) {
    super();
  }

  loadAnalyticScripts() {
    this.user = this.userStoreService.user;
    if (this.user['analyticsWindow']) {
      window.analyticsWindow = this.user['analyticsWindow'];
    }

    if (this.user['analyticsUrl']) {
      window.analyticsUrl = this.user['analyticsUrl'];
    }
    const analytics = this.userStoreService.config.analytics ? this.userStoreService.config.analytics.split(',') : [];
    analytics.forEach(analyticService => {
      switch (analyticService.trim()) {
        case AppConstants.analyticServices.MATOMO: // ANALYTICS FOR DELTA, WF LOYALTY VARS
          if (this.userStoreService.config.matomoEndPoint && this.userStoreService.config.matomoSiteId) {
            this.matomoService.loadInitialScript();
          }
          break;
        case AppConstants.analyticServices.TEALIUM: // ANALYTICS FOR UA VAR
          this.tealiumService.loadInitialScript();
          break;
        case AppConstants.analyticServices.WEBTRENDS: // ANALYTICS FOR RBC VAR
          this.webtrendsService.loadInitialScript();
          break;
        case AppConstants.analyticServices.ENSIGHTEN: // ANALYTICS FOR CITI VARS
          this.ensightenService.loadInitialScript();
          break;
        case AppConstants.analyticServices.HEAP: // ANALYTICS FOR HEAP SERVICE
          this.heapService.loadInitialScript();
          break;
        default:
          break;
      }
    });
  }

}
