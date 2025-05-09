import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { HeaderDirective } from '@app/modules/header/header.directive';
import { TemplateStoreService } from '@app/state/template-store.service';
import { HeaderComponent } from './header.component';
import { HeaderService } from '@app/modules/header/header.service';
import { LogoComponent } from '@app/components/vars/default/header/logo/logo.component';
import { WelcomeMsgComponent } from '@app/components/vars/default/header/welcome-msg/welcome-msg.component';
import { Router } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { ChaseHeaderComponent } from '@app/components/vars/chase/header/chase-header.component';
import { InterpolatePipe } from '@app/pipes/interpolate.pipe';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let templateStoreService: TemplateStoreService;
  const configData = require('assets/mock/configData.json');

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        HeaderComponent,
        HeaderDirective,
        LogoComponent,
        WelcomeMsgComponent,
        ChaseHeaderComponent,
        InterpolatePipe,
        CurrencyFormatPipe
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: HeaderService },
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate') }
        }
      ]
    })
    .compileComponents();
    templateStoreService = TestBed.inject(TemplateStoreService);
    configData['configData'].templates.header = {
      class: 'class1',
      template: 'default',
      userLineTemplate: {
        template: 'header',
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
    templateStoreService.template = configData['configData'];
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    component.user = require('assets/mock/user.json');
    component.program = require('assets/mock/program.json');
    component.config = component.program['config'];
    component.messages = require('assets/mock/messages.json');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get headerTemplate value', () => {
    expect(component).toBeTruthy();
  });

});
