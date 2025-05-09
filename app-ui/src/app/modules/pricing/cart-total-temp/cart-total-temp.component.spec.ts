import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { CartTotalTempComponent } from './cart-total-temp.component';

describe('CartTotalTempComponent', () => {
  let component: CartTotalTempComponent;
  let fixture: ComponentFixture<CartTotalTempComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ CartTotalTempComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CartTotalTempComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should return relevant values when ctExt is `exceeded`', () => {
    component.pricingTemplate = 'points_only';
    component.getCartTotalPriceTemp(component.pricingTemplate, 'exceeded');
    expect(component.isPoints).toBe(component.pricingTemplate.split('_')[0] === 'points');
  });

  it('should return relevant values when ctExt is `sub`', () => {
    component.pricingTemplate = 'points_only';
    const temp3 = component.getCartTotalPriceTemp(component.pricingTemplate, 'sub');
    const temp4 = (component.isPoints) ? 'totalPointsPrice' : 'totalCashPrice';
    expect(temp3).toBe('sub-total_pd-finance.htm');
    expect(component.ctPriceTempMsg).toBe(temp4);
  });

  it('getCartTotalPriceTemp to return values based on params', () => {
    expect(component.getCartTotalPriceTemp('', '')).toBe('total-single-template.htm');
  });

  it('getPricingTemp to return values based on params', () => {
    expect(component.getCartTotalPriceTemp('', 'exceeded')).toBe('exceeded_total-cash-template.htm');
  });

  it('getPricingTemp to return values based on params', () => {
    expect(component.getCartTotalPriceTemp('', 'sub')).toBe('sub-total_pd-finance.htm');
  });

  it('should return relevant values when template type is `no_pay`', () => {
    const temp = component.getCartTotalPriceTemp('no_pay', '');
    expect(temp).toBe('');
    expect(component.ctPriceTempMsg).toBe('');
  });

  it('should return relevant values when template type is `cash_points`', () => {
    const temp = component.getCartTotalPriceTemp('cash_points', '');
    expect(temp).toBe('total-' + 'cash_points' + '-template.htm');
    expect(component.ctPriceTempMsg).toBe('');
  });

  it('should return relevant values when template type is `points_cash`', () => {
    const temp = component.getCartTotalPriceTemp('points_cash', '');
    expect(temp).toBe('total-' + 'points_cash' + '-template.htm');
    expect(component.ctPriceTempMsg).toBe('');
  });
});
