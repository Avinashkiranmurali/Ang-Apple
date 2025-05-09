import { Injectable } from '@angular/core';
import { UserStoreService } from '@app/state/user-store.service';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Messages } from '@app/models/messages';
import { User } from '@app/models/user';
import { Config } from '@app/models/config';
import { TimeoutWarningModelComponent } from '@app/components/modals/timeout-warning/timeout-warning-model.component';
import { TimeoutComponent } from '@app/components/modals/timeout/timeout.component';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { Observable,  throwError } from 'rxjs';
import { BaseService } from '@app/services/base.service';
import { DomSanitizer } from '@angular/platform-browser';
import { SharedService } from '../modules/shared/shared.service';
import { SessionAllUrls } from '@app/models/session-all-urls';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { AppConstants } from '@app/constants/app.constants';
import { KeyStoneSyncService } from '@app/services/key-stone-sync.service';
@Injectable({
  providedIn: 'root'
})
export class SessionService extends BaseService {
  isTimeoutModal: boolean;
  messages: Messages;
  user: User;
  config: Config;
  timeoutCounter;
  sessionWarningCounter;
  currentSession;
  oauth;
  sessionAllUrls: SessionAllUrls;
  public sessionURL;
  public keepAliveUrl;
  currentSessionData;

  constructor(
    public userStore: UserStoreService,
    private messagesStore: MessagesStoreService,
    private bootstrapModal: NgbModal,
    private http: HttpClient,
    private sanitizer: DomSanitizer,
    private sharedService: SharedService,
    private matomoService: MatomoService,
    public keyStoneSyncService: KeyStoneSyncService
  ) {
    super();
    this.initSession();
    this.messages = this.messagesStore.messages;
    this.user = this.userStore.user;
    this.config = this.userStore.config;
    this.sessionWarningCounter = 0;
    this.timeoutCounter = 0;
  }

  initSession() {
    this.sharedService.endSession$.subscribe(isEndSession => { // receive from keystonsync service
      if (isEndSession){
        this.endSession(); // this will end the session and show the timeout pop-up.
      }
    });
    this.sharedService.triggerPartnerSignOutUrls$.subscribe(() => {
      this.triggerPartnerSignOutUrls();
    });
    if (window['oauth'] && Object.keys(window['oauth']).length) {
      this.oauth = {};
      this.oauth.on = false;
      this.oauth.oauthCheckSessionIframUrl = window['oauth'].oauthCheckSessionIframUrl;
      this.oauth.oauthClientId = window['oauth'].oauthClientId;
      this.oauth.oauthTokenSessionState = window['oauth'].oauthTokenSessionState;
      if (this.oauth.oauthCheckSessionIframUrl && this.oauth.oauthClientId && this.oauth.oauthTokenSessionState) {
        this.oauth.on = true;
        // create dynamic iframe:
        this.oauth.oauthFrame = document.createElement('iframe');
        this.oauth.oauthFrame.setAttribute('src', this.oauth.oauthCheckSessionIframUrl);
        this.oauth.oauthFrame.name = 'oauthFrame';
        this.oauth.oauthFrame.id = 'oauthFrame';
        this.oauth.oauthFrame.style.display = 'none';
        this.oauth.oauthFrame.setAttribute('sandbox', 'allow-same-origin');
        document.body.appendChild(this.oauth.oauthFrame);
        window.addEventListener('message', this.receiveMessage, false);
      }
    }

  }

  getSessionURLs(): Observable<any> {
    const initParam = (!this.keyStoneSyncService.initKeyStoneUrls) ? 'validSession?initial=true' : 'validSession';
    if (!this.keyStoneSyncService.initKeyStoneUrls){
      this.keyStoneSyncService.initKeyStoneUrls = true;
    }

    const url = this.baseUrl + initParam;

    return this.http.get(url, this.httpOptions)
      .pipe(
        map((response) => response),
        catchError((error: HttpErrorResponse) => {
          this.handleError(error);
          return throwError(error);
        })
      );
  }

