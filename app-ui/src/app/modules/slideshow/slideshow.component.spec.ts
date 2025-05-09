import { DecimalPipe } from '@angular/common';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SlideshowComponent } from './slideshow.component';
import { TemplateBannerComponent } from './template-banner/template-banner.component';
import { BannerConfigPipe } from '@app/pipes/banner-config.pipe';
import { RouterTestingModule } from '@angular/router/testing';
import { SafePipe } from '@app/pipes/safe.pipe';
import { SingleBannerComponent } from './single-banner/single-banner.component';
import { SharedService } from '@app/modules/shared/shared.service';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { of } from 'rxjs';
import { UserStoreService } from '@app/state/user-store.service';
import {TranslateService} from '@ngx-translate/core';
import {TranslateServiceStub} from '@app/modules/shared/media-product/media-product.component.spec';

(window as any).UIkit = {
  util: {
    on: (element: string, event: string, cb: any) => {}
  }
};

describe('SlideshowComponent', () => {
  let component: SlideshowComponent;
  let fixture: ComponentFixture<SlideshowComponent>;
  const programData = require('assets/mock/program.json');
  const userMock = require('assets/mock/user.json');
  programData['imageServerUrl'] = 'https://als-static.bridge2rewards.com/dev';
  userMock['program'] = programData;
  const userData = {
    user: userMock,
    program: programData,
    config: programData['config'],
    get: () => of(userMock)
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        SlideshowComponent,
        SingleBannerComponent,
        TemplateBannerComponent,
        BannerConfigPipe,
        SafePipe
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule
      ],
      providers: [
        { provide: UserStoreService, useValue: userData },
        {
          provide: TranslateService,
          useClass: TranslateServiceStub
        },
        SharedService,
        SafePipe,
        BannerConfigPipe,
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SlideshowComponent);
    component = fixture.componentInstance;
    const storeLandingBanners = require('assets/mock/store-landing-banners.json');
    component.banners = storeLandingBanners['landing'];
    component.isMobile = false;
    component.imageServerUrl = 'https://als-static.bridge2rewards.com/dev2';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should add aria-label', () => {
    const element = document.querySelectorAll('.uk-dotnav');
    const ele = document.createElement('li');
    ele.appendChild(document.createElement('a'));
    element.forEach(element => element.appendChild(ele));
    spyOn(component, 'ngAfterViewInit').and.callThrough();
    fixture.detectChanges();
    component.ngAfterViewInit();
    expect(component.ngAfterViewInit).toHaveBeenCalled();
  });

});
