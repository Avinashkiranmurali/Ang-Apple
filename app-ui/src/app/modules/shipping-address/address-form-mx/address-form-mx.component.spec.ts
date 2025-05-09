import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from '@angular/core/testing';
import { DecimalPipe, KeyValuePipe } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { AddressFormMxComponent } from './address-form-mx.component';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { DataMaskingModule } from '@bakkt/data-masking';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { HttpClient } from '@angular/common/http';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

describe('AddressFormMxComponent', () => {
  let component: AddressFormMxComponent;
  let fixture: ComponentFixture<AddressFormMxComponent>;
  const formBuilder: FormBuilder = new FormBuilder();
  const shipAddr = formBuilder.group({
    name: '',
    businessName :  '' ,
    address1 :  '' ,
    address2 :  '' ,
    address3 :  '' ,
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
      declarations: [ AddressFormMxComponent ],
      imports: [
        TranslateModule.forRoot(),
        FormsModule,
        ReactiveFormsModule,
        DataMaskingModule,
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [{provide: KeyValuePipe},
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
    fixture = TestBed.createComponent(AddressFormMxComponent);
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

    it('should call address3Field',  fakeAsync(() => {
      const changeShipAddress = {
        address3: 'test'
      };
      tick(100);
      fixture.detectChanges();
      spyOn(component.changeShipAddress, 'get').withArgs('address3').and.callThrough();
      component.changeShipAddress.get('address3');
      tick(100);
      component.address3Field.get(changeShipAddress.address3);
      expect(component.changeShipAddress.get).toBeDefined();
      expect(component.changeShipAddress.get).toHaveBeenCalled();
      component.address3Field.get(changeShipAddress.address3);
      expect(component.changeShipAddress.get('address3')).toBeInstanceOf(Object);
    }));

});
