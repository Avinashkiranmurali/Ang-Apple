import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../shared/shared.module';
import { PricingModule } from '../pricing/pricing.module';
import { AppleCareComponent } from './apple-care.component';
import { AppleCareModalComponent } from '@app/components/modals/apple-care-modal/apple-care-modal.component';

@NgModule({
  declarations: [
    AppleCareComponent,
    AppleCareModalComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    PricingModule
  ],
  exports: [
    AppleCareComponent,
    AppleCareModalComponent
  ]
})

export class AppleCareModule { }
