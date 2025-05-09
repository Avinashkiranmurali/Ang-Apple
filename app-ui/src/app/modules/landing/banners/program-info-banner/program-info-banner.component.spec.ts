import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ProgramInfoBannerComponent } from './program-info-banner.component';
import { SafePipe } from '@app/pipes/safe.pipe';

describe('ProgramInfoBannerComponent', () => {
  let component: ProgramInfoBannerComponent;
  let fixture: ComponentFixture<ProgramInfoBannerComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ProgramInfoBannerComponent, SafePipe ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProgramInfoBannerComponent);
    component = fixture.componentInstance;
    component.programBanner = {};
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
