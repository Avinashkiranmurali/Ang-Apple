import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { OrderConfirmButtonsComponent } from './order-confirm-buttons.component';
import { Router } from '@angular/router';
import { UserStoreService } from '@app/state/user-store.service';
import { Program } from '@app/models/program';
import { AppConstants } from '@app/constants/app.constants';
import { SharedService } from '@app/modules/shared/shared.service';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';


describe('OrderConfirmButtonsComponent', () => {
  let component: OrderConfirmButtonsComponent;
  let sharedService: SharedService;
  let fixture: ComponentFixture<OrderConfirmButtonsComponent>;
  const programData: Program = require('assets/mock/program.json');
  const userData = {
    config: programData['config']
  };
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ OrderConfirmButtonsComponent ],
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [
        { provide: UserStoreService,  useValue : userData },
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate') }
        },
        { provide: CurrencyFormatPipe },
        { provide: DecimalPipe },
        { provide: CurrencyPipe }
      ]
    })
    .compileComponents();
    sharedService = TestBed.inject(SharedService);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OrderConfirmButtonsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call continueShopping Method', () => {
    spyOn(component, 'continueShopping').and.callThrough();
    component.paymentTemplate = AppConstants.paymentTemplate.cash_subsidy;
    const data = {
      signOutUrl: 'javascript:void(0)'
    };
    sessionStorage.setItem('sessionURLs', JSON.stringify(data));
    component.continueShopping();
    expect(component.continueShopping).toHaveBeenCalled();
  });

  it('should call continueShopping Method - else check', () => {
    spyOn(component, 'continueShopping').and.callThrough();
    component.paymentTemplate = 'abcd';
    const data = {
      signOutUrl: 'javascript:void(0)'
    };
    sessionStorage.setItem('sessionURLs', JSON.stringify(data));
    component.continueShopping();
    expect(component.continueShopping).toHaveBeenCalled();
  });

});
