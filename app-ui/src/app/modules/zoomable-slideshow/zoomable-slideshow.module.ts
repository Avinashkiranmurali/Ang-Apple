import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ZoomableSlideshowComponent } from './zoomable-slideshow.component';

@NgModule({
  declarations: [
    ZoomableSlideshowComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
  ],
  exports: [
    ZoomableSlideshowComponent
  ]
})

export class ZoomableSlideshowModule {
}
