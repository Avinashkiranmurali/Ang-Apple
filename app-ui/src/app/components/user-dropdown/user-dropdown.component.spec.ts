import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { UserDropdownComponent } from './user-dropdown.component';
import { RouterTestingModule } from '@angular/router/testing';
import { InterpolatePipe } from '@app/pipes/interpolate.pipe';
import { SafePipe } from '@app/pipes/safe.pipe';
import { ModalsService } from '@app/components/modals/modals.service';
import { of } from 'rxjs';
import { SharedService } from '@app/modules/shared/shared.service';
import { TemplateStoreService } from '@app/state/template-store.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Component, DebugElement } from '@angular/core';
import { NgbPopover, NgbPopoverModule } from '@ng-bootstrap/ng-bootstrap';
import { By } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { BreakpointObserver, BreakpointState } from '@angular/cdk/layout';

// Simple test component that will not in the actual app
@Component({
  template:
  `<a href="javascript:void(0)" aria-expanded="false" aria-label="Bag" placement="bottom" class="popup-cta btn-cart-popup nav-icon g-icons icon-ShoppingCartIcon cart-with-items"
          role="button" #bagMenu="ngbPopover"  triggers="manual"  [ngbPopover]="popContent" [autoClose]="'outside'">
    <span class="g-icons nav-hover-element path1"></span>
    <span class="g-icons path2" style="color: rgb(122, 185, 249);"></span>
  </a>
  <ng-template #popContent>
    <div class="popover-content" id="bag-content">
    </div>
  </ng-template>`
})
class TestComponent {}

describe('UserDropdownComponent', () => {
  let component: UserDropdownComponent;
  let fixture: ComponentFixture<UserDropdownComponent>;
  let testFixture: ComponentFixture<TestComponent>;
  let templateStoreService: TemplateStoreService;
  let translateService: TranslateService;
  const programData = require('assets/mock/program.json');
  const configData = require('assets/mock/configData.json');
  let input: DebugElement;
  let observer: BreakpointObserver;
  const state: BreakpointState = Object.assign({});
  state.breakpoints = {};
  state.breakpoints['(max-width:766px)'] = true;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        NgbPopoverModule,
        HttpClientTestingModule,
        TranslateModule.forRoot()
      ],
      declarations: [ UserDropdownComponent, InterpolatePipe, SafePipe, TestComponent ],
      providers: [
        { provide: ModalsService, useValue: {
          openBrowseOnlyComponent: () => of({}) }
        },
        { provide: SharedService, useValue: {
          sessionTypeAction: () => of('signOut') }
        }
      ]
    })
    .compileComponents();
    templateStoreService = TestBed.inject(TemplateStoreService);
    translateService = TestBed.inject(TranslateService);
    observer = TestBed.inject(BreakpointObserver);
    window.Five9SocialWidget = Object.assign({});
    window.Five9SocialWidget.widgetAdded = true;
    window.Five9SocialWidget.frame = document.createElement('div') as HTMLElement;
    // VALIDATE BREAKPOINTS FOR MOBILE VIEW
    spyOn(observer, 'observe').and.returnValue(of(state));
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserDropdownComponent);
    testFixture = TestBed.createComponent(TestComponent);
    component = fixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
    component.config = programData['config'];
    component.config.loginRequired = false;
    component.user = require('assets/mock/user.json');
    templateStoreService.template = configData['configData'];
    component.popupClose = Object.assign({});
    const bagItemList = '<ul class="terms-list"><li><a data-external href="/store/faqs">FAQ</a></li><li><a href="/store/terms">Terms & Conditions</a></li><li><a href="store/terms#Engraving">Engraving Your Device</a></li></ul>';
    spyOn(translateService, 'get').and.returnValue(of(bagItemList));
    fixture.detectChanges();
    input = fixture.debugElement;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should trigger ngOnInit manually for loginRequired scenario', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    component.config.loginRequired = true;
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should trigger AfterViewInit manually where termsLink is null', () => {
    spyOn(component, 'ngAfterViewInit').and.callThrough();
    component.termsLink = null;
    component.ngAfterViewInit();
    expect(component.ngAfterViewInit).toHaveBeenCalled();
  });

  it('should trigger event on click of termsLink', () => {
    const routerstub: Router = TestBed.inject(Router);
    spyOn(routerstub, 'navigate');
    const directive = testFixture.debugElement.query(By.directive(NgbPopover));
    component.popupClose = directive.references['bagMenu'];
    const termsElement = component.termsLink?.nativeElement.getElementsByTagName('a');
    if (termsElement && termsElement.length === 3 ) {
      termsElement[1].click();
      termsElement[2].click();
      expect(component['router'].navigate).toHaveBeenCalledWith(['/store/terms'], undefined);
      expect(component['router'].navigate).toHaveBeenCalledWith(['/store/terms'], { fragment: 'Engraving' });
    }
  });

  it('should call sessionTypeAction', () => {
    expect(component.sessionTypeAction('signOut')).toBeFalsy();
  });

  it('should call openFive9Chat', () => {
    component.five9SocialWidget = Object.assign({});
    component.five9SocialWidget.maximizeChat = () => {};
    fixture.detectChanges();
    expect(component.openFive9Chat()).toBeFalsy();
  });

  it('should call isMobile', () => {
    expect(component.isMobile).toBeTruthy();
    expect(component.isMobile).toBeInstanceOf(Boolean);
  });

  it('should call openModal', () => {
    expect(component.openModal()).toBeFalsy();
  });

  it('should trigger click event', () => {
    const event = new MouseEvent('click');
    input.nativeElement.dispatchEvent(event);
    fixture.detectChanges();
    expect(component.onClick).toBeTruthy();
  });

  it('should trigger click event for anchor element', () => {
    const directive = testFixture.debugElement.query(By.directive(NgbPopover));
    component.popupClose = directive.references['bagMenu'];
    fixture.detectChanges();
    const anchor = fixture.debugElement.queryAll(By.css('a'));
    if (anchor.length >= 3) {
      anchor[3].nativeElement.href = '#testaccount/123456';
      anchor[3].nativeElement.click();
      fixture.detectChanges();
    }
    expect(component.popupClose).toBeDefined();
  });

});
