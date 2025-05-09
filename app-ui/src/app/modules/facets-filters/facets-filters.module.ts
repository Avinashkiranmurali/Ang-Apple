import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { PricingModule } from '@app/modules/pricing/pricing.module';
import { SharedModule } from '@app/modules/shared/shared.module';
import { FacetsFiltersComponent } from './facets-filters.component';
import { RouterModule } from '@angular/router';
import { A11yModule } from '@angular/cdk/a11y';

@NgModule({
  declarations: [
    FacetsFiltersComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    NgbModule,
    PricingModule,
    SharedModule,
    RouterModule,
    A11yModule
  ],
  exports: [
    FacetsFiltersComponent
  ]
})

export class FacetsFiltersModule { }
