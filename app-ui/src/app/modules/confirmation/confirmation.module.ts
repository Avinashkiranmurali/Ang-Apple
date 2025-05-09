import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '@app/modules/shared/shared.module';
import { OrderConfirmShippingDetailsComponent } from '@app/modules/confirmation/order-confirm-shipping-details/order-confirm-shipping-details.component';
import { OrderConfirmItemsListComponent } from '@app/modules/confirmation/order-confirm-items-list/order-confirm-items-list.component';
import { ConfirmationComponent } from './confirmation.component';
import { OrderConfirmButtonsComponent } from '@app/modules/confirmation/order-confirm-buttons/order-confirm-buttons.component';
import { ConfirmationRoutingModule } from './confirmation-routing.module';
import { PricingModule } from '@app/modules/pricing/pricing.module';
import { DecimalPipe } from '@angular/common';
import { DataMaskingModule } from '@bakkt/data-masking';
import { AppleCareModule } from '@app/modules/apple-care/apple-care.module';
import { CartGiftPromoModule } from '@app/modules/cart-gift-promo/cart-gift-promo.module';
import { PaymentSummaryModule } from '../payment-summary/payment-summary.module';

@NgModule({
  declarations: [
    ConfirmationComponent,
    OrderConfirmShippingDetailsComponent,
    OrderConfirmItemsListComponent,
    OrderConfirmButtonsComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    ConfirmationRoutingModule,
    PricingModule,
    DataMaskingModule,
    CartGiftPromoModule,
    AppleCareModule,
    PaymentSummaryModule
  ],
  providers: [DecimalPipe],
  exports: []
})
export class ConfirmationModule { }
