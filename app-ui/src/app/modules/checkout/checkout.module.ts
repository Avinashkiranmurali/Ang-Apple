import { NgModule } from '@angular/core';
import { CommonModule, TitleCasePipe, DecimalPipe } from '@angular/common';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { SharedModule } from '@app/modules/shared/shared.module';
import { CheckoutSummaryComponent } from '@app/modules/checkout/checkout-summary/checkout-summary.component';
import { CheckoutComponent } from '@app/modules/checkout/checkout.component';
import { CheckoutItemsComponent } from '@app/modules/checkout/checkout-items/checkout-items.component';
import { CheckoutButtonsComponent } from '@app/modules/checkout/checkout-buttons/checkout-buttons.component';
import { FormsModule } from '@angular/forms';
import { CheckoutRoutingModule } from '@app/modules/checkout/checkout-routing.module';
import { PricingModule } from '@app/modules/pricing/pricing.module';
import { TranslateModule } from '@ngx-translate/core';
import { DataMaskingModule } from '@bakkt/data-masking';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { CartGiftPromoModule } from '@app/modules/cart-gift-promo/cart-gift-promo.module';
import { AppleCareModule } from '@app/modules/apple-care/apple-care.module';
import { PaymentSummaryModule } from '../payment-summary/payment-summary.module';

@NgModule({
  declarations: [
    CheckoutComponent,
    CheckoutSummaryComponent,
    CheckoutItemsComponent,
    CheckoutButtonsComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    FormsModule,
    CheckoutRoutingModule,
    PricingModule,
    TranslateModule,
    DataMaskingModule,
    NgbModule,
    CartGiftPromoModule,
    AppleCareModule,
    PaymentSummaryModule
  ],
  exports: [],
  providers: [
    TitleCasePipe,
    DecimalPipe,
    CurrencyPipe
  ]
})
export class CheckoutModule { }