  getSession(): void {
    // TODO handle the get session here
    this.currentSession = this.getSessionURLs().subscribe((data) => {
      // If successful, determine whether to use keep
      const initData = data;
      sessionStorage.setItem('sessionURLs', JSON.stringify(data));
      this.currentSessionData = data;
      const hasKeepAliveUrl = initData.hasOwnProperty('keepAliveUrl');
      const hasKeepAliveSource = initData.hasOwnProperty('keepAliveUrlSource');

      /**
       * start points bank
       */
      if (initData.hasOwnProperty('keepAliveUrlPointsBank')) {
        const keepAliveUrlPointsBank = this.currentSessionData['keepAliveUrlPointsBank'];
        const method = (location.host === this.getRootUrl(keepAliveUrlPointsBank)) ? 'GET' : 'JSONP';
        if (method === 'JSONP') {
          this.http.jsonp(keepAliveUrlPointsBank, 'callback').toPromise().then(result => {
            },
            (error: HttpErrorResponse) => {
            });
        } else {
          this.http.get(keepAliveUrlPointsBank).subscribe((getResult) => {
            },
            (error: HttpErrorResponse) => {
            });
        }
      }
      // end points bank

      // Start partnerTimeOutUrls
      if (initData.hasOwnProperty('partnerTimeOutUrls')) {
        const partnerTimeOutUrls = this.currentSessionData['partnerTimeOutUrls'];
        sessionStorage.setItem('partnerTimeOutUrls', partnerTimeOutUrls);
      }
      // End partnerTimeOutUrls

      if (initData.hasOwnProperty('keystoneUrls')) {
        this.keyStoneSyncService.setKeyStoneUrl({...initData.keystoneUrls});
        // this.keyStoneSyncService.keyStoneUrls = {...initData.keystoneUrls}; // TODO later
        delete initData.keystoneUrls;
      }
      if (this.keyStoneSyncService.isKeyStoneSync('keepAlive')) {
        this.keyStoneSyncService.dispatchKeepAliveSyncEvent();
      }
      if (initData.hasOwnProperty('updatedPointsBalance')) {
        this.keyStoneSyncService.setHeaderBalance({...initData.updatedPointsBalance});
        delete initData.updatedPointsBalance;
      }

      /**
       * oauth path flow
       */
      if (this.oauth && this.oauth.on) {
        const element: HTMLIFrameElement = document.getElementById(this.oauth.oauthFrame.id) as HTMLIFrameElement;
        this.oauth.iframe = element.contentWindow;

        if (initData.hasOwnProperty('OAUTH_TOKEN_SESSION_STATE')) {
          this.oauth.oauthTokenSessionState = initData.OAUTH_TOKEN_SESSION_STATE;
          this.oauth.iframe.postMessage(this.oauth.oauthClientId + ' ' + this.oauth.oauthTokenSessionState, window.location.pathname);
        }
        else {
          console.error('Error: session state token not provided in api call');
        }
      }
      // end of oauth path flow

      // NEW SESSION MGMT
      // When there is a value for keepAliveUrl run keep alive
      if (hasKeepAliveUrl) {
        this.keepAlive(initData);
      } else if (hasKeepAliveSource) {
        const keepAliveUrlSource = this.currentSessionData['keepAliveUrlSource'];
        const currentRootUrl = location.host;
        const kaRootUrl = this.getRootUrl(keepAliveUrlSource);
        const methodType = (currentRootUrl === kaRootUrl) ? 'GET' : 'JSONP';
        const keepAliveSourceInitUrl = keepAliveUrlSource + '?initial=true';

        if (methodType === 'JSONP') {
          this.http.jsonp(keepAliveSourceInitUrl, 'callback').subscribe(jsonpData => {
            this.keepAliveSourceCall(jsonpData);
          }, (error: HttpErrorResponse) => {
            this.handleError(error);
            return throwError(error);
          });
        } else {
          this.http.get(keepAliveUrlSource).subscribe((aliveData) => {
            this.keepAliveSourceCall(aliveData);
          },
            (error: HttpErrorResponse) => {
              this.handleError(error);
              // console.log('error', error);
              return throwError(error);
            });
        }
      }
      // END NEW SESSION MGMT

      // Set global var for timeout modal to false if undefined
      if (sessionStorage.getItem('isTimeoutModal') === undefined) {
        sessionStorage.setItem('isTimeoutModal', 'false');
      }
      /* Clear and Initiate sessionWarning counter */
      if (this.userStore.program.config?.sessionTimeoutWarning) {
        clearTimeout(this.sessionWarningCounter);
        clearTimeout(this.timeoutCounter);
        this.sessionWarningCounter = setTimeout(() => {
          this.showTimeoutWarning();
        }, this.userStore.program.config.sessionTimeoutWarning * 60 * 1000);
      }

    }, (error) => {
      const status = error.status;
      // Check Session Timeout using status code
      if (status === 401 || status === 0) {
        this.showTimeout();
      } // else if (status >= 500) {
      // Error loading webapp
      // }
    });
    // return this.currentSession;
  }

