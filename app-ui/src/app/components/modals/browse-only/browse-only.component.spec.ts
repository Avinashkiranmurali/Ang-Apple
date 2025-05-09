import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { NgbActiveModal, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule, TranslatePipe } from '@ngx-translate/core';
import { BrowseOnlyComponent } from './browse-only.component';

describe('BrowseOnlyComponent', () => {
  let component: BrowseOnlyComponent;
  let fixture: ComponentFixture<BrowseOnlyComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        BrowseOnlyComponent,
        TranslatePipe
      ],
      providers: [
        TranslatePipe,
        { provide: NgbActiveModal }
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        NgbModule,
        TranslateModule.forRoot()
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BrowseOnlyComponent);
    component = fixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('cancel should return false', () => {
    expect(component.cancel()).toBeFalse();
  });

});
