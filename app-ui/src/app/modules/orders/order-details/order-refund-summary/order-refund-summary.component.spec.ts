import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { OrderRefundSummaryComponent } from './order-refund-summary.component';
import { TransitionService } from '@app/transition/transition.service';
import { RouterTestingModule } from '@angular/router/testing';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateModule } from '@ngx-translate/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { DecimalPipe } from '@angular/common';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

describe('OrderRefundSummaryComponent', () => {
  let component: OrderRefundSummaryComponent;
  let fixture: ComponentFixture<OrderRefundSummaryComponent>;
  const programData = require('assets/mock/program.json');
  const mockUser = require('assets/mock/user.json');
  const userData = {
    user: mockUser,
    program: programData,
    config: programData['config']
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OrderRefundSummaryComponent, OrderByPipe, CurrencyFormatPipe ],
      imports: [
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        RouterTestingModule,
      ],
      providers: [
        { provide: MessagesStoreService },
        { provide: UserStoreService, useValue: userData },
        { provide: CurrencyFormatPipe },
        { provide: DecimalPipe },
        { provide: CurrencyPipe }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OrderRefundSummaryComponent);
    component = fixture.componentInstance;
    component.refundSummary = {
      lineItems: [
        {
          productName: 'AirTag',
          itemPrice: {
            amount: 26,
            currencyCode: 'USD',
            points: 5400
          },
          taxPrice: {
            amount: 203,
            currencyCode: '',
            points: 800
          },
          feesPrice: {
            amount: 0,
            currencyCode: '',
            points: 0
          }
        }
      ],
      subTotal: {
        amount: 26,
        currencyCode: '',
        points: 5400
      },
      taxesAndFees: {
        amount: 203,
        currencyCode: '',
        points: 800
      },
      total: {
        amount: 229,
        currencyCode: '',
        points: 6200
      },
      refunds: {
        amount: 0,
        currencyCode: 'USD',
        points: 5800
      }
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create formatPointName', () => {
    component['userStore'].program.formatPointName = '';
    expect(component).toBeTruthy();
  });

  it('should create formatPointName with value', () => {
    component.program.formatPointName = 'delta.points';
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.pointLabel).toBeDefined();
    expect(component).toBeTruthy();
  });

  it('should create formatPointName with empty string', () => {
    component.program.formatPointName = '';
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.pointLabel).toBeDefined();
    expect(component).toBeTruthy();
  });

});
