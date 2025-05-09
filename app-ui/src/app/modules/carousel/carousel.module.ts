import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { PricingModule } from '@app/modules/pricing/pricing.module';
import { SharedModule } from '@app/modules/shared/shared.module';
import { CarouselComponent } from './carousel.component';

@NgModule({
  declarations: [
    CarouselComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    NgbModule,
    PricingModule,
    SharedModule,
    RouterModule
  ],
  exports: [
    CarouselComponent
  ]
})
export class CarouselModule { }
