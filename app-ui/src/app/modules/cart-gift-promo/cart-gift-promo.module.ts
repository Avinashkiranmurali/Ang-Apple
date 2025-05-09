import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../shared/shared.module';
import { PricingModule } from '../pricing/pricing.module';
import { CartGiftPromoComponent } from './cart-gift-promo.component';
import { AppleCareModule } from '@app/modules/apple-care/apple-care.module';

@NgModule({
  declarations: [
    CartGiftPromoComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    PricingModule,
    AppleCareModule
  ],
  exports: [
    CartGiftPromoComponent
  ]
})

export class CartGiftPromoModule { }
