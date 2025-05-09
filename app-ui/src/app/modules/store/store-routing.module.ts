
import {ActivatedRouteSnapshot, Resolve, Router, RouterModule, RouterStateSnapshot, Routes} from '@angular/router';
import { Injectable, NgModule } from '@angular/core';
import { EMPTY, Observable } from 'rxjs';
import { StoreComponent } from './store.component';
import { TemplateService } from '@app/services/template.service';
import { catchError } from 'rxjs/operators';
import { AppConstants } from '@app/constants/app.constants';


@Injectable({ providedIn: 'root' })
export class TemplateResolver implements Resolve<object> {
  constructor(private templateService: TemplateService,
              private router: Router ) {}

  resolve(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<any>|Promise<any>|any {
    return this.templateService.getTemplate()
      .pipe(catchError(() => {
        this.router.navigate(['/login-error']);
        return EMPTY;
      }));
  }
}

const routes: Routes = [
  {
    path: '',
    component: StoreComponent,
    data : {
      brcrumb: 'Store',
      brcrumbVal: '',
      theme: 'main-store',
      pageName: 'STORE-PARENT',
      analyticsObj: {
        pgName: 'apple_products:home',
        pgType: 'landing',
        pgSectionType: 'landing'
      }
    },
    resolve: {
      template: TemplateResolver
    },
    children: [
      {
        path: '',
        loadChildren: () => import('../landing/landing.module').then(module => module.LandingModule),
        data : {
          brcrumb: 'Store',
          brcrumbVal: '',
          theme: 'main-store',
          pageName: 'STORE',
          analyticsObj: {
            pgName: 'apple_products:home',
            pgType: 'landing',
            pgSectionType: 'landing'
          }
        }
      },
      {
        path: 'browse',
        loadChildren: () => import('../browse/browse.module').then(module => module.BrowseModule)
      },
      {
        path: 'configure/:category',
        loadChildren: () => import('../landing/landing.module').then(module => module.LandingModule),
        data : {
          brcrumb: 'Category',
          brcrumbVal: 'catname',
          theme: 'main-configure-category',
          pageName: 'CLP',
          analyticsObj: {
            pgName: 'apple_products:clp:<category>',
            pgType: 'clp',
            pgSectionType: 'products|merchandise'
          }
        }
      },
      {
        path: 'configure/:category/:subcat',
        loadChildren: () => import('../configure/configure.module').then(module => module.ConfigureModule),
        data: {
          brcrumb: 'Filter',
          brcrumbVal: 'subcatname',
          theme: 'main-configure-subcategory',
          pageName: 'PCP',
          analyticsObj: {
            pgName: 'apple_products:pdp:<product>',
            pgType: 'pdp',
            pgSectionType: 'products|merchandise'
          }
        }
      },
      {
        path: 'configure/:category/:subcat/:sku',
        loadChildren: () => import('../configure/configure.module').then(module => module.ConfigureModule)
      },
      {
        path: 'curated/:category',
        loadChildren: () => import('../grid/grid.module').then(module => module.GridModule),
        data: {
          brcrumb: 'Category',
          brcrumbVal: 'catname',
          theme: 'main-curated-category',
          pageName: 'AGP'
        }
      },
      {
        path: 'webshop/:category',
        loadChildren: () => import('../grid/grid.module').then(module => module.GridModule)
      },
      {
        path: 'shipping-address',
        loadChildren: () => import('../shipping-address/shipping-address.module').then(module => module.ShippingAddressModule),
        data: {
          brcrumb: 'Address',
          theme: 'main-checkout',
          pageName: 'SHIPPING_ADDRESS'
        }
      },
      {
        path: 'cart',
        loadChildren: () => import('../cart/cart.module').then(module => module.CartModule),
        data: {
          brcrumb: 'Cart',
          theme: 'main-cart',
          pageName: 'BAG',
          analyticsObj: {
            pgName: 'apple_products:checkout_step_1:cart',
            pgType: 'checkout',
            pgSectionType: 'apple_products'
          }
        }
      },
      {
        path: 'payment',
        loadChildren: () => import('../payment/payment.module').then(module => module.PaymentModule)
      },
      {
        path: 'checkout',
        loadChildren: () => import('../checkout/checkout.module').then(module => module.CheckoutModule),
        data: {
          brcrumb: 'Checkout',
          theme: 'main-checkout',
          pageName: 'REVIEW',
          analyticsObj: {
            pgName: 'apple_products:checkout_step_2:review',
            pgType: 'checkout',
            pgSectionType: 'apple_products'
          }
        }
      },
      {
        path: 'confirmation',
        loadChildren: () => import('../confirmation/confirmation.module').then(module => module.ConfirmationModule),
        data: {
          brcrumb: 'Confirmation',
          theme: 'main-confirm',
          pageName: 'CONFIRMATION',
          analyticsObj: {
            pgName: 'apple_products:checkout_step_3:confirmation',
            pgType: 'checkout',
            pgSectionType: 'apple_products'
          }
        }
      },
      {
        path: 'order-history',
        loadChildren: () => import('../orders/orders.module').then(module => module.OrdersModule)
      },
      {
        path: 'search/:keyword',
        loadChildren: () => import('../search/search.module').then(module => module.SearchModule),
        data: {
          brcrumb: 'Search',
          theme: 'main-search',
          pageName: 'SEARCH',
          analyticsObj: {
            pgName: 'apple_products:slp:<search_term>',
            pgType: 'slp',
            pgSectionType: 'products'
          }
        }
      },
      {
        path: 'postback',
        loadChildren: () => import('../postback/postback.module').then(module => module.PostbackModule)
      },
      {
        path: 'terms',
        loadChildren: () => import('../terms/terms.module').then(module => module.TermsModule),
        data: {
          brcrumb: 'Terms',
          theme: 'main-terms',
          pageName: 'TERMS',
          analyticsObj: {
            pgName: 'apple_products:store_policies',
            pgType: 'admin',
            pgSectionType: 'information'
          }
        }
      },
      {
        path: 'faqs',
        loadChildren: () => import('../faq/faq.module').then(module => module.FaqModule)
      },
      {
        path: 'gift-promo/:cartItemId/:qualifyingPsid',
        loadChildren: () => import('../gift-promo/gift-promo.module').then(module => module.GiftPromoModule)
      },
      {
        path: 'related-products/:psid',
        loadChildren: () => import('../related-products/related-products.module').then(module => module.RelatedProductsModule)
      },
      {
        path: '**',
        redirectTo: '',
        pathMatch: 'full'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})

export class StoreRoutingModule { }
