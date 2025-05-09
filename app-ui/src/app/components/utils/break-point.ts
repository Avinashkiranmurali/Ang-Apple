import { BreakpointObserver, BreakpointState } from '@angular/cdk/layout';
import { Injector } from '@angular/core';
import { Subscription, Observable } from 'rxjs';

export class BreakPoint {
  breakPointObservable$: Observable<BreakpointState>;
  protected breakPointObserver: BreakpointObserver;
  isMobile: boolean;
  isTablet: boolean;
  isDesktop: boolean;
  constructor(
    public injector: Injector
  ) {
    this.breakPointObserver = injector.get(BreakpointObserver);
    this.breakPoints();
  }

  breakPoints(): void {
    const mobileWidth = '(max-width:766px)';
    const tabletWidth = '(min-width:767px) and (max-width:1027px)';
    const desktopWidth = '(min-width: 1028px)';
    this.breakPointObservable$ = this.breakPointObserver.observe([mobileWidth, tabletWidth, desktopWidth]);
    this.breakPointObservable$.subscribe(state => {
      this.isMobile = state.breakpoints[mobileWidth];
      this.isTablet = state.breakpoints[tabletWidth];
      this.isDesktop = state.breakpoints[desktopWidth];

      if (window.Five9SocialWidget && window.Five9SocialWidget.widgetAdded) {
        if (this.isMobile) {
          window.Five9SocialWidget.frame.style.display = 'none';
        } else {
          window.Five9SocialWidget.frame.style.display = 'block';
        }
      }
    });
  }

}
