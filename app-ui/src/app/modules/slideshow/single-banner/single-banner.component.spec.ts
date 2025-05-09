import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SafePipe } from '@app/pipes/safe.pipe';

import { SingleBannerComponent } from './single-banner.component';

describe('SingleBannerComponent', () => {
  let component: SingleBannerComponent;
  let fixture: ComponentFixture<SingleBannerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SingleBannerComponent, SafePipe ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SingleBannerComponent);
    component = fixture.componentInstance;
    component.banner = {
      categoryId: '-1',
      categoryName: null,
      bannerType: 'programInfo',
      config: {
        mobileImageUrl: '/apple-gr/assets/img/banners/BannerImage-PIB-Mobile.png',
        desktopImageUrl: '/apple-gr/assets/img/banners/BannerImage-PIB.png',
        imageTxt: 'Program Info Banner',
        showBtn: true,
        ctaClass: 'left-text justify-content-center',
        shortDescription: 'Use your points to pay for part or all of your purchases.',
        btnEnabled: true,
        title: '',
        tagLine: 'Shop Apple<sup>®</sup> on Ultimate Rewards<sup>®</sup>',
        tileBgColor: '#FFFFFF',
        btnType: 'link',
        btnTxt: 'Learn More',
        btnTextColor: '#0070c9',
        btnLink: '/store/terms',
        templateClass: 'chase-PIB',
        btnClass: 'icon icon-arrow-right'
      },
      products: null
    }
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
