import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { CheckoutComponent } from '@app/modules/checkout/checkout.component';

const routes: Routes = [
  {
    path: '',
    component: CheckoutComponent,
    data: {
      brcrumb: 'Checkout',
      theme: 'main-checkout',
      pageName: 'REVIEW'
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CheckoutRoutingModule { }
