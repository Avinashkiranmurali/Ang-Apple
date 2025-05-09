import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { OrderDetailsComponent } from './order-details.component';
import { TranslateModule } from '@ngx-translate/core';
import { HttpClientModule } from '@angular/common/http';
import { RouterTestingModule } from '@angular/router/testing';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';
import { SessionService } from '@app/services/session.service';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { of, throwError } from 'rxjs';
import { OrderShippingPaymentDetailsComponent } from '@app/modules/orders/order-details/order-shipping-payment-details/order-shipping-payment-details.component';
import { OrderRefundSummaryComponent } from '@app/modules/orders/order-details/order-refund-summary/order-refund-summary.component';
import { OrderPaymentSummaryComponent } from '@app/modules/orders/order-details/order-payment-summary/order-payment-summary.component';
import { OrderItemsComponent } from '@app/modules/orders/order-details/order-items/order-items.component';
import { Offer } from '@app/models/offer';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { DecimalPipe } from '@angular/common';

describe('OrderDetailsComponent', () => {
  let component: OrderDetailsComponent;
  let fixture: ComponentFixture<OrderDetailsComponent>;
  const programData = require('assets/mock/program.json');
  const mockUser = require('assets/mock/user.json');
  const userData = {
    user: mockUser,
    program: programData,
    config: programData['config']
  };
  const lineItemData = require('assets/mock/order-history-details.json');

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        OrderDetailsComponent,
        OrderShippingPaymentDetailsComponent,
        OrderRefundSummaryComponent,
        OrderPaymentSummaryComponent,
        OrderItemsComponent
      ],
      imports: [
      TranslateModule.forRoot(),
      HttpClientModule,
      RouterTestingModule,
      HttpClientTestingModule
      ],
      providers: [
        { provide: ActivatedRoute, useValue: {
            params: of({ orderId: 2100455637 }),
            snapshot: {
              data: of({})
            }
          }
        },
        { provide: MessagesStoreService },
        { provide: UserStoreService, useValue: userData },
        { provide: EnsightenService, useValue: {
            broadcastEvent: () => {}
          }},
        { provide: SessionService, useValue: {
            showTimeout: () => {}
        }},
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OrderDetailsComponent);
    component = fixture.componentInstance;
    component.lineItems = lineItemData.lineItems;
    lineItemData.lineItems.forEach((lineItem, index) => {
      const offer = {
        displayPrice: {...lineItem.price},
        unpromotedDisplayPrice: lineItem.unpromotedPrice ? {...lineItem.unpromotedPrice} : null
      } as Offer;
      lineItemData.lineItems[index].offer = offer;
    });
    component.messages = require('assets/mock/messages.json');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call ngOnInit method', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    component['activatedRoute'].params.subscribe(
      params => {
        component.orderId = params['orderId'];
      }
    );
    component['activatedRoute'].snapshot.data = {
      analyticsObj : {
        pgName: 'Order Details',
        pgType: '',
        pgSectionType : ''
      }
    };
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should call getOrderDetails method for Detail view - 0 error response', () => {
    const errorResponse = { status: 0, statusText: 'Not Found' };
    spyOn(component['ordersService'], 'getOrderDetails').and.callFake(() => throwError(errorResponse));
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should call mapLineItemOffer method', () => {
    spyOn(component, 'mapLineItemOffer').and.callThrough();
    component.mapLineItemOffer(lineItemData.lineItems);
    expect(component.mapLineItemOffer).toHaveBeenCalled();
  });

  it('should call getOrderDetails method for signOutAnon flow', () => {
    spyOn(component['ordersService'], 'getOrderDetails').and.returnValue(of(lineItemData));
    component.config['isSignOutEnabledForAnon'] = true;
    spyOn(component, 'getOrderDetails').and.callThrough();
    component.getOrderDetails();
    expect(component.getOrderDetails).toHaveBeenCalled();
    expect(component.isLoaded).toBeTruthy();
    expect(component.orderDetails).toBeDefined();
  });

  it('should call getOrderDetails method', () => {
    spyOn(component['ordersService'], 'getOrderDetails').and.returnValue(of(lineItemData));
    component.config['isSignOutEnabledForAnon'] = false;
    spyOn(component, 'getOrderDetails').and.callThrough();
    component.getOrderDetails();
    expect(component.getOrderDetails).toHaveBeenCalled();
    expect(component.isLoaded).toBeTruthy();
    expect(component.orderDetails).toBeDefined();
  });

  it('should call getOrderDetails method for Product view - 401 error response', () => {
    const errorResponse = { status: 401, statusText: 'Not Found' };
    spyOn(component['ordersService'], 'getOrderDetails').and.callFake(() => throwError(errorResponse));
    spyOn(component, 'getOrderDetails').and.callThrough();
    component.getOrderDetails();
    expect(component.getOrderDetails).toHaveBeenCalled();
  });

  it('should call getOrderDetails method for Product view - 0 error response', () => {
    const errorResponse = { status: 0, statusText: 'Not Found' };
    spyOn(component['ordersService'], 'getOrderDetails').and.callFake(() => throwError(errorResponse));
    spyOn(component, 'getOrderDetails').and.callThrough();
    component.getOrderDetails();
    expect(component.getOrderDetails).toHaveBeenCalled();
  });

  it('should call getOrderDetails method for Product view - 404 error response', () => {
    const errorResponse = { status: 404, statusText: 'Not Found' };
    spyOn(component['ordersService'], 'getOrderDetails').and.callFake(() => throwError(errorResponse));
    spyOn(component, 'getOrderDetails').and.callThrough();
    component.getOrderDetails();
    expect(component.getOrderDetails).toHaveBeenCalled();
  });

});
