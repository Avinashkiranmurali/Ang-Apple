import { NgModule } from '@angular/core';
import { CommonModule, TitleCasePipe, DecimalPipe } from '@angular/common';
import { PaymentRoutingModule } from './payment-routing.module';
import { PaymentComponent } from './payment.component';
import { SharedModule } from '../shared/shared.module';
import { SelectComponent } from './select/select.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SplitComponent } from './split/split.component';
import { PricingModule } from '@app/modules/pricing/pricing.module';
import { OrderSummaryComponent } from '@app/modules/payment/order-summary/order-summary.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { PaymentSummaryComponent } from '@app/modules/payment/payment-summary/payment-summary.component';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { SelectionOptionComponent } from './select/option/option.component';
import { SplitOptionComponent } from './split/option/option.component';
import { CcEntryComponent } from './cc-entry/cc-entry.component';
import { CardComponent } from './cc-entry/card/card.component';
import { BillingAddressComponent } from './cc-entry/billing-address/billing-address.component';
import { MinValidatorDirective } from '@app/modules/payment/split/min-validator.directive';
import { MaxValidatorDirective } from '@app/modules/payment/split/max-validator.directive';
import { DisplayFormatDirective } from '@app/modules/payment/split/display-format.directive';
import { CardTypeDirective } from './cc-entry/card/card-type.directive';
import { DataMaskingModule } from '@bakkt/data-masking';

@NgModule({
  declarations: [
    PaymentComponent,
    SelectComponent,
    SplitComponent,
    PaymentSummaryComponent,
    OrderSummaryComponent,
    SelectionOptionComponent,
    SplitOptionComponent,
    CcEntryComponent,
    CardComponent,
    BillingAddressComponent,
    MinValidatorDirective,
    MaxValidatorDirective,
    DisplayFormatDirective,
    CardTypeDirective
  ],
  imports: [
    CommonModule,
    PaymentRoutingModule,
    SharedModule,
    FormsModule,
    NgbModule,
    PricingModule,
    ReactiveFormsModule,
    DataMaskingModule
  ],
  exports: [
    SharedModule
  ],
  providers: [
    TitleCasePipe,
    DecimalPipe,
    CurrencyPipe
  ]
})

  export class PaymentModule { }
