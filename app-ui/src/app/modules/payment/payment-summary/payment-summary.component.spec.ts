import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { PaymentSummaryComponent } from './payment-summary.component';
import { UserStoreService } from '@app/state/user-store.service';
import { PaymentStoreService } from '@app/state/payment-store.service';
import { CartService } from '@app/services/cart.service';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { TemplateStoreService } from '@app/state/template-store.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { DecimalPipe, TitleCasePipe } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { Program } from '@app/models/program';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

describe('PaymentSummaryComponent', () => {
  let component: PaymentSummaryComponent;
  let fixture: ComponentFixture<PaymentSummaryComponent>;
  let templateStoreService: TemplateStoreService;
  let userStoreService: UserStoreService;
  let pricingService: PricingService;
  const programData: Program = require('assets/mock/program.json');
  const userMock = require('assets/mock/user.json');
  userMock['program'] = programData;
  const userData = {
    user: userMock,
    program: programData,
    config: programData['config']
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [PaymentSummaryComponent],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot(),
        HttpClientModule,
      ],
      providers: [
        { provide: MessagesStoreService },
        { provide: PaymentStoreService },
        { provide: CartService },
        { provide: PricingService },
        { provide: TranslateService },
        { provide: TitleCasePipe },
        { provide: CurrencyFormatPipe },
        { provide: DecimalPipe },
        { provide: CurrencyPipe }
      ]
    })
      .compileComponents();
    templateStoreService = TestBed.inject(TemplateStoreService);
    pricingService = TestBed.inject(PricingService);
    userStoreService = TestBed.inject(UserStoreService);
    userStoreService.addUser(userData.user);
    userStoreService.addProgram(userData.program);
    userStoreService.addConfig(userData.config);
    spyOn(pricingService, 'getPricingOption').and.returnValue({option: 'unbundledDetails', isUnbundled: false});
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PaymentSummaryComponent);
    component = fixture.componentInstance;
    const mockTemplate = require('assets/mock/configData.json');
    component.template = mockTemplate['configData'];
    component.messages = require('assets/mock/messages.json');
    component.subtitle = 'Title';
    component.heading = 'Heading';
    component.pointLabel = 'Point Label';
    component.state = {
      selections: {
        payment: {
          splitPayOption: {
            pointsToUse: 198000
          }
        }
      }
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  afterAll(() => {
    component.userStore.program.formatPointName = 'delta.points';
  });

  it('should formatPointName to be null', () => {
    component.userStore.program.formatPointName = '';
    fixture.detectChanges();
    expect(component.userStore.program.formatPointName).toEqual('');
  });

});
