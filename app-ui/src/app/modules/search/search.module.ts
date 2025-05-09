import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchRoutingModule } from './search-routing.module';
import { SearchComponent } from './search.component';
import { StoreModule } from '@app/modules/store/store.module';
import { NgbActiveModal, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { TileModule } from '../tile/tile.module';
import { FacetsFiltersModule } from '../facets-filters/facets-filters.module';
import { SortByModule } from '../sort-by/sort-by.module';

@NgModule({
  declarations: [SearchComponent],
  imports: [
    CommonModule,
    SearchRoutingModule,
    StoreModule,
    NgbModule,
    TileModule,
    FacetsFiltersModule,
    SortByModule
  ],
  providers: [
    NgbActiveModal
  ]
})

export class SearchModule { }
