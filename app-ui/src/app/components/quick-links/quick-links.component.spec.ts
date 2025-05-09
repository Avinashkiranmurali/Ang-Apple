import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { QuickLinksComponent } from './quick-links.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { DebugElement, Component } from '@angular/core';
import { By } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { NgbPopover, NgbPopoverModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';

// Simple test component that will not in the actual app
@Component({
  template:
  `<a id="mobile-nav" role="button" popoverClass="mobile-main-nav-menu" class=""
    #mobileNav="ngbPopover" triggers="manual" placement="bottom-left" [ngbPopover]="menuContent">
  </a>
  <ng-template #menuContent >
    <div class="search-overlay">
    </div>
  </ng-template>`
})
class TestComponent {}

describe('QuickLinksComponent', () => {
  let component: QuickLinksComponent;
  let fixture: ComponentFixture<QuickLinksComponent>;
  let testFixture: ComponentFixture<TestComponent>;
  let input: DebugElement;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule, RouterTestingModule, NgbPopoverModule, TranslateModule.forRoot() ],
      declarations: [ QuickLinksComponent, TestComponent ],
      providers: [
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate') }
        }
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(QuickLinksComponent);
    testFixture = TestBed.createComponent(TestComponent);
    component = fixture.componentInstance;
    component.quickLinks = require('assets/mock/quickLinks.json');
    fixture.detectChanges();
    input = fixture.debugElement;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should trigger click event', () => {
    const event = new MouseEvent('click');
    input.nativeElement.dispatchEvent(event);
    fixture.detectChanges();
    expect(component.onClick).toBeTruthy();
  });

  it('should trigger click event for anchor element', () => {
    const anchor = input.nativeElement.querySelector('a');
    anchor.click();
  });

  it('should trigger click event for anchor element with popover', () => {
    const directive = testFixture.debugElement.query(By.directive(NgbPopover));
    component.mobileNav = directive.references['mobileNav'];
    const anchor = input.nativeElement.querySelector('a');
    anchor.click();
  });
});
