import { Component, OnInit, Input, ChangeDetectionStrategy, ViewEncapsulation } from '@angular/core';
import { UserStoreService } from '@app/state/user-store.service';
import { LandingBannerCategory } from '@app/models/banners';
import { Config } from '@app/models/config';
import { Params } from '@app/models/params'
import { Category } from '@app/models/category'
import { NavStoreService } from '@app/state/nav-store.service';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { Messages } from '@app/models/messages';
import { AppConstants } from '@app/constants/app.constants';

@Component({
  selector: 'app-column-grid-banner',
  templateUrl: './column-grid-banner.component.html',
  styleUrls: ['./column-grid-banner.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
})
export class ColumnGridBannerComponent implements OnInit {

  @Input() columnGridBanners: Array<LandingBannerCategory> = [];
  @Input() bannerTitle: string = '';
  @Input() isMobileView: boolean;
  @Input() parentClass: string = '';
  messages: Messages;
  mainNav: Array<Category>;
  config: Config;

  constructor(
    private messageStoreService: MessagesStoreService,
    private userStoreService: UserStoreService,
    public mainNavStore: NavStoreService
  ) {
    this.messages = this.messageStoreService.messages;
    this.config = this.userStoreService.config;
    this.mainNav = this.mainNavStore.mainNav;
  }

  ngOnInit(): void {}

  getProductBannerRouterLink(slug: string, tileLink) {
    if(tileLink) {
      return tileLink;
    } else {
      const category: Category = this.getCategoryObject(this.mainNav, slug);
      if (category) {
        if (category.detailUrl) { // DETAIL URL EXISTS WITH PSID
          const urlSplit = category.detailUrl.split('/');
          const params: Params = {
            category: category.parents[0]?.parents[0]?.slug ? category.parents[0]?.parents[0]?.slug : category.parents[0].slug,
            subcat: category.parents[0]?.parents[0]?.slug && category.parents[0].slug ? category.parents[0].slug : category.slug,
            addCat: urlSplit.length > 1 ? urlSplit[urlSplit.length - 2] : '',
            psid: urlSplit[urlSplit.length - 1]
          };

          if (params.subcat === params.addCat) {
            return AppConstants.BROWSE_BASE_URL + `/${params.category}/${params.subcat}/${params.psid}`;
          } else {
            return AppConstants.CURATED_BASE_URL + `/${params.category}/${params.subcat}/${params.addCat}/${params.psid}`;
          }
        } else {
          if (category.templateType === AppConstants.templateType.LANDING) { // LANDING
            if (category.configurable) {
              return AppConstants.CONFIGURE_BASE_URL + slug; // CONFIGURE URL
            } else {
              return AppConstants.BROWSE_BASE_URL + slug; // BROWSE URL
            }
          } else if (category.templateType === AppConstants.templateType.CONFIGURABLE) { // CONFIGURE URL
            return AppConstants.CONFIGURE_BASE_URL + category.parents[0]?.slug + '/' + slug;
          } else if (category.templateType === AppConstants.templateType.LISTORGRID) {  // List/GRID BROWSE URL
            return AppConstants.BROWSE_BASE_URL + category.parents[0]?.slug + '/' + slug;
          } else if (category.templateType === AppConstants.templateType.CATEGORYLIST) { // CATEGORYLIST
            let curatedURL = AppConstants.CURATED_BASE_URL;
            curatedURL += category.parents[0]?.parents[0]?.slug ? '/' + category.parents[0]?.parents[0]?.slug : '';
            curatedURL += category.parents[0]?.slug ? '/' + category.parents[0]?.slug : '';
            curatedURL += category.slug ? '/' + category.slug : '';
            return curatedURL; // CURATED URL
          } else if (category.templateType == AppConstants.templateType.CATEGORY) {
            return AppConstants.CURATED_BASE_URL + '/' + category.slug + '/' + category.subCategories[0].slug;
          }
        }
      }
      return [];
    }
  }

  getCategoryObject(categories: Array<Category>, slug: string): Category {
    let result = null;
    for (const category of categories) {
      result = (category && category.slug === slug) ? category : this.getCategoryObject(this.setParentObject(category, category.subCategories), slug);
      if (result) {
        return result;
      }
    }
    return result;
  }

  setParentObject(category: Category, subCategories: Array<Category>): Array<Category> {
    if (subCategories.length > 0) {
      subCategories.forEach(cat => cat.parents = [category]);
    }
    return subCategories;
  }

}
