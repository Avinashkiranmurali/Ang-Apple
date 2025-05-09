import { NgModule } from '@angular/core';
import { CommonModule, TitleCasePipe } from '@angular/common';
import { OrdersRoutingModule } from './orders-routing.module';
import { SharedModule } from '@app/modules/shared/shared.module';
import { FormsModule } from '@angular/forms';
import { DataMaskingModule } from '@bakkt/data-masking';
import { OrderTilesComponent } from './order-tiles/order-tiles.component';
import { OrdersComponent } from './orders.component';
import { OrderDetailsComponent } from './order-details/order-details.component';
import { OrderItemsComponent } from './order-details/order-items/order-items.component';
import { OrderPaymentSummaryComponent } from './order-details/order-payment-summary/order-payment-summary.component';
import { OrderRefundSummaryComponent } from './order-details/order-refund-summary/order-refund-summary.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { PricingModule } from '@app/modules/pricing/pricing.module';
import { OrderShippingPaymentDetailsComponent } from './order-details/order-shipping-payment-details/order-shipping-payment-details.component';

@NgModule({
  declarations: [
    OrdersComponent,
    OrderTilesComponent,
    OrderDetailsComponent,
    OrderItemsComponent,
    OrderPaymentSummaryComponent,
    OrderRefundSummaryComponent,
    OrderShippingPaymentDetailsComponent
  ],
  imports: [
    CommonModule,
    OrdersRoutingModule,
    SharedModule,
    PricingModule,
    FormsModule,
    NgbModule,
    DataMaskingModule
  ],
  providers: [
    TitleCasePipe
  ]
})

export class OrdersModule {
}
