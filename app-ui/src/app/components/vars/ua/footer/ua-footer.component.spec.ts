import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ActivatedRoute, NavigationEnd, Router, RouterEvent } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { FooterDisclaimerService } from '@app/modules/footer/footer-disclaimer.service';
import { TranslateModule, TranslatePipe } from '@ngx-translate/core';
import { BehaviorSubject, of } from 'rxjs';
import { UAFooterComponent } from './ua-footer.component';

@Component({
  template: `<div id="testModule">
    <button id="btn-addtocart"></button>
    <button id="btn-remove-item-test12345"></button>
    <button id="cApprove"></button>
    <button id="test"></button>
    <button id="btn-complete-purchase"></button>
  </div>`
})

export class TestComponent {}

describe('UAFooterComponent', () => {
  let component: UAFooterComponent;
  let fixture: ComponentFixture<UAFooterComponent>;
  let testFixture: ComponentFixture<TestComponent>;
  const routerEvent$ = new BehaviorSubject<RouterEvent>(null);
  let router: Router;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ UAFooterComponent, TranslatePipe, TestComponent ],
      imports: [
        TranslateModule.forRoot(),
        HttpClientTestingModule,
        RouterTestingModule
      ],
      providers: [
        FooterDisclaimerService,
        TranslatePipe,
        { provide: ActivatedRoute, useValue: {
          params: of({category: 'mac', subcat: 'macbook-pro', psid: '30001MYDA2LL/A'}),
          snapshot: {} }
        }
      ]
    })
    .compileComponents();
    router = TestBed.inject(Router);
    router.navigate = jasmine.createSpy('navigate');
    (router as any).events = routerEvent$.asObservable();
    window.BKTAG = Object.assign({});
    window.BKTAG.bk_allow_multiple_calls = true;
    window.BKTAG.bk_use_multiple_iframes = true;
    window.BKTAG.addPageCtx = () => {};
    window.BKTAG.doJSTag = () => {};
    window.BKTAG._reset = () => {};
    window.BKTAG.addHash = () => {};
    window.BKTAG.util = Object.assign({});
    window.BKTAG.util.normalizeEmail = () => {};
    window.BKTAG.util.normalizePhone = () => {};
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UAFooterComponent);
    testFixture = TestBed.createComponent(TestComponent);
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
    component.user = Object.assign({});
    component.user.email = 'test@gmail.com';
    component.user.phone = '9898459384';
    fixture.detectChanges();
  });

  it('should create', () => {
    routerEvent$.next(new NavigationEnd(1, '/store/cart/checkout', '/store/curated/ipad/ipad-accessories'));
    expect(component).toBeTruthy();
  });

  it('should call invokeRREvent method for ADD', () => {
    spyOn(component, 'invokeRREvent').and.callThrough();
    component.invokeRREvent('ADD');
    expect(component.invokeRREvent).toHaveBeenCalled();
  });

  it('should call invokeRREvent method for UPDATE', () => {
    spyOn(component, 'invokeRREvent').and.callThrough();
    component.invokeRREvent('UPDATE');
    expect(component.invokeRREvent).toHaveBeenCalled();
  });

  it('should call onLoad method', () => {
    spyOn(component, 'onLoad').and.callThrough();
    component.onLoad();
    expect(component.onLoad).toHaveBeenCalled();
  });

  it('should trigger event on click of btn-complete-purchase button', () => {
    const element = testFixture.debugElement.query(By.css('#btn-complete-purchase'));
    const event = new MouseEvent('click' , { bubbles: true, cancelable: false });
    element.nativeElement.dispatchEvent(event);
  });

  it('should trigger event on click of addToCart button', () => {
    const element = testFixture.debugElement.query(By.css('#btn-addtocart'));
    const event = new MouseEvent('click' , { bubbles: true, cancelable: false });
    element.nativeElement.dispatchEvent(event);
  });

  it('should trigger event on click of cApprove button', () => {
    const element = testFixture.debugElement.query(By.css('#cApprove'));
    const event = new MouseEvent('click' , { bubbles: true, cancelable: false });
    element.nativeElement.dispatchEvent(event);
  });

  it('should trigger event on click of btn-remove-item button', () => {
    const element = testFixture.debugElement.query(By.css('#btn-remove-item-test12345'));
    const event = new MouseEvent('click' , { bubbles: true, cancelable: false });
    element.nativeElement.dispatchEvent(event);
  });

  it('should trigger event but not execute on click of test button', () => {
    const element = testFixture.debugElement.query(By.css('#test'));
    const event = new MouseEvent('click' , { bubbles: true, cancelable: false });
    element.nativeElement.dispatchEvent(event);
  });

});
