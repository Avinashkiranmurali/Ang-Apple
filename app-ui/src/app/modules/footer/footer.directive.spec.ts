import { ViewContainerRef } from '@angular/core';
import { FooterDirective } from './footer.directive';

describe('FooterDirective', () => {
  // eslint-disable-next-line prefer-const
  let viewContainerRef: ViewContainerRef;

  it('should create an instance', () => {
    const directive = new FooterDirective(viewContainerRef);
    expect(directive).toBeTruthy();
  });
});
