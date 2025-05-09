import { DecimalPipe } from '@angular/common';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { BannerConfigPipe } from '@app/pipes/banner-config.pipe';
import { SafePipe } from '@app/pipes/safe.pipe';
import { UserStoreService } from '@app/state/user-store.service';
import { of } from 'rxjs';
import { TemplateBannerComponent } from './template-banner.component';
import { SharedService } from '@app/modules/shared/shared.service';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import {TranslateService} from '@ngx-translate/core';
import {TranslateServiceStub} from '@app/modules/shared/media-product/media-product.component.spec';

describe('TemplateBannerComponent', () => {
  let component: TemplateBannerComponent;
  let fixture: ComponentFixture<TemplateBannerComponent>;
  const storeLandingBanners = require('assets/mock/store-landing-banners.json');
  const banner = storeLandingBanners['landing'];
  const programData = require('assets/mock/program.json');
  programData.config.MercAddressLocked = false;
  const mockUser = require('assets/mock/user.json');
  mockUser['program'] = programData;
  const userData = {
    user: mockUser,
    program: programData,
    config: programData['config'],
    get: () => of(mockUser)
  };
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TemplateBannerComponent, SafePipe, BannerConfigPipe ],
      imports: [ HttpClientTestingModule, RouterTestingModule ],
      providers: [
        SharedService,
        BannerConfigPipe,
        { provide: UserStoreService, useValue: userData },
        {
          provide: TranslateService,
          useClass: TranslateServiceStub
        },
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateBannerComponent);
    component = fixture.componentInstance;
    component.templateBanner = banner[1].config.template;
    component.imageServerUrl = 'https://als-static.bridge2rewards.com/dev2';
    component.config = userData.config;
    component.categoryId = 'macbook-air';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should trigger AfterViewInit manually where termsLink is null', () => {
    spyOn(component, 'ngAfterViewInit').and.callThrough();
    component.templateBanner = undefined;
    component.ngAfterViewInit();
    expect(component.ngAfterViewInit).toHaveBeenCalled();
  });

  it('should trigger event on click of termsLink', () => {
    const routerstub: Router = TestBed.inject(Router);
    spyOn(routerstub, 'navigate');
    const element = component.banner?.nativeElement.getElementsByTagName('a');
    element[0]?.click();
    expect(component['router'].navigate).toHaveBeenCalledWith(['/btnLink'], undefined);
  });

  it('should trigger event on click of termsLink with fragments', () => {
    const routerstub: Router = TestBed.inject(Router);
    banner[1].config.btnLink = '/store/terms#Engraving';
    component.templateBanner = banner[1].config.template;
    fixture.detectChanges();
    spyOn(routerstub, 'navigate');
    const element = component.banner?.nativeElement.getElementsByTagName('a');
    element[0]?.click();
    expect(component['router'].navigate).toHaveBeenCalled();
  });
});
