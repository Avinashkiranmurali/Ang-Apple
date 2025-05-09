import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';
import { CheckoutSummaryComponent } from './checkout-summary.component';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { TemplateService } from '@app/services/template.service';
import { Messages } from '../../../models/messages';
import { TitleCasePipe, DecimalPipe } from '@angular/common';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { BehaviorSubject, of, ReplaySubject } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';
import { NavigationEnd, Router } from '@angular/router';


describe('CheckoutSummaryComponent', () => {
  let component: CheckoutSummaryComponent;
  let fixture: ComponentFixture<CheckoutSummaryComponent>;
  const program = require('assets/mock/program.json');
  const userData = {
    user : {
      locale: 'en_US'
    },
    config : {
      loginRequired : false,
      SFProWebFont : true
    }
  };
  const routerEvent$ = new BehaviorSubject<NavigationEnd>(null);
  let router: Router;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ CheckoutSummaryComponent, CurrencyFormatPipe ],
      imports: [ TranslateModule.forRoot(), HttpClientTestingModule, RouterTestingModule ],
      providers: [
        TitleCasePipe,
        DecimalPipe,
        CurrencyFormatPipe,
        {
          provide: TemplateService,
          useValue: {
            getTemplatesProperty: (prop: string) => 'pink'
          }
        },
        { provide: CurrencyPipe, useValue: {
            program: of(program),
            transform: () => of({}) }
        },
        {
          provide: MessagesStoreService,
          useValue: {
            messages: {
              importantNote: 'Please ensure your shipping address and email address are correct before proceeding to the next step'
            }
          }
        },
        { provide: UserStoreService, useValue : userData },
      ]
    })
    .compileComponents();
    router = TestBed.inject(Router);
    router.navigate = jasmine.createSpy('navigate');
    (router as any).events = routerEvent$.asObservable();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CheckoutSummaryComponent);
    component = fixture.componentInstance;
    component.config.MercAddressLocked = true;
    component.config.ContactInfoLocked = true;
    component.config.ShipToNameLocked = true;
    component.config.businessNameLocked = true;
    component.userStore = Object.assign({
        program: {
          formatPointName: 'formatPointName'
        }
    });
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('ngOnchange assign value for checkoutAddress', () => {
    component.checkoutAddress = Object.assign({});
    component.checkoutAddress.firstName = 'abc';
    component.checkoutAddress.middleName = 'mno';
    component.checkoutAddress.lastName = 'xyz';
    spyOn(component, 'ngOnChanges').and.callThrough();
    component.ngOnChanges();
    expect(component.ngOnChanges).toHaveBeenCalled();
  });

  it('should call updatePaymentMethod method', () => {
    component.userStore.program.formatPointName = '';
    spyOn(component, 'updatePaymentMethod').and.callThrough();
    component.updatePaymentMethod();
    expect(component.userStore.program.formatPointName = '').toEqual('');
  });

  it('should call updatePaymentMethod method', () => {
    component.creditItem = Object.assign({});
    component.creditItem.ccLast4 = program;
    component.pointsUsed = program;
    spyOn(component, 'updatePaymentMethod').and.callThrough();
    component.updatePaymentMethod();
    expect(component.updatePaymentMethod).toHaveBeenCalled();
  });

});
