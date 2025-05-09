import { SimpleChanges } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { SubNavComponent } from './sub-nav.component';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';

describe('SubNavComponent', () => {
  let component: SubNavComponent;
  let fixture: ComponentFixture<SubNavComponent>;
  const categories = require('assets/mock/categories.json');

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ SubNavComponent ],
      imports: [
        BrowserAnimationsModule,
        RouterTestingModule,
        HttpClientTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: MessagesStoreService }
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SubNavComponent);
    component = fixture.componentInstance;
    component.mainNav = categories;
    component.subNav = categories[0]['subCategories'];
    component.currentSubcat = 'macbook-air';
    component.currentSlug = 'macbook-air';
    component.messages = require('assets/mock/messages.json');
    component.categories = categories[0]['subCategories'][0];
    component.showSubNavBar = true;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('toggleBtn should work', () => {
    component.toggleBtn('left', true);
    expect(component.leftButtonAriaHidden).toBeFalsy();

    component.toggleBtn('left', false);
    expect(component.leftButtonAriaHidden).toBeTruthy();

    component.toggleBtn('right', true);
    expect(component.rightButtonAriaHidden).toBeFalsy();

    component.toggleBtn('right', false);
    expect(component.rightButtonAriaHidden).toBeTruthy();
  });

  it('scrollNav should work', () => {
    component.scrollNav('left');
    expect(component.checkScrollButtons).toBeFalsy();

    component.scrollNav('right');
    expect(component.checkScrollButtons).toBeFalsy();
  });

  it('executeSubMenuAnimation should work', () => {
    component.executeSubMenuAnimation();
    expect(component.animationState).toEqual('offset');
  });

  it('should call window resize method when resize event dispatch', () => {
    spyOn(component, 'onResize').and.callThrough();
    window.dispatchEvent(new Event('resize'));
    fixture.detectChanges();
    expect(component.onResize).toHaveBeenCalled();
  });

  it('should trigger ngOnChange method - not to reset filter & sort', () => {
    spyOn(component, 'ngOnChanges').and.callThrough();
    const changesObj: SimpleChanges = Object.assign({});
    component.ngOnChanges(changesObj);
    expect(component.ngOnChanges).toHaveBeenCalled();
  });

  it('should call getNavStateParams method', () => {
    const category = require('assets/mock/product-detail.json');
    category['categories'][0]['detailUrl'] = 'test/url';
    const params = {
      category: 'ipad',
      subcat: 'ipad-pro',
      psid: '30001MXG22LL/A'
    };
    expect(component.getNavStateParams(category['categories'][0], null, params)).toEqual('curated/ipad/ipad-pro/test/url');
  });

  it('should call getNavStateParams method - else check', () => {
    const category = require('assets/mock/product-detail.json');
    category['categories'][0]['detailUrl'] = 'ipad-pro/30001MXG22LL';
    const params = {
      category: 'ipad',
      subcat: 'ipad-pro',
      psid: '30001MXG22LL/A',
      addCat: 'ipad-pro'
    };
    expect(component.getNavStateParams(category['categories'][0], 'browse', params)).toEqual('browse/ipad/ipad-pro/30001MXG22LL');
  });

});
