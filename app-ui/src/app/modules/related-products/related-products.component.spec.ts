import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Observable, of, throwError } from 'rxjs';
import { RelatedProductsComponent } from './related-products.component';
import { ModalsService } from '@app/components/modals/modals.service';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateModule, TranslatePipe, TranslateService } from '@ngx-translate/core';
import { TileComponent } from './tile/tile.component';
import { Pipe, PipeTransform, Injectable } from '@angular/core';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { AplImgSizePipe } from '@app/pipes/apl-img-size.pipe';

@Pipe({ name: 'translate' })
export class TranslatePipeStub implements PipeTransform {
  public transform(key: string, ...args: any[]): any {
    return key;
  }
}
@Injectable()
export class TranslateServiceStub {
  public get<T>(key: T): Observable<T> {
    return of(key);
  }
  public instant(key: string): any {
    return '';
  }
}
describe('RelatedProductsComponent', () => {
  let component: RelatedProductsComponent;
  let fixture: ComponentFixture<RelatedProductsComponent>;
  const relatedProductDetail = require('assets/mock/product-detail.json');
  const itemData = relatedProductDetail.relatedProducts[0];
  const programData = require('assets/mock/program.json');
  const mockUser = require('assets/mock/user.json');
  mockUser['program'] = programData;
  const userStoreData = {
      user: mockUser,
      program: programData,
      config: programData['config'],
      get: () => of(mockUser)
  };
  userStoreData.user['program'] = programData;
  userStoreData.user['browseOnly'] = false;
  userStoreData.config['loginRequired'] = false;
  let router: Router;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot()
      ],
      declarations: [
        RelatedProductsComponent,
        TileComponent,
        TranslatePipeStub,
        OrderByPipe,
        AplImgSizePipe
      ],
      providers: [
        { provide: UserStoreService, useValue: userStoreData },
        { provide: TranslatePipe },
        {
          provide: TranslateService,
          useClass: TranslateServiceStub
        },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({psid: '30001MYDA2LL/A'})
          }
        },
        {
          provide: ModalsService,
          useValue: {
            openAnonModalComponent: () => {},
            openBrowseOnlyComponent: () => {}
          }
        },
        {
          provide: MatomoService,
          useValue: {
            broadcast: () => {}
          }
        },
        CurrencyPipe,
        DecimalPipe,
        CurrencyFormatPipe,
        AplImgSizePipe
      ]
    })
    .compileComponents();
    router = TestBed.inject(Router);
    router.navigate = jasmine.createSpy('navigate');
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RelatedProductsComponent);
    component = fixture.componentInstance;
    component.isMobile = false;
    component.productDetail = relatedProductDetail;
    component.messages = require('assets/mock/messages.json');
    component.config['loginRequired'] = false;
    component.user.browseOnly = false;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call getProductDetail - call success', () => {
    spyOn(component['detailService'], 'getDetails').and.returnValue(of(relatedProductDetail));
    component.getProductDetail('30001MYDA2LL/A');
    expect(component.productDetail).toBeDefined();
  });

  it('should call getProductDetail - for mobile view call success', () => {
    component.isMobile = true;
    spyOn(component['detailService'], 'getDetails').and.returnValue(of(relatedProductDetail));
    component.getProductDetail('30001MYDA2LL/A');
    expect(component.productDetail).toBeDefined();
  });

  it('should call getProductDetail - 404 failure', () => {
    const errorResponse = {
      status: 404,
      statusText: 'Not Found'
    };
    spyOn(component, 'getProductDetail').and.callThrough();
    spyOn(component['detailService'], 'getDetails').and.returnValue(throwError(errorResponse));
    component.getProductDetail('30001MYDA2LL/A');
    expect(component.getProductDetail).toHaveBeenCalled();
  });

  it('should call getProductDetail - 401 failure', () => {
    const errorResponse = {
      status: 401,
      statusText: 'Not Found'
    };
    spyOn(component, 'getProductDetail').and.callThrough();
    spyOn(component['detailService'], 'getDetails').and.returnValue(throwError(errorResponse));
    component.getProductDetail('30001MYDA2LL/A');
    expect(component.getProductDetail).toHaveBeenCalled();
  });

  it('should call getProductDetail - 0 failure', () => {
    const errorResponse = {
      status: 0,
      statusText: 'Not Found'
    };
    spyOn(component, 'getProductDetail').and.callThrough();
    spyOn(component['detailService'], 'getDetails').and.returnValue(throwError(errorResponse));
    component.getProductDetail('30001MYDA2LL/A');
    expect(component.getProductDetail).toHaveBeenCalled();
  });

  it('should call navigateToBagPage', () => {
    spyOn(component, 'navigateToBagPage').and.callThrough();
    component.navigateToBagPage();
    expect(component.navigateToBagPage).toHaveBeenCalled();
  });

  it('should call getLastRowIndexes', () => {
    component.productDetail = null;
    spyOn(component, 'getLastRowIndexes').and.callThrough();
    component.getLastRowIndexes();
    expect(component.getLastRowIndexes).toHaveBeenCalled();
  });

  it('should call addItemToCart - default state', () => {
    spyOn(component, 'addItemToCart').and.callThrough();
    const addToCartResponse = require('assets/mock/addToCart.json');
    spyOn(component['cartService'], 'addItemToCart').and.returnValue(of(addToCartResponse));
    component.addItemToCart(itemData);
    expect(component.addItemToCart).toHaveBeenCalled();
  });

  it('should call addItemToCart - default state with item selection', () => {
    spyOn(component, 'addItemToCart').and.callThrough();
    const addToCartResponse = require('assets/mock/addToCart.json');
    spyOn(component['cartService'], 'addItemToCart').and.returnValue(of(addToCartResponse));
    component.productDetail.relatedProducts[1]['selected'] = true;
    component.addItemToCart(itemData);
    expect(component.addItemToCart).toHaveBeenCalled();
  });

  it('should call addItemToCart - anonymous state', () => {
    spyOn(component, 'addItemToCart').and.callThrough();
    component.config.loginRequired = true;
    component.addItemToCart(itemData);
    expect(component.addItemToCart).toHaveBeenCalled();
  });

  it('should call addItemToCart - browseOnly state', () => {
    spyOn(component, 'addItemToCart').and.callThrough();
    component.user.browseOnly = true;
    component.addItemToCart(itemData);
    expect(component.addItemToCart).toHaveBeenCalled();
  });

  it('should call addItemToCart - 401 error state', () => {
    spyOn(component, 'addItemToCart').and.callThrough();
    const errorResponse = {
      status: 401,
      statusText: 'Not Found'
    };
    spyOn(component['cartService'], 'addItemToCart').and.returnValue(throwError(errorResponse));
    component.addItemToCart(itemData);
    expect(component.addItemToCart).toHaveBeenCalled();
  });

  it('should call addItemToCart - 0 error state', () => {
    spyOn(component, 'addItemToCart').and.callThrough();
    const errorResponse = {
      status: 0,
      statusText: 'Not Found'
    };
    spyOn(component['cartService'], 'addItemToCart').and.returnValue(throwError(errorResponse));
    component.addItemToCart(itemData);
    expect(component.addItemToCart).toHaveBeenCalled();
  });

  it('should call addItemToCart - 500 error state', () => {
    spyOn(component, 'addItemToCart').and.callThrough();
    const errorResponse = {
      status: 500,
      statusText: 'Internal Server Error'
    };
    spyOn(component['cartService'], 'addItemToCart').and.returnValue(throwError(errorResponse));
    component.addItemToCart(itemData);
    expect(component.addItemToCart).toHaveBeenCalled();
  });

  it('should call addItemToCart - 400 error state', () => {
    spyOn(component, 'addItemToCart').and.callThrough();
    const errorResponse = {
      status: 400,
      statusText: 'Not Found'
    };
    spyOn(component['cartService'], 'addItemToCart').and.returnValue(throwError(errorResponse));
    component.addItemToCart(itemData);
    expect(component.addItemToCart).toHaveBeenCalled();
  });

  it('should call addItemToCart - 400 error state for pricing full', () => {
    spyOn(component, 'addItemToCart').and.callThrough();
    const errorResponse = {
      error: {
        pricingFull: true
      },
      status: 400,
      statusText: 'Not Found'
    };
    spyOn(component['cartService'], 'addItemToCart').and.returnValue(throwError(errorResponse));
    component.addItemToCart(itemData);
    expect(component.addItemToCart).toHaveBeenCalled();
  });

  it('should call addItemToCart - 400 error state for pricingFull is false', () => {
    spyOn(component, 'addItemToCart').and.callThrough();
    const errorResponse = {
      error: {
        pricingFull: false
      },
      status: 400,
      statusText: 'Not Found'
    };
    spyOn(component['cartService'], 'addItemToCart').and.returnValue(throwError(errorResponse));
    component.addItemToCart(itemData);
    expect(component.addItemToCart).toHaveBeenCalled();
  });

  it('should call trackByFn', () => {
    spyOn(component, 'trackByFn').and.callThrough();
    component.trackByFn(1);
    expect(component.trackByFn).toHaveBeenCalled();
    expect(component.trackByFn(1)).toBe(1);
  });

  it('should call getDetails - default state', () => {
    spyOn(component, 'getDetails').and.callThrough();
    const detailResponse = require('assets/mock/product-detail.json');
    spyOn(component['detailService'], 'getDetails').and.returnValue(of(detailResponse));
    component.getDetails(itemData);
    expect(component.getDetails).toHaveBeenCalled();
  });

  it('should call getDetails - default state with index', () => {
    spyOn(component, 'getDetails').and.callThrough();
    const detailResponse = require('assets/mock/product-detail.json');
    spyOn(component['detailService'], 'getDetails').and.returnValue(of(detailResponse));
    itemData['index'] = 0;
    component.getDetails(itemData);
    expect(component.getDetails).toHaveBeenCalled();
  });

  it('should call getDetails - 401 error state', () => {
    spyOn(component, 'getDetails').and.callThrough();
    const errorResponse = {
      status: 401,
      statusText: 'Not Found'
    };
    spyOn(component['detailService'], 'getDetails').and.returnValue(throwError(errorResponse));
    component.getDetails(itemData);
    expect(component.getDetails).toHaveBeenCalled();
  });

  it('should call getDetails - 0 error state', () => {
    spyOn(component, 'getDetails').and.callThrough();
    const errorResponse = {
      status: 0,
      statusText: 'Not Found'
    };
    spyOn(component['detailService'], 'getDetails').and.returnValue(throwError(errorResponse));
    component.getDetails(itemData);
    expect(component.getDetails).toHaveBeenCalled();
  });

  it('should call getDetails - 500 error state', () => {
    spyOn(component, 'getDetails').and.callThrough();
    const errorResponse = {
      status: 500,
      statusText: 'Internal Server Error'
    };
    spyOn(component['detailService'], 'getDetails').and.returnValue(throwError(errorResponse));
    component.getDetails(itemData);
    expect(component.getDetails).toHaveBeenCalled();
  });

});
