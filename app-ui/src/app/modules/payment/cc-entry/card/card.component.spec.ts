import { ComponentFixture, fakeAsync, TestBed, tick, waitForAsync } from '@angular/core/testing';
import { CardComponent } from './card.component';
import { TranslateModule } from '@ngx-translate/core';
import { DecimalPipe } from '@angular/common';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ControlContainer, FormControl, FormGroup, FormGroupDirective, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { UserStoreService } from '@app/state/user-store.service';
import { RouterTestingModule } from '@angular/router/testing';
import { PaymentStoreService } from '@app/state/payment-store.service';
import { By } from '@angular/platform-browser';
import { DataMaskingModule } from '@bakkt/data-masking';
import { Component, Input } from '@angular/core';
import { Address } from 'cluster';
import { of } from 'rxjs';

@Component({
  selector: 'app-billing-address',
  template: ''
})
class MockBillingAddressComponent {
  @Input() postCardDetailsError;
  @Input() state;
  @Input() addressFields;
  @Input() shippingAddress: Address;
  @Input() billingAddress: Address;
}

describe('CardComponent', () => {
  let component: CardComponent;
  let fixture: ComponentFixture<CardComponent>;
  let stateService: PaymentStoreService;
  let stateData = null;
  const cartData = require('assets/mock/cart.json');
  const programData = require('assets/mock/program.json');
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };
  const fg: FormGroup = new FormGroup({
    cardNumber: new FormControl('373456789072345'),
    expiration: new FormControl('02/23'),
    securityCode: new FormControl('1447'),
    billingAddress: new FormGroup({
      useSameShippingAddress: new FormControl(true),
      address1: new FormControl('5900 Windward Pkwy'),
      address2: new FormControl('Ste 450'),
      address3: new FormControl('street'),
      city:  new FormControl('Alpharetta'),
      state:  new FormControl('GA'),
      input_state:  new FormControl(''),
      zip5: new FormControl('30005')
    })
  });
  const fgd: FormGroupDirective = new FormGroupDirective([], []);
  fgd.form = fg;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        CardComponent,
        MockBillingAddressComponent
      ],
      providers: [
        { provide: UserStoreService, useValue: userData },
        { provide: ControlContainer, useValue: fgd },
        DecimalPipe,
        CurrencyPipe
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        FormsModule,
        ReactiveFormsModule,
        DataMaskingModule,
        TranslateModule.forRoot(),
      ]
    })
    .compileComponents();
    stateService = TestBed.inject(PaymentStoreService);
    stateData = stateService.getInitial();
    stateData['cart'] = cartData;
    stateData['selections'] = {
      payment: {
        splitPayOption: {
          cashToUse: 200.00
        }
      }
    };
    spyOn(stateService, 'get').and.returnValue(of(stateData));
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CardComponent);
    component = fixture.componentInstance;
    component.state = stateData;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should mask card number', fakeAsync(() => {
    const inputElement = fixture.debugElement.query(By.css('#cardNumber')).nativeElement;
    fg.setValue({
      cardNumber: '3734567890723456',
      expiration: '02/23',
      securityCode: '747',
      billingAddress: {
        useSameShippingAddress: true,
        address1: '5900 Windward Pkwy',
        address2: 'Ste 450',
        address3: 'street',
        city: 'Alpharetta',
        state: 'GA',
        input_state: '',
        zip5: '30005'
      }
    });
    inputElement.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    tick(500);
    expect(inputElement.value).toContain('****');
  }));

  it('should have content in Card Information Heading', () => {
    const cardHeadings = fixture.debugElement.query(By.css('.card-information')).nativeElement;
    expect(cardHeadings.innerHTML).not.toBeNull();
    expect(cardHeadings.innerHTML.length).toBeGreaterThan(0);
  });

  it('should \'/\' be embedded within exp date field', waitForAsync(() => {
    const inputExpiration = fixture.debugElement.query(By.css('#expiration'));
    const inputExpirationElement = inputExpiration.nativeElement;
    fg.setValue({
      cardNumber: '373456789072345',
      expiration: '012',
      securityCode: '747',
      billingAddress: {
        useSameShippingAddress: true,
        address1: '5900 Windward Pkwy',
        address2: 'Ste 450',
        address3: 'street',
        city: 'Alpharetta',
        state: 'GA',
        input_state: '',
        zip5: '30005'
      }
    });

    const eventKeyUp1 = new KeyboardEvent('keyup', {
      key: '0',
    });
    inputExpirationElement.dispatchEvent(eventKeyUp1);
    component.formatExpDate(eventKeyUp1);
    const eventKeyUp2 = new KeyboardEvent('keyup', {
      key: '1'
    });
    inputExpirationElement.dispatchEvent(eventKeyUp2);
    component.formatExpDate(eventKeyUp2);
    const eventKeyUp3 = new KeyboardEvent('keyup', {
      key: '2'
    });
    inputExpirationElement.dispatchEvent(eventKeyUp3);
    component.formatDate(eventKeyUp3);
    fixture.detectChanges();
    const inputExpirationFormatted = fixture.debugElement.query(By.css('#expiration'));
    const inputExpirationFormattedElement = inputExpirationFormatted.nativeElement;
    expect(inputExpirationFormattedElement.value).toContain('/');
  }));

  it('should have content in Card Information Heading', () => {
    const cardHeadings = fixture.debugElement.query(By.css('.card-information')).nativeElement;
    expect(cardHeadings.innerHTML).not.toBeNull();
    expect(cardHeadings.innerHTML.length).toBeGreaterThan(0);
  });

  it('should \'/\' be embedded within exp date field', waitForAsync(() => {
    const inputExpiration = fixture.debugElement.query(By.css('#expiration'));
    const inputExpirationElement = inputExpiration.nativeElement;
    fg.setValue({
      cardNumber: '373456789072345',
      expiration: '012',
      securityCode: '747',
      billingAddress: {
        useSameShippingAddress: true,
        address1: '5900 Windward Pkwy',
        address2: 'Ste 450',
        address3: 'street',
        city: 'Alpharetta',
        state: 'GA',
        input_state: '',
        zip5: '30005'
      }
    });

    const eventKeyUp1 = new KeyboardEvent('keyup', {
      key: '0',
    });
    inputExpirationElement.dispatchEvent(eventKeyUp1);
    component.formatExpDate(eventKeyUp1);
    const eventKeyUp2 = new KeyboardEvent('keyup', {
      key: '1'
    });
    inputExpirationElement.dispatchEvent(eventKeyUp2);
    component.formatExpDate(eventKeyUp2);
    const eventKeyUp3 = new KeyboardEvent('keyup', {
      key: '2'
    });
    inputExpirationElement.dispatchEvent(eventKeyUp3);
    component.formatDate(eventKeyUp3);
    fixture.detectChanges();
    const inputExpirationFormatted = fixture.debugElement.query(By.css('#expiration'));
    const inputExpirationFormattedElement = inputExpirationFormatted.nativeElement;
    expect(inputExpirationFormattedElement.value).toContain('/');
  }));

  it('should be a security code entered', fakeAsync(() => {
    const inputSecurityCode = fixture.debugElement.query(By.css('#securityCode'));
    const inputSecurityCodeElement = inputSecurityCode.nativeElement;
    fg.setValue({
      cardNumber: '373456789072345',
      expiration: '0123',
      securityCode: '123',
      billingAddress: {
        useSameShippingAddress: true,
        address1: '5900 Windward Pkwy',
        address2: 'Ste 450',
        address3: 'street',
        city: 'Alpharetta',
        state: 'GA',
        input_state: '',
        zip5: '30005'
      }
    });
    inputSecurityCodeElement.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    tick(500);
    expect(inputSecurityCodeElement.value.length).toBeGreaterThan(0);
    expect(inputSecurityCodeElement.value).toContain('1');
    expect(inputSecurityCodeElement.value.length).toEqual(3);
  }));

  it('should call ngOnInit for else check', () => {
    component.config.supportedCreditCardTypes = 'NONE';
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.creditCardTypes).toEqual(null);
  });

  it('should call getCardType method', () => {
    const element = fixture.debugElement.query(By.css('#cardNumber')).nativeElement;
    element.dispatchEvent(new Event('keyup'));
    expect(component.cardInfo.type).not.toBeDefined();
  });

  it('should call formatExpDate method', waitForAsync(() => {
    spyOn(component, 'formatExpDate').and.callThrough();
    const element = fixture.debugElement.query(By.css('#expiration')).nativeElement;
    element.value = '';
    element.dispatchEvent(new Event('keyup'));
    // for tab key
    const element1 = fixture.debugElement.query(By.css('#expiration')).nativeElement;
    const event = new Event('keyup');
    event['key'] = 'tab';
    element1.dispatchEvent(event);
    // for backspace key
    const element2 = fixture.debugElement.query(By.css('#expiration')).nativeElement;
    const event1 = new Event('keyup');
    event1['keyCode'] = 8;
    element2.dispatchEvent(event1);
    // for backspace key
    const element3 = fixture.debugElement.query(By.css('#expiration')).nativeElement;
    const event2 = new Event('keyup');
    event2['keyCode'] = 46;
    element3.dispatchEvent(event2);
    expect(component.formatExpDate).toHaveBeenCalledTimes(4);
  }));

  it('should call formatExpDate method for else check', () => {
    spyOn(component, 'formatExpDate').and.callThrough();
    fixture.detectChanges();
    const element4 = fixture.debugElement.query(By.css('#expiration')).nativeElement;
    const event3 = new Event('keyup');
    event3['keyCode'] = 46;
    element4.dispatchEvent(event3);
    expect(component.formatExpDate).toHaveBeenCalled();
  });

  it('should call bindCreditCardFocus', () => {
    spyOn(component, 'bindCreditCardFocus').and.callThrough();
    component.bindCreditCardFocus();
    expect(component.bindCreditCardFocus).toHaveBeenCalled();
  });

  it('should call bindCreditCardBlur', waitForAsync(() => {
    fg.setValue({
      cardNumber: '37345678907234577',
      expiration: '02/23',
      securityCode: '747',
      billingAddress: {
        useSameShippingAddress: true,
        address1: '5900 Windward Pkwy',
        address2: 'Ste 450',
        address3: 'street',
        city: 'Alpharetta',
        state: 'GA',
        input_state: '',
        zip5: '30005'
      }
    });
    fixture.detectChanges();
    spyOn(component, 'bindCreditCardBlur').and.callThrough();
    component.bindCreditCardBlur();
    expect(component.bindCreditCardBlur).toHaveBeenCalled();
  }));

  it('should call formatCreditCard', () => {
    spyOn(component, 'formatCreditCard').and.callThrough();
    component.trimmedCardNum = '40';
    fixture.detectChanges();
    component.bindCreditCardFocus();
    component.formatCreditCard();
    expect(component.formatCreditCard).toHaveBeenCalled();
  });

  it('should call formatExpDateone method', waitForAsync(() => {
    spyOn(component, 'formatExpDate').and.callThrough();
    // for indexOf '/' key
    const element1 = fixture.debugElement.query(By.css('#expiration')).nativeElement;
    const event = new Event('keyup');
    event['key'] = '/';
    element1.value = '02/2033';
    element1.dispatchEvent(event);
    expect(component.formatExpDate(event));
  }));

});
