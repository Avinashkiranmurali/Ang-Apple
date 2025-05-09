import { Directive, forwardRef, Input, Renderer2, ElementRef, HostListener } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { Config } from '@app/models/config';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

const DISPLAY_FORMAT_CONTROL_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => DisplayFormatDirective),
  multi: true
};

@Directive({
  selector: '[appDisplayFormat]',
  providers: [DISPLAY_FORMAT_CONTROL_VALUE_ACCESSOR]
})
export class DisplayFormatDirective implements ControlValueAccessor {

  onChange;
  onTouched;

  @Input() displayFormat: string;
  @Input() config: Config;

  constructor(private renderer: Renderer2,
              private element: ElementRef,
              private currencyPipe: CurrencyPipe ) {
  }

  @HostListener('input', [ '$event.target.value' ])
  input( value ) {
    if (this.onChange) {
      this.onChange(value);
    }
  }

  writeValue(value: any ): void {
    const element = this.element.nativeElement;
    this.renderer.setProperty(element, 'value', this.formatValue(this.displayFormat, value));
  }

  registerOnChange(fn: any ): void {
    this.onChange = fn;
  }

  formatValue(format: string, value){
    if (value === null || value === undefined || isNaN(value)) {
      return value;
    }
    const val = format === 'points_decimal' ? parseFloat(value) / 100 : parseFloat(value);
    if (this.config.showDecimal) {
      return this.currencyPipe.transform(val);
    } else {
      return this.currencyPipe.transform(val, '', 'symbol', '1.0-0');
    }
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }
}
