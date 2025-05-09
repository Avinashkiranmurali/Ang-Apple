import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { OrdersComponent } from './orders.component';
import { OrderDetailsComponent } from './order-details/order-details.component';

const routes: Routes = [
  {
    path: '',
    component: OrdersComponent,
    data: {
      brcrumb: 'OrderHistory',
      theme: 'main-orderhistory',
      pageName: 'ORDERS_HISTORY',
      analyticsObj: {
        pgName: 'apple_products:order_history',
        pgType: 'admin',
        pgSectionType: 'admin'
      }
    }
  },
  {
    path: ':orderId',
    component: OrderDetailsComponent,
    data: {
      brcrumb: 'OrderStatus',
      brcrumbVal: 'orderId',
      theme: 'main-orderhistory',
      pageName: 'ORDER_STATUS',
      analyticsObj: {
        pgName: 'apple_products:order_history:order_status:<orderId>',
        pgType: 'admin',
        pgSectionType: 'admin'
      }
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class OrdersRoutingModule { }
