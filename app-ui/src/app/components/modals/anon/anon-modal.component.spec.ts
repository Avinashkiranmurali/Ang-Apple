import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { AnonModalComponent } from './anon-modal.component';
import { SharedService } from '@app/modules/shared/shared.service';
import { TemplateStoreService } from '@app/state/template-store.service';
import { of } from 'rxjs';
import { SessionService } from '@app/services/session.service';
import { HttpClient, HttpHandler } from '@angular/common/http';

describe('ModalsComponent', () => {
  let component: AnonModalComponent;
  let fixture: ComponentFixture<AnonModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ AnonModalComponent ],
      providers: [
        { provide: NgbActiveModal },
        { provide: SessionService, useValue: {
          getSession: () => {} }
        },
        { provide: HttpClient },
        { provide: HttpHandler },
        {
          provide: SharedService, useValue: {
            sessionTypeAction: () => of('signIn')
          }
        },
        { provide: TemplateStoreService, useValue: {
            anonymousModal: () => of({})
          }
        }
      ],
      imports: [
        TranslateModule.forRoot()
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    const programData: object = require('assets/mock/program.json');
    fixture = TestBed.createComponent(AnonModalComponent);
    component = fixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
    component.config = programData['config'];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('cancel should return false', () => {
    expect(component.cancel()).toBeFalse();
  });

  it('doSignIn should return false', () => {
    expect(component.doSignIn()).toBeFalse();
  });
});
