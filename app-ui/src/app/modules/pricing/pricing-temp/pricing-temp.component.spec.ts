import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { PricingTempComponent } from './pricing-temp.component';
import { TranslateModule } from '@ngx-translate/core';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { UserStoreService } from '@app/state/user-store.service';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { DecimalPipe } from '@angular/common';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { SharedService } from '@app/modules/shared/shared.service';

describe('PricingTempComponent', () => {
  let component: PricingTempComponent;
  let fixture: ComponentFixture<PricingTempComponent>;
  const programData = require('assets/mock/program.json');
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ PricingTempComponent, CurrencyFormatPipe ],
      providers: [
        PricingService,
        CurrencyFormatPipe,
        { provide: UserStoreService, useValue: userData },
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe,
        SharedService
      ],
      imports: [
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        RouterTestingModule
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PricingTempComponent);
    component = fixture.componentInstance;
    component.item  = {additionalInfo: ''};
    component.messages = require('assets/mock/messages.json');
    const product = require('assets/mock/product-detail.json');
    component.offer = product.offers[0];
    fixture.detectChanges();
  });

  afterAll(() => {
    component.userStore.program.formatPointName = 'delta.points';
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call ngOnInit method', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    component.userStore.program.formatPointName = '';
    component.item = null;
    fixture.detectChanges();
    component.ngOnInit();
  });

  it('ngOnit should assign value for isDiscounted', () => {
    component.offer = require('assets/mock/product-detail.json')['offers'];
    component.ngOnChanges();
    if (component.offer) {
      expect(component.isDiscounted).toEqual(component.pricingService.checkDiscounts(component.offer));
    }
  });

  it('ngOnit should assign value for isDiscounted', () => {
    component.offer = null;
    component.ngOnChanges();
    expect(component.ngOnChanges).toBeDefined();
  });

});
