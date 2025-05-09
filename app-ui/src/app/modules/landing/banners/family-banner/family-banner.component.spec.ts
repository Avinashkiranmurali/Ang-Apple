import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { FilterBannerPipe } from '@app/pipes/filter-banner.pipe';
import { FamilyBannerComponent } from '@app/modules/landing/banners/family-banner/family-banner.component';

describe('FamilyBannerComponent', () => {
  let component: FamilyBannerComponent;
  let fixture: ComponentFixture<FamilyBannerComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [FamilyBannerComponent, OrderByPipe, FilterBannerPipe],
      providers: [
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FamilyBannerComponent);
    component = fixture.componentInstance;
    const banner = require('assets/mock/banner.json');
    component.bannerData = banner[0];
    component.messages = require('assets/mock/messages.json');
    component.imageServerUrl = '';
    component.category = require('assets/mock/categories.json');

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call preventChildClick', () => {
    const event = new Event('click');
    component.preventChildClick(event);
  });
});
