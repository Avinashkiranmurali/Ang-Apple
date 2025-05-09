import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { FooterDisclaimerService } from '@app/modules/footer/footer-disclaimer.service';
import { TranslateModule, TranslatePipe } from '@ngx-translate/core';
import { of, ReplaySubject } from 'rxjs';
import { RBCFooterComponent } from './rbc-footer.component';
import isWebview from 'is-ua-webview';

describe('RBCFooterComponent', () => {
  let component: RBCFooterComponent;
  let fixture: ComponentFixture<RBCFooterComponent>;
  const eventSubject = new ReplaySubject<NavigationEnd>(1);
  const mockRouter = {
    events: eventSubject.asObservable(),
    url: 'mac/macbook-pro',
    navigate: jasmine.createSpy('navigate')
  };
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RBCFooterComponent ],
      imports: [
        TranslateModule.forRoot(),
        HttpClientTestingModule
      ],
      providers: [
        TranslatePipe,
        { provide: FooterDisclaimerService },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: {
          params: of({category: 'mac', subcat: 'macbook-pro', psid: '30001MYDA2LL/A'}) }
        }
      ]
    })
    .compileComponents();
    spyOnProperty(navigator, 'userAgent').and.returnValue('Mozilla/5.0 (Linux; Android 5.1.1; Nexus 5 Build/LMY48B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/43.0.2357.65 Mobile Safari/537.36');
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RBCFooterComponent);
    component = fixture.componentInstance;
    component.footerData = {};
    component.footerData['currentYear'] = new Date().getFullYear();
    component.messages = require('assets/mock/messages.json');
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

  it('should Call hideElement', () => {
    fixture.detectChanges();
    spyOn(component, 'hideElements').and.callThrough();
    component.hideElements();
    expect(component.hideElements).toHaveBeenCalled();
  });

  it('If is isWebview should Call hideElement', () => {
    if (isWebview(navigator.userAgent)) {
      spyOn(component, 'hideElements').and.callThrough();
      component.hideElements();
      expect(component.hideElements).toHaveBeenCalled();
    }
  });

  it('should call hideElements methods', () => {
    spyOn(component, 'hideElements').and.callThrough();
    component.hideElements();
    expect(component.hideElements).toHaveBeenCalled();
  });

});
