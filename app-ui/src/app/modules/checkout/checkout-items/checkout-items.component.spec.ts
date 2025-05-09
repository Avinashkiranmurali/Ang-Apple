import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { CheckoutItemsComponent } from './checkout-items.component';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { TemplateService } from '@app/services/template.service';
import { Router } from '@angular/router';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { HttpClient } from '@angular/common/http';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

describe('CheckoutItemsComponent', () => {
  let component: CheckoutItemsComponent;
  let templateService: TemplateService;
  let fixture: ComponentFixture<CheckoutItemsComponent>;
  const userData = {
    user : {
      locale: 'en_US'
    },
    config : {
      loginRequired : false,
      SFProWebFont : true
    }
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ CheckoutItemsComponent, OrderByPipe ],
      imports: [ TranslateModule.forRoot(), RouterTestingModule, HttpClientTestingModule ],
      providers: [
        MessagesStoreService,
        {
          provide: TemplateService,
          useValue: {
            getBtnColor: () => {},
            getTemplatesProperty: () => ({
              checkoutItemsList: {
                template: 'checkout-subsidy.htm'
              }
            })
          },
        },
        { provide: UserStoreService, useValue : userData },
        { provide: HttpClient },
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate') }
        },
        { provide: MatomoService, useValue: {
          broadcast: () => {} }
        },
        { provide: CurrencyFormatPipe },
        { provide: DecimalPipe },
        { provide: CurrencyPipe }
      ]
    })
    .compileComponents();
    templateService = TestBed.inject(TemplateService);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CheckoutItemsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create if SingleItemPurchase is true', () => {
    component['userStore'].config.SingleItemPurchase = true;
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should call getButtonColor method', () => {
    spyOn(templateService, 'getBtnColor').and.callThrough();
    templateService.getBtnColor();
    expect(templateService.getBtnColor).toHaveBeenCalled();
  });

  it('should call changeItemSelection if fullCatalog is false', () => {
    component.config.fullCatalog = false;
    const item = require('assets/mock/product-detail.json');
    spyOn(component, 'changeItemSelection').and.callThrough();
    component.changeItemSelection(item);
    expect(component.changeItemSelection).toHaveBeenCalled();
  });

  it('should call changeItemSelection if fullCatalog is true', () => {
    component.config.fullCatalog = true;
    const item = require('assets/mock/product-detail.json');
    spyOn(component, 'changeItemSelection').and.callThrough();
    component.changeItemSelection(item);
    expect(component.changeItemSelection).toHaveBeenCalled();
  });

});
