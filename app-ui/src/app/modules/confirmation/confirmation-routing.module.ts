import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ConfirmationComponent } from '@app/modules/confirmation/confirmation.component';

const routes: Routes = [
  {
    path: '',
    component: ConfirmationComponent,
    data: {
      brcrumb: 'Confirmation',
      theme: 'main-confirm',
      pageName: 'CONFIRMATION'
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ConfirmationRoutingModule { }
