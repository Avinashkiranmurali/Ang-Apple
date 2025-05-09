import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FamilyBannerComponent } from '@app/modules/landing/banners/family-banner/family-banner.component';
import { ProductBannerComponent } from '@app/modules/landing/banners/product-banner/product-banner.component';
import { MarketingBannerComponent } from '@app/modules/landing/banners/marketing-banner/marketing-banner.component';
import { MultiProductBannerComponent } from '@app/modules/landing/banners/multi-product-banner/multi-product-banner.component';
import { SharedModule } from '@app/modules/shared/shared.module';
import { ProgramInfoBannerComponent } from '@app/modules/landing/banners/program-info-banner/program-info-banner.component';
import { RouterModule } from '@angular/router';
import { ColumnGridBannerComponent } from '@app/modules/landing/banners/column-grid-banner/column-grid-banner.component';
import { LandingRoutingModule } from './landing-routing.module';
import { LandingComponent } from '@app/modules/landing/landing.component';
import { CarouselModule } from '@app/modules/carousel/carousel.module';
import { SlideshowModule } from '../slideshow/slideshow.module';

@NgModule({
  declarations: [
    ProgramInfoBannerComponent,
    MultiProductBannerComponent,
    MarketingBannerComponent,
    ProductBannerComponent,
    FamilyBannerComponent,
    ColumnGridBannerComponent,
    LandingComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    RouterModule,
    LandingRoutingModule,
    SlideshowModule,
    CarouselModule
  ],
  exports: [
    ProgramInfoBannerComponent,
    MultiProductBannerComponent,
    MarketingBannerComponent,
    LandingComponent,
    ProductBannerComponent,
    FamilyBannerComponent,
    ColumnGridBannerComponent
  ]
})
export class LandingModule { }
