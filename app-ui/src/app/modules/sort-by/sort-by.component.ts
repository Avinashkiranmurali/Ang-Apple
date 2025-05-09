import { Component, Input, OnInit } from '@angular/core';
import { SortOptions, SortOptionsItems } from '@app/models/filter-products';
import { find } from 'lodash';
import { ActivatedRoute, Router } from '@angular/router';
import { SharedService } from '@app/modules/shared/shared.service';

@Component({
  selector: 'app-sort-by',
  templateUrl: './sort-by.component.html',
  styleUrls: ['./sort-by.component.scss']
})
export class SortByComponent implements OnInit {
  @Input() parentClass: string;
  sortOptions: SortOptions;
  isOpen = false;
  sortBy: SortOptionsItems;

  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    public sharedService: SharedService,
  ) {
  }

  ngOnInit(): void {
    this.sortOptions = this.sharedService.sortOptions;

    this.sortOptions.relevancy.hidden = this.router.url.indexOf('search') <= -1;

    this.activatedRoute.queryParams.subscribe(queryParams => {
      if (queryParams.sort) {
        this.sortBy = find(this.sortOptions, {label: queryParams.sort});
      } else {
        if (this.activatedRoute.snapshot.params.subcat?.split('-')[1] === 'accessories') {
          this.sortBy = this.sortOptions.popularity;
        } else {
          if (this.router.url.indexOf('search') > -1) {
            this.sortBy = this.sortOptions.relevancy;
          } else {
             this.sortBy = this.sortOptions.price_low_to_high;
          }
        }
      }
    });
  }

  closePopover() {
    this.isOpen = false;
  }

  popupTabEvent() {
    this.isOpen = true;
  }

}
