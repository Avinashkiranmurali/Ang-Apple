import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GiftPromoComponent } from './gift-promo.component';
import { GiftPromoRoutingModule } from './gift-promo-routing.module';
import { SharedModule } from '@app/modules/shared/shared.module';
import { PricingModule } from '@app/modules/pricing/pricing.module';

@NgModule({
  declarations: [GiftPromoComponent],
  imports: [
    CommonModule,
    GiftPromoRoutingModule,
    SharedModule,
    PricingModule
  ]
})
export class GiftPromoModule { }
