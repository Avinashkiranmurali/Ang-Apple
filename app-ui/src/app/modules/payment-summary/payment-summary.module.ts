import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../shared/shared.module';
import { PricingModule } from '../pricing/pricing.module';
import { PaymentSummaryComponent } from './payment-summary.component';

@NgModule({
  declarations: [
    PaymentSummaryComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    PricingModule
  ],
  exports: [
    PaymentSummaryComponent
  ]
})

export class PaymentSummaryModule { }
