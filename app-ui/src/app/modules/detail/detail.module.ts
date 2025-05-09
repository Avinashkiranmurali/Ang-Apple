import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DetailRoutingModule } from './detail-routing.module';
import { SharedModule } from '../shared/shared.module';
import { DetailComponent } from './detail.component';
import { PricingModule } from '../pricing/pricing.module';
import { FormsModule } from '@angular/forms';
import { AppleCareModule } from '@app/modules/apple-care/apple-care.module';
import { CarouselModule } from '@app/modules/carousel/carousel.module';
import { ZoomableSlideshowModule } from '@app/modules/zoomable-slideshow/zoomable-slideshow.module';

@NgModule({
  declarations: [
     DetailComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    PricingModule,
    DetailRoutingModule,
    FormsModule,
    AppleCareModule,
    CarouselModule,
    ZoomableSlideshowModule
  ],
  exports: [
    SharedModule
  ]

})

export class DetailModule { }
