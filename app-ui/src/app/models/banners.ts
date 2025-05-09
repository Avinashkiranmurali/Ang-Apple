import { Config } from './config';

export interface Banners {
    family: Array<LandingBannerCategory>;
    landing: Array<LandingBannerCategory>;
    whatsnew: Array<LandingBannerCategory>;
}

export interface LandingBannerCategory {
    categoryId: string;
    categoryName: string;
    bannerType: string;
    config: Config;
    products?: Array<LandingBannerCategory>;
}
