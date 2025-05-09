import { AbstractControl } from '@angular/forms';
import { MaxValidatorDirective } from './max-validator.directive';

describe('MaxValidatorDirective', () => {
  let maxValidatorDirective: MaxValidatorDirective;

  beforeEach(() => {
    maxValidatorDirective = new MaxValidatorDirective();
  });

  it('should create an instance', () => {
    expect(maxValidatorDirective).toBeTruthy();
  });

  it('should call validate method', () => {
    const control: AbstractControl = Object.assign({});
    maxValidatorDirective.validate(control);
    expect(maxValidatorDirective.validate).toBeDefined();
    expect(maxValidatorDirective.validate).toBeTruthy();
  });
});
