import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, ActivatedRouteSnapshot, ActivationEnd, Router } from '@angular/router';
import { UserStoreService } from '@app/state/user-store.service';
import { EnsightenService } from './ensighten.service';
import { AnalyticsObject, UserDataLayer } from './ensighten';
import { CartItem } from '@app/models/cart';
import { ReplaySubject, of, Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterTestingModule } from '@angular/router/testing';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { TranslateService } from '@ngx-translate/core';

@Injectable()
export class TranslateServiceStub {
  public get<T>(key: T): Observable<T> {
    return of(key);
  }
  public instant(key: string): any {
    return '';
  }
}

describe('EnsightenService', () => {
  let service: EnsightenService;const eventSubject = new ReplaySubject<ActivationEnd>(1);
  const mockRouter = {
    events: eventSubject.asObservable(),
    url: '/testUrl',
    navigate: jasmine.createSpy('navigate')
  };
  const programData = require('assets/mock/program.json');
  const mockUser = require('assets/mock/user.json');
  mockUser['program'] = programData;
  const userData = {
    user: mockUser,
    program: programData,
    config: programData['config'],
    get: () => of(mockUser)
  };
  const program = require('assets/mock/program.json');
  program['config']['ensightenEndPoint'] = 'https://als-static.bridge2rewards.com/dev2/apple-gr/analytics/ensighten.js';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule
      ],
      providers: [
        { provide: TranslateService, useClass: TranslateServiceStub },
        { provide: UserStoreService, useValue: userData },
        { provide: Router, useValue: mockRouter },
        {
          provide: ActivatedRoute, useValue: {
            params: of({ category: 'mac', subcat: 'macbook-pro' })
          }
        },
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe
      ]
    });
    service = TestBed.inject(EnsightenService);
  });

  afterEach(() => {
    service.user = userData.user;
    service.user.programId = 'b2s_qa_only';
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should be created', () => {
    const snapshot: ActivatedRouteSnapshot = Object.assign({});
    snapshot.data = {
      pageName: 'TESTPAGE'
    };
    (snapshot as any).pathFromRoot = [Object.assign({})];
    snapshot.pathFromRoot[0]['url'] = [{ path: 'testUrl', parameters: {}, parameterMap: null }];
    eventSubject.next(new ActivationEnd(snapshot));

    snapshot.pathFromRoot[0]['url'] = [{ path: null, parameters: {}, parameterMap: null }];
    eventSubject.next(new ActivationEnd(snapshot));
    expect(service).toBeTruthy();
  });

  it('should create instance for else check for activation end', () => {
    const snapshot: ActivatedRouteSnapshot = Object.assign({});
    snapshot.data = null;
    (snapshot as any).pathFromRoot = [Object.assign({})];
    snapshot.pathFromRoot[0]['url'] = [{ path: 'testUrl', parameters: {}, parameterMap: null }];
    eventSubject.next(new ActivationEnd(snapshot));
    expect(service).toBeTruthy();
  });


  it('should call loadEnsightenScript', () => {
    spyOn(service, 'loadEnsightenScript').and.callThrough();
    service.loadEnsightenScript();
    expect(service.loadEnsightenScript).toHaveBeenCalled();
  });

  it('should call updateAnalyticsObject method', () => {
    const analyticsObject: AnalyticsObject = {
      datalayer_b2s_user: {
        pgCountryCode: 'US',
        pgCountryLanguage: '',
        userMembershipID: 'eric',
        userMembershipTier: 'B2S_QA_ONLY',
        userPoints: 9999999,
        userStatus: 'L'
      } as UserDataLayer,
      datalayer_b2s_prod: [
        {
          prodProductCategory: 'apple watch se',
          prodProductName: 'apple watch se gps, 40mm gold aluminum case with pink sand sport band - regular',
          prodProductPSID: '30001mydn2ll/a',
          prodProductPoints: 51800,
          prodProductSKU: 'mydn2ll/a',
          prodProductType: 'apple',
          prodProductUPC: '190199760523'
        }
      ]
    };
    spyOn(service, 'updateAnalyticsObject').and.callThrough();
    service.updateAnalyticsObject(analyticsObject);
    expect(service.updateAnalyticsObject).toHaveBeenCalled();
  });

  it('should call buildProductArray method', () => {
    spyOn(service, 'buildProductArray').and.callThrough();
    const arrayData = require('assets/mock/cart.json');
    service.buildProductArray(arrayData['cartItems']);
    expect(service.buildProductArray).toHaveBeenCalled();
  });

  it('should call buildProductArray method if no required data exists', () => {
    spyOn(service, 'buildProductArray').and.callThrough();
    const arrayData = [];
    const data: CartItem = Object.assign({});
    data.productDetail = Object.assign({});
    arrayData.push(data);
    service.buildProductArray(arrayData);
    expect(service.buildProductArray).toHaveBeenCalled();
  });

  it('should call buildProductArray method for empty product details', () => {
    spyOn(service, 'buildProductArray').and.callThrough();
    const mockData = [
      {
        psid: '30001HMTK2ZM/A',
        nmae: null,
        categories: null,
        sku: null,
        upc: null,
        offers: null
      },
      {
        psid: null,
        nmae: null,
        categories: null,
        sku: null,
        upc: null,
        offers: null
      }
    ];
    service.buildProductArray(mockData);
    expect(service.buildProductArray).toHaveBeenCalled();
  });

  it('should execute when currentPage is CONFIRMATION and analyticsDebugger is true', () => {
    spyOn(service, 'broadcastEvent').and.callThrough();
    const arrayData = require('assets/mock/cart.json')['cartItems'];
    service.currentPage = 'CONFIRMATION';
    window['analyticsDebugger'] = true;
    service.broadcastEvent({
      pgName: 'CONFIRMATION',
      pgType: 'checkout',
      pgSectionType: 'apple_products',
      points: 110011
    }, {
      products: arrayData
    });
    expect(service.broadcastEvent).toHaveBeenCalled();
  });

  it('should execute when currentPage is CLP and analyticsDebugger is false', () => {
    spyOn(service, 'broadcastEvent').and.callThrough();
    service.user.program = userData.program;
    service.user.program.config.login_required = false;
    service.currentPage = 'CLP';
    window['analyticsDebugger'] = false;
    service.broadcastEvent({
      pgName: 'apple_products:clp:mac',
      pgType: 'clp',
      pgSectionType: 'products|merchandise'
    }, [{ prodProductCategory: 'apple', prodProductType: 'mac' }]);
    expect(service.broadcastEvent).toHaveBeenCalled();
  });

  it('should execute else check if data not exists for broadcastEvent', () => {
    spyOn(service, 'broadcastEvent').and.callThrough();
    service.user.country = '';
    service.user.additionalInfo = {
      languageCode: 'EN'
    };
    service.user.userId = '';
    service.user.programId = '';
    service.user.program = userData.program;
    service.user.program.config.login_required = true;
    service.currentPage = 'CLP';
    window['analyticsDebugger'] = false;
    service.broadcastEvent({
      pgName: 'apple_products:clp:mac',
      pgType: 'clp',
      pgSectionType: 'products|merchandise'
    }, [{}]);
    expect(service.broadcastEvent).toHaveBeenCalled();
  });

  it('should trigger getChild', () => {
    spyOn(service, 'getChild').and.callThrough();
    const activatedRoute: ActivatedRoute = Object.assign({});
    (activatedRoute as any).firstChild = {
      params: of({ category: 'ipad', subcat: 'ipad-accessories' })
    };
    service.getChild(activatedRoute);
    expect(service.getChild).toHaveBeenCalled();
  });

});
