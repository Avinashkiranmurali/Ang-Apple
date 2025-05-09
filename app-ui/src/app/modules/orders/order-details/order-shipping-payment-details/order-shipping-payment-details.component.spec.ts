import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UserStoreService } from '@app/state/user-store.service';
import { SafePipe } from '@app/pipes/safe.pipe';
import { TranslateModule } from '@ngx-translate/core';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { OrderShippingPaymentDetailsComponent } from './order-shipping-payment-details.component';
import { DataMaskingModule } from '@bakkt/data-masking';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';

describe('OrderShippingPaymentDetailsComponent', () => {
  let component: OrderShippingPaymentDetailsComponent;
  let fixture: ComponentFixture<OrderShippingPaymentDetailsComponent>;
  const programData = require('assets/mock/program.json');
  const userMock = require('assets/mock/user.json');
  const orderShippingPaymentData = require('assets/mock/order-history-details.json');
  userMock['program'] = programData;
  const userData = {
    user: userMock,
    program: programData,
    config: programData['config']
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        OrderShippingPaymentDetailsComponent,
        SafePipe,
        OrderByPipe,
        CurrencyFormatPipe
      ],
      imports: [
        TranslateModule.forRoot(),
        RouterTestingModule,
        HttpClientTestingModule,
        DataMaskingModule
      ],
      providers: [
        PricingService,
        { provide: MessagesStoreService },
        { provide: UserStoreService, useValue: userData },
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OrderShippingPaymentDetailsComponent);
    component = fixture.componentInstance;
    component.shippingAddress = orderShippingPaymentData.deliveryAddress;
    component.billToAddress = orderShippingPaymentData.billTo;
    component.paymentInfo = orderShippingPaymentData.paymentInfo;
    component.contactInfo = orderShippingPaymentData.contactInfo;
    component.messages = require('assets/mock/messages.json');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create medium', () => {
    component['userStore'].config.mediumDate = 'MMMM d yyyy';
    expect(component).toBeTruthy();
  });
  it('should call ngOnInit method', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });
});
