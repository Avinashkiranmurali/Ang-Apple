import { Component, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { trigger, state, style, animate, transition } from '@angular/animations';
import { TransitionService } from './transition.service';
import { Config } from '@app/models/config';
import { UserStoreService } from '@app/state/user-store.service';
import { Subscription } from 'rxjs';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { AppConstants } from '@app/constants/app.constants';


@Component({
  selector: 'app-transition',
  templateUrl: './transition.component.html',
  styleUrls: ['./transition.component.scss'],
  animations: [
    trigger('fadeInOut', [
      transition(':enter', [
        style({ opacity: 0}),
        animate('300ms', style({ opacity: 1 })),
      ]),
      transition(':leave', [
        animate('1500ms', style({ opacity: 0 })),
      ]),
    ])
  ]
})
export class TransitionComponent implements OnDestroy, OnInit {

  config: Config;
  errorCode: string;
  imageServerUrl: string;
  overlayName: string;
  overlayType: string;
  offscreenTransitionText: { [key: string]: string };
  showOverlay: boolean;
  private subscriptions: Subscription[] = [];

  constructor(  private transitionService: TransitionService,
                public userStore: UserStoreService,
                private matomoService: MatomoService) {
    this.config = this.userStore.config;
    this.imageServerUrl = this.cleanImageUrl(this.config.imageServerUrl);
    this.subscriptions.push(
      this.transitionService.showOverlay$.subscribe(data => {
        this.showOverlay = data;
      }),
      this.transitionService.overlayName$.subscribe(data => {
        this.overlayName = data ? data + '-temp.htm' : '';

        if (this.overlayName === 'failure-temp.htm' || this.overlayName === 'failure-code-temp.htm') {
          this.failureTemplateCall();
        }
      }),
      this.transitionService.overlayType$.subscribe(data => {
        this.overlayType = data;
      }),
      this.transitionService.offscreenTransitionText$.subscribe(data => {
        this.offscreenTransitionText = data;
      }),
      this.transitionService.errorCode$.subscribe(data => {
        this.errorCode = data;
      })
    );
  }

  ngOnInit(): void {
  }

  cleanImageUrl(url): string {
    const cleanedUrl = url.substr(-1) === '/' ? (url.slice(0, -1)) : url;
    return cleanedUrl;
  }

  animationDone(event) {
    // fadeIn animation callback state: void => null
    if (event.fromState === 'void') {
      const windowHeight = window.innerHeight || document.documentElement.clientHeight;
      this.transitionService.fadeInCallback(windowHeight);
    } else {
      // fadeOut animation callback state: null => void
      this.transitionService.fadeOutCallback();
    }
  }

  closeTransition(type: string) {
    this.transitionService.closeTransition(type);
  }

  failureTemplateCall() {
    this.matomoService.broadcast(AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.CANONICAL_PAGE, {
        payload: {
            location: location.href,
            canonicalTitle: AppConstants.analyticServices.CANONICAL_CONSTANTS.ERROR
        }
    });
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }
}
