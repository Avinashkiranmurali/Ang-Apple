import { ComponentFixture, fakeAsync, TestBed, tick, waitForAsync } from '@angular/core/testing';
import { SplitComponent } from './split.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { DecimalPipe, TitleCasePipe } from '@angular/common';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { EventEmitter, Injectable } from '@angular/core';
import { Observable, of, Subject, throwError } from 'rxjs';
import { Program } from '@app/models/program';
import { Cart } from '@app/models/cart';
import { UserStoreService } from '@app/state/user-store.service';
import { FormsModule } from '@angular/forms';
import { PaymentStoreService } from '@app/state/payment-store.service';
import { PaymentSummaryComponent } from '../payment-summary/payment-summary.component';
import { SplitOptionComponent } from './option/option.component';
import { Router } from '@angular/router';
import { PaymentService } from '@app/services/payment.service';
import { PurchasePointsResponse } from '@app/models/pricing-model';
import { DisplayFormatDirective } from '@app/modules/payment/split/display-format.directive';
import { OrderSummaryComponent } from '../order-summary/order-summary.component';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { AppConstants, STATE } from '@app/constants/app.constants';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';

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

describe('SplitComponent', () => {
  let component: SplitComponent;
  let fixture: ComponentFixture<SplitComponent>;
  let paymentStoreService: PaymentStoreService;
  let httpTestingController: HttpTestingController;
  let paymentService: PaymentService;
  let newState = null;
  const mockInputStream = new Subject<number>();
  const cartData: Cart = require('assets/mock/cart.json');
  const programData: Program = require('assets/mock/program.json');
  const userMock = require('assets/mock/user.json');
  userMock['program'] = programData;
  const userData = {
    user: userMock,
    program: programData,
    config: programData['config'],
    get: () => {}
  };
  let router: Router;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        SplitComponent,
        PaymentSummaryComponent,
        SplitOptionComponent,
        OrderSummaryComponent,
        DisplayFormatDirective,
        CurrencyFormatPipe
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot(),
        FormsModule
      ],
      providers: [
        { provide: TranslateService, useClass: TranslateServiceStub },
        { provide: UserStoreService, useValue: userData },
        TitleCasePipe,
        DecimalPipe,
        CurrencyPipe,
        DisplayFormatDirective,
        CurrencyFormatPipe,
        { provide: EnsightenService, useValue: {
          broadcastEvent: () => {}
        }}
      ]
    })
    .compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
    paymentStoreService = TestBed.inject(PaymentStoreService);
    paymentService = TestBed.inject(PaymentService);
    router = TestBed.inject(Router);
    router.navigate = jasmine.createSpy('navigate');
    // Create state object
    newState = STATE;
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
    newState['selections']['payment']['splitPayOption'] = {
      cashToUse: 0,
      pointsToUse: 222800,
      subView: {
        name: 'useCustomPoints'
      }
    };
    newState['cart'] = cartData;
    newState['cart']['cartTotals'] = {
      points: 445600,
      amount: 2405.36
    };
    newState['paymentInfo'] = {
      paymentType: AppConstants.paymentType.points_only
    };
    newState['redemptionPaymentLimit'] = cartData.redemptionPaymentLimit;
    paymentStoreService.set(newState);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SplitComponent);
    component = fixture.componentInstance;
    component.state = newState;
    component.pointsToUse = 222800;
    component.isUnbundled = false;
    component.inputStream$ = component['paymentService'].getPurchasePointsObservable();
    mockInputStream.next(1);
    fixture.detectChanges();
  });

  afterAll(() => {
    component.userStore.program.formatPointName = 'delta.points';
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create formatPointName', () => {
    component['userStore'].program.formatPointName = '';
    expect(component).toBeTruthy();
  });

  it('should create instance if cashAmount is zero', () => {
    spyOn(paymentService, 'getPurchasePointsObservable').and.returnValue(mockInputStream);
    newState['cart']['cartTotals']['points'] = 445601;
    paymentStoreService.set(newState);
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should create instance if cashAmount is not equal to zero', () => {
    spyOn(paymentService, 'getPurchasePointsObservable').and.returnValue(mockInputStream);
    newState['cart']['cartTotals']['points'] = 445600;
    paymentStoreService.set(newState);
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should create instance with payFrequency', () => {
    component.user['program']['config']['payFrequency'] = 'TEST';
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.payFrequency).toEqual('TEST');
  });

  it('should create instance without payFrequency value', () => {
    component.user['program']['config']['payFrequency'] = '';
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.payFrequency).toEqual('');
  });

  it('should call toggleOrderSummary', () => {
    component.displayOrderSummOnMobile = false;
    fixture.detectChanges();
    component.toggleOrderSummary(null);
    expect(component.displayOrderSummOnMobile).toBeTruthy();
  });

  it('should call splitPointsChanged method', () => {
    component.pointsToUseField.control.setValue(222800);
    fixture.detectChanges();
    spyOn(component, 'splitPointsChanged').and.callThrough();
    component.splitPointsChanged(222800);
    expect(component.splitPointsChanged).toHaveBeenCalled();
  });

  it('should call splitPointsChanged method - error check', () => {
    component.pointsToUse = 2000;
    component.pointsToUseField.control.setValue(2200);
    component.pointsToUseField.control.setErrors({invalid: true});
    fixture.detectChanges();
    spyOn(component, 'splitPointsChanged').and.callThrough();
    component.splitPointsChanged(2200);
    expect(component.splitPointsChanged).toHaveBeenCalled();
  });

  it('should call submitPaymentDetails method - cashAmount is 100', () => {
    const response: PurchasePointsResponse = {
      cashAmount: 100,
      earnPoints: 20
    };
    spyOn(component['cartService'], 'addPurchasePoints').and.returnValue(of(response));
    spyOn(component, 'submitPaymentDetails').and.callThrough();
    component.submitPaymentDetails();
    expect(component.submitPaymentDetails).toHaveBeenCalled();
  });

  it('should call submitPaymentDetails method - cashAmount is 0', () => {
    const response: PurchasePointsResponse = {
      cashAmount: 0,
      earnPoints: 0
    };
    spyOn(component['cartService'], 'addPurchasePoints').and.returnValue(of(response));
    spyOn(component, 'submitPaymentDetails').and.callThrough();
    component.submitPaymentDetails();
    expect(component.submitPaymentDetails).toHaveBeenCalled();
  });

  it('should call submitPaymentDetails method - error response', waitForAsync(() => {
    spyOn(component, 'submitPaymentDetails').and.callThrough();
    const errorResponse = { status: 404, statusText: 'Not Found' };
    spyOn(component['cartService'], 'addPurchasePoints').and.returnValue(throwError(errorResponse));
    component.submitPaymentDetails();
    expect(component.submitPaymentDetails).toHaveBeenCalled();
  }));

  it('should call backToPaymentPage method', () => {
    spyOn(paymentStoreService, 'set').and.callFake(() => {});
    spyOn(component, 'backToPaymentPage').and.callThrough();
    component.backToPaymentPage();
    expect(component.backToPaymentPage).toHaveBeenCalled();
  });

  it('should call nextBtnMsg method - if selections is null', fakeAsync(() => {
    component.state.selections = null;
    tick(500);
    spyOn(component, 'nextBtnMsg').and.callThrough();
    component.nextBtnMsg();
    expect(component.nextBtnMsg).toHaveBeenCalled();
  }));

  it('should call nextBtnMsg method - payment.splitPayOption is undefined', fakeAsync(() => {
    component.state.selections.payment.splitPayOption = undefined;
    tick(500);
    spyOn(component, 'nextBtnMsg').and.callThrough();
    component.nextBtnMsg();
    expect(component.nextBtnMsg).toHaveBeenCalled();
  }));

  it('should call nextBtnMsg method - payment.splitPayOption with btnMsg', fakeAsync(() => {
    component.state.selections.payment.splitPayOption['nextStepBtnLabel'] = 'nextStepPayment';
    tick(500);
    spyOn(component, 'nextBtnMsg').and.callThrough();
    component.nextBtnMsg();
    expect(component.nextBtnMsg).toHaveBeenCalled();
  }));

  it('should call nextBtnMsg method - payment.splitPayOption is null', fakeAsync(() => {
    component.state.selections.payment.splitPayOption = null;
    tick(500);
    spyOn(component, 'nextBtnMsg').and.callThrough();
    component.nextBtnMsg();
    expect(component.nextBtnMsg).toHaveBeenCalled();
  }));

  it('should call nextBtnMsg method - splitPaymentsForm is valid', fakeAsync(() => {
    component.state.selections.payment.splitPayOption['isValid'] = false;
    tick(500);
    spyOn(component, 'nextBtnMsg').and.callThrough();
    component.nextBtnMsg();
    expect(component.nextBtnMsg).toHaveBeenCalled();
  }));

  it('should call nextBtnMsg method - splitPaymentsForm is invalid', fakeAsync(() => {
    component.state.selections.payment.splitPayOption['isValid'] = false;
    component.splitPaymentsForm.control.setErrors({invalid: true});
    tick(500);
    spyOn(component, 'nextBtnMsg').and.callThrough();
    component.nextBtnMsg();
    expect(component.nextBtnMsg).toHaveBeenCalled();
  }));

  it('should get state object with no cart data', () => {
    const state = paymentStoreService.getInitial();
    state.cart = null;
    paymentStoreService.set(state);
  });

  it('should get state object with no cartTotal data exist', () => {
    const state = paymentStoreService.getInitial();
    state.cart['cartTotal'] = null;
    paymentStoreService.set(state);
  });

  it('should get state object with no redemptionPaymentLimit data exist', () => {
    const state = paymentStoreService.getInitial();
    state.cart['redemptionPaymentLimit'] = {
      useMinPoints: null,
      useMaxPoints: null
    };
    paymentStoreService.set(state);
  });

  it('should get state object with pointsToUse is null', () => {
    const state = component.state;
    state.cart = null;
    state.selections.payment.splitPayOption.subView.name = 'useMaxPoints';
    fixture.detectChanges();
    paymentStoreService.set(state);
  });

});
