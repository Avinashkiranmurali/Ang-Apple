import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { SharedModule } from '@app/modules/shared/shared.module';
import { SortByComponent } from './sort-by.component';
import { RouterModule } from '@angular/router';
import { A11yModule } from '@angular/cdk/a11y';

@NgModule({
  declarations: [
    SortByComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    NgbModule,
    SharedModule,
    RouterModule,
    A11yModule
  ],
  exports: [
    SortByComponent
  ]
})

export class SortByModule { }
