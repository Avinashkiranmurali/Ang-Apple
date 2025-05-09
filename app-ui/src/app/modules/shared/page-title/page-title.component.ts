import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NavStoreService } from '@app/state/nav-store.service';
import { Category } from '@app/models/category';
import { AppConstants } from '@app/constants/app.constants';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-page-title',
  templateUrl: './page-title.component.html',
  styleUrls: ['./page-title.component.scss']
})
export class PageTitleComponent implements OnInit  {

  pageName: string;
  category: string;
  subcat: string;
  addcat: string;
  psid?: string;
  mainNav: Array<Category>;
  items = [];
  scroll = false;
  size = 12;

  constructor(
    private activatedRoute: ActivatedRoute,
    private mainNavStore: NavStoreService,
    private translateService: TranslateService,
    private router: Router
  ) {
    this.mainNav = this.mainNavStore.mainNav;
  }

  @Input() pageTitle?: string;
  ngOnInit(): void {
    this.activatedRoute.params.subscribe(params => {
      this.category = params.category;
      this.subcat = params.subcat;
      this.addcat = params.addCat;
      this.psid = params.psid ?  params.psid : '';
      this.setPageTitle();
    });
  }

  setPageTitle(): void {
    if (this.pageTitle) {
      this.pageName = this.pageTitle;
    } else if (this.router.url.indexOf('webshop') > -1) {
      this.pageName = this.translateService.instant('webShopHeading_' + this.category);
    } else {
      const currentCatData = this.mainNav.filter(nav => nav.slug === this.category);
      const currentSubCatObj = (currentCatData && currentCatData.length && currentCatData[0].subCategories) ? currentCatData[0].subCategories : [];
      const currentSubCatData = currentSubCatObj.filter(subcat => subcat.slug === this.subcat);

      const categoryName = (currentCatData && currentCatData.length && currentCatData[0].i18nName) ? currentCatData[0].i18nName : '';
      const subCatName = (currentSubCatData && currentSubCatData.length && currentSubCatData[0].i18nName) ? currentSubCatData[0].i18nName : '';
      if (this.addcat && this.addcat !== '') {
        const currentAddCatObj = (currentSubCatData && currentSubCatData.length && currentSubCatData[0].subCategories) ? currentSubCatData[0].subCategories : [];
        const currentAddCatData = currentAddCatObj.filter(addCat => addCat.slug === this.addcat);
        this.pageName = this.psid !== '' ? this.category === AppConstants.ACCESSORIES ? categoryName : subCatName : (currentAddCatData && currentAddCatData.length && currentAddCatData[0].i18nName ) ? currentAddCatData[0].i18nName : subCatName;
      } else {
        this.pageName = (subCatName !== '') ? subCatName : categoryName;
      }
    }
  }

}
