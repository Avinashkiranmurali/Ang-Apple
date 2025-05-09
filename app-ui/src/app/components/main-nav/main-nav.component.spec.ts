import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MainNavComponent } from './main-nav.component';
import { NgbPopover, NgbPopoverModule } from '@ng-bootstrap/ng-bootstrap';
import { UserStoreService } from '@app/state/user-store.service';
import { NavStoreService } from '@app/state/nav-store.service';
import { Component } from '@angular/core';
import { By } from '@angular/platform-browser';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { CustomHoverLinkDirective } from '@app/directives/custom-hover-link.directive';

// Simple test component that will not in the actual app
@Component({
  template:
  `<a id="mobile-nav" role="button" popoverClass="mobile-main-nav-menu" class=""
    #mobileNav="ngbPopover" triggers="manual" placement="bottom-left" [ngbPopover]="menuContent">
  </a>
  <ng-template #menuContent >
    <div class="search-overlay">
    </div>
  </ng-template>`
})
class TestComponent {}

describe('MainNavComponent', () => {
  let component: MainNavComponent;
  let fixture: ComponentFixture<MainNavComponent>;
  let testFixture: ComponentFixture<TestComponent>;
  let navStore: NavStoreService;
  const mainNav = require('assets/mock/categories.json');
  const programData = require('assets/mock/program.json');
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        TranslateModule.forRoot(),
        RouterTestingModule,
        NgbPopoverModule,
        HttpClientTestingModule
      ],
      declarations: [
        MainNavComponent,
        CustomHoverLinkDirective,
        TestComponent
      ],
      providers: [
        { provide: UserStoreService, useValue: userData },
        CustomHoverLinkDirective
      ]
    })
    .compileComponents();
    navStore = TestBed.inject(NavStoreService);
    navStore.addMainNav(mainNav);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MainNavComponent);
    testFixture = TestBed.createComponent(TestComponent);
    component = fixture.componentInstance;
    component.mainNav = mainNav;
    const directive = testFixture.debugElement.query(By.directive(NgbPopover));
    component.mobileNav = directive.references['mobileNav'];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit on click', () => {
    spyOn(component.changeSubNav, 'emit');

    // trigger the click
    // eslint-disable-next-line prefer-const
    const nativeElement = fixture.nativeElement;
    const anchor = nativeElement.querySelector('a');
    anchor.dispatchEvent(new Event('click'));
    fixture.detectChanges();
    expect(component.changeSubNav.emit).toHaveBeenCalled();
  });
});
