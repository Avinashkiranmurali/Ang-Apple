import { Directive, Input, forwardRef } from '@angular/core';
import { NG_VALIDATORS, Validator, AbstractControl, Validators } from '@angular/forms';

@Directive({
  selector: '[appMaxValidator]',
  providers: [{ provide: NG_VALIDATORS, useExisting: MaxValidatorDirective, multi: true }]
})
export class MaxValidatorDirective {

  @Input() max: number;

  validate(control: AbstractControl): { [key: string]: any } {
    return Validators.max(this.max)(control);
  }
}
