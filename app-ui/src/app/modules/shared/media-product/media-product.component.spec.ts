import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { ModalsService } from '@app/components/modals/modals.service';
import { MediaProductComponent } from './media-product.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Injectable, Pipe, PipeTransform } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';
import { NgbActiveModal, NgbModal, NgbModalModule } from '@ng-bootstrap/ng-bootstrap';
import { MediaProductModalComponent } from '@app/modules/shared/media-product-modal/media-product-modal.component';
import { UserStoreService } from '@app/state/user-store.service';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

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

// Mock class for NgbModalRef
export class MockNgbModalResolve {
  componentInstance = {
    config: '',
    messages: '',
    mediaProduct: ''
  };
  result: Promise<any> = Promise.resolve(true);
}

describe('MediaProductComponent', () => {
  let component: MediaProductComponent;
  let fixture: ComponentFixture<MediaProductComponent>;
  let bootstrapModal: NgbModal;
  const mockModalResolve = new MockNgbModalResolve();
  const programData = require('assets/mock/program.json');
  const userMock = require('assets/mock/user.json');
  programData['imageServerUrl'] = 'https://als-static.bridge2rewards.com/dev';
  userMock['program'] = programData;
  const userData = {
    user: userMock,
    program: programData,
    config: programData['config'],
    get: () => of(userMock)
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        RouterTestingModule,
        NgbModalModule
      ],
      declarations: [
        MediaProductComponent,
        MediaProductModalComponent
      ],
      providers: [
        { provide: ModalsService, useValue: {
            settop: () => ({})
          }
        },
        { provide: ActivatedRoute,
          useValue: { snapshot: { data: {pageName: 'CONFIRMATION'}}}
        },
        {
          provide: TranslateService,
          useClass: TranslateServiceStub
        },
        { provide: Router, useValue: {
            navigate: jasmine.createSpy('navigate') }
        },
        { provide: UserStoreService, useValue: userData },
        NgbModal,
        NgbActiveModal,
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe
      ]
    })
    .compileComponents();
    bootstrapModal = TestBed.inject(NgbModal);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MediaProductComponent);
    component = fixture.componentInstance;
    component.config['imageServerUrl'] = 'https://als-static.bridge2rewards.com/dev2';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call doMediaSubscribe method - when disableBtn is true', () => {
    spyOn(component, 'doMediaSubscribe').and.callThrough();
    const mediaProduct = {
      itemId: 'amp-music',
      addedToCart: true
    };
    component.disableBtn = true;
    const cartResponse = require('assets/mock/cart.json');
    delete cartResponse.subscriptions;
    spyOn(component['cartService'], 'modifyCart').and.returnValue(of(cartResponse));
    component.doMediaSubscribe(mediaProduct);
    expect(component.doMediaSubscribe).toHaveBeenCalled();
  });

  it('should call doMediaSubscribe method - when disableBtn is false', () => {
    spyOn(component, 'doMediaSubscribe').and.callThrough();
    const mediaProduct = {
      itemId: 'amp-music',
      addedToCart: false
    };
    component.disableBtn = false;
    const cartData = require('assets/mock/cart.json');
    cartData.subscriptions = [
      {itemId: 'amp-music', addedToCart: true},
      {itemId: 'amp-tv-plus', addedToCart: false},
      {itemId: 'amp-news-plus', addedToCart: false}
    ];
    spyOn(component['cartService'], 'modifyCart').and.returnValue(of(cartData));
    component.doMediaSubscribe(mediaProduct);
    expect(component.doMediaSubscribe).toHaveBeenCalled();
  });

  it('should call doMediaSubscribe method for failure response 401', () => {
    spyOn(component, 'doMediaSubscribe').and.callThrough();
    const mediaProduct = {
      itemId: 'amp-music',
      addedToCart: false
    };
    const errorResponse = {
      status: 401,
      statusMessage: 'Failure'
    };
    spyOn(component['cartService'], 'modifyCart').and.returnValue(throwError(errorResponse));
    component.doMediaSubscribe(mediaProduct);
    expect(component.doMediaSubscribe).toHaveBeenCalled();
  });

  it('should call doMediaSubscribe method for failure response 0', () => {
    spyOn(component, 'doMediaSubscribe').and.callThrough();
    const mediaProduct = {
      itemId: 'amp-music',
      addedToCart: false
    };
    component.disableBtn = true;
    const errorResponse = {
      status: 0,
      statusMessage: 'Failure'
    };
    spyOn(component['cartService'], 'modifyCart').and.returnValue(throwError(errorResponse));
    component.doMediaSubscribe(mediaProduct);
    expect(component.doMediaSubscribe).toHaveBeenCalled();
  });

  it('should call doMediaSubscribe method for failure response 404', () => {
    spyOn(component, 'doMediaSubscribe').and.callThrough();
    const mediaProduct = {
      itemId: 'amp-music',
      addedToCart: false
    };
    component.disableBtn = false;
    const errorResponse = {
      status: 404,
      statusMessage: 'Not found'
    };
    spyOn(component['cartService'], 'modifyCart').and.returnValue(throwError(errorResponse));
    component.doMediaSubscribe(mediaProduct);
    expect(component.doMediaSubscribe).toHaveBeenCalled();
  });

  it('should call openMediaProductModal method', () => {
    spyOn(component, 'openMediaProductModal').and.callThrough();
    mockModalResolve.result = Promise.resolve(true);
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalResolve as any);
    const mediaProduct = {
      itemId: 'amp-music',
      addedToCart: true
    };
    component.openMediaProductModal(mediaProduct);
    expect(component.openMediaProductModal).toHaveBeenCalled();
  });

  it('should call openMediaProductModal method - When no data exists', () => {
    spyOn(component, 'openMediaProductModal').and.callThrough();
    mockModalResolve.result = Promise.resolve(false);
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalResolve as any);
    const mediaProduct = {
      itemId: 'amp-music',
      addedToCart: true
    };
    component.openMediaProductModal(mediaProduct);
    expect(component.openMediaProductModal).toHaveBeenCalled();
  });

  it('should call openMediaProductModal method - When rejects', () => {
    spyOn(component, 'openMediaProductModal').and.callThrough();
    mockModalResolve.result = Promise.reject(false);
    spyOn(bootstrapModal, 'open').and.returnValue(mockModalResolve as any);
    const mediaProduct = {
      itemId: 'amp-music',
      addedToCart: true
    };
    component.openMediaProductModal(mediaProduct);
    expect(component.openMediaProductModal).toHaveBeenCalled();
  });

});
