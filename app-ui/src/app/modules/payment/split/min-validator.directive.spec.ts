import { AbstractControl } from '@angular/forms';
import { MinValidatorDirective } from './min-validator.directive';

describe('MinValidatorDirective', () => {

  let minValidatorDirective: MinValidatorDirective;

  beforeEach(() => {
    minValidatorDirective = new MinValidatorDirective();
  });

  it('should create an instance', () => {
    expect(minValidatorDirective).toBeTruthy();
  });

  it('should call validate method', () => {
    const control: AbstractControl = Object.assign({});
    minValidatorDirective.validate(control);
    expect(minValidatorDirective.validate).toBeDefined();
    expect(minValidatorDirective.validate).toBeTruthy();
  });
});
