import { CustomSelectDirective } from './custom-select.directive';
import { Component, DebugElement, ElementRef } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

// Simple test component that will not in the actual app
@Component({
  template:
  `<div class='row' appCustomSelect>
    <div class='address-fields' id='addressContainer'>
      <div class="select-selected"></div>
      <ul class="select-items select-hide" role="list"></ul>
    </div>
  </div>`
})
class TestComponent {}

describe('CustomSelectDirective', () => {
  let component: TestComponent;
  let fixture: ComponentFixture<TestComponent>;
  let directive: CustomSelectDirective;
  // eslint-disable-next-line prefer-const
  let elementRef: ElementRef;
  let input: DebugElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [
        TestComponent,
        CustomSelectDirective
      ],
      imports: [
        FormsModule,
        ReactiveFormsModule
      ]
    });
    fixture = TestBed.createComponent(TestComponent);
    component = fixture.componentInstance;
    elementRef = fixture.debugElement;
    directive = new CustomSelectDirective(elementRef);
    input = fixture.debugElement.query(By.directive(CustomSelectDirective));
    fixture.detectChanges();
  });

  it('should create an instance', () => {
    const dir = new CustomSelectDirective(elementRef);
    expect(dir).toBeDefined();
    expect(dir).toBeTruthy();
  });

  it('should trigger click event for an open element', () => {
    const event = new MouseEvent('click');
    input.nativeElement.children[0].classList.add('open');
    input.nativeElement.dispatchEvent(event);
    fixture.detectChanges();
    expect(directive.onClick).toBeTruthy();
  });

  it('should trigger click event for not an open element', () => {
    const event = new MouseEvent('click');
    if (input.nativeElement.children[0].classList.contains('open')) {
      input.nativeElement.children[0].classList.remove('open');
    }
    input.nativeElement.dispatchEvent(event);
    fixture.detectChanges();
    expect(directive.onClick).toBeTruthy();
  });

  it('should trigger focusout event for an open element', () => {
    const event = new MouseEvent('focusout');
    input.nativeElement.children[0].classList.add('open');
    input.nativeElement.dispatchEvent(event);
    fixture.detectChanges();
    expect(directive.onFocusout).toBeTruthy();
  });

  it('should trigger focusout event for an open element', () => {
    const event = new MouseEvent('focusout');
    input.nativeElement.children[0].classList.add('close');
    input.nativeElement.dispatchEvent(event);
    fixture.detectChanges();
    expect(directive.onFocusout).toBeTruthy();
  });
});
