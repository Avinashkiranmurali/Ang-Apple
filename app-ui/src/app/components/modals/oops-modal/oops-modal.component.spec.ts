import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { OopsModalComponent } from './oops-modal.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TemplateStoreService } from '@app/state/template-store.service';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { Router } from '@angular/router';

describe('OopsModalComponent', () => {
  let component: OopsModalComponent;
  let fixture: ComponentFixture<OopsModalComponent>;
  let service: TemplateStoreService;
  const config = require('assets/mock/configData.json');

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ OopsModalComponent ],
      providers: [
        { provide: NgbActiveModal },
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate') }
        }
      ],
      imports: [
        TranslateModule.forRoot(), RouterTestingModule
      ]
    })
    .compileComponents();
    service = TestBed.inject(TemplateStoreService);
    service.addTemplate(config['configData']);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OopsModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterAll( () => {
    const configData = require('assets/mock/configData.json');
    config['configData'].templates.buttonColor = '';
    service.addTemplate(configData['configData']);
  });

  it('should create', () => {
    config['configData'].templates.buttonColor = '#fff';
    service.addTemplate(config['configData']);
    expect(component).toBeTruthy();
  });

  it('should call complete method', () => {
    spyOn(component, 'complete').and.callThrough();
    component.complete('testUrl');
    expect(component.complete).toHaveBeenCalled();
  });

  it('should call cancel method', () => {
    spyOn(component, 'cancel').and.callThrough();
    component.cancel();
    expect(component.cancel).toHaveBeenCalled();
  });
});
