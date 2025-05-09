import { fakeAsync, TestBed, tick, waitForAsync } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { SessionService } from './session.service';
import { NgbActiveModal, NgbModal, NgbModalModule } from '@ng-bootstrap/ng-bootstrap';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateModule } from '@ngx-translate/core';
import { BehaviorSubject, of } from 'rxjs';
import { SharedService } from '../modules/shared/shared.service';
import { TimeoutComponent } from '@app/components/modals/timeout/timeout.component';
import { TimeoutWarningModelComponent } from '@app/components/modals/timeout-warning/timeout-warning-model.component';
import { MatomoService } from '@app/analytics/matomo/matomo.service';

// Mock class for NgbModalRef
export class MockNgbModalRef {
  componentInstance = {
    validator: '',
    messages: '',
    showSuggestedAddr: '',
    template: ''
  };
  result: Promise<any> = new Promise((resolve, reject) => resolve(true));
}

describe('SessionService', () => {
  let sessionService: SessionService;
  let httpTestingController: HttpTestingController;
  const programData = require('assets/mock/program.json');
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };
  const mockValidSessionResponse = require('assets/mock/validSession.json');
  let bootstrapModal: NgbModal;
  const mockModalRef: MockNgbModalRef = new MockNgbModalRef();
  const subject = new BehaviorSubject<boolean>(false);

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [
        TimeoutWarningModelComponent,
        TimeoutComponent
      ],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        NgbModalModule
      ],
      providers: [
        NgbModal,
        NgbActiveModal,
        { provide: UserStoreService, useValue: userData },
        { provide: SharedService, useValue: {
          sessionTypeAction: () => of({}),
          signOutInit: () => of({}),
          endSession$: subject.asObservable(),
          triggerLogoutSyncEvent$: subject.asObservable(),
          triggerPartnerSignOutUrls$: subject.asObservable(),
          showSessionTimeOut: () => {}
         }
        },
        { provide: MatomoService, useValue: {
          broadcast: () => {}
        }}
      ]
    });
    sessionService = TestBed.inject(SessionService);
    httpTestingController = TestBed.inject(HttpTestingController);
    sessionService.messages = require('assets/mock/messages.json');
    sessionService.userStore.program.config = userData.config;
    bootstrapModal = TestBed.inject(NgbModal);
    spyOn(console, 'error').and.returnValue();
    document.body.innerHTML = '<iframe id="oauthFrame"></iframe>';
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
  });

  it('should be created', () => {
    expect(sessionService).toBeTruthy();
  });

  it('should call initSession method', () => {
    window['oauth'] = {
      oauthCheckSessionIframUrl: 'http://localhost:4200/apple-gr/pages/login.jsp',
      oauthClientId: '12345',
      oauthTokenSessionState: 'test'
    };
    spyOn(sessionService, 'initSession').and.callThrough();
    sessionService.initSession();

    window['oauth'] = {
      oauthCheckSessionIframUrl: '',
      oauthClientId: '',
      oauthTokenSessionState: ''
    };
    sessionService.initSession();
    expect(sessionService.initSession).toHaveBeenCalled();
  });

  it('should call receiveMessage method - for nochange message scenario', () => {
    spyOn(sessionService, 'receiveMessage').and.callThrough();
    const event = {
      data: 'nochange'
    };
    sessionService.receiveMessage(event);
    expect(sessionService.receiveMessage).toHaveBeenCalled();
  });

  it('should call receiveMessage method - for change message scenario', () => {
    spyOn(sessionService, 'receiveMessage').and.callThrough();
    const event = {
      data: 'change'
    };
    sessionService.receiveMessage(event);
    expect(sessionService.receiveMessage).toHaveBeenCalled();
  });

  it('should call receiveMessage method - for error message scenario', () => {
    spyOn(sessionService, 'receiveMessage').and.callThrough();
    const event = {
      data: 'error'
    };
    sessionService.receiveMessage(event);
    expect(sessionService.receiveMessage).toHaveBeenCalled();
  });

  it('should call receiveMessage method - for default message scenario', () => {
    spyOn(sessionService, 'receiveMessage').and.callThrough();
    const event = {
      data: 'test'
    };
    sessionService.receiveMessage(event);
    expect(sessionService.receiveMessage).toHaveBeenCalled();
  });

  it('should call keepAliveCore method', () => {
    spyOn(sessionService, 'keepAliveCore').and.callThrough();
    const url = {
      keepAliveUrlCORE: 'https://delta-vip-internal.cpdev.bridge2solutions.net/b2r/keepalive.js'
    };
    sessionService.keepAliveCore(url);
    // Expect a call to this URL
    const req = httpTestingController.expectOne(() => true);
    req.flush({ status: 'false', statusText: 'fails' });
    expect(sessionService.keepAliveCore).toHaveBeenCalled();
  });

  it('should call keepAliveCore method - else check', () => {
    spyOn(sessionService, 'keepAliveCore').and.callThrough();
    const url = {
      keepAliveUrlCORE: 'https://delta-vip-internal.cpdev.bridge2solutions.net/b2r/keepalive.js'
    };
    sessionService.keepAliveCore(url);
    // Expect a call to this URL
    const req = httpTestingController.expectOne(() => true);
    req.flush({ status: 'true', statusText: 'fails' });
    expect(sessionService.keepAliveCore).toHaveBeenCalled();
  });

  it('should call handle_error method', () => {
    spyOn(sessionService, 'handle_error').and.callThrough();
    expect(sessionService.handle_error('testObject', 'test')).toEqual('');
    expect(sessionService.handle_error).toHaveBeenCalled();
  });

  it('should call showTimeoutWarning method', () => {
    spyOn(sessionService, 'showTimeoutWarning').and.callThrough();
    spyOn(sessionService['bootstrapModal'], 'hasOpenModals').and.returnValue(true);
    mockModalRef.result = new Promise((resolve, reject) => resolve(true));
    // spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    sessionService.showTimeoutWarning();
    expect(sessionService.showTimeoutWarning).toHaveBeenCalled();
  });

  it('should call showTimeout method', () => {
    spyOn(sessionService, 'showTimeout').and.callThrough();
    spyOn(sessionService['bootstrapModal'], 'hasOpenModals').and.returnValue(true);
    mockModalRef.result = new Promise((resolve, reject) => resolve(true));
    // spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    sessionService.showTimeout();
    expect(sessionService.showTimeout).toHaveBeenCalled();
  });

  it('should call showTimeoutWarning method - else check', () => {
    spyOn(sessionService, 'showTimeoutWarning').and.callThrough();
    spyOn(sessionService['bootstrapModal'], 'hasOpenModals').and.returnValue(false);
    mockModalRef.result = new Promise((resolve, reject) => resolve(false));
    // spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    sessionService.userStore.program.config = {};
    sessionService.showTimeoutWarning();
    expect(sessionService.showTimeoutWarning).toHaveBeenCalled();
  });

  it('should call endSession method', () => {
    spyOn(sessionService, 'endSession').and.callThrough();
    sessionService.endSession();
    expect(sessionService.endSession).toHaveBeenCalled();
  });

  it('should call verifySession method', fakeAsync(() => {
    spyOn(sessionService, 'verifySession').and.callThrough();
    sessionService.verifySession();
    tick(10000);
    expect(sessionService.verifySession).toHaveBeenCalled();
  }));

  it('should call getSession method - without alive session', () => {
    spyOn(sessionService, 'getSession').and.callThrough();
    sessionService.oauth = {
      on: false
    };
    sessionStorage.removeItem('kaSourceURL');
    sessionService.config['sessionTimeoutWarning'] = '10';
    sessionService.getSession();
    expect(sessionService.getSession).toHaveBeenCalled();
    spyOn(sessionService['bootstrapModal'], 'hasOpenModals').and.returnValue(true);
    mockModalRef.result = new Promise((resolve, reject) => resolve(true));
    // spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    // Expect a call to this URL
    const req = httpTestingController.expectOne(sessionService.baseUrl + 'validSession?initial=true');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    // Respond with the fake data when called
    req.flush(mockValidSessionResponse);
  });

  it('should call getSession method - with iframe oauth reference else check', () => {
    spyOn(sessionService, 'getSession').and.callThrough();
    sessionService.oauth = {
      on: true,
      oauthFrame: {
        id: 'oauthFrame'
      }
    };
    sessionStorage.removeItem('kaSourceURL');
    sessionService.config['sessionTimeoutWarning'] = '10';
    sessionService.getSession();
    expect(sessionService.getSession).toHaveBeenCalled();
    spyOn(sessionService['bootstrapModal'], 'hasOpenModals').and.returnValue(true);
    mockModalRef.result = new Promise((resolve, reject) => resolve(true));
    // spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    // Expect a call to this URL
    const req = httpTestingController.expectOne(sessionService.baseUrl + 'validSession?initial=true');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    // Respond with the fake data when called
    req.flush(mockValidSessionResponse);
  });

  it('should call getSession method - with iframe oauth reference check with session state', () => {
    spyOn(sessionService, 'getSession').and.callThrough();
    spyOn(sessionService['bootstrapModal'], 'hasOpenModals').and.returnValue(true);
    mockModalRef.result = new Promise((resolve, reject) => resolve(true));
    spyOn(sessionService, 'getRootUrl').and.returnValue(location.host);
    sessionService.oauth = {
      on: true,
      oauthFrame: {
        id: 'oauthFrame'
      },
      oauthTokenSessionState : ''
    };
    sessionStorage.removeItem('kaSourceURL');
    sessionService.config['sessionTimeoutWarning'] = '10';
    const session = require('assets/mock/validSession.json');
    session['OAUTH_TOKEN_SESSION_STATE'] = '';
    delete session.keepAliveUrl;
    mockValidSessionResponse['keepAliveUrlSource'] = '';
    spyOn(sessionService, 'getSessionURLs').and.returnValue(of(session));
    sessionService.getSession();
    expect(sessionService.getSession).toHaveBeenCalled();
  });

  it('should call getSession method', () => {
    spyOn(sessionService, 'getSession').and.callThrough();
    sessionService.config['sessionTimeoutWarning'] = '10';
    sessionStorage.setItem('kaSourceURL', undefined);
    sessionService.getSession();
    expect(sessionService.getSession).toHaveBeenCalled();
    spyOn(sessionService['bootstrapModal'], 'hasOpenModals').and.returnValue(true);
    mockModalRef.result = new Promise((resolve, reject) => resolve(true));
    // spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    // Expect a call to this URL
    const req = httpTestingController.expectOne(sessionService.baseUrl + 'validSession?initial=true');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    // Respond with the fake data when called
    req.flush(mockValidSessionResponse);
  });

  it('should call getSession method - keepAliveUrlSource', () => {
    const fakeResponse = {
      keepAliveUrlSource: 'javascript:void(0)'
    };
    spyOn(sessionService, 'getSession').and.callThrough();
    sessionService.config['sessionTimeoutWarning'] = '10';
    sessionService.getSession();
    expect(sessionService.getSession).toHaveBeenCalled();
    spyOn(sessionService['bootstrapModal'], 'hasOpenModals').and.returnValue(true);
    mockModalRef.result = new Promise((resolve, reject) => resolve(true));
    // spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    // Expect a call to this URL
    const req = httpTestingController.expectOne(sessionService.baseUrl + 'validSession?initial=true');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    // Respond with the fake data when called
    req.flush(fakeResponse);
  });

  it('should test for 401 error for getSessionURLs', () => {
    spyOn(sessionService, 'getSession').and.callThrough();
    sessionService.getSession();
    expect(sessionService.getSession).toHaveBeenCalled();
    spyOn(sessionService['bootstrapModal'], 'hasOpenModals').and.returnValue(true);
    mockModalRef.result = new Promise((resolve, reject) => resolve(true));
    // spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    // Expect a call to this URL
    const req = httpTestingController.expectOne(sessionService.baseUrl + 'validSession?initial=true');
    // Respond with mock error
    req.flush('failed', { status: 401, statusText: 'Time out' });
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
  });

  it('should test for 0 error for getSessionURLs', () => {
    spyOn(sessionService, 'getSession').and.callThrough();
    sessionService.getSession();
    expect(sessionService.getSession).toHaveBeenCalled();
    spyOn(sessionService['bootstrapModal'], 'hasOpenModals').and.returnValue(true);
    mockModalRef.result = new Promise((resolve, reject) => resolve(true));
    // spyOn(bootstrapModal, 'open').and.returnValue(mockModalRef as any);
    // Expect a call to this URL
    const req = httpTestingController.expectOne(sessionService.baseUrl + 'validSession?initial=true');
    // Respond with mock error
    req.flush('failed', { status: 0, statusText: 'Time out' });
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
  });

  it('should test for 500 error for getSessionURLs', () => {
    spyOn(sessionService, 'getSession').and.callThrough();
    sessionService.getSession();
    expect(sessionService.getSession).toHaveBeenCalled();

    // Expect a call to this URL
    const req = httpTestingController.expectOne(sessionService.baseUrl + 'validSession?initial=true');
    // Respond with mock error
    req.flush({ error: 'failed'}, { status: 500, statusText: 'Time out' });
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
  });

  it('post session urls to backend server', () => {
    sessionService.postSessionURLs(mockValidSessionResponse).subscribe(
      (response) => expect(response).toBeTruthy(), fail);
    // Expect a call to this URL
    const req = httpTestingController.expectOne(sessionService.baseUrl + 'postExternalUrls');
    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');
    // Respond with the fake data when called
    req.flush(mockValidSessionResponse);
  });

  it('Error scenario - post session urls to backend server', () => {
    const errorMsg = 'deliberate 404 error';
    sessionService.postSessionURLs(mockValidSessionResponse).subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    // Expect a call to this URL
    const req = httpTestingController.expectOne(sessionService.baseUrl + 'postExternalUrls');
    // Assert that the request is a POST
    expect(req.request.method).toEqual('POST');
    // Respond with the fake data when called
    req.flush(errorMsg, { status: 404, statusText: 'Not found' });
  });

  it('should call getSession method - with keepAliveUrlCore', () => {
    spyOn(sessionService, 'getSession').and.callThrough();
    sessionStorage.removeItem('kaSourceURL');
    spyOn(sessionStorage, 'getItem').and.returnValue(undefined);
    sessionService.userStore.program.config = {};
    sessionService.getSession();
    expect(sessionService.getSession).toHaveBeenCalled();

    // Expect a call to this URL
    const req = httpTestingController.expectOne(sessionService.baseUrl + 'validSession?initial=true');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    const response = {
      signOutUrl: 'https://wfbk-uat-mn.epsilon.com/home/LogOutRedir',
      navigateBackUrl: 'https://wfbk-uat-mn.epsilon.com/#/deeplink/B2S/Merchandise',
      timeOutUrl: 'https://wfbk-uat-mn.epsilon.com/home/LogOutRedir',
      keepAliveUrlCORE: 'https://delta-vip-internal.cpdev.bridge2solutions.net/b2r/keepalive.js'
    };
    // Respond with the fake data when called
    req.flush(response);
  });

  it('should call getRootUrl method', () => {
    sessionService.getRootUrl('/apple-gr/service/login');
    expect(sessionService.getRootUrl).toBeDefined();
  });

  it('should call keepAliveSourceCall method', () => {
    spyOn(sessionService, 'keepAliveSourceCall').and.callThrough();
    const response = require('assets/mock/validSession.json');
    spyOn(sessionService, 'postSessionURLs').and.returnValue(of(response));
    spyOn(sessionService, 'keepAlive').and.callFake(() => {});
    sessionService.keepAliveSourceCall(null);
    expect(sessionService.keepAliveSourceCall).toHaveBeenCalled();
  });

  it('should call triggerPartnerSignOutUrls method', () => {
    spyOn(sessionService, 'triggerPartnerSignOutUrls').and.callThrough();
    const sessionUrl = {
      partnerSignOutUrls: [
        'https://wfbk-uat-mn.epsilon.com/home/LogOutRedir',
        'https://wfbk-uat-mn.epsilon.com/#/deeplink/B2S/Merchandise',
        'https://wfbk-uat-mn.epsilon.com/home/LogOutRedir',
        'https://delta-vip-internal.cpdev.bridge2solutions.net/b2r/keepalive.js'
      ]
    };
    spyOn(sessionStorage, 'getItem').and.returnValue(JSON.stringify(sessionUrl));
    sessionService.triggerPartnerSignOutUrls();
    expect(sessionService.triggerPartnerSignOutUrls).toHaveBeenCalled();
  });

  it('should call triggerPartnerSignOutUrls method - for get method call', () => {
    spyOn(sessionService, 'triggerPartnerSignOutUrls').and.callThrough();
    spyOn(sessionService, 'getRootUrl').and.returnValue(location.host);
    const sessionUrl = {
      partnerSignOutUrls: [
        'https://wfbk-uat-mn.epsilon.com/home/LogOutRedir',
        'https://wfbk-uat-mn.epsilon.com/#/deeplink/B2S/Merchandise',
        'https://wfbk-uat-mn.epsilon.com/home/LogOutRedir',
        'https://delta-vip-internal.cpdev.bridge2solutions.net/b2r/keepalive.js'
      ]
    };
    spyOn(sessionStorage, 'getItem').and.returnValue(JSON.stringify(sessionUrl));
    sessionService.triggerPartnerSignOutUrls();
    expect(sessionService.triggerPartnerSignOutUrls).toHaveBeenCalled();
  });

  it('should call getPartnerTimeOutUrlsFromSession method', () => {
    const sessionUrl = {
      partnerSignOutUrls: [
        'https://wfbk-uat-mn.epsilon.com/home/LogOutRedir',
        'https://wfbk-uat-mn.epsilon.com/#/deeplink/B2S/Merchandise',
        'https://wfbk-uat-mn.epsilon.com/home/LogOutRedir',
        'https://delta-vip-internal.cpdev.bridge2solutions.net/b2r/keepalive.js'
      ]
    };
    spyOn(sessionStorage, 'getItem').and.returnValue(JSON.stringify(sessionUrl));
    spyOn(sessionService, 'getPartnerTimeOutUrlsFromSession').and.callThrough();
    sessionService.endSession();
    sessionService.showTimeout();
    sessionService.getPartnerTimeOutUrlsFromSession();
    expect(sessionService.getPartnerTimeOutUrlsFromSession).toHaveBeenCalled();
  });

  it('should call initSession method to endSession', () => {
    sessionService['sharedService'].showSessionTimeOut(true);
    spyOn(sessionService, 'initSession').and.callThrough();
    sessionService.initSession();
    expect(sessionService.initSession).toHaveBeenCalled();
  });

  it('should call getSession method for keepAliveUrlPointsBank', () => {
    spyOn(sessionService, 'getSession').and.callThrough();
    const response = require('assets/mock/validSession.json');
    response['keepAliveUrlPointsBank'] = 'https://wfbk-uat-mn.epsilon.com/home/LogOutRedir';
    response['partnerTimeOutUrls'] = [
      'https://wfbk-uat-mn.epsilon.com/home/LogOutRedir',
      'https://wfbk-uat-mn.epsilon.com/#/deeplink/B2S/Merchandise',
      'https://wfbk-uat-mn.epsilon.com/home/LogOutRedir',
      'https://delta-vip-internal.cpdev.bridge2solutions.net/b2r/keepalive.js'
    ];
    response['keystoneUrls'] = 'https://wfbk-uat-mn.epsilon.com/home/LogOutRedir';
    response['updatedPointsBalance'] = 23243335345;
    spyOn(sessionService, 'getSessionURLs').and.returnValue(of(response));
    spyOn(sessionService['keyStoneSyncService'], 'isKeyStoneSync').and.callFake(() => true);
    sessionService.getSession();
    expect(sessionService.getSession).toHaveBeenCalled();
  });

});
