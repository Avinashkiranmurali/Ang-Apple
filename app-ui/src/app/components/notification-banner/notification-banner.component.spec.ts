import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { NotificationBannerComponent } from './notification-banner.component';
import { NotificationRibbonService } from '@app/services/notification-ribbon.service';

describe('NotificationBannerComponent', () => {
  let component: NotificationBannerComponent;
  let notificationRibbonService: NotificationRibbonService;
  let fixture: ComponentFixture<NotificationBannerComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ NotificationBannerComponent ],
      providers: [
        { provide: NotificationRibbonService }
        ],
    })
    .compileComponents();
    notificationRibbonService = TestBed.inject(NotificationRibbonService);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NotificationBannerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call emitChange method from NotificationRibbonService in constructor', () => {
    notificationRibbonService.emitChange([true, 'abc']);
  });

  it('should not call emitChange method from NotificationRibbonService in constructor', () => {
    notificationRibbonService.emitChange([false, 'abc']);
  });

  it('should call onCustomNotificationRibbonClose method', () => {
    spyOn(component, 'onCustomNotificationRibbonClose').and.callThrough();
    const event = new Event('click');
    component.onCustomNotificationRibbonClose(event);
    expect(component.isEnableCustomNotificationRibbon).toBeFalse();
  });

  it('should call onNotificationRibbonClose method', () => {
    spyOn(component, 'onNotificationRibbonClose').and.callThrough();
    const event = new Event('click');
    component.onNotificationRibbonClose(event);
    expect(component.isEnableNotificationRibbon).toBeFalse();
  });

  it('should call persistNotificationRibbon method', () => {
    spyOn(component, 'persistNotificationRibbon').and.callThrough();
    component.persistNotificationRibbon();
    sessionStorage.setItem('persistCustomNotificationRibbon', 'true');
    expect(component.persistCustomNotificationRibbon).toBeTrue();
  });
});
