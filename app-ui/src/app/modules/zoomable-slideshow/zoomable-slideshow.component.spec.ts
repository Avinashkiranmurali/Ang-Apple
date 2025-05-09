import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ZoomableSlideshowComponent } from './zoomable-slideshow.component';

(window as any).UIkit = {
  util: {
    on: (element: string, event: string, cb: any) => {}
  },
  slider: (element: string, index: number) => { return [] }
};

describe('ZoomableSlideshowComponent', () => {
  let component: ZoomableSlideshowComponent;
  let fixture: ComponentFixture<ZoomableSlideshowComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ZoomableSlideshowComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ZoomableSlideshowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call deActivateZoom method', () => {
    component.zoomActivated = true;
    component.deActivateZoom();
    expect(component.zoomActivated).toBeFalsy();
  });

  it('should call deActivateZoom method - when zoomActivated is false', () => {
    component.zoomActivated = false;
    component.deActivateZoom();
    expect(component.zoomActivated).toBeFalsy();
  });

  it('should call window resize method when resize event dispatch', () => {
    spyOn(component, 'onResize').and.callThrough();
    window.dispatchEvent(new Event('resize'));
    fixture.detectChanges();
    expect(component.onResize).toHaveBeenCalled();
  });

  it('should call activateZoom method', () => {
    spyOn(component, 'activateZoom').and.callThrough();
    component.activateZoom(1);
    expect(component.activateZoom).toHaveBeenCalled();
  });

  it('should call ngAfterViewChecked method', () => {
    const element = document.querySelectorAll('.uk-slider-container');
    const ele = document.createElement('li');
    ele.appendChild(document.createElement('a'));
    element.forEach(element => element.appendChild(ele));
    spyOn(component, 'ngAfterViewChecked').and.callThrough();
    fixture.detectChanges();
    component.ngAfterViewChecked();
    expect(component.ngAfterViewChecked).toHaveBeenCalled();
  });

});
