import { DecimalPipe } from '@angular/common';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { SessionService } from '@app/services/session.service';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateModule, TranslatePipe } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { OrderTilesComponent } from './order-tiles/order-tiles.component';
import { OrdersComponent } from './orders.component';

describe('OrdersComponent', () => {
  let component: OrdersComponent;
  let fixture: ComponentFixture<OrdersComponent>;
  const programData = require('assets/mock/program.json');
  const mockUser = require('assets/mock/user.json');
  const userData = {
    user: mockUser,
    program: programData,
    config: programData['config']
  };
  const orderHistoryData = require('assets/mock/order-history.json');

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        OrdersComponent,
        OrderTilesComponent,
        OrderByPipe,
        TranslatePipe
      ],
      imports: [
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        RouterTestingModule,
      ],
      providers: [
        { provide: MessagesStoreService },
        { provide: UserStoreService, useValue: userData },
        { provide: EnsightenService, useValue: {
          broadcastEvent: () => {}
        }},
        { provide: ActivatedRoute, useValue: {
          snapshot: {
            data: of({})
          }
        }},
        { provide: SessionService, useValue: {
          showTimeout: () => {}
        }},
        { provide: CurrencyFormatPipe },
        { provide: DecimalPipe },
        { provide: CurrencyPipe }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OrdersComponent);
    component = fixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call ngOnInit method', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    component['activatedRoute'].snapshot.data = {
      analyticsObj: {
        pgName: 'apple_products:order_history',
        pgType: 'orderHistory',
        pgSectionType: 'information'
      }
    };
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should call ngOnInit method - no analytics data', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    component['activatedRoute'].snapshot.data = {
      analyticsObj: {}
    };
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should call getOrders method', () => {
    spyOn(component['ordersService'], 'getOrderHistory').and.returnValue(of(orderHistoryData));
    spyOn(component, 'getOrders').and.callThrough();
    component.getOrders();
    expect(component.getOrders).toHaveBeenCalled();
  });

  it('should call getOrders method for Product view - 401 error response', () => {
    const errorResponse = { status: 401, statusText: 'Not Found' };
    spyOn(component['ordersService'], 'getOrderHistory').and.callFake(() => throwError(errorResponse));
    spyOn(component, 'getOrders').and.callThrough();
    component.getOrders();
    expect(component.getOrders).toHaveBeenCalled();
  });

  it('should call getOrders method for Product view - 0 error response', () => {
    const errorResponse = { status: 0, statusText: 'Not Found' };
    spyOn(component['ordersService'], 'getOrderHistory').and.callFake(() => throwError(errorResponse));
    spyOn(component, 'getOrders').and.callThrough();
    component.getOrders();
    expect(component.getOrders).toHaveBeenCalled();
  });

  it('should call getOrders method for Product view - 404 error response', () => {
    const errorResponse = { status: 404, statusText: 'Not Found' };
    spyOn(component['ordersService'], 'getOrderHistory').and.callFake(() => throwError(errorResponse));
    spyOn(component, 'getOrders').and.callThrough();
    component.getOrders();
    expect(component.getOrders).toHaveBeenCalled();
  });

});
