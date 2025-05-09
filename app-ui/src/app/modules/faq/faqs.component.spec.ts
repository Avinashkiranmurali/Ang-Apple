import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FaqsComponent } from './faqs.component';
import { TemplateStoreService } from '@app/state/template-store.service';
import { TranslateModule } from '@ngx-translate/core';

describe('FaqsComponent', () => {
  let component: FaqsComponent;
  let fixture: ComponentFixture<FaqsComponent>;
  let templateStoreService: TemplateStoreService;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ FaqsComponent ],
      imports: [TranslateModule.forRoot()]
    })
    .compileComponents();
    templateStoreService = TestBed.inject(TemplateStoreService);
    const configData = require('assets/mock/configData.json');
    templateStoreService.addTemplate(configData['configData']);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FaqsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
