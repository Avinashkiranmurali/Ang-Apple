import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TitleCasePipe, DecimalPipe } from '@angular/common';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { SplitOptionComponent } from './option.component';
import { PaymentStoreService } from '@app/state/payment-store.service';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { UserStoreService } from '@app/state/user-store.service';
import { Program } from '@app/models/program';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { Cart } from '@app/models/cart';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';

describe('OptionComponent', () => {
  let component: SplitOptionComponent;
  let fixture: ComponentFixture<SplitOptionComponent>;
  let userStoreService: UserStoreService;
  let paymentStoreService: PaymentStoreService;
  let newState = null;
  const cartData: Cart = require('assets/mock/cart.json');
  const programData: Program = require('assets/mock/program.json');
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ SplitOptionComponent ],
      imports: [
        TranslateModule.forRoot(),
        RouterTestingModule,
        HttpClientTestingModule,
        FormsModule,
        ReactiveFormsModule
      ],
      providers: [
        TitleCasePipe,
        CurrencyPipe,
        OrderByPipe,
        PricingService,
        DecimalPipe,
        CurrencyFormatPipe
      ]
    })
    .compileComponents();
    paymentStoreService = TestBed.inject(PaymentStoreService);
    userStoreService = TestBed.inject(UserStoreService);
    userStoreService.addUser(userData.user);
    userStoreService.addProgram(userData.program);
    userStoreService.addConfig(userData.config);
    // Create state object
    newState = paymentStoreService.getInitial();
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
        splitPayOption: {
          cashToUse: 0
        }
      }
    };
    newState['cart'] = cartData;
    newState['redemptionPaymentLimit'] = cartData.redemptionPaymentLimit;
    spyOn(paymentStoreService, 'get').and.returnValue(of(newState));
    spyOn(paymentStoreService, 'set').and.callFake(() => {});
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SplitOptionComponent);
    component = fixture.componentInstance;
    component.state = newState;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // For Custom changeSplitPayOption - useMaxPoints
  it('should call changeSplitPayOption - if selection payment option is useMaxPoints', () => {
    component.state.redemptionPaymentLimit.useMaxPoints.amount = 100;
    if (component.state.selections && component.state.selections.payment) {
      component.state.selections.payment.splitPayOption = component.state.selections.payment.splitPayOptions['useMaxPoints'];
      component.changeSplitPayOption();
      expect(component.state.selections.payment.splitPayOption.nextStepBtnLabel).toEqual('nextStepPayment');

      // else check for branch coverage
      component.state.redemptionPaymentLimit.useMaxPoints.amount = 0;
      component.changeSplitPayOption();
      expect(component.state.selections.payment.splitPayOption.nextStepBtnLabel).toEqual('nextStepReviewYourOrder');
    }
  });

  // For Custom changeSplitPayOption - useMinPoints
  it('should call changeSplitPayOption - if selection payment option is useMinPoints', () => {
    if (component.state.selections && component.state.selections.payment) {
      component.state.selections.payment.splitPayOption = component.state.selections.payment.splitPayOptions['useMinPoints'];
      component.changeSplitPayOption();
      expect(component.state.selections.payment.splitPayOption.nextStepBtnLabel).toEqual('nextStepPayment');
    }
  });

  // For Custom changeSplitPayOption - useCustomPoints
  it('should call changeSplitPayOption - if selection payment option is useCustomPoints', () => {
    component.state.redemptionPaymentLimit.useMaxPoints.amount = 0;
    if (component.state.selections && component.state.selections.payment) {
      component.state.selections.payment.splitPayOption = component.state.selections.payment.splitPayOptions['useCustomPoints'];
      component.changeSplitPayOption();
      expect(component.state.selections.payment.splitPayOption.nextStepBtnLabel).toEqual('nextStepReviewYourOrder');

      // else check for branch coverage
      component.state.selections.payment.splitPayOption = component.state.selections.payment.splitPayOptions['useCustomPoints'];
      component.state.selections.payment.splitPayOption.name = 'pointsFixed';
      component.changeSplitPayOption();
      expect(component.state.selections.payment.splitPayOption.nextStepBtnLabel).toEqual('nextStepReviewYourOrder');
    }
  });

  // For Custom getsplitPayOptionsJson
  it('should call getsplitPayOptionsJson', () => {
    component.state = undefined;
    expect(component.getsplitPayOptionsJson()).toEqual([]);
  });

});
