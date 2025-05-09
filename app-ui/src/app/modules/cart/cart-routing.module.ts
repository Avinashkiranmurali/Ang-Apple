import { RouterModule, Routes } from '@angular/router';

import { NgModule } from '@angular/core';
import { CartComponent } from './cart.component';

const routes: Routes = [
  {
    path: '',
    component: CartComponent,
    data: {
      brcrumb: 'Cart',
      theme: 'main-cart',
      pageName: 'BAG'
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})

export class CartRoutingModule { }
