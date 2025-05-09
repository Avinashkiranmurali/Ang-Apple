import { DOCUMENT } from '@angular/common';
import { Component, ElementRef, Inject, Input, OnInit, OnDestroy, OnChanges, SimpleChanges, ViewChildren, QueryList } from '@angular/core';
import { UserStoreService } from '@app/state/user-store.service';
import { Product } from '@app/models/product';
import { Router, ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Program } from '@app/models/program';
import { SharedService } from '@app/modules/shared/shared.service';
import { Carousel } from '@app/models/carousel';
import { Messages } from '@app/models/messages';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { Subscription } from 'rxjs';
import { CarouselService } from '@app/modules/carousel/carousel.service';
import { CartService } from '@app/services/cart.service';

@Component({
  selector: 'app-carousel',
  templateUrl: './carousel.component.html',
  styleUrls: ['./carousel.component.scss']
})
export class CarouselComponent implements OnInit, OnDestroy, OnChanges {

  program: Program;
  messages: Messages;
  pointLabel: string;
  carouselDataArray: Carousel[] = [];
  carouselData: Carousel[];
  subscriptions: Subscription = new Subscription();
  loader: boolean;
  pageName: string;
  carouselEnabled: boolean;
  tilesPerCarousel = 3;
  @Input() isCartEmpty: boolean;
  @ViewChildren('carouselElem') carouselElements: QueryList<ElementRef>;

  constructor(
    @Inject(DOCUMENT) private document: Document,
    private userStore: UserStoreService,
    private messageStoreSerive: MessagesStoreService,
    private router: Router,
    private cartService: CartService,
    private translateService: TranslateService,
    public sharedService: SharedService,
    private activateRoute: ActivatedRoute,
    private carouselService: CarouselService
  ) {
    this.program = this.userStore.program;
    this.messages = this.messageStoreSerive.messages;
  }

  ngOnInit(): void {
    this.pageName = this.activateRoute.snapshot.data?.pageName;
    this.carouselEnabled = this.sharedService.isCarouselEnabled(this.pageName);
    if (this.program.formatPointName !== '') {
      this.pointLabel = this.translateService.instant(this.program.formatPointName);
    }

    if (this.carouselEnabled) {
      this.getCarouselData(this.pageName);
    }
  }

  getCarouselData(pageName: string) {
    this.loader = true;
    this.subscriptions.add(
      this.carouselService.getCarouselData(pageName).subscribe((data: Carousel[]) => {
        this.carouselData = data;
        this.mapCarouselData();
        this.loader = false;
      }, error => {
        this.loader = false;
      })
    );
  }

  mapCarouselData() {
    if (this.carouselData && this.carouselData.length > 0) {
      this.carouselData.forEach(data => {
        const carousel: Carousel = data;
        carousel.config.showNavigationArrows = data.config.showNavigationArrows === true ? data.products.length > this.tilesPerCarousel : false;
        carousel.config.showNavigationIndicators = data.config.showNavigationIndicators === true ? data.products.length > this.tilesPerCarousel : false;
        carousel['tilesPerSlide'] = this.constructTilesPerSlide(data.products);
        carousel['showCarousel'] = (this.pageName.toLowerCase() === 'bag' && data.config.showOnlyOnEmptyCart) ? (this.isCartEmpty && data.products.length > 0) : data.products.length > 0;
        this.carouselDataArray.push(carousel);
      });
      setTimeout(() => this.setMaxSlideHeight(), 100);
    }
  }

  getTilesPerRowConfiguration(data: Product[]) {
    const productsLength = data.length;
    const arrayLength = Math.ceil(productsLength / this.tilesPerCarousel);
    return new Array(arrayLength).fill(this.tilesPerCarousel, 0);
  }

  constructTilesPerSlide(data: Product[]) {
    let productsLength = 0;
    let currentRow = 0;
    const tilesPerSlide: Product[][] = [];
    const rowConfig = this.getTilesPerRowConfiguration(data);

    data.forEach((tile, tileIndex) => {
      const rowIndex = productsLength - tileIndex;
      if (rowIndex === 0) {
        if (tileIndex > 0) {
          currentRow++;
        }
        productsLength += rowConfig[currentRow];
      }
      tile['image'] = this.getProductImage(tile.images);
      tilesPerSlide[currentRow] ? tilesPerSlide[currentRow].push(tile) : tilesPerSlide[currentRow] = [tile];
    });
    return tilesPerSlide;
  }

