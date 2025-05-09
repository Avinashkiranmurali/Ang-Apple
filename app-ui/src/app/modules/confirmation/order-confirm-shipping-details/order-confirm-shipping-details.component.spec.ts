import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { OrderConfirmShippingDetailsComponent } from './order-confirm-shipping-details.component';
import { TemplateStoreService } from '@app/state/template-store.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

describe('OrderConfirmShippingDetailsComponent', () => {
  let component: OrderConfirmShippingDetailsComponent;
  let fixture: ComponentFixture<OrderConfirmShippingDetailsComponent>;
  let service: TemplateStoreService;
  const orderConfirmation = require('assets/mock/order-history-details.json');

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ OrderConfirmShippingDetailsComponent ],
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [
        { provide: TemplateStoreService },
        { provide: CurrencyFormatPipe },
        { provide: CurrencyPipe },
        { provide: DecimalPipe } ]
    })
    .compileComponents();
    service = TestBed.inject(TemplateStoreService);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OrderConfirmShippingDetailsComponent);
    component = fixture.componentInstance;
    const configData = require('assets/mock/configData.json');
    service.template = configData['configData'];
    fixture.detectChanges();
  });
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('displayPaymentAgreement to return false', () => {
    expect(component.displayPaymentAgreement()).toBeFalse();
  });
});
