import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '@app/modules/shared/shared.module';
import { CartItemsComponent } from '@app/modules/cart/cart-items/cart-items.component';
import { CartComponent } from '@app/modules/cart/cart.component';
import { CartButtonsComponent } from '@app/modules/cart/cart-buttons/cart-buttons.component';
import { CartRoutingModule } from '@app/modules/cart/cart-routing.module';
import { PricingModule } from '../pricing/pricing.module';
import { ItemEngraveComponent } from '@app/modules/cart/item-engrave/item-engrave.component';
import { AppleCareModule } from '@app/modules/apple-care/apple-care.module';
import { CartGiftPromoModule } from '@app/modules/cart-gift-promo/cart-gift-promo.module';
import { CarouselModule } from '@app/modules/carousel/carousel.module';

@NgModule({
  declarations: [
    CartComponent,
    CartItemsComponent,
    CartButtonsComponent,
    ItemEngraveComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    CartRoutingModule,
    FormsModule,
    PricingModule,
    CartGiftPromoModule,
    AppleCareModule,
    CarouselModule
  ],
  exports: [
    CartComponent,
    CartItemsComponent,
    CartButtonsComponent,
    ItemEngraveComponent
  ]
})
export class CartModule { }
