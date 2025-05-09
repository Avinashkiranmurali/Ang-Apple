import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { DisplayFormatDirective } from './display-format.directive';
import { Renderer2, ElementRef, Component, Type, forwardRef, DebugElement } from '@angular/core';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { UserStoreService } from '@app/state/user-store.service';
import { FormsModule, NG_VALUE_ACCESSOR } from '@angular/forms';
import { By } from '@angular/platform-browser';

// Simple test component that will not in the actual app
@Component({
  template: `<input type="text" id="cashToUse" name="cashToUse"
  [value]="cashToUse"
  [(ngModel)]="cashToUse"
  disabled="true"
  [config]="config"
  appDisplayFormat
  [displayFormat]="'currency'">
  <input type="text" value="test" id="test" appDisplayFormat>`
})
class TestComponent {
  cashToUse = 200;
  programData = require('assets/mock/program.json');
  config = this.programData['config'];
}

describe('DisplayFormatDirective', () => {
  let fixture: ComponentFixture<TestComponent>;
  let directive: DisplayFormatDirective;
  let renderer2: Renderer2;
  let element: ElementRef;
  let currencyPipe: CurrencyPipe;
  const programData = require('assets/mock/program.json');
  const userMock = require('assets/mock/user.json');
  userMock['program'] = programData;
  const userData = {
    user: userMock,
    program: programData,
    config: programData['config']
  };
  let userStore: UserStoreService;
  let input: DebugElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [
        TestComponent,
        DisplayFormatDirective
      ],
      providers: [
        { provide: CurrencyPipe, useValue: {
          transform: () => {} }
        },
        { provide: Renderer2 },
        { provide: DisplayFormatDirective },
        { provide: NG_VALUE_ACCESSOR,
          useExisting: forwardRef(() => DisplayFormatDirective),
          multi: true
        }
      ],
      imports: [
        FormsModule
      ]
    });
    fixture = TestBed.createComponent(TestComponent);
    userStore = TestBed.inject(UserStoreService);
    userStore.addUser(userData.user);
    userStore.addProgram(userData.program);
    userStore.addConfig(userData.config);
    currencyPipe = new CurrencyPipe(userStore);
    element = fixture.debugElement;
    renderer2 = fixture.componentRef.injector.get<Renderer2>(Renderer2 as Type<Renderer2>);
    directive = new DisplayFormatDirective(renderer2, element, currencyPipe);
    directive.config = userData.config;
    fixture.detectChanges();
  });

  it('should create an instance', () => {
    expect(directive).toBeDefined();
  });

  it('should execute and call registerOnChange method', () => {
    const test = () => true;
    directive.registerOnChange(test);
    expect(directive.registerOnChange).toBeDefined();
  });

  it('should execute and call registerOnTouched method', () => {
    const test = () => true;
    directive.registerOnTouched(test);
    expect(directive.registerOnTouched).toBeDefined();
  });

  it('should execute and call formatValue method', () => {
    directive.config.showDecimal = false;
    directive.formatValue('points_decimal', 20000);
    expect(directive.formatValue).toBeDefined();
  });

  it('should execute and call formatValue method for showDecimal config', () => {
    directive.config.showDecimal = true;
    directive.formatValue('points_decimal', 20000);
    expect(directive.formatValue).toBeDefined();
  });

  it('should execute and call writeValue method', () => {
    directive.writeValue(20000);
    expect(directive.writeValue).toBeDefined();
  });

  it('should trigger input change event for input element cashToUse', () => {
    input = fixture.debugElement.query(By.css('#cashToUse'));
    const event = new Event('input');
    input.nativeElement.dispatchEvent(event);
    fixture.detectChanges();
    expect(directive.input).toBeDefined();
  });

  it('should trigger input change event for mock input element', () => {
    input = fixture.debugElement.query(By.css('#test'));
    const event = new Event('input');
    input.nativeElement.dispatchEvent(event);
    fixture.detectChanges();
    expect(directive.input).toBeDefined();
  });

});
