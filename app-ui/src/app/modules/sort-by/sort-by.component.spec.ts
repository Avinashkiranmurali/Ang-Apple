import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { SortByComponent } from './sort-by.component';
import { SharedService } from '@app/modules/shared/shared.service';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { RouterTestingModule } from '@angular/router/testing';
import { SortOptionsItems } from '@app/models/filter-products';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

describe('SortByComponent', () => {
  let component: SortByComponent;
  let fixture: ComponentFixture<SortByComponent>;
  let router;
  let activatedRoute: ActivatedRoute;
  let sharedService: SharedService;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        TranslateModule.forRoot(),
        RouterTestingModule,
        NgbModule,
        HttpClientTestingModule
      ],
      declarations: [ SortByComponent ],
      providers: [
        { provide: NgbActiveModal },
        SharedService,
        { provide: CurrencyFormatPipe },
        { provide: CurrencyPipe },
        { provide: DecimalPipe }
      ]
    })
    .compileComponents();
    router = TestBed.inject(Router);
    activatedRoute = TestBed.inject(ActivatedRoute);
    sharedService = TestBed.inject(SharedService);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SortByComponent);
    component = fixture.componentInstance;
    component.sortBy = {} as SortOptionsItems;
    activatedRoute.queryParams = of({});
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('Should set the default sortBy value for accessories page', () => {
    activatedRoute.snapshot.params = {subcat: 'all-accessories'};
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('Should set the sortBy value', () => {
    activatedRoute.queryParams = of({sort: 'byPriceHighToLow'});
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('Should set the default sortBy value for search page', () => {
    spyOnProperty(router, 'url', 'get').and.returnValue('/store/search/airpods');
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

  it('expect return zero', () => {
    spyOn(sharedService, 'returnZero').and.callThrough();
    sharedService.returnZero();
    expect(sharedService.returnZero).toHaveBeenCalled();
  });

  it('should call closePopover - sortBy', () => {
    component.closePopover();
    expect(component.isOpen).toBeFalsy();
  });

  it('should call popupTabEvent - sortBy', () => {
    component.popupTabEvent();
    expect(component.isOpen).toBeTruthy();
  });
});
