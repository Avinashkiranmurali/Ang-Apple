import { HttpClientTestingModule } from '@angular/common/http/testing';
import { waitForAsync, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterEvent, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { BannerStoreService } from '@app/state/banner-store.service';
import { NavStoreService } from '@app/state/nav-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateModule } from '@ngx-translate/core';
import { BehaviorSubject } from 'rxjs';
import { ColumnGridBannerComponent } from './column-grid-banner.component';

describe('ProductFamilyBannerComponent', () => {
  let component: ColumnGridBannerComponent;
  let fixture: ComponentFixture<ColumnGridBannerComponent>;
  let userStoreService: UserStoreService;
  let navStoreService: NavStoreService;
  let bannerStoreService: BannerStoreService;
  const routerEvent$ = new BehaviorSubject<RouterEvent>(null);
  let router: Router;
  const mainNavData = require('assets/mock/categories.json');
  const bannerData = require('assets/mock/banner.json');
  const programData = require('assets/mock/program.json');
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ColumnGridBannerComponent ],
      imports: [
        RouterTestingModule,
        TranslateModule.forRoot(),
        HttpClientTestingModule
      ]
    })
    .compileComponents();
    userStoreService = TestBed.inject(UserStoreService);
    navStoreService = TestBed.inject(NavStoreService);
    bannerStoreService = TestBed.inject(BannerStoreService);
    userStoreService.addUser(userData.user);
    userStoreService.addProgram(userData.program);
    userStoreService.addConfig(userData.config);
    navStoreService.addMainNav(mainNavData);
    bannerStoreService.addBanner(bannerData);
    router = TestBed.inject(Router);
    (router as any).events = routerEvent$.asObservable();
    router.navigate = jasmine.createSpy('navigate');
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ColumnGridBannerComponent);
    component = fixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
    component.mainNav = mainNavData;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call getProductBannerRouterLink method - Mac not configurable', () => {
    spyOn(component, 'getProductBannerRouterLink').and.callThrough();
    const result = component.getProductBannerRouterLink('mac', null);
    expect(result).not.toBeNull();
  });

  it('should call getProductBannerRouterLink method - apple-tv with detailUrl', () => {
    spyOn(component, 'getProductBannerRouterLink').and.callThrough();
    const result = component.getProductBannerRouterLink('apple-tv-apple-tv', null);
    expect(result).not.toBeNull();
  });

  it('should call getProductBannerRouterLink method - iPhone not configurable', () => {
    spyOn(component, 'getProductBannerRouterLink').and.callThrough();
    const result = component.getProductBannerRouterLink('iphone', null);
    expect(result).not.toBeNull();
  });

  it('should call getProductBannerRouterLink method - Accessories', () => {
    spyOn(component, 'getProductBannerRouterLink').and.callThrough();
    const result = component.getProductBannerRouterLink('accessories', null);
    expect(result).not.toBeNull();
  });

  it('should call getProductBannerRouterLink method - no category matches', () => {
    spyOn(component, 'getProductBannerRouterLink').and.callThrough();
    const result = component.getProductBannerRouterLink('test', null);
    expect(result).toEqual([]);
  });

  it('should call getProductBannerRouterLink method - subCategory', () => {
    spyOn(component, 'getProductBannerRouterLink').and.callThrough();
    const result = component.getProductBannerRouterLink('all-accessories-beats', null);
    expect(result).not.toBeNull();
  });

  it('should call getProductBannerRouterLink method - Accessories detail url', () => {
    spyOn(component, 'getProductBannerRouterLink').and.callThrough();
    component.mainNav[6]['subCategories'][0]['subCategories'][0].detailUrl = 'all-accessories-product-red/30001ML4Q2LL-A';
    const result = component.getProductBannerRouterLink('all-accessories-product-red', null);
    expect(result).not.toBeNull();
  });

});
