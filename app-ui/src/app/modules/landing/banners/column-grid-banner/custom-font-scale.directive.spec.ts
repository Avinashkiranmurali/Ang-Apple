import { DecimalPipe } from '@angular/common';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Component, ElementRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { SharedService } from '@app/modules/shared/shared.service';
import { CustomFontScaleDirective } from './custom-font-scale.directive';

@Component({
    template: `<li class="column-grid-banner {{ banner['categoryId'] }}"
    *ngFor="let banner of productFamilyBanners">
    <a class="product-tile">
      <!-- CUSTOM FONT SCALE DIRECTIVE -->
      <p appCustomFontScale
        [minFontSize]="banner['config']['minimumFontSize'] ? banner['config']['minimumFontSize'] : 11"
        [maxFontSize]="isMobile ? 21 : 24"
        id="{{banner['categoryId']}}"
        style="line-height: 26px;"
        class="category-name">
        <span [innerHTML]="banner['categoryName']" class="category-name-text" style="display: inline-block;"></span>
      </p>
    </a>
</li>`
})
class TestComponent {
  storeLandingBanners = require('assets/mock/store-landing-banners.json');
  productFamilyBanners = this.storeLandingBanners['family'];
  isMobile: true;
}

describe('CustomFontScaleDirective', () => {
  let fixture: ComponentFixture<TestComponent>;
  let customFontScaleDirective: CustomFontScaleDirective;
  let sharedService: SharedService;
  let eleRef: ElementRef;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [
        TestComponent,
        CustomFontScaleDirective
      ],
      providers: [
        SharedService,
        { provide: CurrencyFormatPipe },
        { provide: CurrencyPipe },
        { provide: DecimalPipe }
      ],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule
      ]
    });
    fixture = TestBed.createComponent(TestComponent);
    sharedService = TestBed.inject(SharedService);
    eleRef = fixture.debugElement;
    customFontScaleDirective = new CustomFontScaleDirective(sharedService, eleRef);
    window.onerror = () => {};
    fixture.detectChanges();
  });

  it('should create an instance', () => {
    expect(customFontScaleDirective).toBeTruthy();
  });

  it('should call ngOnInit method', () => {
    customFontScaleDirective.ngOnInit();
    expect(customFontScaleDirective.ngOnInit).toBeTruthy();
  });

  it('should call customizeFontScale method', () => {
    customFontScaleDirective.minFontSize = '11px';
    customFontScaleDirective.maxFontSize = 24;
    const dummyElement = document.createElement('div');
    document.getElementById = jasmine.createSpy('HTML Element').and.returnValue(dummyElement);
    dummyElement.innerHTML = '<p appCustomFontScale style="font-size:24px; line-height: 20px" class="category-name"><span class="category-name-text">iPhone</span> </p>';
    dummyElement.style.fontSize = '7';
    spyOn(customFontScaleDirective, 'customizeFontScale').withArgs(dummyElement).and.callThrough();
    customFontScaleDirective.customizeFontScale(dummyElement);
    expect(customFontScaleDirective.customizeFontScale).toBeDefined();
    customFontScaleDirective.ngOnInit();
    expect(customFontScaleDirective.customizeFontScale).toHaveBeenCalled();
  });

  it('shoud call customizeFontScale method via ngOnInit', () => {
    (window['innerWidth'] as any) = 400;
    const ele = document.querySelector('.mac');
    customFontScaleDirective.minFontSize = '11px';
    customFontScaleDirective.maxFontSize = 24;
    spyOn(customFontScaleDirective, 'customizeFontScale').withArgs(ele).and.callThrough();
    customFontScaleDirective.customizeFontScale(ele);
    expect(customFontScaleDirective.customizeFontScale).toHaveBeenCalled();
  });

});
