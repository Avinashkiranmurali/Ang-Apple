import { CustomHoverLinkDirective } from './custom-hover-link.directive';
import { Component, DebugElement, ElementRef, NgZone } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';

// Simple test component that will not in the actual app
@Component({
  template:
  `<div class="main nav-animation" appCustomHoverLink [navBarColors]="navBarColors">
    <p class=''>Test this paragraph</p>
    <p class='main nav-animation'>Test this paragraph</p>
    <p class=''>Test this paragraph</p>
  </div>`
})
class TestComponent {
  navBarColors = {
    textColor: '#333',
    activeTextColor: '#7ab9f9',
    hoverColor: '#111'
  };
}

describe('CustomHoverLinkDirective', () => {
  let component: TestComponent;
  let fixture: ComponentFixture<TestComponent>;
  let directive: CustomHoverLinkDirective;
  // eslint-disable-next-line prefer-const
  let elementRef: ElementRef;
  let input: DebugElement;
  let ngZone: NgZone;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [
        TestComponent,
        CustomHoverLinkDirective
      ]
    });
    fixture = TestBed.createComponent(TestComponent);
    component = fixture.componentInstance;
    elementRef = fixture.debugElement;
    ngZone = fixture.ngZone;
    directive = new CustomHoverLinkDirective(elementRef, ngZone);
    input = fixture.debugElement.query(By.directive(CustomHoverLinkDirective));
    fixture.detectChanges();
  });

  it('should create an instance', () => {
    const dir = new CustomHoverLinkDirective(elementRef, ngZone);
    expect(dir).toBeDefined();
    expect(dir).toBeTruthy();
  });

  it('should trigger mouseleave event for active element', () => {
    const event = new MouseEvent('mouseleave');
    input.nativeElement.children[0].classList.add('active');
    input.nativeElement.dispatchEvent(event);
    fixture.detectChanges();
    expect(directive.onMouseLeave).toBeTruthy();
  });

  it('should trigger mouseleave event for inactive element', () => {
    const event = new MouseEvent('mouseleave');
    if (input.nativeElement.children[0].classList.contains('active')) {
      input.nativeElement.children[0].classList.remove('active');
    }
    input.nativeElement.dispatchEvent(event);
    fixture.detectChanges();
    expect(directive.onMouseLeave).toBeTruthy();
  });

  it('should trigger mouseover event', () => {
    const event = new MouseEvent('mouseover');
    input.nativeElement.dispatchEvent(event);
    expect(directive.onMouseOver).toBeTruthy();
  });

  it('should trigger click event', () => {
    const event = new MouseEvent('click');
    input.nativeElement.dispatchEvent(event);
    expect(directive.onClick).toBeTruthy();
  });

});
