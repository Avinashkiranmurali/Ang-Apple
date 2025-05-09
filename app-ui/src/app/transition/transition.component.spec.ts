import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TransitionComponent } from './transition.component';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateModule } from '@ngx-translate/core';
import { TransitionService } from './transition.service';
import { Router } from '@angular/router';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

describe('TransitionComponent', () => {
  let component: TransitionComponent;
  let fixture: ComponentFixture<TransitionComponent>;
  let transitionService: TransitionService;
  const programData = require('assets/mock/program.json');
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };
  userData['user']['program'] = programData;
  userData['user']['matomoTrackerURL'] = 'test/url';
  userData['config']['imageServerUrl'] = 'https://als-static.bridge2rewards.com/dev2';

  const mockRouter = {
    url: 'testUrl',
    navigate: jasmine.createSpy('navigate'),
    parseUrl: () => ({
      root: {
        children: {
          primary: {
            segments: [{ path: 'store/cart'}]
          }
        }
      }
    })
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        NoopAnimationsModule,
        TranslateModule.forRoot()
      ],
      declarations: [ TransitionComponent ],
      providers: [
        { provide: UserStoreService, useValue : userData },
        { provide: TransitionService },
        { provide: Router, useValue: mockRouter},
        { provide: MatomoService, useValue: { broadcast: () => {} } },
        NgbActiveModal
      ]
    })
    .compileComponents();
    transitionService = TestBed.inject(TransitionService);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TransitionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call failureTemplateCall method', () => {
    spyOn(component, 'failureTemplateCall').and.callThrough();
    component.failureTemplateCall();
    expect(component.failureTemplateCall).toHaveBeenCalled();
  });

  it('should call closeTransition method', () => {
    spyOn(component, 'closeTransition').and.callThrough();
    component.closeTransition('test');
    expect(component.closeTransition).toHaveBeenCalled();
  });

  it('should call animationDone method', () => {
    spyOn(component, 'animationDone').and.callThrough();
    const event = {
      fromState: 'return'
    };
    component.animationDone(event);
    expect(component.animationDone).toHaveBeenCalled();
  });

  it('should call cleanImageUrl method', () => {
    transitionService['_overlayName'].next('failure');
    spyOn(component, 'cleanImageUrl').and.callThrough();
    component.cleanImageUrl('apple-ui/store/browse/mac/');
    expect(component.cleanImageUrl).toHaveBeenCalled();
  });

  it('should call animationDone method', () => {
    (window['innerHeight'] as any) = undefined;
    spyOn(component, 'animationDone').and.callThrough();
    const event = {
      fromState: 'void'
    };
    component.animationDone(event);
    expect(component.animationDone).toHaveBeenCalled();
  });
});
