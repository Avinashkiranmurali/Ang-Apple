export interface AnalyticsObject {
    datalayer_b2s_user: UserDataLayer;
    datalayer_b2s_prod: Array<ProductDataLayer> | ProductDataLayer;
}

export interface UserDataLayer {
    pgName: string;
    pgType: string;
    pgSectionType: string;
    pgCountryCode: string;
    pgCountryLanguage: string;
    userMembershipTier: string;
    userMembershipID?: string;
    userPoints?: number;
    userStatus: string;
}

export interface ProductDataLayer {
    prodProductCategory: string;
    prodProductName: string;
    prodProductPSID: string;
    prodProductPoints: number;
    prodProductSKU: string;
    prodProductType: string;
    prodProductUPC: string;
}

export interface Bootstrapper {
    ensEvent: {
      trigger: TriggerFunc;
    };
}

type TriggerFunc = (subString: string) => void;
