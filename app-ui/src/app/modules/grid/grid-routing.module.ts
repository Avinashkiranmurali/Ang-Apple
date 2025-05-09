import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { GridComponent } from './grid.component';

const routes: Routes = [
  {
    path: '',
    component: GridComponent,
    data : {
      brcrumb: 'Results',
      brcrumbVal: 'subcatname',
      theme: 'main-browse-subcategory',
      pageName: 'PGP'
    }
  },
  {
    path: ':subcat',
    component: GridComponent,
    data : {
      brcrumb: 'Results',
      brcrumbVal: 'subcatname',
      theme: 'main-curated-subcategory',
      pageName: 'AGP',
      analyticsObj: {
        pgName: 'apple_products:plp:acc_accessories:<product>',
        pgType: 'plp',
        pgSectionType: 'products|merchandise'
      }
    }
  },
  {
    path: ':subcat/:addCat',
    component: GridComponent,
    data : {
      brcrumb: 'Additional',
      brcrumbVal: 'addcatname',
      pageName: 'AGP',
      theme: 'main-curated-addcategory'
    }
  },
  {
    path: ':subcat/:addCat/:psid',
    loadChildren: () => import('../detail/detail.module').then(module => module.DetailModule),
    data: {
      brcrumb: 'Detail',
      brcrumbVal: 'detailsname',
      theme: 'main-curated-detail',
      pageName: 'PDP',
      analyticsObj: {
        pgName: 'apple_products:pdp:<product>',
        pgType: 'pdp',
        pgSectionType: 'products|merchandise'
      }
    }
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class GridRoutingModule { }
