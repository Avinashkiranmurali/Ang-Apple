import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { DefaultFooterComponent } from './default-footer.component';
import { RouterTestingModule } from '@angular/router/testing';
import { InterpolatePipe } from '@app/pipes/interpolate.pipe';
import { TranslateModule, TranslatePipe } from '@ngx-translate/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { FooterDisclaimerService } from '@app/modules/footer/footer-disclaimer.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FooterDirective } from '@app/modules/footer/footer.directive';
import { Five9Service } from '@app/services/five9.service';
import { of, ReplaySubject } from 'rxjs';


describe('DefaultFooterComponent', () => {
  let component: DefaultFooterComponent;
  let fixture: ComponentFixture<DefaultFooterComponent>;
  const eventSubject = new ReplaySubject<NavigationEnd>(1);
  const mockRouter = {
    events: eventSubject.asObservable(),
    url: 'mac/macbook-pro',
    navigate: jasmine.createSpy('navigate')
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        DefaultFooterComponent,
        InterpolatePipe,
        TranslatePipe,
        FooterDirective
      ],
      imports: [
        RouterTestingModule,
        TranslateModule.forRoot(),
        HttpClientTestingModule
      ],
      providers: [
        InterpolatePipe,
        TranslatePipe,
        Five9Service,
        { provide: FooterDisclaimerService },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: {
          params: of({category: 'mac', subcat: 'macbook-pro', psid: '30001MYDA2LL/A'}) }
        }
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DefaultFooterComponent);
    component = fixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
    component.footerData = {};
    component.footerData['currentYear'] = new Date().getFullYear();
    const program = require('assets/mock/program.json');
    component.config = program['config'];
    component.scrollToTop = () => {
      window.scroll(0, 0 );
      return false;
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call openFive9Chat', () => {
    window.Five9SocialWidget = Object.assign({});
    window.Five9SocialWidget.maximizeChat = () => {};
    expect(component.openFive9Chat()).toBeFalsy();
  });
});
