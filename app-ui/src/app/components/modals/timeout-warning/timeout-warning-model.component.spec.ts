import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { TimeoutWarningModelComponent } from './timeout-warning-model.component';

describe('TimeoutWarningModelComponent', () => {
  let component: TimeoutWarningModelComponent;
  let fixture: ComponentFixture<TimeoutWarningModelComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ TimeoutWarningModelComponent ],
      providers: [
        {provide: NgbActiveModal},
      ],
      imports: [
        TranslateModule.forRoot()
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TimeoutWarningModelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call cancel function and return false', () => {
    const result = component.cancel();
    expect(result).toBe(false);
  });

  it('should call extendSession function and return false', () => {
    const result = component.extendSession();
    expect(result).toBe(false);
  });
});
