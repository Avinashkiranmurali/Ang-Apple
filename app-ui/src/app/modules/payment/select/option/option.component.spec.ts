import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TitleCasePipe, DecimalPipe } from '@angular/common';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { SelectionOptionComponent } from './option.component';
import { TranslateService } from '@ngx-translate/core';
import { Injectable, EventEmitter } from '@angular/core';
import { of } from 'rxjs/internal/observable/of';
import { Observable, Subject } from 'rxjs';
import { PaymentStoreService } from '@app/state/payment-store.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { UserStoreService } from '@app/state/user-store.service';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { FormsModule } from '@angular/forms';

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

describe('OptionComponent', () => {
  let component: SelectionOptionComponent;
  let fixture: ComponentFixture<SelectionOptionComponent>;
  let stateService: PaymentStoreService;
  const cartData = require('assets/mock/cart-with-noSufficientPoints.json');
  const programData = require('assets/mock/program.json');
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };
  const subject = new Subject();
  const params = { params: null, titleCaseParams: null };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ SelectionOptionComponent ],
      imports: [ TranslateModule.forRoot(), RouterTestingModule, HttpClientTestingModule, FormsModule ],
      providers: [
        TitleCasePipe,
        CurrencyPipe,
        OrderByPipe,
        DecimalPipe,
        CurrencyFormatPipe,
        { provide: TranslateService, useClass: TranslateServiceStub },
        { provide: UserStoreService, useValue: userData },
        { provide: SharedService, useValue: {
          updateRedemptionOption: () => {},
          triggerLogoutSyncEvent$: subject.asObservable(),
          getTranslateParams: () => params
         }
        }
      ]
    })
    .compileComponents();
    stateService = TestBed.inject(PaymentStoreService);
    // Create state object
    const newState = stateService.getInitial();
    newState['paymentInfo'] = {
      pointsSplitUnavailable: true
    };
    newState['selections'] = {
      payment: {
        name: 'splitpay',
        value: 'Split Pay',
        isDisabled: false,
        optionHeading: 'payment-splitpay',
        optionDescription: 'payment-splitpay-desc',
        paySummarySubtitle: 'splitpay-paymentSummarySubtitle',
        paySummaryTemplate: '',
        checked: false,
        actionPanel: {
          nextStepBtnLabel: 'nextStepHowManyPoints',
          nextStep: ['/store', 'payment', 'split']
        },
        splitPayOptions: {
          useMaxPoints: {
            name: 'useMaxPoints',
            optionHeading: 'payment-useMaxPoints',
            optionDescription: 'payment-useMaxPoints-desc',
            optionDescriptionPayment: 'payment-useMaxPoints-payment-desc',
            paySummarySubtitle: 'splitpay-paymentSummarySubtitle',
            paySummaryTemplate: 'additional-amount',
            checked: false,
            nextStepBtnLabel: 'nextStepReviewYourOrder',
            isPaymentRequired: false,
            isValid: true
          },
          useMinPoints: {
            name: 'useMinPoints',
            optionHeading: 'payment-useMinPoints',
            optionDescription: 'payment-useMinPoints-desc',
            paySummarySubtitle: 'splitpay-paymentSummarySubtitle',
            paySummaryTemplate: 'additional-amount',
            checked: false,
            nextStepBtnLabel: 'nextStepPayment',
            isPaymentRequired: true,
            isValid: true
          },
          useCustomPoints: {
            name: 'useCustomPoints',
            optionHeading: 'payment-useCustomPoints',
            optionDescription: 'payment-useCustomPoints-desc',
            paySummarySubtitle: 'splitpay-paymentSummarySubtitle',
            paySummaryTemplate: 'additional-amount',
            checked: false,
            nextStepBtnLabel: 'nextStepReviewYourOrder',
            isPaymentRequired: true,
            isValid: true,
            subView: {
              name: 'useCustomPoints'
            }
          }
        },
        splitPayOption: {}
      }
    };
    newState['cart'] = cartData;
    newState['redemptionPaymentLimit'] = cartData.redemptionPaymentLimit;

    newState['redemptions'] = {
      pointsonly: { paymentOption: 'pointsonly' },
      splitpay: { paymentOption: 'splitpay' }
    };
    spyOn(stateService, 'get').and.returnValue(of(newState));
    spyOn(stateService, 'set').and.callFake(() => {});
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectionOptionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call selectionChanged method', () => {
    spyOn(component, 'selectionChanged').and.callThrough();
    component.selectionChanged();
    expect(component.selectionChanged).toHaveBeenCalled();
  });

  it('should call removeDefaultSelect method - else check', () => {
    component.state = null;
    spyOn(component, 'removeDefaultSelect').and.callThrough();
    component.removeDefaultSelect();
    expect(component.removeDefaultSelect).toHaveBeenCalled();
  });

});
