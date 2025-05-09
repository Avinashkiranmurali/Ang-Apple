import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { UserStoreService } from '@app/state/user-store.service';
import { Config } from '@app/models/config';

@Injectable({
  providedIn: 'root'
})
export class FooterDisclaimerService {

  displayFooterWatchExtendedDisclaimer: boolean;
  config: Config;

  constructor(
    private router: Router,
    public userStore: UserStoreService,
  ) {
    this.config = this.userStore.config;
    this.displayFooterWatchExtendedDisclaimer = this.config.displayFooterWatchExtendedDisclaimer;
  }

  isWatchLanding(): boolean {
    return this.router.url.endsWith('/watch');
  }

}
