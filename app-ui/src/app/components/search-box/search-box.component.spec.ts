import { HttpClientModule } from '@angular/common/http';
import { ComponentFixture, TestBed, fakeAsync, tick, waitForAsync } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '@app/services/product.service';
import { UserStoreService } from '@app/state/user-store.service';
import { of, Subscription } from 'rxjs';
import { SearchBoxComponent } from './search-box.component';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { By } from '@angular/platform-browser';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Component } from '@angular/core';
import { NgbPopover, NgbPopoverModule } from '@ng-bootstrap/ng-bootstrap';
import { QuickLinksComponent } from '@app/components/quick-links/quick-links.component';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

// Simple test component that will not in the actual app
@Component({
  template:
  `<a id="mobile-nav" role="button" popoverClass="mobile-main-nav-menu" class=""
    #mobileNav="ngbPopover" triggers="manual" placement="bottom-left" [ngbPopover]="menuContent">
  </a>
  <ng-template #menuContent >
    <div class="search-overlay">
    </div>
  </ng-template>`
})
class TestComponent {}

describe('SearchBoxComponent', () => {
  let component: SearchBoxComponent;
  let fixture: ComponentFixture<SearchBoxComponent>;
  let testFixture: ComponentFixture<TestComponent>;
  let httpTestingController: HttpTestingController;
  let productService: ProductService;
  const programData = require('assets/mock/program.json');
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        SearchBoxComponent,
        TestComponent,
        QuickLinksComponent
      ],
      imports: [
        FormsModule,
        TranslateModule.forRoot(),
        HttpClientModule,
        RouterTestingModule,
        NgbPopoverModule,
        HttpClientTestingModule
      ],
      providers: [
        { provide: UserStoreService, useValue: userData },
        { provide: ProductService },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({
              subcat: 'all-accessories',
              category: 'accessories',
              keyword: 'music',
            }),
          },
        },
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate').and.returnValue(new Promise((resolve) => resolve(true))) }
        },
        { provide: CurrencyFormatPipe },
        { provide: CurrencyPipe },
        { provide: DecimalPipe }
      ]
    })
    .compileComponents();
    httpTestingController = TestBed.inject(HttpTestingController);
    productService = TestBed.inject(ProductService);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchBoxComponent);
    testFixture = TestBed.createComponent(TestComponent);
    component = fixture.componentInstance;
    component.searchPage = false;
    component.isSearchTextEntered = false;
    component.terms = 'accessories';
    component.quickLinks = require('assets/mock/quickLinks.json');
    component.searchFromNavBar = true;
    component.showQuickLinks = true;
    component.messages = require('assets/mock/messages.json');
    component.isTablet = true;
    fixture.detectChanges();
  });

  afterEach(() => {
    TestBed.resetTestingModule();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should the search field displayed on navigation bar', fakeAsync(() => {
    const searchIcon = fixture.debugElement.query(By.css('.mainnav-search'));
    const searchIconElement = searchIcon.nativeElement;
    searchIconElement.click();
    tick(500);
    fixture.detectChanges();
    const navBarSearchField = fixture.debugElement.query(By.css('#search-input'));
    const searchInputFieldElement = navBarSearchField.nativeElement;
    expect(searchInputFieldElement).toHaveClass('on-nav-bar');

  }));

  it('should focus on search field', waitForAsync(() => {
    component.isTablet = false;
    component.isDesktop = false;
    fixture.detectChanges();
    spyOn(component, 'focusSearchInput').and.callThrough();
    component.focusSearchInput();
    expect(component.mobileSearchFocused).toBeTruthy();
    expect(component.focusSearchInput).toHaveBeenCalled();
  }));

  it('should blur search field', () => {
    spyOn(component, 'blurSearchInput').and.callThrough();
    component.blurSearchInput();
    expect(component.blurSearchInput).toHaveBeenCalled();
    expect(component.searchIsFocused).toBeFalsy();
    expect(component.showQuickLinks).toBeFalsy();
  });

  it('should execute when search text change', () => {
    spyOn(component, 'onSearchTextChange').and.callThrough();
    const val = {
      target: {
        value: 'test'
      }
    };
    component.onSearchTextChange(val);
    expect(component.onSearchTextChange).toHaveBeenCalled();
    expect(component.isSearchTextEntered).toBeTruthy();
  });

  it('should reset search form', () => {
    spyOn(component, 'resetSearchForm').and.callThrough();
    component.resetSearchForm();
    expect(component.resetSearchForm).toHaveBeenCalled();
    expect(component.isSearchTextEntered).toBeFalsy();
  });

  it('should call redirectToFirst method', () => {
    spyOn(component, 'redirectToFirst').and.callThrough();
    const event = new Event('click');
    event['which'] = 9;
    event['shiftKey'] = false;
    const dummyElement = document.createElement('div');
    document.getElementById = jasmine.createSpy('HTML Element').and.returnValue(dummyElement);
    component.redirectToFirst(event, dummyElement);
    expect(component.redirectToFirst).toHaveBeenCalled();
  });

  it('should call redirectToLast method', () => {
    spyOn(component, 'redirectToLast').and.callThrough();
    const dummyElement = document.createElement('div');
    document.getElementById = jasmine.createSpy('HTML Element').and.returnValue(dummyElement);
    const event = new Event('click');
    event['which'] = 9;
    event['shiftKey'] = true;
    fixture.detectChanges();
    component.redirectToLast(event, dummyElement);
    expect(component.redirectToLast).toHaveBeenCalled();
  });

  it('should trigger hostListeners event for keydown', () => {
    spyOn(component, 'onKeydownHandler').and.callThrough();
    const event = new KeyboardEvent('keydown', {key: 'escape'});
    fixture.detectChanges();
    document.dispatchEvent(event);
    expect(component.onKeydownHandler).toHaveBeenCalled();
  });

  it('should submit search form - no redirectUrl & alternateSearchText', () => {
    fixture.detectChanges();
    const compiled = fixture.debugElement.nativeElement;
    // Supply id of your form below formID
    const getForm = fixture.debugElement.query(By.css('form'));
    expect(getForm.triggerEventHandler('submit', compiled)).toBeUndefined();

    const fakeResponse = require('assets/mock/facets-filters.json');
    fakeResponse['searchRedirect'] = null;
    // Expect a call to this URL
    const req = httpTestingController.expectOne(productService.baseUrl + 'filterProducts');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');
    // Respond with the fake data when called
    req.flush(fakeResponse);
  });

  it('should submit search form - redirectUrl', () => {
    fixture.detectChanges();
    const compiled = fixture.debugElement.nativeElement;
    // Supply id of your form below formID
    const getForm = fixture.debugElement.query(By.css('form'));
    expect(getForm.triggerEventHandler('submit', compiled)).toBeUndefined();

    const fakeResponse = require('assets/mock/facets-filters.json');
    fakeResponse['searchRedirect'] = {
      redirectURL: 'testUrl'
    };
    // Expect a call to this URL
    const req = httpTestingController.expectOne(productService.baseUrl + 'filterProducts');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');
    // Respond with the fake data when called
    req.flush(fakeResponse);
  });

  it('should submit search form - alternateSearchText', () => {
    const directive = testFixture.debugElement.query(By.directive(NgbPopover));
    component.mobileNav = directive.references['mobileNav'];
    fixture.detectChanges();
    const compiled = fixture.debugElement.nativeElement;
    // Supply id of your form below formID
    const getForm = fixture.debugElement.query(By.css('form'));
    expect(getForm.triggerEventHandler('submit', compiled)).toBeUndefined();

    const fakeResponse = require('assets/mock/facets-filters.json');
    fakeResponse['searchRedirect'] = {
      alternateSearchText: 'testUrl'
    };
    // Expect a call to this URL
    const req = httpTestingController.expectOne(productService.baseUrl + 'filterProducts');
    // Assert that the request is a GET
    expect(req.request.method).toEqual('POST');
    // Respond with the fake data when called
    req.flush(fakeResponse);
  });

  it('should unsubscribe on destroy', () => {
    component['subscriptions'].push(new Subscription());
    component.ngOnDestroy();
    expect(component.ngOnDestroy).toBeTruthy();
  });

  it('should call onSearchClose method', () => {
    spyOn(component, 'onSearchClose').and.callThrough();
    component.onSearchClose();
    expect(component.onSearchClose).toHaveBeenCalled();
  });

  it('should call popupTabEvent method', waitForAsync(() => {
    spyOn(component, 'popupTabEvent').and.callThrough();
    component.popupTabEvent();
    expect(component.popupTabEvent).toHaveBeenCalled();
  }));

});
