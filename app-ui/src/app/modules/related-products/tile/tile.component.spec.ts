import { HttpClientTestingModule } from '@angular/common/http/testing';
import { EventEmitter, Injectable } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { PricingTempComponent } from '@app/modules/pricing/pricing-temp/pricing-temp.component';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';
import { GiftPromoService } from '@app/services/gift-promo.service';
import { TranslateModule, TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { TileComponent } from './tile.component';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { DecimalPipe } from '@angular/common';
import { AplImgSizePipe } from '@app/pipes/apl-img-size.pipe';

@Injectable()
export class TranslateServiceStub {
  public onLangChange: EventEmitter<any> = new EventEmitter();
  public onTranslationChange: EventEmitter<any> = new EventEmitter();
  public onDefaultLangChange: EventEmitter<any> = new EventEmitter();

  public get<T>(key: T): Observable<T> {
    return of(key);
  }
  public instant(key: string): any {
    return '';
  }
}

describe('RelatedProductTileComponent', () => {
  let component: TileComponent;
  let fixture: ComponentFixture<TileComponent>;
  const relatedProductDetail = require('assets/mock/product-detail.json');
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        TileComponent,
        PricingTempComponent,
        TranslatePipe,
        OrderByPipe,
        AplImgSizePipe
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        GiftPromoService,
        ParsePsidPipe,
        CurrencyPipe,
        CurrencyFormatPipe,
        TranslatePipe,
        OrderByPipe,
        DecimalPipe,
        AplImgSizePipe,
        { provide: TranslateService, useClass: TranslateServiceStub }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TileComponent);
    component = fixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
    component.item = require('assets/mock/products-watch.json');
    component.itemOptions = component['sharedService'].transformOptions(component.item.optionsConfigurationData, '', false);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call addItemToCart method', () => {
    spyOn(component, 'addItemToCart').and.callThrough();
    const productDetail = relatedProductDetail.relatedProducts[1];
    component.addItemToCart('30001MYDA2LL/A', productDetail);
    expect(component.addItemToCart).toHaveBeenCalled();
  });

  it('should call changeVariantOption method', () => {
    spyOn(component, 'changeVariantOption').and.callThrough();
    component.item = relatedProductDetail.relatedProducts[2];
    component.selectedVariant = { color: 'product_red' };
    const data = {
      disabled: true,
      hidden: false,
      isDenomination: false,
      name: 'color',
      optionData: [{
        image: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/MXAA2ref_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1611165303000&qlt=95',
        key: 'black',
        optDisable: false,
        optHidden: false,
        points: null,
        tabindex: 0,
        value: 'Black'
      }],
      orderBy: 0,
      title: 'Color'
    };
    component.itemOptions.push(data);
    component.userProductsFilterBySelections = component.item.variations;
    fixture.detectChanges();
    const optionSet = component.itemOptions[0];
    spyOn(component, 'productFindingByConfigOptions').and.callFake(() => {});
    component.changeVariantOption(component.selectedVariant['color'], optionSet);
    expect(component.changeVariantOption).toHaveBeenCalled();
  });

  it('should call mouseHoverEvent method', () => {
    component.mouseHoverEvent('#fff');
    expect(component.hoverColor).toEqual('#fff');
  });

  it('should call mouseLeaveEvent method', () => {
    component.mouseLeaveEvent();
    expect(component.hoverColor).toEqual('');
  });

  it('should call setFilteredProducts method', () => {
    spyOn(component, 'setFilteredProducts').and.callThrough();
    spyOn(component['sharedService'], 'filterProducts').and.returnValue([]);
    component.setFilteredProducts(relatedProductDetail.relatedProducts[2].variations);
    expect(component.setFilteredProducts).toHaveBeenCalled();
  });

  it('should call setFilteredProducts method - no selected variant', () => {
    spyOn(component, 'setFilteredProducts').and.callThrough();
    component.selectedVariant = {};
    component.setFilteredProducts(relatedProductDetail.relatedProducts[2].variations);
    expect(component.setFilteredProducts).toHaveBeenCalled();
  });

  it('should call setFilteredProducts method - else check for unavailable products', () => {
    spyOn(component, 'setFilteredProducts').and.callThrough();
    component.selectedVariant = {language: 'ru_RU', color: 'white'};
    component.item = relatedProductDetail.relatedProducts[2];
    component.setFilteredProducts(relatedProductDetail.relatedProducts[2].variations);
    expect(component.setFilteredProducts).toHaveBeenCalled();
  });

});
