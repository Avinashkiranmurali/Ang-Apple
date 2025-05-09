import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { SwatchesColorComponent } from './swatches-color.component';

describe('SwatchesColorComponent', () => {
  let component: SwatchesColorComponent;
  let fixture: ComponentFixture<SwatchesColorComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ SwatchesColorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SwatchesColorComponent);
    component = fixture.componentInstance;
    component.item = require('assets/mock/products-ipad.json');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
