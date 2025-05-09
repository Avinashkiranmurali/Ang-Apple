import { DecimalPipe } from '@angular/common';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { SharedService } from '@app/modules/shared/shared.service';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateModule } from '@ngx-translate/core';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';

import { ChaseHeaderComponent } from './chase-header.component';

describe('ChaseHeaderComponent', () => {
  let component: ChaseHeaderComponent;
  let fixture: ComponentFixture<ChaseHeaderComponent>;
  let userStoreService: UserStoreService;
  const programData = require('assets/mock/program.json');
  programData['config']['externalHeaderUrl'] = '';
  const userMock = require('assets/mock/user.json');
  userMock['program'] = programData;
  const userData = {
    user: userMock,
    program: programData,
    config: programData['config']
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ChaseHeaderComponent ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot()
      ],
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ],
      providers: [
        SharedService,
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe,
        ParsePsidPipe
      ]
    })
    .compileComponents();
    userStoreService = TestBed.inject(UserStoreService);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ChaseHeaderComponent);
    component = fixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
    component.config = userData.config;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should trigger ngOnInit and check externalHeaderUrl exists', () => {
    component.config.externalHeaderUrl = 'https://cep01.jpqa2.bridge2solutions.net/apple-sso/';
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.baseUrl).toEqual('https://cep01.jpqa2.bridge2solutions.net/apple-sso/');
  });

  it('onScroll() to call sharedService to close the popup ', () => {
    spyOn(component, 'onScroll').and.callThrough();
    document.body.innerHTML += '<div class="popover cartAbandonPopup"><div class="cart-pop-up"></div></div>';
    fixture.detectChanges();
    window.dispatchEvent(new Event('scroll'));
    fixture.detectChanges();
    expect(component.onScroll).toHaveBeenCalled();
  });

});
