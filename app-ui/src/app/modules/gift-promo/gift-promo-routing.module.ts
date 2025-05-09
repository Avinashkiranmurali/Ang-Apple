import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { GiftPromoComponent } from './gift-promo.component';

const routes: Routes = [
  {
    path: '',
    component: GiftPromoComponent,
    data: {
      brcrumb: 'GiftPromo',
      brcrumbVal: 'subcatname',
      theme: 'main-curated-category',
      pageName: 'GIFT_PROMO'
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class GiftPromoRoutingModule { }
