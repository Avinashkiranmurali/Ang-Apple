import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MultiProductBannerComponent } from './multi-product-banner.component';
import { BannerTemplate } from '@app/models/banner-template';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { InterpolatePipe } from '@app/pipes/interpolate.pipe';
import { UserService } from '@app/services/user.service';

describe('MultiProductBannerComponent', () => {
  let component: MultiProductBannerComponent;
  let fixture: ComponentFixture<MultiProductBannerComponent>;
  const programData = require('assets/mock/program.json');
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ MultiProductBannerComponent, InterpolatePipe ],
      imports: [
        RouterTestingModule,
        TranslateModule.forRoot(),
        HttpClientTestingModule
      ],
      providers: [
        { provide: UserService, useValue: userData }
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MultiProductBannerComponent);
    component = fixture.componentInstance;
    component.multiProductBanner = {} as BannerTemplate;
    component.messages = require('assets/mock/messages.json');
    component.multiProductBannerList = [];
    component.items = [] as Array<Array<BannerTemplate>>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call ngOnChanges - with items', () => {
    component.items = [
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
        },
        {
          additionalInfo: '',
          additionalInfoClass: 'orange-thin',
          btnClass: 'icon icon-arrow-right',
          btnLink: '/store/browse/mac/mac-mini/',
          btnTxt: 'See available options',
          btnTxtColor: '',
          externalImageUrl: 'https://als-static.bridge2rewards.com/dev2/static/images/MPBImage-Macmini.png',
          title: 'Mac mini'
        }
      ]
    ] as Array<Array<BannerTemplate>>;
    spyOn(component, 'ngOnChanges').and.callThrough();
    component.ngOnChanges();
    expect(component.ngOnChanges).toHaveBeenCalled();
  });

  it('should call ngOnChanges - without additionalitems', () => {
    component.items = [
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
    ] as Array<Array<BannerTemplate>>;
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
