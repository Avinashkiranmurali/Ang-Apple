import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ControlContainer, FormControl, FormGroup, FormGroupDirective, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { CartService } from '@app/services/cart.service';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateModule } from '@ngx-translate/core';
import { BillingAddressComponent } from './billing-address.component';
import { DataMaskingModule } from '@bakkt/data-masking';
import { User } from '@app/models/user';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

describe('BillingAddressComponent', () => {
  let component: BillingAddressComponent;
  let fixture: ComponentFixture<BillingAddressComponent>;
  let httpTestingController: HttpTestingController;
  let cartService: CartService;
  const programData = require('assets/mock/program.json');
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };
  const fg: FormGroup = new FormGroup({
    useSameShippingAddress: new FormControl(true)
  });
  const fgd: FormGroupDirective = new FormGroupDirective([], []);
  fgd.form = fg;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        BillingAddressComponent
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot(),
        DataMaskingModule,
        FormsModule,
        ReactiveFormsModule
      ],
      providers: [
        { provide: ControlContainer, useValue: fgd },
        { provide: UserStoreService, useValue: userData },
        { provide: CurrencyFormatPipe },
        { provide: DecimalPipe },
        { provide: CurrencyPipe }
      ]
    })
    .compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
    cartService = TestBed.inject(CartService);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BillingAddressComponent);
    component = fixture.componentInstance;
    const cartData = require('assets/mock/cart.json');
    component.shippingAddress = cartData['shippingAddress'];
    const dataMock = {
      cart: cartData
    };
    component.state = dataMock;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create instance for locale zh_tw', waitForAsync(() => {
    spyOn(component, 'ngOnInit').and.callThrough();
    const mockUserForTWLocale = {
      programid: 'b2s_qa_only',
      userid: 'eric',
      varid: 'TW',
      additionalInfo: {
        countryCode: 'TW',
        languageCode: 'ZHO'
      },
      fullName: 'Eric Theall',
      varId: 'TW',
      userId: 'eric',
      balance: 999999999,
      programId: 'b2s_qa_only',
      locale: 'zh_TW',
      country: 'TW'
    };
    component.user = mockUserForTWLocale as unknown as User;
    fixture.detectChanges();
    component.ngOnInit();
    const mockStateResponse = require('assets/mock/getStates.json');
    // Expect a call to this URL
    const req = httpTestingController.expectOne(cartService.baseUrl + 'address/getStates');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    // Respond with the fake data when called
    req.flush(mockStateResponse);
    const mockCitiesResponse = require('assets/mock/getCities.json');
    // Expect a call to this URL
    const cityreq = httpTestingController.expectOne(cartService.baseUrl + '/address/cities');
    // Assert that the request is a GET
    expect(cityreq.request.method).toEqual('GET');
    // Respond with the fake data when called
    cityreq.flush(mockCitiesResponse);
    expect(component.ngOnInit).toHaveBeenCalled();
  }));

  it('should create instance for locale ru_ru', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    const mockUserForRULocale = {
      programid: 'b2s_qa_only',
      userid: 'eric',
      varid: 'RU',
      additionalInfo: {
        countryCode: 'RU',
        languageCode: 'RU'
      },
      fullName: 'Eric Theall',
      varId: 'RU',
      userId: 'eric',
      balance: 999999999,
      programId: 'b2s_qa_only',
      locale: 'ru_ru',
      country: 'RU'
    };
    component.user = mockUserForRULocale as unknown as User;
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
    expect(component.isPostalCode).toBeTruthy();
  });
});
