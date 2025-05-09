import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ConsentFormComponent } from './consent-form.component';
import { TranslateModule } from '@ngx-translate/core';
import { Router } from '@angular/router';

describe('ConsentFormComponent', () => {
  let component: ConsentFormComponent;
  let fixture: ComponentFixture<ConsentFormComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ConsentFormComponent ],
      imports: [ TranslateModule.forRoot() ],
      providers: [
        { provide: NgbActiveModal },
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate') }
        }
      ],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConsentFormComponent);
    component = fixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
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
    const result = component.agree();
    expect(result).toBe(false);
  });
});
