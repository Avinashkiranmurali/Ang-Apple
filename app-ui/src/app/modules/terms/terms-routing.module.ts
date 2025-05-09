import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { TermsComponent } from './terms.component';

const routes: Routes = [
  {
    path: '',
    component: TermsComponent,
    data: {
      brcrumb: 'Terms',
      theme: 'main-terms',
      pageName: 'TERMS'
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})

export class TermsRoutingModule { }
