import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DisplayPriceComponent } from './display-price.component';

describe('DisplayPriceComponent', () => {
  let component: DisplayPriceComponent;
  let fixture: ComponentFixture<DisplayPriceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DisplayPriceComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DisplayPriceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call getPricingTemplate if points/cash', () => {
    spyOn(component, 'getPricingTemplate').withArgs('points_cash').and.callThrough();
    component.getPricingTemplate('points_cash');
    fixture.detectChanges();
    expect(component.getPricingTemplate).toHaveBeenCalled();
  });

  it('should call getPricingTemplate if cash/point', () => {
    spyOn(component, 'getPricingTemplate').withArgs('cash_points').and.callThrough();
    component.getPricingTemplate('cash_points');
    fixture.detectChanges();
    expect(component.getPricingTemplate).toHaveBeenCalled();
  });

  it('should call getPricingTemplate if default', () => {
    spyOn(component, 'getPricingTemplate').withArgs('card').and.callThrough();
    component.getPricingTemplate('card');
    fixture.detectChanges();
    expect(component.getPricingTemplate).toHaveBeenCalled();
  });


});
