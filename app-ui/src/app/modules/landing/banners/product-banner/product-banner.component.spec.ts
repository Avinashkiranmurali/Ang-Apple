import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ProductBannerComponent } from './product-banner.component';
import { InterpolatePipe } from '@app/pipes/interpolate.pipe';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('ProductBannerComponent', () => {
  let component: ProductBannerComponent;
  let fixture: ComponentFixture<ProductBannerComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ProductBannerComponent, InterpolatePipe ],
      imports: [
        RouterTestingModule,
        TranslateModule.forRoot(),
        HttpClientTestingModule
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProductBannerComponent);
    component = fixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
    component.imageServerUrl = 'https://als-static.bridge2rewards.com/dev2';
    const banner = require('assets/mock/banner.json');
    component.productBanner = banner[0]['bannerTemplateObj'];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

});
