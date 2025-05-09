import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from '@angular/core/testing';
import { SelectComponent } from './select.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';
import { SharedService } from '@app/modules/shared/shared.service';
import { RouterTestingModule } from '@angular/router/testing';
import { UserStoreService } from '@app/state/user-store.service';
import { DecimalPipe, TitleCasePipe } from '@angular/common';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { SelectionOptionComponent } from './option/option.component';
import { PaymentSummaryComponent } from '../payment-summary/payment-summary.component';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { FormsModule } from '@angular/forms';
import { ModalsService } from '@app/components/modals/modals.service';
import { Router } from '@angular/router';
import { of, Subject } from 'rxjs';
import { CartService } from '@app/services/cart.service';
import { PaymentStoreService } from '@app/state/payment-store.service';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { AppConstants, STATE } from '@app/constants/app.constants';
import { TransitionService } from '@app/transition/transition.service';

describe('SelectComponent', () => {
  let component: SelectComponent;
  let fixture: ComponentFixture<SelectComponent>;
  let userStore: UserStoreService;
  let cartService: CartService;
  let stateService: PaymentStoreService;
  let tempState: object;
  const programData = require('assets/mock/program.json');
  const userMock = require('assets/mock/user.json');
  userMock.program = programData;
  const userData = {
    user: userMock,
    program: programData,
    config: programData['config']
  };
  const fakeResponse = { cashAmount: 100, earnPoints: 100 };
  const subject = new Subject();
  const params = { params: null, titleCaseParams: null };
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        SelectComponent,
        SelectionOptionComponent,
        PaymentSummaryComponent
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        FormsModule,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: SharedService, useValue: {
          updateRedemptionOption: () => {},
          isPointsFixed: () => {},
          isCashOnlyRedemption: () => {},
          triggerLogoutSyncEvent$: subject.asObservable(),
          getTranslateParams: () => params
        }
        },
        { provide: ModalsService, useValue: {
          openConsentFormComponent: () => {} }
        },
        { provide: Router, useValue:
          { navigate: jasmine.createSpy('navigate') }
        },
        TransitionService,
        TitleCasePipe,
        DecimalPipe,
        CurrencyPipe,
        OrderByPipe,
        CurrencyFormatPipe
      ]
    })
    .compileComponents();
    userStore = TestBed.inject(UserStoreService);
    cartService = TestBed.inject(CartService);
    stateService = TestBed.inject(PaymentStoreService);
    userStore.addUser(userData.user);
    userStore.addProgram(userData.program);
    userStore.addConfig(userData.config);
    spyOn(cartService, 'addPurchasePoints').and.returnValue(of(fakeResponse));
    spyOn(stateService, 'set').and.callFake(() => {});
    tempState = stateService.getInitial();
    tempState['payments'] = {
      pointsonly: {
        name: 'pointsonly',
        value: 'Points Only',
        isDisabled: false,
        optionHeading: 'payment-pointsonly',
        optionDescription: 'payment-pointsonly-desc', // add a low balance case
        paySummarySubtitle: 'pointsonly-paymentSummarySubtitle',
        paySummaryTemplate: 'formula-view',
        checked: false,
        actionPanel: {
          nextStepBtnLabel: 'nextStepReviewYourOrder',
          nextStep: ['/store', 'checkout']
        }
      },
      cashonly: {
        name: 'cashonly',
        value: 'Pay by Card',
        isDisabled: false,
        optionHeading: 'payment-cashonly',
        optionDescription: 'payment-cashonly-desc',
        paySummarySubtitle: 'cashonly-paymentSummarySubtitle',
        paySummaryTemplate: 'amount-due',
        checked: false,
        actionPanel: {
          nextStepBtnLabel: 'nextStepCardPayment',
          nextStep: ['/store', 'payment', 'card']
        }
      },
      splitpay: {
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
        }
      }
    };
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectComponent);
    component = fixture.componentInstance;
    component.state = stateService.getInitial();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should trigger ngOnInit', waitForAsync( () => {
    component.user['program']['config']['payFrequency'] = 'test';
    fixture.detectChanges();
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();

    component.user['program']['config']['payFrequency'] = '';
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  }));

  it('should call toggleOrderSummary method', () => {
    const result = component.displayOrderSummOnMobile;
    component.toggleOrderSummary('');
    expect(component.displayOrderSummOnMobile).toEqual(!result);
  });

  it('should call routeChange method for cashonly', () => {
    component.state.selections = {
      payment: {
        name: 'cashonly',
        value: 'Pay by Card',
        isDisabled: false,
        optionHeading: 'payment-cashonly',
        optionDescription: 'payment-cashonly-desc',
        paySummarySubtitle: 'cashonly-paymentSummarySubtitle',
        paySummaryTemplate: 'amount-due',
        checked: false,
        actionPanel: {
          nextStepBtnLabel: 'nextStepCardPayment',
          nextStep: ['/store', 'payment', 'card']
        }
      }
    };
    component.state.cart = {
      cartTotals: {
        points: 445600,
        amount: 2405.36
      }
    };
    component.config.consentForm = true;
    spyOn(component, 'routeChange').and.callThrough();
    component.routeChange();
    expect(component.routeChange).toHaveBeenCalled();
  });

  it('should call routeChange method for pointsonly', () => {
    component.state.selections = {
      payment: {
        name: 'pointsonly',
        value: 'Points Only',
        isDisabled: false,
        optionHeading: 'payment-pointsonly',
        optionDescription: 'payment-pointsonly-desc', // add a low balance case
        paySummarySubtitle: 'pointsonly-paymentSummarySubtitle',
        paySummaryTemplate: 'formula-view',
        checked: false,
        actionPanel: {
          nextStepBtnLabel: 'nextStepReviewYourOrder',
          nextStep: ['/store', 'checkout']
        }
      }
    };
    component.config.consentForm = false;
    spyOn(component, 'routeChange').and.callThrough();
    component.routeChange();
    expect(component.routeChange).toHaveBeenCalled();
  });

  it('should call routeChange method for pointsfixed', () => {
    component.state.selections = {
      payment: {
        name: 'pointsfixed',
        value: 'Points Fixed',
        isDisabled: false,
        optionHeading: 'payment-pointsonly',
        optionDescription: 'payment-pointsonly-desc', // add a low balance case
        paySummarySubtitle: 'pointsfixed-paymentSummarySubtitle',
        paySummaryTemplate: 'pointsfixed',
        checked: false,
        actionPanel: {
          nextStepBtnLabel: 'nextStepReviewYourOrder',
          nextStep: ['/store', 'payment', 'card']
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
            isPaymentRequired: true,
            isValid: true
          }
        }
      }
    };
    component.config.consentForm = false;
    spyOn(component, 'routeChange').and.callThrough();
    component.routeChange();
    expect(component.routeChange).toHaveBeenCalled();
  });

  it('should get cart response', waitForAsync(() => {
    spyOn(stateService, 'get').and.returnValue(of(tempState));
    spyOn(component, 'getCart').and.callThrough();
    const data = require('assets/mock/cart.json');
    spyOn(cartService, 'getCart').and.returnValue(of(data));
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  }));

  it('should get cart response - points fixed payment type', waitForAsync(() => {
    spyOn(component, 'getCart').and.callThrough();
    const mockResponse = require('assets/mock/cart.json');
    mockResponse.shippingAddress.address2 = 'Ste 450';
    mockResponse.shippingAddress.address3 = 'street';
    mockResponse.shippingAddress.businessName = 'For Testing Module';
    spyOn(cartService, 'getCart').and.returnValue(of(mockResponse));
    spyOn(stateService, 'get').and.returnValue(of(tempState));
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  }));

  it('should get cart response - else check', () => {
    const stateData = STATE;
    spyOn(stateService, 'get').and.returnValue(of(stateData));
    spyOn(component, 'getCart').and.callThrough();
    const data = require('assets/mock/cart-with-noSufficientPoints.json');
    spyOn(cartService, 'getCart').and.returnValue(of(data));
    component.getCart();
    expect(component.getCart).toHaveBeenCalled();
  });

  it('should call setStates method with updated paymentTemplate points_fixed', () => {
    spyOn(component, 'setStates').and.callThrough();
    const cartResponse = require('assets/mock/cart-with-noSufficientPoints.json');
    const newState = component.state;
    newState['paymentInfo'] = {
      paymentType: '',
      invalidCartTotal: false,
      paymentTemplate: AppConstants.paymentTemplate.points_fixed,
      pointsSplitUnavailable: false
    };
    newState['payments'] = {
      splitpay: {
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
          }
        }
      }
    };
    newState['redemptionPaymentLimit'] = cartResponse.redemptionPaymentLimit;
    newState.cart = cartResponse;
    component.setStates(newState);
    expect(component.setStates).toHaveBeenCalled();
  });

  it('should call setStates method with updated paymentType cash_only', () => {
    spyOn(component, 'setStates').and.callThrough();
    const cartResponse = require('assets/mock/cart-with-noSufficientPoints.json');
    const newState = component.state;
    newState['paymentInfo'] = {
      paymentType: AppConstants.paymentType.cash_only,
      invalidCartTotal: false,
      paymentTemplate: '',
      pointsSplitUnavailable: false
    };
    newState['payments'] = {
      cashonly: {
        name: 'cashonly',
        value: 'Pay by Card',
        isDisabled: false,
        optionHeading: 'payment-cashonly',
        optionDescription: 'payment-cashonly-desc',
        paySummarySubtitle: 'cashonly-paymentSummarySubtitle',
        paySummaryTemplate: 'amount-due',
        checked: false,
        actionPanel: {
          nextStepBtnLabel: 'nextStepCardPayment',
          nextStep: ['/store', 'payment', 'card']
        }
      }
    };
    newState['redemptionPaymentLimit'] = cartResponse.redemptionPaymentLimit;
    newState.cart = cartResponse;
    component.setStates(newState);
    expect(component.setStates).toHaveBeenCalled();
  });

  it('should call setStates method with points fixed', fakeAsync(() => {
    component.state = {
      redemptionPaymentLimit: {
        cashMaxLimit: {
          amount: 9999
        }
      },
      cart: {
        cost: 7777
      }
    };
    tick(200);
    spyOn(component, 'setStates').withArgs(component.state).and.callThrough();
    component.setStates(component.state);
    expect(component.setStates).toHaveBeenCalled();
  }));

});
