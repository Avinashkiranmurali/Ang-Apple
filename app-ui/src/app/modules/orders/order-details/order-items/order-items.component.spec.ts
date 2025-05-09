import { DecimalPipe } from '@angular/common';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { CartPricingTempComponent } from '@app/modules/pricing/cart-pricing-temp/cart-pricing-temp.component';
import { Offer } from '@app/models/offer';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateModule } from '@ngx-translate/core';
import { OrderItemsComponent } from './order-items.component';
import { AplImgSizePipe } from '@app/pipes/apl-img-size.pipe';
import { TemplateStoreService } from '@app/state/template-store.service';

describe('OrderItemsComponent', () => {
  let component: OrderItemsComponent;
  let fixture: ComponentFixture<OrderItemsComponent>;
  const programData = require('assets/mock/program.json');
  const userMock = require('assets/mock/user.json');
  const lineItemData = require('assets/mock/order-history-details.json');
  const configData = require('assets/mock/configData.json');
  let templateStoreService: TemplateStoreService;
  userMock['program'] = programData;
  const userData = {
    user: userMock,
    program: programData,
    config: programData['config']
  };
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        OrderItemsComponent,
        CartPricingTempComponent,
        OrderByPipe,
        AplImgSizePipe
      ],
      imports: [
        TranslateModule.forRoot(),
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        PricingService,
        { provide: MessagesStoreService },
        { provide: UserStoreService, useValue: userData },
        { provide: CurrencyFormatPipe },
        { provide: DecimalPipe },
        { provide: CurrencyPipe },
        { provide: TemplateStoreService },
        AplImgSizePipe
      ]
    })
    .compileComponents();
    templateStoreService = TestBed.inject(TemplateStoreService);
    templateStoreService.addTemplate(configData['configData']);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OrderItemsComponent);
    component = fixture.componentInstance;
    lineItemData.lineItems.forEach((lineItem, index) => {
      const offer = {
        displayPrice: {...lineItem.price},
        unpromotedDisplayPrice: lineItem.unpromotedPrice ? {...lineItem.unpromotedPrice} : null
      } as Offer;
      lineItemData.lineItems[index].offer = offer;
    });
    component.lineItems = lineItemData.lineItems;
    component.messages = require('assets/mock/messages.json');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create medium', () => {
    component['userStore'].config.mediumDate = 'M/d/yyyy';
    expect(component).toBeTruthy();
  });

  it('should create SingleItemPurchase', () => {
    component['userStore'].config.SingleItemPurchase = true;
    expect(component).toBeTruthy();
  });

  it('should call ngOnInit method', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    component.lineItems = lineItemData.lineItems;
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should display item engraving text', waitForAsync(() => {
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.lineItems[0].engrave.line1 + ' ' + component.lineItems[0].engrave.line2).toEqual('hello world');
  }));

  it('should call orderItemStatus method - delayedShippingInfo', () => {
    component.lineItems = lineItemData.lineItems;
    component.lineItems[0].delayedShippingInfo = {shippingAvailability: '4-6', asOfDate: ''};
    expect(component.orderItemStatus(component.lineItems[0])).toBe('delayed');
  });

  it('should call orderItemStatus method - shipmentInfo', () => {
    component.lineItems = lineItemData.lineItems;
    component.lineItems[0].delayedShippingInfo = null;
    component.lineItems[0].shipmentInfo = {shipmentDate: new Date(), trackingID: ''};
    expect(component.orderItemStatus(component.lineItems[0])).toBe('shipped');
  });

});
