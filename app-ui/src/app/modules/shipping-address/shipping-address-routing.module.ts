import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ShippingAddressComponent } from './shipping-address.component';

const routes: Routes = [
  {
    path: '',
    component: ShippingAddressComponent,
    data: {
      brcrumb: 'Address',
      theme: 'main-checkout',
      pageName: 'SHIPPING_ADDRESS'
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})

export class ShippingAddressRoutingModule { }
