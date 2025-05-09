import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { LandingComponent } from '@app/modules/landing/landing.component';
import { GridComponent } from '../grid/grid.component';

const routes: Routes = [
  {
    path: '',
    component: LandingComponent,
    data : {
      theme: 'main-browse-category',
      pageName: 'CLP',
      brcrumb: 'Category',
      brcrumbVal: 'catname',
      analyticsObj: {
        pgName: 'apple_products:clp:<category>',
        pgType: 'clp',
        pgSectionType: 'products|merchandise'
      }
    }
  },
  {
    path: ':category',
    component: LandingComponent,
    data : {
      theme: 'main-browse-category',
      pageName: 'CLP',
      brcrumb: 'Category',
      brcrumbVal: 'catname',
      analyticsObj: {
        pgName: 'apple_products:clp:<category>',
        pgType: 'clp',
        pgSectionType: 'products|merchandise'
      }
    }
  },
  {
    path: ':category/:subcat',
    component: GridComponent,
    data : {
      brcrumb: 'Results',
      brcrumbVal: 'subcatname',
      theme: 'main-browse-subcategory',
      pageName: 'PGP',
      analyticsObj: {
        pgName: 'apple_products:plp:<product>',
        pgType: 'plp',
        pgSectionType: 'products|merchandise'
      }
    }
  },
  {
    path: ':category/:subcat/:psid',
    loadChildren: () => import('../detail/detail.module').then(module => module.DetailModule),
    data: {
      brcrumb: 'Detail',
      brcrumbVal: 'detailsname',
      theme: 'main-browse-detail',
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
export class BrowseRoutingModule { }
