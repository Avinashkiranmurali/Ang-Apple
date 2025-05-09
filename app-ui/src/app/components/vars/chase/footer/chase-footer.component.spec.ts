import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { FooterDisclaimerService } from '@app/modules/footer/footer-disclaimer.service';
import { UserStoreService } from '@app/state/user-store.service';
import { of, ReplaySubject } from 'rxjs';
import { ChaseFooterComponent } from './chase-footer.component';

describe('ChaseFooterComponent', () => {
  let component: ChaseFooterComponent;
  let fixture: ComponentFixture<ChaseFooterComponent>;
  const programData = require('assets/mock/program.json');
  programData['config']['externalHeaderUrl'] = '';
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };
  const eventSubject = new ReplaySubject<NavigationEnd>(1);
  const mockRouter = {
    events: eventSubject.asObservable(),
    url: 'mac/macbook-pro',
    navigate: jasmine.createSpy('navigate')
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ChaseFooterComponent ],
      providers: [
        { provide: FooterDisclaimerService },
        { provide: Router, useValue: mockRouter },
        { provide: UserStoreService, useValue: userData },
        { provide: ActivatedRoute, useValue: {
          params: of({category: 'mac', subcat: 'macbook-pro', psid: '30001MYDA2LL/A'}) }
        }
      ],
      imports: [
        RouterTestingModule
      ],
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ChaseFooterComponent);
    component = fixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
    component.config = userData.config;
    component.config.externalHeaderUrl = 'testUrl';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should define ngOnInit method', () => {
    component.config.externalHeaderUrl = '';
    fixture.detectChanges();
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });
});
