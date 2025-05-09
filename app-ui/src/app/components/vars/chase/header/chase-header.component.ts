import { Component, EventEmitter, HostListener, Inject, Input, OnInit, Output } from '@angular/core';
import { Config } from '@app/models/config';
import { User } from '@app/models/user';
import { Program } from '@app/models/program';
import { Messages } from '@app/models/messages';
import { DOCUMENT } from '@angular/common';
import { UserStoreService } from '@app/state/user-store.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { GiftPromoService } from '@app/services/gift-promo.service';
import { IAccounts, IProfile } from '@app/models/nav-menu.interface';

@Component({
  selector: 'app-chase-header',
  templateUrl: './chase-header.component.html',
  styleUrls: ['./chase-header.component.scss']
})
export class ChaseHeaderComponent implements OnInit {

  @Input() headerTemplate: object;
  @Input() messages: Messages;
  @Input() config: Config;
  @Input() program: Program;
  @Input() user: User;
  baseUrl: string;
  displayDynamicHeaderFooter: boolean = false;
  scriptsLoading: boolean = true;

  // Calculator data starts
  public data: IAccounts;

  public profile: IProfile;

  public homeLinkClicked = true;

  public subNavLevel2Visible = false;

  public currentRpc: string;

  public promotionalDiscount = 0;

  public bannerState = {
    bannerVisible: true,
  };

  @Input()
  public updatePoints: number;

  //Calculator data ends

  @HostListener('window:scroll')
  onScroll(): void {
    const bagMenu = this.document.querySelector('.popover.cartAbandonPopup');
    if (bagMenu) {
      //event emitter - observer
      this.sharedService.closeAbandonCartPopup(true);
    }
  }

  constructor(
    @Inject(DOCUMENT)private document: Document,
    public userStoreService: UserStoreService,
    private sharedService: SharedService,
    private giftPromoService: GiftPromoService,
  ) {
    this.user = this.userStoreService.user;
  }

  ngOnInit(): void {
    this.baseUrl = this.config.externalHeaderUrl;
    this.displayDynamicHeaderFooter = this.config.dynamicHeaderFooterLoad;
    this.loadChaseScript();
  }

  loadChaseScript() {
    const body = this.document.getElementsByTagName('body')[0];
    const scriptElememt = this.document.createElement('script');
    if(this.displayDynamicHeaderFooter) {
      let sessionStateEncoded = this.user.sessionState ? this.user.sessionState : '';
      if(sessionStateEncoded) {
        let sessionStateDecoded = JSON.parse(atob(sessionStateEncoded));
        scriptElememt.src = this.baseUrl + 'nav-menu/nav.js?ai=' + sessionStateDecoded.accountIndex;
      }
    } else {
      scriptElememt.src = this.baseUrl + 'public/chase-navigation/chase-navigation.js';
      let localChase = 'http://localhost:8082/apple-sso/';
      this.giftPromoService.getChaseProfileData(this.baseUrl).subscribe(
        (profile: IProfile) => {
          console.log(profile);
          this.promotionalDiscount = profile.promotionalDiscount;
          this.bannerState.bannerVisible = profile.calculatorEnabled;
          this.updatePoints = profile.profileData.loyaltyAccount.rewardsBalance.amount;

        },
        (error: any) => {
          console.error(error);
        }
      );
    }

    scriptElememt.type = 'text/javascript';
    body.appendChild(scriptElememt);
    this.scriptsLoading = false;
  }

  updateBannerStateValue(value: boolean): void {
      this.bannerState.bannerVisible = value;
  }

  isPromoBannerVisible(): boolean {
    return this.promotionalDiscount > 0 && this.bannerState.bannerVisible;
  }

}
