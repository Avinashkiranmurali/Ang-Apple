import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { PageTitleComponent } from './page-title.component';
import { of } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { ModalsService } from '@app/components/modals/modals.service';

describe('PageTitleComponent', () => {
  let component: PageTitleComponent;
  let fixture: ComponentFixture<PageTitleComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ PageTitleComponent ],
      providers: [
        {
          provide: ActivatedRoute, useValue: {
            params: of({category: 'ipad', subcat: 'ipad-accessories', addCat: 'ipad-accessories', psid: null})
          }
        },
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate'),
          url: 'testUrl' }
        },
        { provide: ModalsService, useValue: {
            openAnonModalComponent: () => ({})
          }
        }
      ],
      imports: [
        TranslateModule.forRoot(),
        RouterModule.forRoot([], { relativeLinkResolution: 'legacy' }),
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PageTitleComponent);
    component = fixture.componentInstance;
    const fakeResponse = require('assets/mock/categories.json');
    component.mainNav = fakeResponse;
    component.addcat = 'ipad-accessories';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('set pageName to pageTitle if pageTitle exists', () => {
    component.pageTitle = 'Accessories';
    component.setPageTitle();
    expect(component.pageName).toEqual(component.pageTitle);
  });

  it('set pageName to pageTitle if pageTitle does not exists', () => {
    component.pageTitle = null;
    component.category = 'ACCESSORIES';
    component.psid = '30001MXG22LL/A';
    component.addcat = 'mac';
    component.setPageTitle();
    expect(component.pageTitle).toEqual(null);
  });

  it('set pageName to pageTitle if pageTitle does not exists and category exist', () => {
    component.pageTitle = null;
    component.category = 'accessories';
    component.psid = '30001MXG22LL/A';
    component.addcat = 'mac';
    component.setPageTitle();
    expect(component.psid).toBeDefined();
  });

  it('set pageName to pageTitle if pageTitle does not exists and category, sub-category exist', () => {
    component.pageTitle = null;
    component.category = 'ipad';
    component.psid = '';
    component.addcat = 'ipad-accessories-accessibility';
    component.subcat = 'ipad-accessories';
    fixture.detectChanges();
    component.setPageTitle();
    expect(component.addcat).toBeDefined();
  });

  it('set pageName to pageTitle if pageTitle does not exists and psid, sub-category is empty', () => {
    component.pageTitle = null;
    component.category = 'ipad';
    component.psid = '';
    component.addcat = '';
    component.subcat = '';
    fixture.detectChanges();
    component.setPageTitle();
    expect(component.category).toBeDefined();
  });

  it('set pageName to pageTitle if add categories does not exists', () => {
    component.addcat = null;
    component.setPageTitle();
    expect(component.addcat).toEqual(null);
  });

  it('set pageName to pageTitle if add categories does not exists', () => {
    spyOn(component, 'setPageTitle').and.callThrough();
    (component['router'] as any).url = 'webshop/testUrl';
    component.setPageTitle();
    expect(component.setPageTitle).toHaveBeenCalled();
  });
});
