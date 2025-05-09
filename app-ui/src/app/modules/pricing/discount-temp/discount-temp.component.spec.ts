import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { DiscountTempComponent } from './discount-temp.component';

describe('DiscountTempComponent', () => {
  let component: DiscountTempComponent;
  let fixture: ComponentFixture<DiscountTempComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ DiscountTempComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DiscountTempComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
