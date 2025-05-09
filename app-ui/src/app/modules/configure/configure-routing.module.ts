import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ConfigureComponent } from './configure.component';

const routes: Routes = [
  {
    path: '',
    component: ConfigureComponent,
    data : {
      brcrumb: 'Filter',
      brcrumbVal: 'subcatname',
      theme: 'main-configure-subcategory',
      pageName: 'PCP',
      analyticsObj: {
        pgName: 'apple_products:pdp:<product>',
        pgType: 'pdp',
        pgSectionType: 'products|merchandise'
      }
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ConfigureRoutingModule { }
