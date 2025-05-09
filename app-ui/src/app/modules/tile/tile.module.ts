import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { PricingModule } from '@app/modules/pricing/pricing.module';
import { SharedModule } from '@app/modules/shared/shared.module';
import { TileComponent } from './tile.component';
import { RouterModule } from '@angular/router';

@NgModule({
  declarations: [
    TileComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    NgbModule,
    PricingModule,
    SharedModule,
    RouterModule
  ],
  exports: [
    TileComponent
  ]
})

export class TileModule { }
