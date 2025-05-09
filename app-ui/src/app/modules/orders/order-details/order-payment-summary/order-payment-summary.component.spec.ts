import { DecimalPipe } from '@angular/common';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { EventEmitter, Injectable } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { CartTotalTempComponent } from '@app/modules/pricing/cart-total-temp/cart-total-temp.component';
import { PricingTempComponent } from '@app/modules/pricing/pricing-temp/pricing-temp.component';
import { AppConstants } from '@app/constants/app.constants';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { OrderPaymentSummaryComponent } from './order-payment-summary.component';

@Injectable()
export class TranslateServiceStub {
  public onLangChange: EventEmitter<any> = new EventEmitter();
  public onTranslationChange: EventEmitter<any> = new EventEmitter();
  public onDefaultLangChange: EventEmitter<any> = new EventEmitter();

  public get<T>(key: T): Observable<T> {
    return of(key);
  }
  public instant(key: string): any {
    return '';
  }
}

describe('OrderPaymentSummaryComponent', () => {
  let component: OrderPaymentSummaryComponent;
  let fixture: ComponentFixture<OrderPaymentSummaryComponent>;
  const programData = require('assets/mock/program.json');
  const userMock = require('assets/mock/user.json');
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
        OrderPaymentSummaryComponent,
        PricingTempComponent,
        CartTotalTempComponent,
        CurrencyFormatPipe,
        CurrencyPipe
      ],
      imports: [
        TranslateModule.forRoot(),
        RouterTestingModule,
        HttpClientTestingModule,
        BrowserAnimationsModule
      ],
      providers: [
        { provide: UserStoreService, useValue: userData },
        { provide: CurrencyFormatPipe },
        { provide: TranslateService, useClass: TranslateServiceStub },
        CurrencyPipe,
        CurrencyFormatPipe,
        DecimalPipe
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OrderPaymentSummaryComponent);
    component = fixture.componentInstance;
    component.user = userData.user;
    component.program = userData.program;
    component.config = userData.config;
    component.messages = require('assets/mock/messages.json');
    component.orderDetails = require('assets/mock/order-history-details.json');
    fixture.detectChanges();
  });

  it('should create', () => {
    component.program.formatPointName = '';
    expect(component).toBeTruthy();
  });

  it('should create - with formatPointName', () => {
    component.program.formatPointName = 'delta.points';
    expect(component).toBeTruthy();
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

  it('should call showAdditionalPayment method', () => {
    spyOn(component['sharedService'], 'isPointsFixed').and.callFake(() => true);
    spyOn(component, 'showAdditionalPayment').and.callThrough();
    expect(component.showAdditionalPayment()).toBeFalsy();
    expect(component.showAdditionalPayment).toHaveBeenCalled();
  });

});