  keepAliveCore(url) {
    const trustedUrl = this.sanitizer.bypassSecurityTrustUrl(url).toString() + '?callback=JSON_CALLBACK';
    this.http.jsonp(trustedUrl, '').subscribe(data => {
      if (data['status'] === 'false') {
        this.sharedService.signOutInit();
        this.showTimeout();
      }
    });
  }

  receiveMessage(event) {
    // TODO handle the receive Message
    // return;
    const message = event.data;

    switch (message) {
      case 'nochange':
        break;
      case 'change':
        this.sharedService.sessionTypeAction('signOut');
        break;
      case 'error':
        console.error('Error: receiving message from oauth iframe');
        break;
      default:
        this.sharedService.sessionTypeAction('signOut');
        break;
    }
  }

  handle_error(jsonpTest: string, ERROR: string): string {
    // TODO handle the handle Error
    return '';
  }

  showTimeout() {
    // this.modalService.openTimeOutComponent();
    if (this.bootstrapModal.hasOpenModals()) {
      this.bootstrapModal.dismissAll();
    }
    this.keyStoneSyncService.dispatchLogoutSyncEvent();

    const browseModal = this.bootstrapModal.open(TimeoutComponent, {
      backdrop: 'static',
      size: 'lg',
      windowClass: 'message-modal template2 modal-theme-817 in',
      backdropClass: 'in',
      ariaLabelledBy: 'timeout-modal-title'
    });
    if (sessionStorage.getItem('partnerTimeOutUrls')) {
      this.getPartnerTimeOutUrlsFromSession();
    }
    browseModal.componentInstance.messages = this.messages;

    // TODO : to to done later
    this.matomoService.broadcast(AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.CANONICAL_PAGE, {
      payload: {
        location: location.href,
        canonicalTitle: AppConstants.analyticServices.CANONICAL_CONSTANTS.SESSION_TIMEOUT
      }
    });
  }

  showTimeoutWarning() {
    if (this.bootstrapModal.hasOpenModals()) {
      this.bootstrapModal.dismissAll();
    }
    let sessionTimeOutCounter;
    if (this.userStore.program.config.sessionTimeoutWarning) {
      sessionTimeOutCounter = (this.userStore.program.config.sessionTimeout - this.userStore.program.config.sessionTimeoutWarning) * 60 * 1000;
      this.timeoutCounter = setTimeout(() => {
        this.verifySession();
      }, sessionTimeOutCounter + 1000);
    }
    const timeoutWarnModal = this.bootstrapModal.open(TimeoutWarningModelComponent, {
      backdrop: 'static',
      size: 'lg',
      windowClass: 'message-modal modal-theme-817 template2 in',
      backdropClass: 'in',
      ariaLabelledBy: 'timeout-warning-modal-title'
    });
    timeoutWarnModal.componentInstance.sessionTimeoutRemaining = this.userStore.config['sessionTimeoutRemaining'];
    timeoutWarnModal.result.then((result) => {
      if (result){
        this.getSession();
      }else{
        this.endSession();
      }
    });
  }
  verifySession() {
    const delay = setTimeout(() => {
      this.getSession();
      clearTimeout(delay);
    }, 10000);
  }
  endSession() {
    if (sessionStorage.getItem('partnerTimeOutUrls')) {
      this.getPartnerTimeOutUrlsFromSession();
    }
    this.sharedService.signOutInit();
    clearTimeout(this.sessionWarningCounter);
    clearTimeout(this.timeoutCounter);
    this.showTimeout();
  }

