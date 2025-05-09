import {
  Component,
  EventEmitter,
  Injector,
  OnInit,
  Output,
  HostListener,
  AfterViewChecked
} from '@angular/core';
import { BreakPoint } from '@app/components/utils/break-point';
import { NavStoreService } from '@app/state/nav-store.service';
import { ActivatedRoute, Router } from '@angular/router';
import { AppConstants } from '@app/constants/app.constants';
import { Category } from '@app/models/category';
import { SharedService } from '@app/modules/shared/shared.service';
@Component({
  selector: 'app-category-navigation-tray',
  templateUrl: './category-navigation-tray.component.html',
  styleUrls: ['./category-navigation-tray.component.scss']
})
export class CategoryNavigationTrayComponent extends BreakPoint implements OnInit, AfterViewChecked {

  constructor(
    public injector: Injector,
    public mainNavStore: NavStoreService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private sharedService: SharedService
    ) {
    super(injector);
    this.showCategoryNavigationTray = false;
  }
  shopByProduct;
  shopByCategory;
  categoryListColumn = 5;
  navTrayHandler = {
    show: true
  };
  showCategoryNavigationTray: boolean;
  categoryNavigationTray = {
    show: false
  };
  categoryListColumnHeight;
  subCategory;
  addtionalCategory;
  category;
  mainNav;
  isAccessoriesShown: boolean;
  @Output() public toggleCategoryNavigationTray = new EventEmitter<void>();

  ngOnInit(): void {
    this.mainNav = this.mainNavStore.mainNav;
    this.shopByProduct = [];
    this.shopByCategory = (this.shopByCategory) ? this.shopByCategory : [];

    this.activatedRoute.params.subscribe(params => {
      this.isAccessoriesShown = this.router.url.indexOf(AppConstants.CURATED) !== -1;
      this.category = params.category;
      this.subCategory = params.subcat;
      if (this.category === AppConstants.ACCESSORIES && !this.subCategory) {
        this.subCategory = AppConstants.ALL_ACCESSORIES;
      }
      const selectedCategory: Category[] = this.mainNav.filter((cat) => cat.slug === this.category );
      if (selectedCategory.length) {
        const selectedSubCat: Category[] = selectedCategory[0].subCategories.filter((subcat) => subcat.slug === this.subCategory);
        this.shopByCategory = (selectedSubCat.length) ? selectedSubCat[0].subCategories : [];
      }
    });

    this.mainNav.forEach((nav) => {
      // Old filter logic code: $filter('filter')(nav.subCategories, {templateType: 'CATEGORYLIST'}, true)[0];
      const navAccessories = nav.subCategories.filter(cat => cat.templateType === 'CATEGORYLIST')[0];
      nav.accCategory = navAccessories || '';
      this.shopByProduct.push(nav);
    });
  }

  ngAfterViewChecked(): void {
    if (this.shopByCategory.length > 15) {
      this.categoryListColumn = Math.ceil(this.shopByCategory.length / 3); // 3 columns container
      this.categoryListColumnHeight = this.sharedService.convertToRemUnit(this.categoryListColumn * 33);
    }
  }

  @HostListener('window:scroll')
  onScroll() {
       if (this.showCategoryNavigationTray && !this.isMobile){
      this.closeNavTray();
        }
  }

  getRouterLink(cat, subcat, addCat= null){
    if (addCat){
      return `/store/curated/${this.category}/${this.subCategory}/${addCat}`;
    } else if (cat.templateType === AppConstants.templateType.CATEGORY) {
      return `/store/curated/${cat.slug}/${cat.subCategories[0].slug}`;
    }
    return `/store/curated/${cat.slug}/${subcat}`;
  }

  toggleCategoryNavigationTrayFucn(){
    this.toggleCategoryNavigationTray.emit();
    this.showCategoryNavigationTray = !this.showCategoryNavigationTray;
  }

  closeNavTray(){
    this.toggleCategoryNavigationTray.emit();
    this.showCategoryNavigationTray = false;
  }

  @HostListener('document:click', ['$event'])
  closeNavTrayEvent(event) {
    // Execlude navtray close action for pageTitleAccessories Component, navtray inner container click action
    if (this.showCategoryNavigationTray){
      const isChildren = event.target.closest('app-page-title, app-category-navigation-tray .nav-tray');
      const navBtn = event.target.closest('.btn-nav-tray-control');
      const isChildrenMobile = event.target.closest('.view-control-container');
      if (!(isChildren || isChildrenMobile || navBtn)){
        this.closeNavTray();
      }
    }
  }

}
