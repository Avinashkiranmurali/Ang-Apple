import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MarketingBannerComponent } from './marketing-banner.component';
import { BannerTemplate } from '@app/models/banner-template';

describe('MarketingBannerComponent', () => {
  let component: MarketingBannerComponent;
  let fixture: ComponentFixture<MarketingBannerComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ MarketingBannerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MarketingBannerComponent);
    component = fixture.componentInstance;
    component.marketingBanner = {} as BannerTemplate;
    component.marketingBannerList = [];
    component.items = [] as Array<BannerTemplate>;
    component.messages = require('assets/mock/messages.json');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call ngOnChanges - with items', () => {
    component.items = [
      {
        isAdditionalInfoExist: true,
        template: 'multi-product.htm',
        list: 'iphone12,iphone12mini',
        multiBannerSubTitle: 'Save up to 20% off select iPhone 12 models and up to 15% off select iPhone 12 models. ',
        multiBannerTitle: 'Just in time for Back-to-School Just in time for Back-to-School Just in time for Back-to-School',
        listDetails: [
          [
            {
              additionalInfo: 'Save 10%',
              additionalInfoClass: 'orange-thin',
              btnClass: 'icon icon-arrow-right',
              btnLink: '/store/configure/mac/macbook-air/',
              btnTxt: 'See available options',
              btnTxtColor: '',
              externalImageUrl: 'https://als-static.bridge2rewards.com/dev2/static/images/MPBImage-MacBookAir.png',
              title: 'MacBook Air'
            }
          ]
        ]
      },
      {
        isAdditionalInfoExist: true,
        template: 'multi-product.htm',
        list: 'macbookpro,macbookair',
        multiBannerSubTitle: 'Save up to 20% off select MacBook Air models and up to 15% off select MacBook Pro models. ',
        multiBannerTitle: 'Back to School',
        listDetails: [
          [
            {
              additionalInfo: '',
              additionalInfoClass: 'orange-thin',
              btnClass: 'icon icon-arrow-right',
              btnLink: '/store/configure/mac/macbook-air/',
              btnTxt: 'See available options',
              btnTxtColor: '',
              externalImageUrl: 'https://als-static.bridge2rewards.com/dev2/static/images/MPBImage-MacBookAir.png',
              title: 'MacBook Air'
            }
          ]
        ]
      },
      {
        additionalInfo: '',
        additionalInfoClass: '',
        btnClass: 'pill-shaped-cta',
        btnLink: '/store/webshop/fdbeats',
        btnTxt: 'Shop Now',
        externalImageUrl: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/MQUF2?wid=460&hei=460&fmt=jpeg&qlt=95&op_usm=0.5,0.5&.v=1576541430199',
        imageTxt: 'Beats Sale',
        tagLine: 'From now until June 15th, 2020',
        title: 'Up to 25% off select Beats'
      }
    ] as Array<BannerTemplate>;
    spyOn(component, 'ngOnChanges').and.callThrough();
    component.ngOnChanges();
    expect(component.ngOnChanges).toHaveBeenCalled();
  });

  it('should call ngOnChanges - without items', () => {
    component.items = null;
    spyOn(component, 'ngOnChanges').and.callThrough();
    component.ngOnChanges();
    expect(component.ngOnChanges).toHaveBeenCalled();
  });

});
