import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FaqRoutingModule } from './faq-routing.module';
import { FaqsComponent } from './faqs.component';
import { SharedModule } from '@app/modules/shared/shared.module';

@NgModule({
  declarations: [
    FaqsComponent,
  ],
  imports: [
    CommonModule,
    FaqRoutingModule,
    SharedModule,
  ]
})

export class FaqModule { }
