import { Component, ElementRef, EventEmitter, HostListener, Input, OnInit, Output } from '@angular/core';
import { GiftPromoService } from '@app/services/gift-promo.service';
import { IAccounts, IProfile } from '@app/models/nav-menu.interface';

@Component({
    selector: 'app-chase-promo-banner',
    templateUrl: './promo-banner.component.html',
    styleUrls: ['../chase-header.component.scss']
})
export class PromoBannerComponent implements OnInit {
    public data: IAccounts;

    public profile: IProfile;

    public homeLinkClicked = true;

    public subNavLevel2Visible = false;

    public currentRpc: string;

    @Input()
    public readonly promotionalDiscount: number;

    @Input()
    public updatedPoints: number;

    @Input()
    public bannerState: boolean;
    @Output() bannerStateEmitter = new EventEmitter<boolean>();

    constructor(
        private readonly giftPromoService: GiftPromoService,
        private el: ElementRef
    ) { }

    @HostListener('window:resize', ['$event'])
    onResize(event) {
        this.isPhabletOrMobile();
    }

    public ngOnInit(): void {
        // let localChase = 'http://localhost:8082/apple-sso/';
        // this.giftPromoService.getChaseProfileData(localChase).subscribe(
        //     (response: IProfile) => {
        //         // this.profileData = response;
        //         console.log(response);
        //         this.profile = response;
        //         this.data = response.profileData;
        //         this.currentRpc = this.data.loyaltyAccount.rewardsProductCode;
        //     },
        //     (error: any) => {
        //       console.error(error);
        //     }
        //   );
        // if (
        //     window.location.href.indexOf("/home") !== -1 ||
        //     window.location.pathname.trim() === "" ||
        //     window.location.pathname.trim() === "/"
        // ) {
        //     const urHomeElement = this.el.nativeElement.querySelector(
        //         ".urHome"
        //     );
        //     if (urHomeElement) {
        //         urHomeElement.classList.add("active");
        //     }
        //     this.homeLinkClicked = true;
        //     if (this.isDesktop()) {
        //         this.subNavLevel2Visible = true;
        //     }
        //     this.clearActiveNavItems();
        // } else {
        //     this.homeLinkClicked = false;
        // }
    }

    public isDesktop(): boolean {
        return this.giftPromoService.getUserDevice() === 'desktop';
    }

    public isMobile(): boolean {
        return this.giftPromoService.getUserDevice() === 'mobile';
    }

    public isPhabletOrMobile(): boolean {
        const width = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth || screen.width;
        return width < 768;
    }

    public closePromoBanner() {
        this.bannerStateEmitter.emit(false);
    }

    public clearActiveNavItems() {
        // TODO: Need to check
    }

    public pointsToUsd(): number {
        if (this.data && this.data.loyaltyAccount) {
            return parseFloat(((this.updatedPoints || this.data.loyaltyAccount.rewardsBalance.amount) / 100).toFixed(2));
        }
    }

    public discountToUsd(): number {
        if (this.data && this.data.loyaltyAccount) {
            return parseFloat(((this.updatedPoints || this.data.loyaltyAccount.rewardsBalance.amount) / 100 * this.promotionalDiscount / 100).toFixed(2));
        }
    }

    public rewardsWithDiscount(): number {
        if (this.data && this.data.loyaltyAccount) {
            return parseFloat(
                (((this.updatedPoints || this.data.loyaltyAccount.rewardsBalance.amount) / 100) * (1 + this.promotionalDiscount / 100)
                ).toFixed(2));
        }
    }
}
