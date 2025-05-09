import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { RelatedProductsRoutingModule } from './related-products-routing.module';
import { RelatedProductsComponent } from './related-products.component';
import { SharedModule } from '@app/modules/shared/shared.module';
import { TileComponent } from './tile/tile.component';
import { PricingModule } from '@app/modules/pricing/pricing.module';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [
    RelatedProductsComponent,
    TileComponent
  ],
  imports: [
    CommonModule,
    RelatedProductsRoutingModule,
    SharedModule,
    PricingModule,
    FormsModule
  ]
})
export class RelatedProductsModule { }