  getProductImage(images) {
    const image = images.large;
    const imgHasQuery = image.indexOf('?') !== -1;
    const imgHasHeight = (image.indexOf('hei') !== -1) ? image.split('&hei=1200').join('') : image;
    return imgHasQuery ? imgHasHeight + '&wid=240&hei=240' : imgHasHeight + '?&wid=240&hei=240';
  }

  addToCart(product: Product) {
    const isCartPage = this.router.url.indexOf('/store/cart') > -1;
    this.cartService.addToCart(product.psid,product, isCartPage);
  }

  setMaxSlideHeight() {
    this.carouselElements.forEach((carouselElement, index) => {
      const slides = carouselElement.nativeElement.getElementsByClassName('carousel-item');
      const isMiniTile = this.carouselDataArray[index].config.styleType === 'miniTile';
      const className = isMiniTile ? 'product-tile' : 'product-section';
      let slideMaxHeight = 0;

      for (const slide of slides) {
        const products = slide.getElementsByClassName(className);
        const offsets = [];

        for (const product of products) {
          offsets.push(product['offsetHeight']);
        }

        slideMaxHeight = Math.max(slideMaxHeight, ...offsets);

        if (!isMiniTile) {
          const maxOffsetHeight = Math.max(...offsets);

          for (const product of products) {
            product['style'].height = maxOffsetHeight + 'px';
          }
        }
      }

      if (slideMaxHeight > 0) {
        slideMaxHeight = isMiniTile ? this.getSlideMaxHeightForMiniTile(carouselElement, slideMaxHeight) : slideMaxHeight;
        carouselElement.nativeElement.getElementsByClassName('carousel-inner')[0]['style'].height = slideMaxHeight + 'px';
      }
      carouselElement.nativeElement.classList.remove('visibility-style-carousel');
      this.updateCarouselAttributes(carouselElement);
    });
  }

  getSlideMaxHeightForMiniTile(carouselElement, slideMaxHeight) {
    const productSlideElement = window.getComputedStyle(carouselElement.nativeElement.getElementsByClassName('product-slide')[0] as HTMLElement);
    return (slideMaxHeight + parseFloat(productSlideElement.paddingTop) + parseFloat(productSlideElement.paddingBottom));
  }

  updateCarouselAttributes(carouselElement) {
    this.document.getElementsByClassName('carousel')[0].removeAttribute('tabindex' );
    const carouselPreviousButtonElement = carouselElement.nativeElement.querySelectorAll('.carousel-control-prev');
    carouselPreviousButtonElement.forEach(element => {
      element.setAttribute('tabindex', '0');
      element.setAttribute('aria-label', 'previous');
    });
    const carouselNextButtonElement = carouselElement.nativeElement.querySelectorAll('.carousel-control-next');
    carouselNextButtonElement.forEach(element => {
      element.setAttribute('tabindex', '0');
      element.setAttribute('aria-label', 'next');
    });
    const carouselIndicators = carouselElement.nativeElement.querySelectorAll('.carousel-indicators');
    carouselIndicators.forEach(element => {
      if (!element.classList.contains('sr-only') && !element.classList.contains('visually-hidden')) {
        const carouselPagination = element.querySelectorAll('button');
        carouselPagination.forEach((page, index) => {
          page.setAttribute('tabindex', '0');
          page.setAttribute('aria-label', `Slide ${(index + 1)}`);
        });
      } else {
        const carouselPagination = element.querySelectorAll('button');
        carouselPagination.forEach((button, index) => {
          button.setAttribute('tabindex', '-1');
          button.setAttribute('aria-label', `Slide ${(index + 1)}`);
        });
      }
    });
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes.isCartEmpty.firstChange) {
      this.carouselDataArray = [];
      if (changes.isCartEmpty.currentValue && this.carouselEnabled) {
        // After product removal (Empty Cart)
        this.getCarouselData(this.pageName);
      } else {
        // After product Added (non Empty Cart)
        this.mapCarouselData();
      }
    }
  }
}
