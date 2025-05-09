export interface Banner {
  name: string;
  subcat: Array<BannerCategory>;
}

export interface BannerCategory {
  name: string;
  imageUrl: string;
  i18nText: string;
  title: string;
  titleImg: string;
  tagLine: string;
  shortDescription: string;
  position: string;
}
