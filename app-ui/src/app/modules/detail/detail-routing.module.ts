import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DetailComponent } from '@app/modules/detail/detail.component';

const routes: Routes = [
  {
    path: '',
    component: DetailComponent,
    data: {
      brcrumb: 'Detail',
      brcrumbVal: 'detailsname',
      theme: 'main-browse-detail',
      pageName: 'PDP'
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DetailRoutingModule { }
