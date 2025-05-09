import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { TemplateService } from '@app/services/template.service';
import { TileComponent } from './tile.component';
import { UserStoreService } from '@app/state/user-store.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { merge } from 'lodash';
import { SwatchesColorComponent } from '@app/modules/shared/swatches-color/swatches-color.component';
import { PricingTempComponent } from '@app/modules/pricing/pricing-temp/pricing-temp.component';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { DecimalPipe } from '@angular/common';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { AplImgSizePipe } from '@app/pipes/apl-img-size.pipe';
import { Router, RouterEvent } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';
import { SharedService } from '../shared/shared.service';
import { ParseNamePipe } from '@app/pipes/parse-name-pipe';

describe('TileComponent', () => {
  let component: TileComponent;
  let fixture: ComponentFixture<TileComponent>;
  let templateService: TemplateService;
  const routerEvent$ = new BehaviorSubject<RouterEvent>(null);
  let router;
  const programData = require('assets/mock/program.json');
  const configData = require('assets/mock/configData.json');
  const mockUser = require('assets/mock/user.json');
  const userData = {
    user: mockUser,
    program: programData,
    config: programData['config']
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        TileComponent,
        SwatchesColorComponent,
        PricingTempComponent,
        CurrencyFormatPipe,
        AplImgSizePipe,
        ParsePsidPipe,
        ParseNamePipe
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        PricingService,
        CurrencyFormatPipe,
        { provide: TemplateService },
        { provide: UserStoreService, useValue: userData },
        { provide: CurrencyPipe },
        { provide: DecimalPipe },
        SharedService,
        AplImgSizePipe,
        ParsePsidPipe,
        ParseNamePipe
      ]
    })
    .compileComponents();
    templateService = TestBed.inject(TemplateService);
    router = TestBed.inject(Router);
    (router as any).events = routerEvent$.asObservable();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TileComponent);
    component = fixture.componentInstance;
    templateService.userStore.user = mockUser;
    templateService.userStore.config = programData['config'];
    templateService.template = configData['configData'];
    component.item = require('assets/mock/product-detail.json');
    component.messages = require('assets/mock/messages.json');
    component.user = merge(mockUser, { program: programData});
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should validate payFrequency text', () => {
    component.user.program.config['payFrequency'] = 'Monthly';
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.payFrequency).toEqual('Monthly');
  });

  it('should execute showGridOptions', () => {
    spyOn(component, 'productConfigurationData').and.returnValue(false);
    expect(component.showGridOptions(null)).toBeFalsy();
  });

  it('should execute showMoreOptionsAvailable method', () => {
    const optionsConfigurationWithoutColorData = {
      storage: [
        {
          name: 'storage',
          value: '32GB',
          key: '32gb',
          i18Name: 'Storage',
          orderBy: 0,
          points: null,
          swatchImageUrl: null
        }
       ],
      communication: [
        {
          name: 'communication',
          value: 'Wi-Fi',
          key: 'wifi',
          i18Name: 'communication',
          orderBy: 0,
          points: null,
          swatchImageUrl: null
        }
      ]
    };
    const categoriesData = require('assets/mock/categories.json');
    const itemList = categoriesData[0]['subCategories'][0];
    itemList['optionsConfigurationData'] = optionsConfigurationWithoutColorData;
    expect(component.showMoreOptionsAvailable(itemList)).toBeFalsy();

    const optionsConfigurationData = {
      color: [
        {
          name: 'color',
          value: 'Silver',
          key: 'silver',
          i18Name: 'Color',
          orderBy: 0,
          points: null,
          swatchImageUrl: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-2020-hero-silver-cell-select_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1598915066000&qlt=95'
        },
        {
          name: 'color',
          value: 'Space Grey',
          key: 'space_gray',
          i18Name: 'Color',
          orderBy: 0,
          points: null,
          swatchImageUrl: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-2020-hero-space-cell-select_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1598915071000&qlt=95'
        }
      ]
    };
    const categories = require('assets/mock/categories.json');
    const item = categories[0]['subCategories'][0];
    item['optionsConfigurationData'] = optionsConfigurationData;
    expect(component.showMoreOptionsAvailable(item)).toBeFalsy();

    const optionsConfiguration = {
      storage: [
        {
          name: 'storage',
          value: '32GB',
          key: '32gb',
          i18Name: 'Storage',
          orderBy: 0,
          points: null,
          swatchImageUrl: null
        },
        {
          name: 'storage',
          value: '128GB',
          key: '128gb',
          i18Name: 'Storage',
          orderBy: 0,
          points: null,
          swatchImageUrl: null
        }
      ]
    };

    const nonColorItem = categories[0]['subCategories'][0];
    nonColorItem['optionsConfigurationData'] = optionsConfiguration;
    expect(component.showMoreOptionsAvailable(nonColorItem)).toBeTruthy();
  });
  
  it('should set details name', () => {
    component.setDetailsname('name');
    expect((component as any).userStore.detailsname).toEqual('name');
  });

  it('expect psid slug convert', () => {
    expect(component.psidSlugConvert('30001MWP22AM/A')).toEqual('30001MWP22AM-A');
  });

  it('expect clean data prev empty', () => {
    const item = require('assets/mock/cart.json')['cartItems'];
    component.constructRouterUrl(item[0]['productDetail'], '');

    component.routeParams = {addCat: 'mac'};
    const routerUrl = spyOnProperty(router, 'url', 'get').and.returnValue('/curated/mac');
    // router.url = '/curated/mac';
    component.constructRouterUrl(item[0]['productDetail'], '');

    routerUrl.and.returnValue('');
    // router.url = '';
    component.constructRouterUrl(item[0]['productDetail'], '');
    expect(component).toBeTruthy();
  });

  it('expect clean data prev empty', () => {
    spyOn(component, 'constructRouterUrl').and.callThrough();
    const item = require('assets/mock/product-detail.json');
    component.constructRouterUrl(item, '');
    expect(component.constructRouterUrl).toHaveBeenCalled();
  });

  it('expect clean data prev as cart', () => {
    spyOn(component, 'constructRouterUrl').and.callThrough();
    const item = require('assets/mock/product-detail.json');
    component.constructRouterUrl(item, 'cart');

    item.categories[0].templateType = 'LIST/GRID';
    component.constructRouterUrl(item, 'cart');

    item.categories[0].templateType = '';
    item.categories[0].parents[0].parents = [{}];
    item.categories[0].slug = 'giftcard';
    component.constructRouterUrl(item, 'cart');

    item.categories[0].templateType = 'CURATED';
    item.categories[0].parents[0].parents = [];
    component.constructRouterUrl(item, 'cart');

    expect(component.constructRouterUrl).toHaveBeenCalled();

  });

  it('expect clean data prev is not a cart', () => {
    spyOn(component, 'constructRouterUrl').and.callThrough();
    const item = require('assets/mock/product-detail.json');
    component.constructRouterUrl(item, '');
    spyOnProperty(router, 'url', 'get').and.returnValue('/store/curated');
    component.routeParams['addCat'] = undefined;
    component.routeParams['category'] = undefined;
    item.categories[0].slug = 'ipad-pro';
    const itemPsid  = component.psidSlugConvert('30001MXG22LL/A');
    expect(component.constructRouterUrl).toHaveBeenCalled();
  });

});
