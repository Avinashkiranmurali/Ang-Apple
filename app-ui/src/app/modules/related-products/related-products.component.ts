import { Component, Injector, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { BreakPoint } from '@app/components/utils/break-point';
import { Product } from '@app/models/product';
import { DetailService } from '@app/services/detail.service';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { Messages } from '@app/models/messages';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { ModalsService } from '@app/components/modals/modals.service';
import { Config } from '@app/models/config';
import { User } from '@app/models/user';
import { CartService } from '@app/services/cart.service';
import { UserStoreService } from '@app/state/user-store.service';
import { AppConstants } from '@app/constants/app.constants';
import { AddToCartResponse } from '@app/models/cart';
import { HeapService } from '@app/analytics/heap/heap.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-related-products',
  templateUrl: './related-products.component.html',
  styleUrls: ['./related-products.component.scss']
})
export class RelatedProductsComponent extends BreakPoint implements OnInit, OnDestroy {

  productDetail: Product;
  messages: Messages;
  indexes: number[] = [];
  config: Config;
  user: User;
  cartFullError: boolean;
  pricingFullError: boolean;
  addCartError: boolean;
  relatedProductLoadError: boolean;

  private subscriptions: Subscription[] = [];

  constructor(
    public injector: Injector,
    private activatedRoute: ActivatedRoute,
    private detailService: DetailService,
    public messageStore: MessagesStoreService,
    public route: Router,
    private modalService: ModalsService,
    private userStore: UserStoreService,
    private cartService: CartService,
    private matomoService: MatomoService,
    private translateService: TranslateService,
    private heapService: HeapService,
    public sharedService: SharedService)
  {
    super(injector);
    this.messages = this.messageStore.messages;
    this.config = this.userStore.config;
    this.user = this.userStore.user;
    this.activatedRoute.params.subscribe(params => {
      const psid = params['psid'].replace('-', '/');
      this.getProductDetail(psid);
    });
  }

  ngOnInit(): void {
    this.subscriptions.push(
      this.breakPointObservable$.subscribe(() => {
        this.getLastRowIndexes();
      })
    );
    this.cartService.initError();
    this.cartService.getCart().subscribe();
  }

  getProductDetail(psid) {
    const params = psid + '?withRelatedProduct=true';
    this.subscriptions.push(
          this.detailService.getDetails(params).subscribe(data => {
          this.productDetail = data;
          this.getLastRowIndexes();
        },
        error => {
          if (error.status === 403) {
            this.sharedService.showSessionTimeOut(true);
          } else {
            this.relatedProductLoadError = true;
          }
        }
      ));
  }

  getLastRowIndexes(): void {
    if (this.productDetail) {
      this.indexes = [];
      const [threshold, loop] = this.isMobile ? [1, [1]] : [3, [1, 2, 3]];
      const productsLength = this.productDetail.relatedProducts.length;
      const rowCount = Math.ceil(productsLength / threshold);
      const lastRow = rowCount * threshold;

      for (const i of loop) {
        this.indexes.push(lastRow - i);
      }
    }
  }

  navigateToBagPage() {
    this.route.navigate(['./store/cart']);
  }

  addItemToCart(itemData) {
    if (this.config.loginRequired) {
      const expirationDate = new Date(new Date().getTime() + (15 * 60 * 1000));
      const dataToStore = {
        itemDetails: itemData.productDetail,
        expirationDate: expirationDate.toISOString(),
        secure: true
      };
      window.sessionStorage.setItem('itemToCart', JSON.stringify(dataToStore));
      this.modalService.openAnonModalComponent();
      return;
    }
    else if (this.user.browseOnly) {
      this.modalService.openBrowseOnlyComponent();
      return;
    } else {
      this.subscriptions.push(
        this.cartService.addItemToCart(itemData.psid).subscribe(
          (data: AddToCartResponse) => {
            this.cartService.setCartUpdateMessage(this.translateService.instant('itemAddedToBagMsg', {productName: itemData.detailProduct?.name}));
            this.heapService.broadcastEvent(AppConstants.analyticServices.HEAP_EVENTS.ITEM_ADDED_TO_CART, itemData.detailProduct );
            this.productDetail.relatedProducts.forEach(item => {
              item['selected'] = item.psid === itemData.psid ? true : item['selected'] ? item['selected'] : false;
            });
            this.cartService.getCart().subscribe();
          }, (error) => {
            this.cartFullError = false;
            this.pricingFullError = false;
            this.addCartError = false;

            if (error.status === 403) {
              this.sharedService.showSessionTimeOut(true);
            } else if (error.status === 400) {
              if (error.error) {
                const data = error.error;
                if (data.pricingFull) {
                  this.pricingFullError = true;
                  this.matomoService.broadcast(AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.CANONICAL_PAGE, {
                    payload: {
                      location: location.href,
                      canonicalTitle: AppConstants.analyticServices.CANONICAL_CONSTANTS.ERROR
                    }
                  });
                }
              } else {
                this.cartFullError = true;
              }
            } else {
              this.addCartError = true;
            }
          })
      );
    }
  }
  getDetails(itemData): void {
    const params = itemData['psid'];
    this.subscriptions.push(
      this.detailService.getDetails(params).subscribe(
        data => {
          const products = [];
          this.productDetail.relatedProducts.forEach((item, index) => {
            if (index === itemData['index']) {
              item = data;
            }
            products.push(item);
          });
          this.productDetail.relatedProducts = products;
        },
        error => {
          this.relatedProductLoadError = false;
          if (error.status === 403) {
            this.sharedService.showSessionTimeOut(true);
          } else {
            this.relatedProductLoadError = true;
          }
        }
      ));
  }
  trackByFn(index){
    return index;
  }
  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }
}
