import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfigureRoutingModule } from './configure-routing.module';
import { SharedModule } from '../shared/shared.module';
import { ConfigureComponent } from './configure.component';
import { FormsModule } from '@angular/forms';
import { PricingModule } from '../pricing/pricing.module';
import { AppleCareModule } from '@app/modules/apple-care/apple-care.module';
import { CarouselModule } from '@app/modules/carousel/carousel.module';
import { ZoomableSlideshowModule } from '@app/modules/zoomable-slideshow/zoomable-slideshow.module';

@NgModule({
  declarations: [
    ConfigureComponent,
  ],
  imports: [
    CommonModule,
    ConfigureRoutingModule,
    SharedModule,
    FormsModule,
    PricingModule,
    AppleCareModule,
    CarouselModule,
    ZoomableSlideshowModule
  ],
  exports: []
})

export class ConfigureModule { }
