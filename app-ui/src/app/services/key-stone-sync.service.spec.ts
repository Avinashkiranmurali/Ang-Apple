import { DecimalPipe } from '@angular/common';
import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { KeyStoneSyncService } from './key-stone-sync.service';
import { RouterTestingModule } from '@angular/router/testing';
import { SharedService } from '@app/modules/shared/shared.service';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';

describe('KeyStoneSyncService', () => {
  let service: KeyStoneSyncService;
  let sharedService: SharedService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        [
          HttpClientTestingModule,
          RouterTestingModule
        ]
      ],
      providers: [
        { provide: CurrencyFormatPipe },
        { provide: CurrencyPipe },
        { provide: DecimalPipe }
      ]
    });
    service = TestBed.inject(KeyStoneSyncService);
    sharedService = TestBed.inject(SharedService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call setKeyStoneUrl method', () => {
    spyOn(service, 'isKeyStoneSync').and.callThrough();
    const keyStoneUrls = {
      keepAlive: 'https://'
    };
    service.setKeyStoneUrl(keyStoneUrls);
    expect(service.isKeyStoneSync('keepAlive')).toBeTruthy();
    expect(service.isKeyStoneSync).toHaveBeenCalled();
  });

  it('should call dispatchLogoutSyncEvent method', () => {
    spyOn(service, 'dispatchLogoutSyncEvent');
    service.dispatchLogoutSyncEvent();
    expect(service.dispatchLogoutSyncEvent).toHaveBeenCalled();
  });

  it('should call setHeaderBalance method', () => {
    spyOn(service, 'setHeaderBalance').withArgs({points: 99999}).and.callThrough();
    service.setHeaderBalance({points: 99999});
    expect(service.setHeaderBalance).toHaveBeenCalled();
  });

  it('should call keepAliveSyncListener method', () => {
    spyOn(service, 'keepAliveSyncListener').and.callThrough();
    const keyStoneUrls = {
      keepAlive: 'https://',
      logout: 'logoutURL'
    };
    service.setKeyStoneUrl(keyStoneUrls);
    spyOn(service, 'isKeyStoneSync').and.callFake(() => true);
    service.keepAliveSyncListener();
    expect(service.keepAliveSyncListener).toHaveBeenCalled();
  });

  it('should dispatchLogoutSyncEvent be defined', fakeAsync(() => {
    spyOn(sharedService, 'triggerLogoutSyncEvent');
    expect(sharedService.triggerLogoutSyncEvent).toBeTruthy();
    tick(1);
    service.dispatchLogoutSyncEvent();
    expect(service.dispatchLogoutSyncEvent).toBeDefined();
  }));

  it('should call postPointsToApps method - success response', () => {
    spyOn(service, 'postPointsToApps').and.callThrough();
    spyOn(service['http'], 'post').and.returnValue(of({}));
    service.postPointsToApps('test', {balanceUpdateToken: { token: 'testData'}});
    service.postPointsToApps('test', {balanceUpdateToken: { token: 'testData'}}).subscribe(
      (response) => expect(response).toEqual({} as HttpResponse<{}>, 'should return fakeResponse'), fail
    );
    expect(service.postPointsToApps).toHaveBeenCalled();
  });

  it('should call setBalance method', () => {
    spyOn(service, 'setBalance').withArgs({balanceUpdateToken: { token: 'testData'}}).and.callThrough();
    spyOn(service['http'], 'post').and.returnValue(of({}));
    const keyStoneUrls = {
      balanceUpdate: 'testData'
    };
    const url = 'https://';
    service.setKeyStoneUrl(keyStoneUrls);
    const resData = {};
    service.setBalance({balanceUpdateToken: { token: 'testData'}});
    service.postPointsToApps(url, {balanceUpdateToken: { token: 'testData'}}).subscribe( balData => {
      expect(balData).toBeTruthy();
    });
    expect(service.setBalance).toHaveBeenCalled();
  });

  it('should call postPointsToApps method - failure case', () => {
    spyOn(service, 'postPointsToApps').and.callThrough();
    spyOn(service['http'], 'post').and.returnValue(throwError({status: 404, statusText: 'Not Found'}));
    service.postPointsToApps('test', {balanceUpdateToken: { token: 'testData'}});
    service.postPointsToApps('test', {balanceUpdateToken: { token: 'testData'}}).subscribe(
      data => fail,
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(undefined, 'message');
      }
    );
    expect(service.postPointsToApps).toHaveBeenCalled();
  });

  it('should call dispatchKeepAliveSyncEvent method', () => {
    spyOn(service, 'dispatchKeepAliveSyncEvent').and.callThrough();
    service.dispatchKeepAliveSyncEvent();
    expect(service.dispatchKeepAliveSyncEvent).toHaveBeenCalled();
  });

  it('should call logoutSyncListener method', () => {
    spyOn(service, 'logoutSyncListener').and.callThrough();
    const keyStoneUrls = {
      keepAlive: 'https://',
      logout: 'logoutURL'
    };
    spyOn(service, 'isKeyStoneSync').and.callFake(() => true);
    service.setKeyStoneUrl(keyStoneUrls);
    service.logoutSyncListener(keyStoneUrls);
    expect(service.logoutSyncListener).toHaveBeenCalled();
  });

  it('should call setPointsBalanceSyncListener method', () => {
    spyOn(service, 'setPointsBalanceSyncListener').and.callThrough();
    const keyStoneUrls = {
      balanceUpdate: 'testData',
      keepAlive: 'https://',
      logout: 'logoutURL'
    };
    service.setKeyStoneUrl(keyStoneUrls);
    spyOn(service, 'postPointsToApps').and.callFake(() => of(null));
    service.setPointsBalanceSyncListener({ detail: {balanceUpdateToken: { token: 'testData'}} });
    expect(service.setPointsBalanceSyncListener).toHaveBeenCalled();
  });

  it('should call keepaliveSync method', () => {
    spyOn(service, 'keepaliveSync').withArgs( 'https://b2r/keepalive.com?a=1&b=2').and.callThrough();
    service.keepaliveSync('https://b2r/keepalive.com?a=1&b=2');
    const url = 'https://b2r/keepalive.com';
    expect(service.keepaliveSync).toHaveBeenCalled();
  });

  it('should call PointsBalanceSyncEventDispatch method - success response', () => {
    spyOn(service, 'PointsBalanceSyncEventDispatch').withArgs().and.callThrough();
    service.PointsBalanceSyncEventDispatch().subscribe( response => {
      expect(response).toBeTruthy();
    });
    expect(service.PointsBalanceSyncEventDispatch).toHaveBeenCalled();
  });

  it('should call PointsBalanceSyncEventDispatch method - failure case', () => {
    spyOn(service, 'PointsBalanceSyncEventDispatch').and.callThrough();
    spyOn(service['http'], 'get').and.returnValue(throwError({status: 404, statusText: 'Not Found'}));
    service.PointsBalanceSyncEventDispatch();
    service.PointsBalanceSyncEventDispatch().subscribe(
      data => fail,
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(undefined, 'message');
      }
    );
    expect(service.PointsBalanceSyncEventDispatch).toHaveBeenCalled();
  });

});
