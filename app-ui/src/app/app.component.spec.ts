import { TestBed, ComponentFixture, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { AppComponent } from './app.component';
import { LoaderComponent } from '@app/loader/loader.component';
import { TranslateModule } from '@ngx-translate/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  const outerWidth = Window['outerWidth'];

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        HttpClientTestingModule,
        TranslateModule.forRoot()
      ],
      declarations: [
        AppComponent,
        LoaderComponent
      ],
      providers: []
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    (window['outerWidth'] as any) = outerWidth;
  });

  it('should create the app', () => {
    expect(component).toBeTruthy();
  });

  it('should have as title \'app-ui\'', () => {
    expect(component.title).toEqual('app-ui');
  });

  it('should call window resize method when resize event dispatch', () => {
    spyOn(component, 'windowResize').and.callThrough();
    window.dispatchEvent(new Event('resize'));
    fixture.detectChanges();
    expect(component.windowResize).toHaveBeenCalled();
  });

  it('should add meta data for mobile version', () => {
    (window['outerWidth'] as any) = 766;
    fixture.detectChanges();
    component.windowResize();
    expect(window['outerWidth']).toEqual(766);
  });

});
