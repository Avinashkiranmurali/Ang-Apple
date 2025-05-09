import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GridRoutingModule } from './grid-routing.module';
import { GridComponent } from './grid.component';
import { SharedModule } from '@app/modules/shared/shared.module';
import { NgbActiveModal, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { StoreModule } from '@app/modules/store/store.module';
import { TileModule } from '../tile/tile.module';
import { FacetsFiltersModule } from '../facets-filters/facets-filters.module';
import { SortByModule } from '../sort-by/sort-by.module';

@NgModule({
  declarations: [
    GridComponent,
  ],
  imports: [
    CommonModule,
    GridRoutingModule,
    SharedModule,
    NgbModule,
    StoreModule,
    TileModule,
    FacetsFiltersModule,
    SortByModule
  ],
  providers: [
    NgbActiveModal
  ]
})

export class GridModule { }
