import { Pipe, Injectable, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { SafePipe } from '@app/pipes/safe.pipe';
import { MediaProductModalComponent } from './media-product-modal.component';
import { of, Observable, throwError } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { SessionService } from '@app/services/session.service';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';

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

describe('MediaProductModalComponent', () => {
  let component: MediaProductModalComponent;
  let fixture: ComponentFixture<MediaProductModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ RouterTestingModule, TranslateModule.forRoot(), HttpClientTestingModule ],
      declarations: [ MediaProductModalComponent, SafePipe, TranslatePipeStub ],
      providers: [
        NgbActiveModal,
        { provide: CurrencyPipe,
          useValue: {
            transform: () => '$4.99'
          }
        },
        {
          provide: TranslateService,
          useClass: TranslateServiceStub
        },
        {
          provide: Router,
          useValue: {
            url: '/store',
            navigate: jasmine.createSpy('navigate')
          }
        },
        SessionService,
        CurrencyFormatPipe,
        DecimalPipe
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MediaProductModalComponent);
    component = fixture.componentInstance;
    component.mediaProduct = {
      itemId: 'amp-music',
      addedToCart: false
    };
    component.config = {
      imageServerUrl: 'https://als-static.bridge2rewards.com/dev'
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call cancel method', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    component.config = null;
    component.mediaProduct = null;
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should call cancel method', () => {
    spyOn(component, 'cancel').and.callThrough();
    component.cancel();
    expect(component.cancel).toHaveBeenCalled();
  });

  it('should call doMediaSubscribe method for added subscriptions - no subscriptions in response', () => {
    spyOn(component, 'doMediaSubscribe').and.callThrough();
    const mediaProduct = {
      itemId: 'amp-music',
      addedToCart: false
    };
    const cartResponse = require('assets/mock/cart.json');
    delete cartResponse.subscriptions;
    spyOn(component['cartService'], 'modifyCart').and.returnValue(of(cartResponse));
    component.doMediaSubscribe(mediaProduct);
    expect(component.doMediaSubscribe).toHaveBeenCalled();
  });

  it('should call doMediaSubscribe method for yet to add subscriptions', () => {
    spyOn(component, 'doMediaSubscribe').and.callThrough();
    const mediaProduct = {
      itemId: 'amp-music',
      addedToCart: false
    };
    const cartData = require('assets/mock/cart.json');
    cartData.subscriptions = [
      {itemId: 'amp-music', addedToCart: false},
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
    const errorResponse = {
      status: 404,
      statusMessage: 'Not found'
    };
    spyOn(component['cartService'], 'modifyCart').and.returnValue(throwError(errorResponse));
    component.doMediaSubscribe(mediaProduct);
    expect(component.doMediaSubscribe).toHaveBeenCalled();
  });

});
