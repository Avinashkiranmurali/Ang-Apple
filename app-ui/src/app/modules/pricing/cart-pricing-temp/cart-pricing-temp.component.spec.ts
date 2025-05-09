import { Injectable } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { CartPricingTempComponent } from './cart-pricing-temp.component';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { of } from 'rxjs/internal/observable/of';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { SharedService } from '@app/modules/shared/shared.service';

@Injectable()
export class TranslateServiceStub {
  public get<T>(key: T): Observable<T> {
    return of(key);
  }
  public instant(key: string): any {
    return '';
  }
}
describe('CartPricingTempComponent', () => {
  let component: CartPricingTempComponent;
  let fixture: ComponentFixture<CartPricingTempComponent>;
  let service: PricingService;
  const programData = require('assets/mock/program.json');
  const user = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };
  const userData = {
    user : {
      varId : 'UA',
      program :  {
        formatPointName: 'ua.miles'
      }
    },
    config : {
      loginRequired : false,
      SFProWebFont : true
    }
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ CartPricingTempComponent ],
      imports: [ HttpClientTestingModule, RouterTestingModule ],
      providers: [ MessagesStoreService, {
        provide: UserStoreService, useValue: userData
      }, {
        provide: TranslateService, useClass: TranslateServiceStub
      },
      CurrencyFormatPipe,
        SharedService,
      CurrencyPipe,
      DecimalPipe ]
    })
    .compileComponents();
    service = TestBed.inject(PricingService);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CartPricingTempComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterAll(() => {
    component.user.program.formatPointName = 'ua.miles';
  });

  it('should create', () => {
    component.user.program.formatPointName = '';
    component['userStore']['config']['payFrequency'] = 'test';
    expect(component).toBeTruthy();
  });

  it('ngOnit should assign value for isDiscounted', () => {
    component.offers = require('assets/mock/product-detail.json')['offers'];
    component.ngOnChanges();
    if (component.offers) {
      expect(component.isDiscounted).toEqual(component.pricingService.checkDiscounts(component.offers));
    }
  });

  it('ngOnit should assign value for isDiscounted', () => {
    component.offers = null;
    component.ngOnChanges();
    expect(component.ngOnChanges).toBeDefined();
  });
});
