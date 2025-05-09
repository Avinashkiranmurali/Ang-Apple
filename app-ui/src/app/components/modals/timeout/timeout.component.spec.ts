import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TimeoutComponent } from './timeout.component';
import { TranslateModule } from '@ngx-translate/core';
import { SharedService } from '@app/modules/shared/shared.service';
import { of } from 'rxjs';

describe('TimeoutComponent', () => {
  let component: TimeoutComponent;
  let fixture: ComponentFixture<TimeoutComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ TimeoutComponent ],
      imports: [ TranslateModule.forRoot() ],
      providers: [
        { provide: NgbActiveModal },
        {
          provide: SharedService, useValue: {
            sessionTypeAction: () => of('signIn')
          }
        }
      ],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TimeoutComponent);
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

  it('should call continue function and return false', () => {
    const result = component.continue();
    expect(result).toBe(false);
  });
});
