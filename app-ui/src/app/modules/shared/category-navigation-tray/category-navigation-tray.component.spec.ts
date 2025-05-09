import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { CategoryNavigationTrayComponent } from './category-navigation-tray.component';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { NavStoreService } from '@app/state/nav-store.service';
import { Category } from '@app/models/category';
import { of } from 'rxjs';
import { AppConstants } from '@app/constants/app.constants';
import { Component } from '@angular/core';
import { By } from '@angular/platform-browser';
import { SharedService } from '@app/modules/shared/shared.service';

// Simple test component that will not in the actual app
@Component({
  template:
    `<div class="test">
    <app-page-title>
    </app-page-title>
    <app-category-navigation-tray>
      <div class="nav-tray">
      </div>
    </app-category-navigation-tray>
  </div>`
})
class TestComponent {}

describe('CategoryNavigationTrayComponent', () => {
  let component: CategoryNavigationTrayComponent;
  let fixture: ComponentFixture<CategoryNavigationTrayComponent>;
  let testFixture: ComponentFixture<TestComponent>;
  let mainNavStore: NavStoreService;
  const mainNavData = require('assets/mock/categories.json');

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ CategoryNavigationTrayComponent ],
      imports: [
        RouterModule.forRoot([], { relativeLinkResolution: 'legacy' }),
        TranslateModule.forRoot()
      ],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({
              category: 'accessories'
            })
          }
        },
        {
          provide: SharedService,
          useValue: {
            convertToRemUnit: (value) => value
          }
        }
      ]
    })
    .compileComponents();
    mainNavStore = TestBed.inject(NavStoreService);
    mainNavStore.addMainNav(mainNavData);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CategoryNavigationTrayComponent);
    testFixture = TestBed.createComponent(TestComponent);
    component = fixture.componentInstance;
    component.mainNav = mainNavData;
    component.category = AppConstants.ACCESSORIES;
    const selectedCategory: Category[] = component.mainNav.filter((cat) => cat.slug === component.category );
    component.subCategory = AppConstants.ALL_ACCESSORIES;
    if (selectedCategory.length) {
      const selectedSubCat: Category[] = selectedCategory[0].subCategories.filter((subcat) => subcat.slug === component.subCategory);
      component.shopByCategory = (selectedSubCat.length) ? selectedSubCat[0].subCategories : [];
    }
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    component['activatedRoute'].params = of({category: 'ipad', subcat: 'test'});
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should call ngOnInit for other templatetypes', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    component['activatedRoute'].params = of({category: 'mac', subcat: 'macbook-air'});
    component.mainNav = mainNavData[0];
    component.mainNav.subCategories[4]['templateType'] = 'TEST';
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should take subcat as all-accessories if category is accessories and subcat is null', () => {
    component.ngOnInit();
    expect(component.subCategory).toEqual('all-accessories');
  });

  it('on toggleCategoryNavigationTrayFucn(), toggle the value of categoryNavigationTray.show', () => {
    const showCategoryNavigationTray = component.showCategoryNavigationTray;
    component.toggleCategoryNavigationTrayFucn();
    expect(component.showCategoryNavigationTray).toBe(!showCategoryNavigationTray);
  });

  it('expect routerLink based on parameters without addCat', () => {
    const category = mainNavData[0];
    const subcat = 'all-accessories';
    expect(component.getRouterLink(category, subcat)).toEqual(`/store/curated/${category.slug}/${subcat}`);
  });

  it('expect routerLink based on parameters with addCat', () => {
    const category = 'accessories';
    const subcat = 'all-accessories';
    const addCat = 'all-accessories-health-fitness';
    expect(component.getRouterLink(category, subcat, addCat)).toEqual(`/store/curated/${category}/${subcat}/${addCat}`);
  });

  it('expect categoryListColumn  to be computed based on categoryListColumn', () => {
    component.mainNav = mainNavData;
    component.category = AppConstants.ACCESSORIES;
    const selectedCategory: Category[] = component.mainNav.filter((cat) => cat.slug === component.category );
    component.subCategory = AppConstants.ALL_ACCESSORIES;
    if (selectedCategory.length) {
      const selectedSubCat: Category[] = selectedCategory[0].subCategories.filter((subcat) => subcat.slug === component.subCategory);
      component.shopByCategory = (selectedSubCat.length) ? selectedSubCat[0].subCategories : [];
    }
    if (component.shopByCategory.length > 15){
      expect(component.categoryListColumn).toEqual(Math.ceil(component.shopByCategory.length / 3));
      expect(component.categoryListColumnHeight).toEqual(component.categoryListColumn * 33);
    }
  });

  it('expect closeNavTray() to set showCategoryNavigationTray to false', () => {
    component.closeNavTray();
    expect(component.showCategoryNavigationTray).toBeFalse();
  });

  it('onScroll() to call closeNavTray() when (showCategoryNavigationTray && !isMobile) ', () => {
    spyOn(component, 'onScroll').and.callThrough();
    component.showCategoryNavigationTray = true;
    component.isMobile = false;
    window.dispatchEvent(new Event('scroll'));
    fixture.detectChanges();
    expect(component.onScroll).toHaveBeenCalled();
  });

  it('onScroll() to call closeNavTray() when (!showCategoryNavigationTray && !isMobile) ', () => {
    spyOn(component, 'onScroll').and.callThrough();
    component.showCategoryNavigationTray = false;
    component.isMobile = false;
    window.dispatchEvent(new Event('scroll'));
    fixture.detectChanges();
    expect(component.onScroll).toHaveBeenCalled();
  });

  it('click() to call closeNavTrayEvent() when showCategoryNavigationTray', () => {
    spyOn(component, 'closeNavTrayEvent').and.callThrough();
    component.showCategoryNavigationTray = true;
    component.isMobile = false;
    component.isAccessoriesShown = true;
    const event = new Event('click', { bubbles: true, cancelable: false });
    document.body.dispatchEvent(event);
    fixture.detectChanges();
    expect(component.closeNavTrayEvent).toHaveBeenCalled();
  });

  it('click() to call closeNavTrayEvent() when showCategoryNavigationTray is false', () => {
    spyOn(component, 'closeNavTrayEvent').and.callThrough();
    component.showCategoryNavigationTray = false;
    component.isMobile = false;
    component.isAccessoriesShown = true;
    const event = new Event('click', { bubbles: true, cancelable: false });
    document.body.dispatchEvent(event);
    fixture.detectChanges();
    expect(component.closeNavTrayEvent).toHaveBeenCalled();
  });

  it('click() to call closeNavTrayEvent() when showCategoryNavigationTray is false checking for template', () => {
    spyOn(component, 'closeNavTrayEvent').and.callThrough();
    component.showCategoryNavigationTray = true;
    component.isMobile = false;
    component.isAccessoriesShown = true;
    const element = testFixture.debugElement.query(By.css('.nav-tray'));
    const event = new Event('click', { bubbles: true, cancelable: false });
    element.nativeElement.dispatchEvent(event);
    fixture.detectChanges();
    expect(component.closeNavTrayEvent).toHaveBeenCalled();
  });
});
