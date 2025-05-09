import { HeaderDirective } from './header.directive';
import { ViewContainerRef } from '@angular/core';

describe('HeaderDirective', () => {
  // eslint-disable-next-line prefer-const
  let viewContainerRef: ViewContainerRef;

  it('should create an instance', () => {
    const directive = new HeaderDirective(viewContainerRef);
    expect(directive).toBeTruthy();
  });
});
