import { Inject, Injectable, Renderer2, RendererFactory2 } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { Router } from '@angular/router';
import { Messages } from '@app/models/messages';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { BehaviorSubject, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TransitionService {

  // eslint-disable-next-line @typescript-eslint/naming-convention,no-underscore-dangle,id-blacklist,id-match
  private readonly _errorCode = new BehaviorSubject<string>(undefined);
  // eslint-disable-next-line @typescript-eslint/naming-convention,no-underscore-dangle,id-blacklist,id-match
  private readonly _overlayName = new BehaviorSubject<string>(undefined);
  // eslint-disable-next-line @typescript-eslint/naming-convention,no-underscore-dangle,id-blacklist,id-match
  private readonly _overlayType = new BehaviorSubject<string>(undefined);
  // eslint-disable-next-line @typescript-eslint/naming-convention,no-underscore-dangle,id-blacklist,id-match
  private readonly _offscreenTransitionText = new BehaviorSubject<{ [key: string]: string }>({});
  // eslint-disable-next-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match
  private readonly _showOverlay = new BehaviorSubject<boolean>(false);

  // Expose the observable$ part of the _user subject (read only stream)
  readonly errorCode$ = this._errorCode.asObservable();
  readonly overlayName$ = this._overlayName.asObservable();
  readonly overlayType$ = this._overlayType.asObservable();
  readonly offscreenTransitionText$ = this._offscreenTransitionText.asObservable();
  readonly showOverlay$ = this._showOverlay.asObservable();

  messages: Messages;
  private renderer: Renderer2;

  // setting up transition message object to use in transitions
  // moved outside the open transition function so the object persists and is not created every time a transition happens
  transitionMessage: { [key: string]: string };

  constructor(
    public rendererFactory: RendererFactory2,
    public messageStore: MessagesStoreService,
    private router: Router,
    @Inject(DOCUMENT) private document: Document) {
      this.messages = this.messageStore.messages;
      this.renderer = rendererFactory.createRenderer(null, null);
      this.transitionMessage = {
        startup: (this.messages && this.messages.firstLoadPrimary && this.messages.firstLoadSecondary) ? this.messages.firstLoadPrimary.concat(' ', this.messages.firstLoadSecondary) : '',
        processing: (this.messages && this.messages.processingPrimary && this.messages.processingSecondary) ? this.messages.processingPrimary.concat(' ', this.messages.processingSecondary) : '',
        'processing-cc': (this.messages && this.messages.processingPrimary && this.messages.processingSecondary) ? this.messages.processingPrimary.concat(' ', this.messages.processingSecondary) : '',
        'processing-payment': (this.messages && this.messages.processingPaymentPrimary && this.messages.processingPaymentSecondary) ? this.messages.processingPaymentPrimary.concat(' ', this.messages.processingPaymentSecondary) : '',
        transfer: (this.messages && this.messages.transferPrimary && this.messages.transferSecondary) ? this.messages.transferPrimary.concat(' ', this.messages.transferSecondary) : '',
        return: (this.messages && this.messages.returnPrimary && this.messages.returnSecondary) ? this.messages.returnPrimary.concat(' ', this.messages.returnSecondary) : '',
        success: (this.messages && this.messages.successPrimary && this.messages.successSecondary) ? this.messages.successPrimary.concat(' ', this.messages.successSecondary) : '',
        failure: (this.messages && this.messages.failurePrimary && this.messages.failureSecondary) ? this.messages.failurePrimary.concat(' ', this.messages.failureSecondary) : '',
        'failure-code': (this.messages && this.messages.failurePrimary && this.messages.failureSecondaryCode) ? this.messages.failurePrimary.concat(' ', this.messages.failureSecondaryCode) : '',
        loading: (this.messages && this.messages.loadingPrimary && this.messages.loadingSecondary) ? this.messages.loadingPrimary.concat(' ', this.messages.loadingSecondary) : ''
      };
  }

  transitionMessageObjectIsSet() {
    return (this.transitionMessage.startup &&
      this.transitionMessage.processing &&
      this.transitionMessage['processing-cc'] &&
      this.transitionMessage['processing-payment'] &&
      this.transitionMessage.transfer &&
      this.transitionMessage.return &&
      this.transitionMessage.success &&
      this.transitionMessage.failure &&
      this.transitionMessage['failure-code'] &&
      this.transitionMessage.loading);
  }

  // set top position for inner content
  fadeInCallback(windowHeight) {
    // Updated modal-processing to display: flex & align-items: center. So commenting this line as of now..
    // const element = document.querySelector('#innerContent');
    // if (element) {
    //   const elemHeight = element.clientHeight;
    //   const elemTop = ((windowHeight - elemHeight) / 2 - 10);
    //   element.setAttribute('style', 'margin-top: ' + (windowHeight / 2) + 'px');
    //   // this.renderer.setStyle(element, 'margin-top', elemTop + 'px');
    // }
  }

  fadeOutCallback() {
    const urlTree = this.router.parseUrl(this.router.url);
    const urlSegments = urlTree.root.children['primary'].segments.map(segment => '/' + segment.path);
    const currentState = urlSegments.join('');
    const locHref = window.location.href;
    const queryPos = locHref.indexOf('?');
    const locParent = locHref.slice(0, queryPos);
    const locSlice = locHref.slice(queryPos + 1);
    const locSplit = locSlice.split('=');

    // this.renderer.selectRootElement('.inner-content').removeAttr('style');
    this.renderer.removeClass(this.document.body, 'hideScroll');
    if (this._overlayType.value === 'return') {
      // $state.go('home.config.checkout', {reload:true});
      if (currentState === '/store/cart' || currentState === '/store/checkout' || currentState === '/store/confirmation') {
        if (locSplit[1] === 'error') {
          window.location.href = locParent;
          location.reload();
        } else {
          this.router.navigate(['/store/cart']);
        }
      } else {
        this.router.navigate(['/store']);
      }
    }
  }

  processAriaTransitions(transitionName: string) {
    // time in milliseconds to wait when changing the dom for screen reader to pick up changes
    const timeToWait = 100;

    // if transitionName was passed we can process the aria transition message as expected
    if (transitionName) {
      setTimeout(() => {
        this._offscreenTransitionText.next({ msg: this.transitionMessage[transitionName] });
      }, timeToWait);
    }
  }

  openTransition(name?: string, errorCode?: string, hideScroll?: boolean) {
    // if name is undefined set name to loading for message object
    if (!name) {
      name = 'loading';
    }

    hideScroll = (typeof hideScroll === undefined) ? true : hideScroll;

    this._offscreenTransitionText.next({ msg: '' }); // reset scope variable
    this._errorCode.next(errorCode);
    this._overlayName.next(name);
    if (hideScroll) {
      this.renderer.addClass(this.document.body, 'hideScroll');
    }
    if (!this._showOverlay.value) {
      this._showOverlay.next(true);
    }

    this.processAriaTransitions(name);
  }

  closeTransition(type?: string) {
    this._overlayType.next(type);
    this._showOverlay.next(false);
  }
}
