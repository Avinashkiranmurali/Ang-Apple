import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { DefaultHeaderComponent } from './default-header.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { LogoComponent } from './logo/logo.component';
import { WelcomeMsgComponent } from './welcome-msg/welcome-msg.component';
import { UserStoreService } from '@app/state/user-store.service';
import { InterpolatePipe } from '@app/pipes/interpolate.pipe';
import { TranslateModule } from '@ngx-translate/core';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

describe('DefaultHeaderComponent', () => {
  let component: DefaultHeaderComponent;
  let fixture: ComponentFixture<DefaultHeaderComponent>;
  let userStoreService: UserStoreService;
  const programData = require('assets/mock/program.json');
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        DefaultHeaderComponent,
        LogoComponent,
        WelcomeMsgComponent,
        InterpolatePipe
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        InterpolatePipe,
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe
      ]
    })
    .compileComponents();
    userStoreService = TestBed.inject(UserStoreService);
    userStoreService.addUser(userData.user);
    userStoreService.addProgram(userData.program);
    userStoreService.addConfig(userData.config);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DefaultHeaderComponent);
    component = fixture.componentInstance;
    component.config = {clientHeaderBackgroundColor: 'red'};
    component.messages = require('assets/mock/messages.json');
    component.user = userData.user;
    component.program = userData.program;
    component.headerTemplate = {
      class: 'class1',
      userLineTemplate: {
        template: 'default',
        class: 'user-nav',
        btnClass: '',
        welcomeMsg: {
          template: 'custom-msg'
        }
      },
      logoTemplate: {
        template: 'logo-container.htm',
        class: 'logo-container'
      },
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
