import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { SharedService } from '@app/modules/shared/shared.service';
import { LogoComponent } from './logo.component';
import { TemplateService } from '@app/services/template.service';
import { TemplateStoreService } from '@app/state/template-store.service';
import { of } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';

describe('LogoComponent', () => {
  let component: LogoComponent;
  let fixture: ComponentFixture<LogoComponent>;
  let templateStoreService: TemplateStoreService;
  const configData = require('assets/mock/configData.json');

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ LogoComponent ],
      providers: [
        { provide: TemplateService },
        { provide: SharedService, useValue: {
            sessionTypeAction: () => of('navigateBack')
          }
        }
      ],
      imports: [
        TranslateModule.forRoot()
      ]
    })
    .compileComponents();
    templateStoreService = TestBed.inject(TemplateStoreService);
    templateStoreService.template = configData['configData'];
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LogoComponent);
    component = fixture.componentInstance;
    component.headerTemplate = templateStoreService.headerTemplate;
    component.program = require('assets/mock/program.json');
    component.config = component.program['config'];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('navigateToClient must have called sessionTypeAction with navigateBack as parameter', () => {
    spyOn(component.sharedService, 'sessionTypeAction').and.callThrough();
    component.navigateToClient();
    expect(component.sharedService.sessionTypeAction).toHaveBeenCalled();
  });

  it('navigateToStore must have called sessionTypeAction with navigateToStore as parameter', () => {
    spyOn(component.sharedService, 'sessionTypeAction').and.callThrough();
    component.navigateToStore();
    expect(component.sharedService.sessionTypeAction).toHaveBeenCalled();
  });

  it('getImgSrc must return imgSrc', () => {
    spyOn(component, 'getImgSrc').and.callThrough();
    component.program.imageUrl = 'als-static.bridge2rewards.com/dev2/apple-gr/vars/delta/logo.png';
    const imgSrc = 'https://als-static.bridge2rewards.com/dev2/apple-gr/vars/delta/logo.png';
    const returnData = component.getImgSrc();
    expect(component.getImgSrc).toHaveBeenCalled();
  });

  it('getImgSrc must return imgSrc', () => {
    // const program = require('assets/mock/program.json');
    spyOn(component, 'getImgSrc').and.callThrough();
    component.program.imageUrl = 'als-static.bridge2rewards.com/dev2/apple-gr/vars/delta/logo.png';
    const imgSrc = 'https://als-static.bridge2rewards.com/dev2/apple-gr/vars/delta/logo.png';
    const returnData = component.getImgSrc();
    expect(component.getImgSrc).toHaveBeenCalled();
  });

  it('getImgSrc must return imgSrc', () => {
    component.program.imageUrl = null;
    const imgSrc = component.config.imageServerUrl + '/' + component.program.imageUrl;
    const returnData = component.getImgSrc();
    expect(returnData).toBe('');
  });
});
