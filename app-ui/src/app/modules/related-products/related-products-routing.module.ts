import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PricingModule } from '@app/modules/pricing/pricing.module';
import { RelatedProductsComponent } from './related-products.component';


const routes: Routes = [{
  path: '',
  component: RelatedProductsComponent,
  data: {
    brcrumb: 'RelatedProducts',
    theme: 'main-related-products',
    pageName: 'RELATED-PRODUCTS'
  }
}];

@NgModule({
  imports: [RouterModule.forChild(routes), PricingModule],
  exports: [RouterModule]
})
export class RelatedProductsRoutingModule { }
