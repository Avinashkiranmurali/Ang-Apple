import { TestBed } from '@angular/core/testing';

import { NotificationRibbonService } from './notification-ribbon.service';

describe('NotificationRibbonService', () => {
  let service: NotificationRibbonService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NotificationRibbonService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call emitChange method', () => {
    spyOn(service, 'emitChange').and.callThrough();
    service.emitChange([true, 'testValue']);
    expect(service.emitChange).toHaveBeenCalled();
  });

  it('should get getCustomRibbonPersist', () => {
    spyOn(service, 'getCustomRibbonPersist').and.callThrough();
    expect(service.getCustomRibbonPersist()).toBeFalsy();
    sessionStorage.setItem('persistCustomNotificationRibbon', 'true');
    expect(service.getCustomRibbonPersist()).toBeTruthy();
    expect(service.getCustomRibbonPersist).toHaveBeenCalled();
  });

});
