import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SlideshowComponent } from './slideshow.component';
import { RouterModule } from '@angular/router';
import { SingleBannerComponent } from './single-banner/single-banner.component';
import { FormsModule } from '@angular/forms';
import { SafePipe } from '@app/pipes/safe.pipe';
import { TemplateBannerComponent } from './template-banner/template-banner.component';
import { BannerConfigPipe } from '@app/pipes/banner-config.pipe';
import { SharedModule } from '../shared/shared.module';
import { BannerModalComponent } from '../../components/modals/banner-modal/banner-modal.component';

@NgModule({
  declarations: [
    SlideshowComponent,
    SingleBannerComponent,
    TemplateBannerComponent,
    BannerConfigPipe,
    BannerModalComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    SharedModule
  ],
  exports: [
    SlideshowComponent,
    SafePipe,
    BannerConfigPipe
  ]
})

export class SlideshowModule { }
