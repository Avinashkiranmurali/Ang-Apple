import { Injectable } from '@angular/core';
import { UserStoreService } from '@app/state/user-store.service';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { SharedService } from '@app/modules/shared/shared.service';
import { BaseService } from '@app/services/base.service';
import { Messages } from '@app/models/messages';
import { User } from '@app/models/user';
import { Config } from '@app/models/config';
import { catchError, map } from 'rxjs/operators';
import { BehaviorSubject, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class KeyStoneSyncService extends BaseService {
  initKeyStoneUrls = false;
  // eslint-disable-next-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match
  _keyStoneUrls = new BehaviorSubject<object>({});
  readonly keyStoneUrls$ = this._keyStoneUrls.asObservable();
  messages: Messages;
  user: User;
  config: Config;
  errorMsg;
  eventsMap = [
    {eName: 'setPointsBalanceSync', eFunc: this.setPointsBalanceSyncListener.bind(this)},
    // {eName: 'setHeaderPointsBalance', eFunc: this.setHeaderPointsBalanceListener.bind(this)},
    {eName: 'keepAliveSync', eFunc: this.keepAliveSyncListener.bind(this)},
    {eName: 'logoutSync', eFunc: this.logoutSyncListener.bind(this)}
  ];
  constructor(public userStore: UserStoreService,
              private messagesStore: MessagesStoreService,
              private http: HttpClient,
              private sharedService: SharedService
              ) {
    super();
    this.messages = this.messagesStore.messages;
    this.user = this.userStore.user;
    this.config = this.userStore.config;
    window['keyStoneSyncEvents'] = window['keyStoneSyncEvents'] || {};
    for (const eventMap of this.eventsMap) {
      if (!window['keyStoneSyncEvents'][eventMap['eName']]) {
        window.addEventListener(eventMap['eName'], eventMap['eFunc']);
        window['keyStoneSyncEvents'][eventMap['eName']] = eventMap['eName'];
      }
    }

    sharedService.triggerLogoutSyncEvent$.subscribe(() => {
      this.dispatchLogoutSyncEvent();
    });
  }
  setKeyStoneUrl(url) {
    this._keyStoneUrls.next(url);
  }

  get keyStoneUrls() {
    return this._keyStoneUrls.value;
  }

  isKeyStoneSync(key) {
    if (Object.keys(this.keyStoneUrls).length > 0 && this.keyStoneUrls[key]) {
      return Object.keys(this.keyStoneUrls[key]).length > 0 ;
    }
  }

  /******************************************************************************
   **** Keystone Point Balance Sync  ******************************************
   ******************************************************************************/

  postPointsToApps(appCtx, detail) {
    const httpOptions = {
      observe: 'response' as const,
      withCredentials: true
    };
    const balanceUpdateToken = detail.balanceUpdateToken.token;
    return this.http.post(appCtx, balanceUpdateToken, httpOptions)
      .pipe(
        map((response) => response),
        catchError((error: HttpErrorResponse) => {
          this.handleError(error);
          return throwError(error);
        })
      );
  }

  setBalance(detail) {
    if (detail.balanceUpdateToken){

      for (const url of this.keyStoneUrls['balanceUpdate']) {
        this.postPointsToApps(url, detail).subscribe(balData => {
          // simply subsribed for call the the balance api
        });
      }
    }
  }

  setPointsBalanceSyncListener(e){
    if (e.detail) {
      this.setBalance(e.detail);
    }
  }

  setHeaderBalance(points){
    // if points redecued then set the point details in the page.
    // set config.pointsBalance
    this.config.pointsBalance = points;
    // set $rootScope.points
   // $rootScope.points =  points;// TODO later

    // cartCtrl //checkoutCtrl
    // $scope.pointsAvailable =
    return true;
  }

  PointsBalanceSyncEventDispatch(){
    const url = this.baseUrl + 'participant/balanceToken';
    let pointsSyncEvent;
    return this.http.get(url, this.httpOptions)
        .pipe(
          map((response) => {
            if (response){
              pointsSyncEvent = new CustomEvent('setPointsBalanceSync', {
                detail: {
                  balanceUpdateToken: response
                }
              });
            }
            window.dispatchEvent(pointsSyncEvent);
            return response;
          }),
          catchError((error: HttpErrorResponse) =>
            // if (error.status !== 401 || error.status !== 0) {
            //   // log to server-side
            //   this.errorMsg = 'Error: program REST service failed to GET program data';
            //   // errorLogService(status, $rootScope.errorMsg); // TODO to be clarified with vinoth
            // }
            // this.handleError(error);
             throwError(error)
          )
        );
  }

  /******************************************************************************
   **** End Keystone Point Balance Sync  *************************************
   ******************************************************************************/


  /******************************************************************************
   **** Keystone keepalive Sync  **********************************************
   ******************************************************************************/

  keepaliveSync(context) {
    const url = context.split('?')[0];
    // const url = context;
    // return $http.jsonp(url);
    this.http.jsonp(url, 'callback').subscribe((data) => {
      // console.log('url', url);
      // console.log('whats up', data);
    }, (error: HttpErrorResponse) => {
      if (url.indexOf('b2r/keepalive') >= 0) {
        // only for b2r/keepalive js
        this.sharedService.showSessionTimeOut(true);
        this.handleError(error);
        throwError(error);
      }
    });
    // this.detailService.getDetails('30001MXG32LL/A').subscribe((data) => {
    //   console.log('result', data)
    // });
    // console.log('result', data)
  }

  keepAliveSyncListener(){
    if (this.isKeyStoneSync('keepAlive')) {
      for (const url of this.keyStoneUrls['keepAlive']) {
       this.keepaliveSync(url);
      }
    }

  }
  dispatchKeepAliveSyncEvent() {
    window.dispatchEvent(new CustomEvent('keepAliveSync'));
  }
  /******************************************************************************
   **** End Keystone keepalive Sync  ******************************************
   ******************************************************************************/

  /******************************************************************************
   **** Keystone Logout Sync  ************************************************
   ******************************************************************************/

  logoutSyncListener(e) {
    if (this.isKeyStoneSync('logout')) {
      for (const url of this.keyStoneUrls['logout']) {
       this.postLogoutToApps(url);
      }
    }
  }

  postLogoutToApps(url) {
    // $http.jsonp(url);
   this.http.jsonp(url, 'callback').subscribe((data) => {
    // console.log('url', url);
    // console.log('whats up', data);
  }, (error: HttpErrorResponse) => {
    this.handleError(error);
    throwError(error);
  });
  }

  dispatchLogoutSyncEvent() {
    window.dispatchEvent(new CustomEvent('logoutSync'));
  }

  /******************************************************************************
   **** End Keystone Logout Sync  ********************************************
   ******************************************************************************/


  /**
   * this prevents duplicate event listeners from being created when angular re-renders template on scope change
   * add your events to this list so the events are only set once
   */
}
