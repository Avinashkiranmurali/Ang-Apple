import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { CartItemsComponent } from './cart-items.component';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { TemplateService } from '@app/services/template.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NavigationEnd, Router } from '@angular/router';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { BehaviorSubject } from 'rxjs';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { DecimalPipe } from '@angular/common';

describe('CartItemsComponent', () => {
  let component: CartItemsComponent;
  let fixture: ComponentFixture<CartItemsComponent>;
  const userData = {
    user : {
      locale: 'en_US',
      program: require('assets/mock/program.json')
    },
    config : {
      loginRequired : false,
      SFProWebFont : true,
      fullCatalog: false
    }
  };
  const routerEvent$ = new BehaviorSubject<NavigationEnd>(null);
  let router: Router;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ CartItemsComponent, OrderByPipe ],
      imports: [ TranslateModule.forRoot(), RouterTestingModule, HttpClientTestingModule ],
      providers: [
        MessagesStoreService,
        {
          provide: TemplateService,
          useValue: {
            getTemplatesProperty: () => ({
                cartItemsList: {
                  template: 'cart-subsidy.htm'
                }
              })
          }
        },
        { provide: UserStoreService, useValue : userData },
        { provide: MatomoService, useValue: {
            broadcast: () => {} }
        },
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate') }
        },
        DecimalPipe,
        CurrencyPipe,
        CurrencyFormatPipe
      ]
    })
    .compileComponents();
    router = TestBed.inject(Router);
    router.navigate = jasmine.createSpy('navigate');
    (router as any).events = routerEvent$.asObservable();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CartItemsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create', () => {
    component.config.SingleItemPurchase = true;
    expect(component).toBeTruthy();
  });

  it('should call removeItem Method', () => {
    spyOn(component, 'removeItem').and.callThrough();
    const item = require('assets/mock/cart.json')['cartItems'];
    const removedItem = item;
    component.removeItem(1, removedItem);
    expect(component.removeItem).toHaveBeenCalled();
  });
  it('should call changeItemSelection Method', () => {
    component.config.fullCatalog = false;
    fixture.detectChanges();
    spyOn(component, 'changeItemSelection').and.callThrough();
    const item = require('assets/mock/cart.json')['cartItems'];
    component.changeItemSelection (item[0]);
    expect(component.changeItemSelection).toHaveBeenCalled();
  });

  it('should call changeItemSelection Method else block', () => {
    component.config.fullCatalog = true;
    fixture.detectChanges();
    spyOn(component, 'changeItemSelection').and.callThrough();
    const item = require('assets/mock/cart.json')['cartItems'];
    component.changeItemSelection (item[0]);
    expect(component.changeItemSelection).toHaveBeenCalled();
  });

  it('should call addOrUpdateCartEvent Method', () => {
    spyOn(component, 'addOrUpdateCartEvent').and.callThrough();
    const item = require('assets/mock/cart.json')['cartItems'];
    const event = {
      type: 'editItemQty',
      itemList: item,
    };
    component.addOrUpdateCartEvent (event);
    expect(component.addOrUpdateCartEvent).toHaveBeenCalled();
  });
  it('should call editItemQty Method', () => {
    spyOn(component, 'editItemQty').and.callThrough();
    const item = require('assets/mock/cart.json')['cartItems'];
    const type = 'editItemQty';
    component.editItemQty (type, 1 , 1 , 2, item);
    expect(component.editItemQty).toHaveBeenCalled();
  });

  it('should call isServicePlansExist is its been selected already', () => {
    spyOn(component, 'isServicePlansExist').and.callThrough();
    const data = {
      selectedAddOns: {
        servicePlan: {}
      }
    };
    expect(component.isServicePlansExist(data)).toBeTruthy();
    expect(component.isServicePlansExist).toHaveBeenCalled();
  });

});