  getPartnerTimeOutUrlsFromSession() {
    const sessionTimeOutUrls = sessionStorage.getItem('partnerTimeOutUrls').split(',');
    sessionTimeOutUrls.forEach((url) => {
      this.killThirdpartySession(url);
    });
    sessionStorage.removeItem('partnerTimeOutUrls');
  }

  killThirdpartySession(url) {
    const method = (location.host === this.getRootUrl(url)) ? 'GET' : 'JSONP';
    if (method === 'JSONP') {
      this.http.jsonp(url, 'callback').toPromise().then(result => {
        },
        (error: HttpErrorResponse) => {
        });
    } else {
      this.http.get(url).subscribe((getResult) => {
        },
        (error: HttpErrorResponse) => {
        });
    }
  }

  triggerPartnerSignOutUrls(){
    const storedUrls = sessionStorage.getItem('sessionURLs');
    const sessionUrls = JSON.parse(storedUrls);
    if (sessionUrls.partnerSignOutUrls){
      sessionUrls.partnerSignOutUrls.forEach((url) => {
        this.killThirdpartySession(url);
      });
    }
  }
  getRootUrl(url) {
    return (url.indexOf('://') > -1) ? url.split('/')[2] : url.split('/')[0];
  }

  postSessionURLs(sessionUrls): Observable<any> {
    const url = this.baseUrl + 'postExternalUrls';

    return this.http.post(url, sessionUrls)
      .pipe(
        map((response) => response),
        catchError((error: HttpErrorResponse) => {
          this.handleError(error);
          return throwError(error);
        })
      );
  }

  // NEW SESSION MGMT
  // initiateKeepalive(targetUrl) {
  //   this.keepAlive();
  // }

  keepAliveSourceCall(data){
    const jsonp = data;
    const jsonType = typeof jsonp;
    let jsonString;
    let jsonObj;

    if (jsonType === 'string') {
      jsonString = (jsonp as string).substring((jsonp as string).indexOf('(') + 1, (jsonp as string).lastIndexOf(')'));
      jsonObj = JSON.parse(jsonString);
    } else {
      jsonObj = jsonp;
    }
    // independent call to keepAlive
    this.keepAlive(jsonObj);
    // TODO LATER configService.postSessionURLs()
    // Sets values and posts urls to server-side
    this.postSessionURLs(jsonObj).subscribe((res) => {
      this.sessionURL = res;
      sessionStorage.setItem('sessionURLs', JSON.stringify(res));
    });
  }


  keepAlive(jsonObj) {
    // Check client session status if available
    if (jsonObj.hasOwnProperty('keepAliveUrl') && jsonObj.keepAliveUrl !== '' && this.userStore.program.config.triggerKeepAlive) {
      const httpOptions = {
        observe: 'response' as const,
        withCredentials: true
      };
      const keepAliveJSONP = (jsonObj.hasOwnProperty('keepAliveJSONP')) ? jsonObj.keepAliveJSONP : false ;
      if (keepAliveJSONP === true || keepAliveJSONP === 'true') {

        this.http.jsonp(jsonObj.keepAliveUrl, 'callback').toPromise().then(res => {
          if (res['status'] === 302) {
            this.endSession();
          } else {
            const isObj = typeof res;
            const hasStatus = isObj === 'object' ? res.hasOwnProperty('status') : false;

            if (hasStatus && res['status'] === 'failed') {
              this.endSession();
            }
          }
        },
        (error: HttpErrorResponse) => {
          this.endSession();
          this.handleError(error);
          return throwError(error);
        });
      } else {
          this.http.get(jsonObj.keepAliveUrl, httpOptions).subscribe((data) => {
          }, (error: HttpErrorResponse) => {
          });
      }
    }
  }

}
