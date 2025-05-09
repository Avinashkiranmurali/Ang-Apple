import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CartService } from '@app/services/cart.service';
import { GiftPromoService } from '@app/services/gift-promo.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { TranslateService } from '@ngx-translate/core';
import { PricingModel } from '@app/models/pricing-model';
import { TemplateService } from '@app/services/template.service';


@Component({
  selector: 'app-gift-promo',
  templateUrl: './gift-promo.component.html',
  styleUrls: ['./gift-promo.component.scss']
})
export class GiftPromoComponent implements OnInit {

  productLoadError = false;
  giftPromoProducts: Array<{[key: string]: string}>;
  routeParams: { [key: string]: string };
  routeQueryParams: { [key: string]: string };
  giftPromoPageName: string;
  priceModel: PricingModel;

  constructor(
    private templateService: TemplateService,
    private giftPromoService: GiftPromoService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private cartService: CartService,
    private translateService: TranslateService,
    private sharedServices: SharedService
  ) {
    this.activatedRoute.params.subscribe(
      params => {
        this.routeParams = {
          cartItemId: params['cartItemId'],
          psid: params['qualifyingPsid'].replace('-', '/')
        };
      });
    this.activatedRoute.queryParams.subscribe(params => {
      this.routeQueryParams = {
        hasRelatedProduct: JSON.parse(params['hasRelatedProduct'] || 'false' )
      };
    });
    this.giftPromoPageName = this.translateService.instant('giftPromoTitle-' + this.routeParams.psid.replace('/', ''));
  }

  ngOnInit(): void {
    this.giftPromoService.getGiftPromoProducts(this.routeParams.psid).subscribe(data => {
      this.giftPromoProducts = data;
    });
  }

  parseName(item) {
    let parsed = item.replace(/\s/g, '');
    parsed = parsed.replace(/\-/g, '').replace(/"/g, '');
    const tempLng = parsed.length;
    if (tempLng > 10) {
      parsed = parsed.substring(0, 10);
    }
    return parsed;
  }

  cancelSelection() {
    if (this.routeQueryParams.hasRelatedProduct){
      this.router.navigate(['store', 'related-products', this.routeParams.psid.replace('/', '-')]);
    }else{
      this.router.navigate(['./store/cart']);
    }
  }

  addGiftItem(item) {
    // TO DO $rootScope.openTransition();
    const giftItemParams = {
      giftItem: {
        productId: item.psid
      }
    };

    this.giftPromoService.giftItemModify(giftItemParams, +this.routeParams.cartItemId).subscribe(
      data => {
        // $rootScope.closeTransition();
        if (item.isEngravable) {
          const engraveItemObj = {
            cartItemId: this.routeParams.cartItemId,
            psIdSlug: item.psid.replace(/[&\/\\#,+()$~%.'":*?<>{}]/g, '-'),
            isGiftPromo: true,
            qualifyingProduct: {
              psid: this.routeParams.psid,
              hasRelatedProduct: this.routeQueryParams.hasRelatedProduct
            }
          };
          this.sharedServices.openEngraveModalDialog(engraveItemObj);
        } else {
          if (this.routeQueryParams.hasRelatedProduct){
            this.router.navigate(['store', 'related-products', this.routeParams.psid.replace('/', '-')]);
          }else{
            this.router.navigate(['./store/cart']);
          }

        }
      },
      error => {
        // TO DO  $rootScope.closeTransition();
        if (error.status === 401 || error.status === 0) {
          // TODO sessionMgmt.showTimeout();
        } else {
          this.productLoadError = true;
        }
      }
    );
  }

}
