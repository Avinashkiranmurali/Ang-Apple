import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { IdologyLibModule, IdologyLibService } from '@bakkt/idology-lib';
import { IdologyModelComponent } from './idology-model.component';

describe('IdologyModelComponent', () => {
  let component: IdologyModelComponent;
  let fixture: ComponentFixture<IdologyModelComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ IdologyModelComponent ],
      providers: [
        NgbActiveModal,
        IdologyLibService
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        IdologyLibModule,
        TranslateModule.forRoot()
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IdologyModelComponent);
    component = fixture.componentInstance;
    component.validator = require('assets/mock/idology.json');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should placeOrder', () => {
    spyOn(component, 'placeOrder').and.callThrough();
    component.placeOrder('test');
    expect(component.placeOrder).toHaveBeenCalled();
  });
});
