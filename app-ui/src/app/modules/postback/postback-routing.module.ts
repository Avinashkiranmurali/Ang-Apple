import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { PostbackComponent } from './postback.component';


const routes: Routes = [
  {
    path: '',
    component: PostbackComponent,
    data : {
      theme: 'post-back',
      pageName: 'POSTBACK'
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PostbackRoutingModule { }
