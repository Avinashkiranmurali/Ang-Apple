import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { FaqsComponent } from './faqs.component';

const routes: Routes = [
  {
    path: '',
    component: FaqsComponent,
    data: {
      brcrumb: 'Faqs',
      theme: 'main-faqs',
      pageName: 'FAQS'
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FaqRoutingModule { }
