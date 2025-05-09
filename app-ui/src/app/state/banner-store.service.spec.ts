import { TestBed } from '@angular/core/testing';
import { BannerStoreService } from './banner-store.service';

describe('BannerStoreService', () => {
  let bannerStoreService: BannerStoreService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    bannerStoreService = TestBed.inject(BannerStoreService);
  });

  it('should be created', () => {
    expect(bannerStoreService).toBeTruthy();
  });

  it('should call addBanner Method', () => {
    spyOn(bannerStoreService, 'addBanner').and.callThrough();
    const data = require('assets/mock/banner.json');
    bannerStoreService.addBanner(data);
    expect(bannerStoreService.addBanner).toHaveBeenCalled();
  });

  it('should get Banner value', () => {
    const data = require('assets/mock/banner.json');
    bannerStoreService.banner = data;
    expect(bannerStoreService.banner).toEqual(data);
  });

  it('should call addBanners Method', () => {
    spyOn(bannerStoreService, 'addBanners').and.callThrough();
    const data = require('assets/mock/store-landing-banners.json');
    bannerStoreService.addBanners(data);
    expect(bannerStoreService.addBanners).toHaveBeenCalled();
  });

  it('should get Banner value', () => {
    const data = require('assets/mock/store-landing-banners.json');
    bannerStoreService.banners = data;
    expect(bannerStoreService.banners).toEqual(data);
  });

});
