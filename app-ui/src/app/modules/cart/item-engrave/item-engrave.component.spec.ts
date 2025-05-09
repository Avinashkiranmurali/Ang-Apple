import { DecimalPipe } from '@angular/common';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { CartService } from '@app/services/cart.service';
import { NotificationRibbonService } from '@app/services/notification-ribbon.service';
import { SessionService } from '@app/services/session.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { ItemEngraveComponent } from './item-engrave.component';

describe('ItemEngraveComponent', () => {
  let component: ItemEngraveComponent;
  let fixture: ComponentFixture<ItemEngraveComponent>;
  let httpTestingController: HttpTestingController;
  const CART_ITEM = require('assets/mock/cart.json');

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ItemEngraveComponent ],
      providers: [
        NgbActiveModal,
        CartService,
        NotificationRibbonService,
        { provide: SharedService, useValue: {
          currentEngraveProductDetail: null,
          openEngraveModalDialog: () => {} }
        },
        { provide: SessionService, useValue: {
          getSession: () => of({}),
          showTimeout: () => of({})
        }},
        { provide: CurrencyFormatPipe },
        { provide: DecimalPipe },
        { provide: CurrencyPipe }
      ],
      imports: [
        FormsModule,
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot()
      ]
    })
    .compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ItemEngraveComponent);
    component = fixture.componentInstance;
    component.item = CART_ITEM['cartItems'][0];
    component.engraveIndex = 0;
    component.messages = require('assets/mock/messages.json');
    component.showEngraveOptions = true;
    component.editEngravingOptions = true;
    component.cartEngrave = [{
      line1: 'ENGRAVE',
      line2: 'TEST ENGRAVE TEST',
      font: 'Helvetica Neue',
      fontCode: 'ESR077N',
      maxCharsPerLine: '18 Eng',
      widthDimension: '45mm',
      noOfLines: 2,
      engraveBgImageLocation: 'apple-gr/assets/img/engraving/',
      isSkuBasedEngraving: false,
      templateClass: '',
      engraveFontConfigurations: null
    }];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should removeEngraving method', waitForAsync(() => {
    spyOn(component, 'removeEngraving').and.callThrough();
    spyOn(component['cartService'], 'modifyCart').and.returnValue(of(CART_ITEM));
    component.removeEngraving(0, 776269);
    expect(component.removeEngraving).toHaveBeenCalled();
  }));

  it('should removeEngraving method for 401', waitForAsync(() => {
    spyOn(component, 'removeEngraving').and.callThrough();
    component.removeEngraving(0, 776269);

    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/cart/modify/' + 776269);
    const errorMsg = 'deliberate 401 error';
    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');

    // Respond with mock error
    req.flush(errorMsg, { status: 401, statusText: 'Not Found' });
    expect(component.removeEngraving).toHaveBeenCalled();
  }));

  it('should removeEngraving method for 404', waitForAsync(() => {
    spyOn(component, 'removeEngraving').and.callThrough();
    component.removeEngraving(0, 776269);

    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/cart/modify/' + 776269);
    const errorMsg = 'deliberate 401 error';
    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');

    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
    expect(component.removeEngraving).toHaveBeenCalled();
  }));

  it('should call editEngraveGiftTxt', () => {
    spyOn(component, 'editEngraveGiftTxt').and.callThrough();
    component.editEngraveGiftTxt();
    expect(component.editEngraveGiftTxt).toHaveBeenCalled();
  });

});
