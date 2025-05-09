export interface NavItem {
    readonly altText: string;
    readonly footerNavItems: NavItem[];
    readonly id: string;
    readonly imgUrl: string;
    readonly isNew: boolean;
    readonly marquee: NavItem[];
    readonly rollover: NavItem[];
    readonly mobileLink: string;
    readonly name: string;
    readonly openInNewTab: boolean;
    readonly url: string;
    readonly hideInHamburger?: boolean;
}

export interface NavHeader {
    readonly accountIcon: NavItem;
    readonly backToAccountLink: string;
    readonly backToAccountLinkMobile: string;
    readonly brandLogo: NavItem;
    readonly brandLogoMobile: NavItem;
    readonly featureLogo: NavItem;
    readonly logoutLink: string;
    readonly logoutLinkMobile: string;
    readonly marquee: NavItem[];
    readonly rewardsActivityTrip: NavItem;
    readonly topLevelNavItemTitle: string;
    readonly urHomeLink: string;
    readonly accountChangeLink: string;
    readonly cashBackSpanish: string;
    readonly hideInHamburger: boolean;
}

export interface NavFooter {
    readonly commonFooterItems: NavItem[];
    readonly footerData: NavItem;
}

export interface NavMenu {
    header: NavHeader;
    footer: NavFooter;
    productData: ProductData;
}

export interface IAccounts {
    loyaltyAccount: IProduct;
    otherLoyaltyAccounts: IProduct[];
    cigProfileId: string;
    customerName: string;
    favoriteCount: number;
    reservationCount: number;
    conversionRate: number;
}

export interface IProduct {
    accountIndex: string;
    apc?: string;
    conversionRate?: number;
    accountName: string;
    enterpriseCustomerIdentifier?: string;
    rewardsBalance: {
        amount: number;
        currency: string
    };
    productCode?: number;
    productNumber: string;
    productType: string;
    rpc?: string;
    selected?: boolean;
    cardName?: string;
    shortProductNumber?: number | string;
    loyaltyVersion: number | string;
    productNumberInternal?: string;
    pointsAvailable?: number;
    displayName?: string;
    rewardsProductCode?: string;
    accountOrganizationCode?: string;
}

export interface ILoyaltyCard {
    productCode: string;
    altText?: string;
    smallImage?: string;
    mediumImage: string;
    originalImage?: string;
    standardImage?: string;
    retinaImage?: string;
}

export interface IProfile {
    acctIndex: string;
    calculatorEnabled: boolean;
    loyaltyVersion: string;
    loyaltyVersionCashPerPoint: string;
    promotionalDiscount: number;
    loyaltyCard: ILoyaltyCard;
    loyaltyCardsNotInContext: object;
    profileData: IAccounts;
    promotionalDisclaimer: string;
    analyticsWindow: AnalyticsWindow;
}

export interface AnalyticsWindow {
    jp_rpc: string;
    jp_aoc: string;
}

export interface ProductData {
    cardArtImageAltText: string;
    mediumCardArtImageUrl: string;
    originalCardArtImageUrl: string;
    productName: string;
    retinaCardArtImageUrl: string;
    smallCardArtImageUrl: string;
    standardCardArtImageUrl: string;
    travelPhoneNumber: string;
}
