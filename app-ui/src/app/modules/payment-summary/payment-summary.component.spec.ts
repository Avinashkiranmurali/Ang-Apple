import { DecimalPipe } from '@angular/common';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { CartService } from '@app/services/cart.service';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateModule } from '@ngx-translate/core';
import { PaymentSummaryComponent } from './payment-summary.component';

describe('PaymentSummaryComponent', () => {
  let component: PaymentSummaryComponent;
  let fixture: ComponentFixture<PaymentSummaryComponent>;
  const programData = require('assets/mock/program.json');
  const userMockData = require('assets/mock/user.json');
  userMockData['program'] = programData;
  const userData = {
    user: userMockData,
    program: programData,
    config: programData['config']
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PaymentSummaryComponent, CurrencyFormatPipe],
      imports: [
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        RouterTestingModule,
        BrowserAnimationsModule
      ],
      providers: [
        MessagesStoreService,
        CartService,
        PricingService,
        { provide: UserStoreService, useValue: userData },
        { provide: CurrencyFormatPipe },
        { provide: DecimalPipe },
        { provide: CurrencyPipe }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PaymentSummaryComponent);
    component = fixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
    fixture.detectChanges();
  });

  afterAll(() => {
    component['userStore'].program.formatPointName = 'delta.points';
  });

  it('should create with payFrequency', () => {
    component['userStore']['config']['payFrequency'] = 'test';
    expect(component).toBeTruthy();
  });

  it('should create', () => {
    component['userStore']['config']['payFrequency'] = '';
    component['userStore'].program.formatPointName = '';
    expect(component).toBeTruthy();
  });

  it('should call ngOnInit', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    component.config.showFeeDetails = true;
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('getPaymentSummaryTemp should return some value', () => {
    spyOn(component['sharedService'], 'isCashOnlyRedemption').and.callFake(() => true);
    spyOn(component, 'getPaymentSummaryTemp').and.callThrough();
    component.paymentTemplate = 'cash_subsidy';
    component.getPaymentSummaryTemp();
    component.paymentTemplate = 'installment_monthly';
    component.getPaymentSummaryTemp();
    component.paymentTemplate = '';
    component.getPaymentSummaryTemp();
    expect(component.getPaymentSummaryTemp).toHaveBeenCalledTimes(3);
  });

  it('getPaymentSummaryTemp should return null if not cashonly', () => {
    spyOn(component['sharedService'], 'isCashOnlyRedemption').and.callFake(() => false);
    spyOn(component, 'getPaymentSummaryTemp').and.callThrough();
    expect(component.getPaymentSummaryTemp()).toBeUndefined();
    expect(component.getPaymentSummaryTemp).toHaveBeenCalled();
  });

  it('should call toggleShowDetails method', () => {
    spyOn(component, 'toggleShowDetails').and.callThrough();
    const event = new Event('click');
    component.toggleShowDetails(event);
    expect(component.toggleShowDetails).toHaveBeenCalled();
  });

  it('should call toggleShowDetails method for animationstate in', () => {
    spyOn(component, 'toggleShowDetails').and.callThrough();
    component.animationState = 'in';
    const event = new Event('click');
    component.toggleShowDetails(event);
    expect(component.toggleShowDetails).toHaveBeenCalled();
  });

  it('should call hasDiscount Method', () => {
    spyOn(component, 'hasDiscount').and.callThrough();
    component.hasDiscount();
    expect(component.hasDiscount).toHaveBeenCalled();
  });

  it('should call discountedSubTotal Method', () => {
    spyOn(component, 'discountedSubTotal').and.callThrough();
    component.discountedSubTotal();
    expect(component.discountedSubTotal).toHaveBeenCalled();
  });

  it('should call showAdditionalPayment method', () => {
    component.paymentRequired = true;
    spyOn(component['sharedService'], 'isPointsFixed').and.callFake(() => true);
    spyOn(component, 'showAdditionalPayment').and.callThrough();
    expect(component.showAdditionalPayment()).toBeTruthy();
    expect(component.showAdditionalPayment).toHaveBeenCalled();
  });

});
