import {
  Component,
  EventEmitter,
  Injector,
  Input,
  OnInit,
  OnDestroy,
  Output,
  ChangeDetectorRef,
  AfterContentChecked
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BreakPoint } from '@app/components/utils/break-point';
import { FilterOption } from '@app/models/filter-products';
import { isEmpty } from 'lodash';
import { Option } from '@app/models/option';
import { Subscription } from 'rxjs';
import { SharedService } from '@app/modules/shared/shared.service';

@Component({
  selector: 'app-facets-filters-component',
  templateUrl: './facets-filters.component.html',
  styleUrls: ['./facets-filters.component.scss']
})

export class FacetsFiltersComponent extends BreakPoint implements OnInit, AfterContentChecked, OnDestroy {
  expanded: object = {};
  filterLimit: Array<number>;
  filterRange: number;

  @Input() facetsFilters: Map<string, Array<Option>>;
  @Input() latestFacetsFilter: {[key: string]: Array<Option>};
  @Input() facetsScrollObj: object;
  @Input() enableResetFlag: boolean;
  @Output() closeModal: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() triggerApiEmitter: EventEmitter<object> = new EventEmitter<object>();
  @Output() triggerScrollEmitter: EventEmitter<object> = new EventEmitter<object>();

  facetsFiltersObj: object;
  currentSelection: string;
  expandCollapse: string;

  queryParamSubscribe: Subscription = null;

  constructor(
    private activatedRoute: ActivatedRoute,
    public injector: Injector,
    public sharedService: SharedService,
    private cdRef: ChangeDetectorRef,
  ) {
    super(injector);
    this.filterRange = 7;
  }

  encodeURIComponent(url){
    url = url.split('-').join('');
    url = url.split(' ').join('');
    return  url.toLowerCase();
  }

  toggleLimit(idx: number, length: number) {
    if (this.filterLimit[idx] === 7) {
      this.filterLimit[idx] = length;
    } else {
      this.filterLimit[idx] = this.filterRange;
    }
  }

  isFacetsFilterDisabled(facetOption: object) {
    const objKey = facetOption['name'];
    const objValue = facetOption['key'];
    let latestFilterOptions;
    if (facetOption['isFiltered']){
      return false;
    }
    if (this.latestFacetsFilter[objKey]){
      latestFilterOptions = this.latestFacetsFilter[objKey];
      latestFilterOptions = latestFilterOptions.filter(
        (item) => (item.name === objKey && item.key === objValue)
      );
      if (objKey.split(' ').join('').toLowerCase() === this.currentSelection && !facetOption['disabled']){
          return false;
      }
      return latestFilterOptions.length === 0;
    }
    return true;
  }

  queryParamsObj(filterOption: FilterOption): {[key: string]: string} {
    const objKey = this.encodeURIComponent(filterOption.name);
    const objValue = this.encodeURIComponent(filterOption.key);
    const currentParams = this.activatedRoute.snapshot.queryParams;
    const deepCloneParams = {...currentParams};

    // make enable disabled based on current selection
    if (this.latestFacetsFilter && !isEmpty(this.latestFacetsFilter)){
      filterOption.disabled = this.isFacetsFilterDisabled(filterOption);
    }
    if (filterOption.disabled) {
      return deepCloneParams;
    } else if (isEmpty(currentParams)) {
      return { [objKey]: objValue };
    }
    deepCloneParams[objKey] = (deepCloneParams[objKey]) ? deepCloneParams[objKey] : '';

    let paramValues = [];
    if (deepCloneParams[objKey]){
      paramValues = deepCloneParams[objKey].split('-');
    }
    const index = paramValues.indexOf(objValue);
    if (index  > -1) {
      paramValues.splice(index, 1);
    }else{
      paramValues.push(objValue);
    }
    delete deepCloneParams[objKey];

    if (paramValues.length > 0){
      delete deepCloneParams[objKey];
      deepCloneParams[objKey] =  paramValues.join('-');
    }
    return deepCloneParams;
  }

  setFiltered(name: string, facetOption: object) {
    const objKey = this.encodeURIComponent(name);
    const objValue = this.encodeURIComponent(facetOption['key']);
    const currentParams = this.activatedRoute.snapshot.queryParams;

    if (!isEmpty(currentParams) && currentParams[objKey] ) {
      facetOption['isFiltered'] = currentParams[objKey].split('-').indexOf(objValue) > -1;
    } else {
      facetOption['isFiltered'] = false;
    }
    return facetOption['isFiltered'];
  }

  closeActiveModal() {
    this.closeModal.emit(false);
  }

  ngOnInit(): void {
    this.expandCollapse = 'expanded';
    setTimeout(() => {
      this.expandCollapse = '';
    }, 100);

    Object.keys(this.facetsFilters).forEach((key, index) => {
      this.expanded[index] = false;
      this.facetsFilters[key].forEach((element) => {
        element.isFiltered = false;
        element.disabled = false;
      });
    });
    this.expanded[0] = true;

    this.filterLimit = Array(
      Object.keys(this.facetsFilters).length
    ).fill(this.filterRange);

    this.queryParamSubscribe = this.activatedRoute.queryParams.subscribe(params => {
      this.doFilterSelection(params);
    });


  }

  ngAfterContentChecked(): void {
    this.cdRef.detectChanges();
  }

  doFilterSelection(queryParams){
    const paramsDeepCopy = {...queryParams};

    this.facetsFiltersObj = {};
    Object.keys(this.facetsFilters).forEach((key, index) => {

      this.facetsFilters[key].forEach((facetOption, optIndex) => {
        this.facetsFilters[key][optIndex].isFiltered = this.setFiltered(key, facetOption);
      });
      const seletedFacetsFilters = this.facetsFilters[key].filter(
        (item) => item.isFiltered === true
      );

      if (seletedFacetsFilters.length > 0) {
          this.facetsFiltersObj[key] = seletedFacetsFilters;
      }
      // do expand or collapse
      this.expanded[index] = index === 0 || seletedFacetsFilters.length > 0;
      if (!this.facetsFiltersObj.hasOwnProperty(key)) {
        this.filterLimit[index] = this.filterRange;
      }
    });
    delete paramsDeepCopy.sort;
    const keys = Object.keys(paramsDeepCopy);
    // Find the current selection
    this.currentSelection = undefined;
    if (keys.length > 0){
      this.currentSelection = keys[keys.length - 1];
    }
    if (this.facetsFilters) {
      this.triggerApiEmitter.emit(this.facetsFiltersObj);
    }
  }

  ngOnDestroy(): void {
    this.queryParamSubscribe.unsubscribe();
  }

  filterGroupExpanded(){
    if (!this.isMobile) {
      setTimeout(() => {
        this.triggerScrollEmitter.emit();
      }, 100);
    }
  }

}
