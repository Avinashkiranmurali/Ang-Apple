import { CurrencyPipe } from '@angular/common';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { NgbActiveModal, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule, TranslatePipe } from '@ngx-translate/core';
import { AppleCareModalComponent } from './apple-care-modal.component';

describe('AppleCareModalComponent', () => {
  let component: AppleCareModalComponent;
  let fixture: ComponentFixture<AppleCareModalComponent>;
  const programData = require('assets/mock/program.json');
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        TranslateModule.forRoot(),
        RouterTestingModule,
        HttpClientTestingModule,
        NgbModule
      ],
      declarations: [
        AppleCareModalComponent,
        CurrencyFormatPipe,
        TranslatePipe
      ],
      providers: [
        TranslatePipe,
        CurrencyFormatPipe,
        CurrencyPipe,
        { provide: NgbActiveModal }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AppleCareModalComponent);
    component = fixture.componentInstance;
    component.user = userData.user;
    component.config = userData.config;
    component.messages = require('assets/mock/messages.json');
    const productDetail = require('assets/mock/product-detail.json');
    component.appleCareService = productDetail.addOns.servicePlans[0];
    component.appleCareService['learnMore'] = 'AppleCare+ benefits are separate from and in addition to the Apple Limited Warranty and any legal rights provided by consumer protection laws in your jurisdiction.';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call cancel method', () => {
    expect(component.cancel()).toBeFalsy();
  });

  it('should call doAppleCareSubscription method', () => {
    component.doAppleCareSubscription();
    expect(component.doAppleCareSubscription).toBeDefined();
  });
});
