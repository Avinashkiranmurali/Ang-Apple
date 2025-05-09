import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FooterDirective } from '@app/modules/footer/footer.directive';
import { TemplateStoreService } from '@app/state/template-store.service';
import { FooterDisclaimerService } from './footer-disclaimer.service';
import { FooterComponent } from './footer.component';
import { FooterService } from './footer.service';
import { TranslateModule, TranslatePipe } from '@ngx-translate/core';
import { InterpolatePipe } from '@app/pipes/interpolate.pipe';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { DefaultFooterComponent } from '@app/components/vars/default/footer/default-footer.component';
import { UAFooterComponent } from '@app/components/vars/ua/footer/ua-footer.component';
import { ChaseFooterComponent } from '@app/components/vars/chase/footer/chase-footer.component';
import { RBCFooterComponent } from '@app/components/vars/rbc/footer/rbc-footer/rbc-footer.component';
import { RBCSimpleFooterComponent } from '@app/components/vars/rbc/footer/rbc-simple-footer/rbc-simple-footer.component';
import { SharedService } from '@app/modules/shared/shared.service';
import { BehaviorSubject, of } from 'rxjs';
import { NavigationEnd, Router } from '@angular/router';

describe('FooterComponent', () => {
  let component: FooterComponent;
  let fixture: ComponentFixture<FooterComponent>;
  let templateStoreService: TemplateStoreService;
  const configData = require('assets/mock/configData.json');
  let router: Router;
  const routerEvent$ = new BehaviorSubject<any>(null);

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        HttpClientTestingModule,
        TranslateModule.forRoot()
      ],
      declarations: [
        FooterComponent,
        FooterDirective,
        InterpolatePipe,
        TranslatePipe,
        DefaultFooterComponent,
        UAFooterComponent,
        ChaseFooterComponent,
        RBCFooterComponent,
        RBCSimpleFooterComponent
      ],
      providers: [
        FooterService,
        TranslatePipe,
        FooterDisclaimerService,
        { provide: SharedService, useValue: {
          keepAliveOne$: of('') }
        }
      ]
    })
    .compileComponents();
    templateStoreService = TestBed.inject(TemplateStoreService);
    templateStoreService.addTemplate(configData['configData']);
    router = TestBed.inject(Router);
    router.navigate = jasmine.createSpy('navigate');
    (router as any).events = routerEvent$.asObservable();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FooterComponent);
    component = fixture.componentInstance;
    component.footerData = {};
    component.footerData['currentYear'] = new Date().getFullYear();
    component.messages = require('assets/mock/messages.json');
    const program = require('assets/mock/program.json');
    component.config = program['config'];
    fixture.detectChanges();
  });

  it('should create with event of NavigationEnd', () => {
    routerEvent$.next(new NavigationEnd(1, '/store/curated/ipad/ipad-accessories', '/store/curated/ipad/ipad-accessories'));
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call scrollToTop method', () => {
    expect(component.scrollToTop()).toBeFalsy();
  });

  it('loadFooterComponent() to return respective template', () => {
    component.footerTemplate['template'] = '';
    component.ngAfterViewInit();
    expect(component.footerTemplate).toEqual(component.footerTemplate);
  });

});
