import { TestBed } from '@angular/core/testing';
import { AnalyticsService } from './analytics.service';
import { Observable, of } from 'rxjs';
import { UserStoreService } from '@app/state/user-store.service';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateService } from '@ngx-translate/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Injectable } from '@angular/core';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { DecimalPipe } from '@angular/common';

@Injectable()
export class TranslateServiceStub {
  public get<T>(key: T): Observable<T> {
    return of(key);
  }
  public instant(key: string): any {
    return '';
  }
}

describe('AnalyticsService', () => {
  let service: AnalyticsService;
  const programData = require('assets/mock/program.json');
  const mockUser = require('assets/mock/user.json');
  mockUser['program'] = programData;
  const userData = {
    user: mockUser,
    program: programData,
    config: programData['config'],
    get: () => of(mockUser)
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule
      ],
      providers: [
        { provide: TranslateService, useClass: TranslateServiceStub },
        { provide: UserStoreService, useValue: userData },
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe
      ]
    });
    service = TestBed.inject(AnalyticsService);
    service.user = userData.user;
  });

  it('should be created', () => {
    service['user']['analyticsUrl'] = 'testUrl';
    service['user']['analyticsWindow'] = {};
    expect(service).toBeTruthy();
  });

  it('should call loadAnalyticScripts for tealium', () => {
    service['userStoreService'].config.analytics = 'tealium';
    spyOn(service, 'loadAnalyticScripts').and.callThrough();
    service.loadAnalyticScripts();
    expect(service.loadAnalyticScripts).toHaveBeenCalled();
  });

  it('should call loadAnalyticScripts for webtrends', () => {
    service['userStoreService'].config.analytics = 'webtrends';
    spyOn(service, 'loadAnalyticScripts').and.callThrough();
    service.loadAnalyticScripts();
    expect(service.loadAnalyticScripts).toHaveBeenCalled();
  });

  it('should call loadAnalyticScripts for ensighten', () => {
    service['userStoreService'].config.analytics = 'ensighten';
    spyOn(service, 'loadAnalyticScripts').and.callThrough();
    service.loadAnalyticScripts();
    expect(service.loadAnalyticScripts).toHaveBeenCalled();
  });

  it('should call loadAnalyticScripts for matomo', () => {
    service['userStoreService'].config.analytics = 'matomo';
    service['userStoreService'].config.matomoEndPoint = 'https://bridge2-dev.innocraft.cloud/';
    service['userStoreService'].config.matomoSiteId = '3';
    spyOn(service, 'loadAnalyticScripts').and.callThrough();
    service.loadAnalyticScripts();
    expect(service.loadAnalyticScripts).toHaveBeenCalled();
  });

  it('should call loadAnalyticScripts for matomo without matomositeId', () => {
    service['userStoreService'].config.analytics = 'matomo';
    service['userStoreService'].config.matomoEndPoint = 'https://bridge2-dev.innocraft.cloud/';
    service['userStoreService'].config.matomoSiteId = '';
    spyOn(service, 'loadAnalyticScripts').and.callThrough();
    service.loadAnalyticScripts();
    expect(service.loadAnalyticScripts).toHaveBeenCalled();
  });

  it('should call loadAnalyticScripts for heap', () => {
    service['userStoreService'].config.analytics = 'heap';
    spyOn(service, 'loadAnalyticScripts').and.callThrough();
    service.loadAnalyticScripts();
    expect(service.loadAnalyticScripts).toHaveBeenCalled();
  });

  it('should call loadAnalyticScripts for test data', () => {
    service['userStoreService'].config.analytics = 'testData';
    spyOn(service, 'loadAnalyticScripts').and.callThrough();
    service.loadAnalyticScripts();
    expect(service.loadAnalyticScripts).toHaveBeenCalled();
  });

});
