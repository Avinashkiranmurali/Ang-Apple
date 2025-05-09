import { TestBed, waitForAsync } from '@angular/core/testing';
import { SharedService } from '@app/modules/shared/shared.service';
import { BannerConfigPipe } from './banner-config.pipe';
import {UserStoreService} from '@app/state/user-store.service';
import {MessagesStoreService} from '@app/state/messages-store.service';
import {Program} from '@app/models/program';
import {Messages} from '@app/models/messages';

describe('BannerConfigPipe', () => {
  let bannerConfigPipe: BannerConfigPipe;
  let userStoreService: UserStoreService = { program : {name : 'test'} as Program} as UserStoreService;
  let messagesStoreService: MessagesStoreService = {messages : 'msg' as unknown as Messages} as MessagesStoreService;
  const redemptionOption = {
    id: 966,
    varId: 'Delta',
    programId: 'b2s_qa_only',
    paymentOption: 'splitpay',
    limitType: 'percentage',
    paymentMinLimit: 0,
    paymentMaxLimit: 50,
    orderBy: 2,
    paymentProvider: null,
    lastUpdatedBy: 'Appl_user',
    lastUpdatedDate: 1527177099217,
    active: true
  };
  const currencyPipeMock: any = {
    userStore: require('assets/mock/user.json'),
    transform: (value) => `$ ${value}`
  };
  let sharedServiceMock = {
    currencyPipe: currencyPipeMock,
    getSplitPayLimitType: (type: string) => redemptionOption
  }
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({})
    .compileComponents();
    bannerConfigPipe = new BannerConfigPipe(sharedServiceMock as SharedService, userStoreService, messagesStoreService);
  }));

  it('create an bannerConfigPipe instance', () => {
    expect(bannerConfigPipe).toBeTruthy();
  });

  it('should execute with templateBanner', () => {
    spyOn(bannerConfigPipe, 'transform').and.callThrough();
    const banner = {
      categoryId: 'music-airpods-3rd-generation',
      categoryName: 'AirPods 3rd Generation',
      bannerType: 'product',
      config: {
        titleImg: '/apple-gr/assets/img/productBanner/airPods3rdGen/BannerTitleImage-AirPods3rdGen.svg',
        btnTxt: 'Shop Now',
        tagLine: '3rd Generation',
        showBtn: true,
        desktopImageUrl: '/apple-gr/assets/img/productBanner/airPods3rdGen/BannerImage-AirPods3rdGen.png',
        btnEnabled: true,
        btnTextColor: '#FFFFFF',
        mobileImageUrl: '/apple-gr/assets/img/productBanner/airPods3rdGen/BannerImage-AirPods3rdGen-Mobile.png',
        btnLink: '/store/configure/music/music-airpods-3rd-generation',
        imageTxt: 'AirPods 3rd Generation',
        ctaClass: 'justify-content-center',
        btnType: 'button',
        tileBgColor: '#FFFFFF',
        showNewIndicator: false,
        btnClass: 'pill-shaped-cta',
        template: '<style> .banner-content.music-airpods-3rd-generation .banner-inner-container { background-color: inherit; display: flex; width: 100%; max-width: inherit; } .banner-content.music-airpods-3rd-generation .banner-inner-container .cta { display: flex; z-index: 1; flex-direction: column; padding-right: 114px; margin: 61px 0 76px 0; } .banner-content.music-airpods-3rd-generation .banner-inner-container .cta .new-indicator { margin-bottom: 25px; } .banner-content.music-airpods-3rd-generation .banner-inner-container .cta .title-image { max-height: 42px; max-width: 193px; } .banner-content.music-airpods-3rd-generation .banner-inner-container .cta h3 { color: #111; font-size: 24px; font-weight: 600; line-height: 29px; text-align: center; margin: 12px 0 0 0; } .banner-content.music-airpods-3rd-generation .banner-inner-container .cta .btn-container { display: flex; padding: 0; margin-top: 15px; justify-content: center; } .banner-content.music-airpods-3rd-generation .banner-inner-container .image-container img { max-width: 171px; } .banner-content.music-airpods-3rd-generation .banner-inner-container .image-container { margin: 30px 0; } @media (max-width: 765px) { .banner-content.music-airpods-3rd-generation .banner-inner-container { flex-direction: column !important; align-self: baseline; } .banner-content.music-airpods-3rd-generation .banner-inner-container .cta { margin: 37px 0 0 0; padding-right: 0; align-items: center; } .banner-content.music-airpods-3rd-generation .banner-inner-container .image-container { margin: 24px 0 0 0; } } @media (min-width: 0px) and (max-width: 767px) { .banner-content.music-airpods-3rd-generation .banner-inner-container .cta .new-indicator { margin-bottom: 20px; }.banner-content.music-airpods-3rd-generation .banner-inner-container .cta .title-image { max-height: 38px; } .banner-content.music-airpods-3rd-generation .banner-inner-container .cta h3 { margin-top: 3px; font-size: 21px; line-height: 25px; } .banner-content.music-airpods-3rd-generation .banner-inner-container .cta .btn-container { margin-top: 14px; } .banner-content.music-airpods-3rd-generation .banner-inner-container .image-container img { max-width: 139px; } } </style> <div class=\'banner-content categoryId\' style=\'background-color: tileBgColor;\'> <section role=\'presentation\' tabindex=\'-1\' class=\'banner-inner-container ctaClass\'> <div class=\'cta\'> <span class=\'new-indicator\' aria-hidden=\'showNewIndicator\' innerHTML=\'newIndicatorText\' style=\'color : newIndicatorColor\'></span> <img role=\'heading\' aria-level=\'2\' class=\'title-image\' alt=\'title\' src=\'titleImg\'> <h3 class=\'banner-tagLine\'>tagLine</h3> <div class=\'btn-container\' aria-hidden=\'showBtn\'> <span class=\'link-base pill-shaped-cta btnClass\'> <a aria-label=\'btnTxt\' aria-disabled=\'btnEnabled\' href=\'btnLink\' style=\'color: btnTextColor;\'>btnTxt</a> </span> </div> </div> <div class=\'image-container\'> <img alt=\'imageTxt\' src=\'desktopImageUrl\'> </div> </section> </div>'
      },
      products: null
    };
    bannerConfigPipe.transform(banner.config.template, banner.config, banner.categoryId);
    expect(bannerConfigPipe.transform).toHaveBeenCalled();
  });

});
