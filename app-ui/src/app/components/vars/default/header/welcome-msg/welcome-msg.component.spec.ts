import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { SharedService } from '@app/modules/shared/shared.service';
import { of } from 'rxjs';
import { WelcomeMsgComponent } from './welcome-msg.component';
import { TranslateModule } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';

describe('WelcomeMsgComponent', () => {
  let component: WelcomeMsgComponent;
  let fixture: ComponentFixture<WelcomeMsgComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ WelcomeMsgComponent ],
      providers: [
        { provide: SharedService, useValue: {
          sessionTypeAction: () => of({})  }
        }
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot(),
        FormsModule,
        CommonModule
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WelcomeMsgComponent);
    component = fixture.componentInstance;
    component.user = require('assets/mock/user.json');
    component.program = require('assets/mock/program.json');
    component.config = component.program['config'];
    component.messages = require('assets/mock/messages.json');
    component.headerTemplate = {
      class: '',
      logoTemplate: {
        template: 'logo-container.htm',
        class: 'logo-container'
      },
      userLineTemplate: {
        template: 'anonymous',
        anonTemplate: 'dual',
        class: 'user-nav',
        btnClass: 'sign-in btn-yellow',
        welcomeMsg: null
      }
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('doSignIn must have called sessionTypeAction with signIn as parameter', () => {
    spyOn(component.sharedService, 'sessionTypeAction').and.callThrough();
    component.doSignIn();
    expect(component.sharedService.sessionTypeAction).toHaveBeenCalled();
  });

  it('doSignOut must have called sessionTypeAction with signOut as parameter', () => {
    spyOn(component.sharedService, 'sessionTypeAction').and.callThrough();
    component.doSignOut();
    expect(component.sharedService.sessionTypeAction).toHaveBeenCalled();
  });
});
