import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AddressFormTwComponent } from './address-form-tw.component';
import { TranslateModule } from '@ngx-translate/core';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { DataMaskingModule } from '@bakkt/data-masking';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

describe('AddressFormTwComponent', () => {
  let component: AddressFormTwComponent;
  let fixture: ComponentFixture<AddressFormTwComponent>;
  const formBuilder: FormBuilder = new FormBuilder();
  const shipAddr = formBuilder.group({
    name: '',
    businessName :  '' ,
    address1 :  '' ,
    address2 :  '' ,
    address3 :  'street' ,
    subCity : null,
    city :  '' ,
    state :  '' ,
    zip5 :  '' ,
    zip4 :  '' ,
    country :  '' ,
    phoneNumber :  '' ,
    faxNumber :  '' ,
    email :  '' ,
    errorMessage : {},
    warningMessage : {},
    ignoreSuggestedAddress :  false ,
    cartTotalModified : false,
    selectedAddressId : 0,
    addressModified : null,
    validAddress : true
  });
  const userData = require('assets/mock/user.json');
  const cartData = require('assets/mock/cart.json');

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ AddressFormTwComponent ],
      imports: [
        TranslateModule.forRoot(),
        FormsModule,
        ReactiveFormsModule,
        DataMaskingModule,
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        { provide: HttpClient },
        { provide: Router, useValue: {
            navigate: jasmine.createSpy('navigate') }
        },
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AddressFormTwComponent);
    component = fixture.componentInstance;
    component.errorMessage = {};
    component.config = {};
    component.user = userData;
    component.shipAddress = cartData.shippingAddress;
    component.changeShipAddress = shipAddr;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('should call multiAddressSelect', () => {
    spyOn(component, 'multiAddressSelect').and.callThrough();
    component.multiAddressSelect(component.user.addresses[0].addressId);
    expect(component.multiAddressSelect).toHaveBeenCalled();
  });
});
