import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { SmartPriceComponent } from './smart-price.component';

describe('SmartPriceComponent', () => {
  let component: SmartPriceComponent;
  let fixture: ComponentFixture<SmartPriceComponent>;
  const programData = require('assets/mock/program.json');
  programData['carouselPages'] = ['pdp'];
  const userMock = require('assets/mock/user.json');
  userMock['program'] = programData;
  const userData = {
    user: userMock,
    program: programData,
    config: programData['config'],
    get: () => of(userMock)
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SmartPriceComponent ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: UserStoreService, useValue: userData },
        CurrencyFormatPipe,
        CurrencyPipe
      ]
    })
    .compileComponents();
    fixture = TestBed.createComponent(SmartPriceComponent);
    component = fixture.componentInstance;
    component.smartPrice = { points: 4180, amount: 167.20, isCashMaxLimitReached: false };
    component.messages = require('assets/mock/messages.json');
    component.ext = '';
    component.parentClass = '';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call ngOnChanges when showDecimal is false', () => {
    component.config.showDecimal = false;
    spyOn(component, 'ngOnChanges').and.callThrough();
    component.ngOnChanges();
    expect(component.ngOnChanges).toHaveBeenCalled();
  });

  it('should call ngOnChanges when smartPrice is null', () => {
    component.smartPrice = null;
    spyOn(component, 'ngOnChanges').and.callThrough();
    component.ngOnChanges();
    component['config'].showDecimal = true;
    component.smartPrice = { points: 4180, amount: 167.20, isCashMaxLimitReached: false };
    fixture.detectChanges();
    component.ngOnChanges();
    expect(component.ngOnChanges).toHaveBeenCalled();
  });

  it('should call getSplitPayTemplate for various options', () => {
    component.smartPrice = { points: 4180, amount: 167.20, isCashMaxLimitReached: false };
    component.ext = 'header-section';
    expect(component.getSplitPayTemplate()).toEqual('header-section-template.htm');
    component.ext = 'cta-section';
    expect(component.getSplitPayTemplate()).toEqual('cta-section-template.htm');
    component.ext = 'config';
    expect(component.getSplitPayTemplate()).toEqual('');
    component.ext = '';
    expect(component.getSplitPayTemplate()).toEqual('');
  });
});
