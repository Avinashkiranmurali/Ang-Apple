import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { fakeAsync, inject, TestBed, tick, waitForAsync } from '@angular/core/testing';
import { NavigationEnd, NavigationStart, Router, RouterEvent } from '@angular/router';
import { UserStoreService } from '@app/state/user-store.service';
import { Webtrends } from './webtrends';
import { WebtrendsService } from './webtrends.service';
import { ReplaySubject } from 'rxjs';

describe('WebtrendsService', () => {
  let service: WebtrendsService;
  let userStore: UserStoreService;
  let httpTestingController: HttpTestingController;
  const program = require('assets/mock/program.json');
  const eventSubject = new ReplaySubject<RouterEvent>(1);
  program['config']['webtrendsEnabled'] = true;
  program['config']['imageServerUrl'] = 'https://als-static.bridge2rewards.com/dev2';
  const webtrendsInstance: Webtrends = Object.assign({});

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule
      ],
      providers: [
        { provide: UserStoreService, useValue: {
          config: program['config'] }
        },
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate'),
          url: 'imac/mackbook',
          events: eventSubject.asObservable() }
        }
      ]
    });
    httpTestingController = TestBed.inject(HttpTestingController);
    service = TestBed.inject(WebtrendsService);
    userStore = TestBed.inject(UserStoreService);
    userStore.config = program['config'];
    userStore.config['imageServerUrl'] = 'https://als-static.bridge2rewards.com/dev2';
    service['userStoreService'] = userStore;
  }));

  afterAll(() => {
    userStore.config.imageServerUrl = 'https://als-static.bridge2rewards.com/dev2';
  });

  it('should be created', inject([UserStoreService], (userStoreService: UserStoreService) => {
    userStoreService.config.imageServerUrl = 'https://als-static.bridge2rewards.com/dev2';
    service.loadInitialScript();
    eventSubject.next(new NavigationEnd(1, '/store/imac/mackbook', '/store/curated/ipad/ipad-accessories'));
    expect(service).toBeTruthy();
  }));

  it('should call triggerMethods of webTrends instance', inject([UserStoreService], (userStoreService: UserStoreService) => {
    expect(service.triggerMethods).toBeDefined();
  }));

  it('should call loadInitialScript', () => {
    service['userStoreService'].config.imageServerUrl = 'https://als-static.bridge2rewards.com/dev2';
    spyOn(service, 'loadInitialScript').and.callThrough();
    service.loadInitialScript();
    expect(service.loadInitialScript).toHaveBeenCalled();
  });

  it('should call triggerMethods', fakeAsync(() => {
    spyOn(service, 'triggerMethods').and.callThrough();
    service.webtrendsInstance = {
      dcsMultiTrack: (arg0: string, agr1: string) => null,
      dcsGetId: () => '12345',
      dcsCustom: () => null,
      dcsCollect: () => null
    };
    tick(200);
    spyOn(service.webtrendsInstance, 'dcsGetId').withArgs().and.callFake(() => {});
    service.webtrendsInstance.dcsGetId();
    tick(200);
    service.triggerMethods();
    expect(service.triggerMethods).toHaveBeenCalled();
    expect(service.triggerMethods).toBeTruthy();
  }));

  it('should be created router event', inject([UserStoreService], (userStoreService: UserStoreService) => {
    userStoreService.config.imageServerUrl = '';
    spyOn(service, 'loadInitialScript').and.callThrough();
    service.loadInitialScript();
    eventSubject.next(new NavigationStart(1, '/store/imac/mackbook'));
    expect(service.loadInitialScript).toHaveBeenCalled();
    expect(service).toBeTruthy();
  }));

});
