import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '@app/modules/shared/shared.module';
import { PricingTempComponent } from '@app/modules/pricing/pricing-temp/pricing-temp.component';
import { DiscountTempComponent } from '@app/modules/pricing/discount-temp/discount-temp.component';
import { CartTotalTempComponent } from '@app/modules/pricing/cart-total-temp/cart-total-temp.component';
import { CartPricingTempComponent } from '@app/modules/pricing/cart-pricing-temp/cart-pricing-temp.component';
import { DisplayPriceComponent } from '@app/modules/pricing/display-price/display-price.component';
@NgModule({
  declarations: [
    PricingTempComponent,
    CartPricingTempComponent,
    DiscountTempComponent,
    CartTotalTempComponent,
    DisplayPriceComponent
  ],
  imports: [
    CommonModule,
    SharedModule
  ],
  exports: [
    PricingTempComponent,
    DiscountTempComponent,
    CartTotalTempComponent,
    CartPricingTempComponent,
    DisplayPriceComponent
  ]
})
export class PricingModule { }
