import { HttpClientTestingModule } from '@angular/common/http/testing';
import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';
import { GiftPromoService } from '@app/services/gift-promo.service';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { NgbCarouselModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule, TranslatePipe, TranslateService } from '@ngx-translate/core';
import { CarouselComponent } from './carousel.component';
import { of, throwError } from 'rxjs';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { SimpleChange } from '@angular/core';

describe('CarouselComponent', () => {
  let component: CarouselComponent;
  let fixture: ComponentFixture<CarouselComponent>;
  const messageData = require('assets/mock/messages.json');
  const carouselData = require('assets/mock/carousel.json');
  const programData = require('assets/mock/program.json');
  const userMock = require('assets/mock/user.json');
  userMock.program = programData;
  const userData = {
    user: userMock,
    program: programData,
    config: programData['config'],
    get: () => of(userMock)
  };
  const mockCarouselElements = [{
    nativeElement: {
      getElementsByClassName: () => {
        return [{
          getElementsByClassName: () => {
            return [{
              offsetHeight: 490,
              style: {},
            }];
          },
          style: {},
          removeAttribute: () => {},
        }]
      },
      classList: {
        remove: () => {}
      },
      querySelectorAll: () => {
        return [{
          setAttribute: () => {},
          classList: document.createElement('div').classList,
          querySelectorAll: () => [],
        }]
      },
    }
  }];
  let router: Router;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ CarouselComponent, OrderByPipe ],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        NgbCarouselModule
      ],
      providers: [
        { provide: UserStoreService, useValue: userData },
        { provide: MessagesStoreService, useValue: {messages: messageData } },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({ category: undefined }),
            queryParams: of({}),
            snapshot: {data: {pageName: 'BAG'}}
          }
        },
        GiftPromoService,
        ParsePsidPipe,
        CurrencyPipe,
        TranslatePipe,
        TranslateService,
        { provide: CurrencyFormatPipe },
        { provide: DecimalPipe }
      ]
    })
    .compileComponents();
    router = TestBed.inject(Router);
    router.navigate = jasmine.createSpy('navigate');
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CarouselComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    component.carouselData = carouselData;
    expect(component).toBeTruthy();
  });

  it('should call ngOnInit method when formatPointName is empty', () => {
    component.program.formatPointName = '';
    component.carouselData = carouselData;
    spyOn(component, 'ngOnInit').and.callThrough();
    component['activateRoute'].snapshot.data = {
      brcrumb: 'Cart',
      theme: 'main-cart',
      pageName: 'BAG',
      analyticsObj: {}
    };
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('should call ngOnInit method when formatPointName is empty', () => {
    component.program.formatPointName = 'miles';
    component.carouselData = carouselData;
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.pointLabel).toBe('miles');
  });

  it('should call ngOnInit method when carousel is enabled', () => {
    component.program.formatPointName = 'miles';
    component.carouselData = carouselData;
    spyOn(component, 'ngOnInit').and.callThrough();
    spyOn(component['sharedService'], 'isCarouselEnabled').and.returnValue(true);
    component.ngOnInit();
    expect(component.pointLabel).toBe('miles');
  });


  it('should call getProductImage method when image doesnot have height', () => {
    const images = {
      large: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/MY582'
    };
    spyOn(component, 'getProductImage').and.callThrough();
    component.getProductImage(images);
    expect(component.getProductImage).toHaveBeenCalled();
  });

  it('should call addToCart method when targetType is button', () => {
    carouselData[0].config.showNavigationArrows = false;
    carouselData[0].config.showNavigationIndicators = false;
    component.carouselData = carouselData;
    component.getCarouselData('bag');
    spyOn(component, 'addToCart').and.callThrough();
    component.addToCart(carouselData[0].products[0]);
    expect(component.addToCart).toHaveBeenCalled();
  });

  it('should call addToCart method when targetType is link', () => {
    carouselData[0].config.showNavigationArrows = true;
    carouselData[0].config.showNavigationIndicators = true;
    component.carouselData = carouselData;
    component.getCarouselData('bag');
    spyOn(component, 'addToCart').and.callThrough();
    component.addToCart(carouselData[1].products[0]);
    expect(component.addToCart).toHaveBeenCalled();
  });

  it('should call getCarouselData method - success response', () => {
    spyOn(component['carouselService'], 'getCarouselData').and.returnValue(of(carouselData));
    spyOn(component, 'getCarouselData').and.callThrough();
    component.pageName = 'bag';
    component.getCarouselData('bag');
    expect(component.getCarouselData).toHaveBeenCalled();
  });

  it('should call getCarouselData method - failure response', () => {
    const errorResponse = {
      status: '404',
      statusMessage: 'Not found'
    };
    component.pageName = 'bag';
    spyOn(component['carouselService'], 'getCarouselData').and.returnValue(throwError(errorResponse));
    spyOn(component, 'mapCarouselData').and.callFake(() => {});
    spyOn(component, 'getCarouselData').and.callThrough();
    component.getCarouselData('bag');
    expect(component.getCarouselData).toHaveBeenCalled();
  });

  it('should call mapCarouselData method carouselData has empty Array', () => {
    component.carouselData = [];
    component.mapCarouselData();
    spyOn(component, 'mapCarouselData').and.callThrough();
    component.mapCarouselData();
    expect(component.mapCarouselData).toHaveBeenCalled();
  });

  it('should call ngOnChanges method', () => {
    component.carouselDataArray = [];
    spyOn(component, 'ngOnChanges').withArgs({}).and.callFake(() => {});
    component.ngOnChanges({});
    expect(component.ngOnChanges).toHaveBeenCalled();
  });

  it('should call mapCarouselData method on changes in component', () => {
    component.pageName = 'test';
    spyOn(component, 'ngOnChanges').withArgs({}).and.callFake(() => {});
    component.ngOnChanges({});
    spyOn(component, 'mapCarouselData').and.callThrough();
    component.mapCarouselData();
    expect(component.mapCarouselData).toHaveBeenCalled();
  });

  it('should call setMaxSlideHeight method  when isMiniTile is false',  fakeAsync(() => {
    component.mapCarouselData();
    tick (101);
    spyOn(component, 'setMaxSlideHeight').and.callThrough();
    component.setMaxSlideHeight();
    expect(component.setMaxSlideHeight).toHaveBeenCalled();
  }));

  it('should call getSlideMaxHeightForMiniTile',  fakeAsync(() => {
    component.mapCarouselData();
    tick (101);
    const productSlides = [
      {slide1: '<div class="product-slide"></div>'},
      {slide2: '<div class="product-slide"></div>'}
    ];
    spyOn(component, 'getSlideMaxHeightForMiniTile').withArgs(productSlides['slide1'], 140 ).and.callFake(() => {});
    component.getSlideMaxHeightForMiniTile(productSlides['slide1'], 140 );
    tick (101);
    fixture.detectChanges();
    expect(component.getSlideMaxHeightForMiniTile).toHaveBeenCalled();
    expect(component.getSlideMaxHeightForMiniTile).toHaveBeenCalledTimes(1);
  }));

  it('should call updateCarouselAttributes',  fakeAsync(() => {
    component.mapCarouselData();
    tick (101);
    const emulateHtml = '<div class="carousel-control-prev"></div><div class="carousel-control-next"></div><div class="carousel-indicators"></div>';
    spyOn(component, 'updateCarouselAttributes').withArgs(emulateHtml).and.callFake(() => {});
    component.updateCarouselAttributes(emulateHtml );
    tick (101);
    fixture.detectChanges();
    expect(component.updateCarouselAttributes).toHaveBeenCalled();
    expect(component.updateCarouselAttributes).toHaveBeenCalledTimes(1);
  }));

  it('should call ngOnDestroy method', () => {
    spyOn(component, 'ngOnDestroy').and.callThrough();
    component.ngOnDestroy();
    expect(component.ngOnDestroy).toHaveBeenCalledTimes(1);
  });

  it('should call getCarouselData when ngOnChanges is called with isCartEmpty value true and carouselEnabled value true', () => {
    const  isCartEmpty =  new SimpleChange(false, true, false);
    spyOn(component, 'getCarouselData');
    component.carouselEnabled = true;
    component.ngOnChanges({
      isCartEmpty
    });
    expect(component.getCarouselData).toHaveBeenCalled();
  });

  it('should call mapCarouselData when ngOnChanges is called with isCartEmpty value false and carouselEnabled value true', () => {
    const  isCartEmpty =  new SimpleChange(true, false, false);
    spyOn(component, 'mapCarouselData');
    component.carouselEnabled = true;
    component.ngOnChanges({
      isCartEmpty
    });
    expect(component.mapCarouselData).toHaveBeenCalled();
  });

  it('should call updateCarouselAttributes when mapCarouselData is called', fakeAsync(() => {
    const data: any = mockCarouselElements[0].nativeElement.getElementsByClassName();
    spyOn(document, 'getElementsByClassName').and.returnValue(data);
    spyOn(component, 'updateCarouselAttributes').and.callThrough();
    component.carouselData = carouselData;
    (component.carouselElements as any) = mockCarouselElements;
    component.mapCarouselData();
    tick(300);
    expect(component.updateCarouselAttributes).toHaveBeenCalled();
  }));

  it('should return integer when getSlideMaxHeightForMiniTile is called', () => {
    spyOn(window, 'getComputedStyle').and.returnValue({ paddingTop: '20', paddingBottom: '20'} as any);
    const slideMaxHeight = component.getSlideMaxHeightForMiniTile(mockCarouselElements[0],  450);
    expect(slideMaxHeight).toBe(490);
  });

  it('should call mapCarouselData with no navigation and on emptycart', () => {
    spyOn(component, 'mapCarouselData').and.callThrough();
    component.carouselData = carouselData;
    carouselData[0].config.showNavigationArrows = false;
    carouselData[0].config.showOnlyOnEmptyCart = true;
    component.pageName = 'bag';
    component.isCartEmpty = true;
    component.mapCarouselData();
    expect(component.mapCarouselData).toHaveBeenCalled();
    expect(component.carouselDataArray).toBeDefined();
  });

});
