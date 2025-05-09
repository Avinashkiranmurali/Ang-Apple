import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PostbackComponent } from './postback.component';
import { PostbackRoutingModule } from './postback-routing.module';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [
    PostbackComponent
],
  imports: [
    CommonModule,
    FormsModule,
    PostbackRoutingModule
    ],
  exports: [
    ]
})

export class PostbackModule { }
