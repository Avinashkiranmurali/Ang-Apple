import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})

export class NotificationRibbonService {

  private isEnableCustomNotificationRibbon: boolean;
  private isShowNotificationRibbon: boolean;
  private isCustomNoteClosed: boolean;

  private emitNotificationSource = new Subject<[boolean, string]>();
  changeEmitted$ = this.emitNotificationSource.asObservable();

  constructor() {
  }

  emitChange(changeValue: [boolean, string]) {
    this.emitNotificationSource.next(changeValue);
  }

  setCustomRibbonShow(val) {
    this.isEnableCustomNotificationRibbon = val;
  }

  getCustomRibbonShow() {
    return this.isEnableCustomNotificationRibbon;
  }

  setNotificationRibbonShow(val) {
    this.isShowNotificationRibbon = val;
  }

  getNotificationRibbonShow() {
    return this.isShowNotificationRibbon;
  }

  setCustomRibbonClosed(val) {
    this.isCustomNoteClosed = val;
  }

  getCustomRibbonClosed() {
    return this.isCustomNoteClosed;
  }

  getCustomRibbonPersist(){
    return sessionStorage.getItem('persistCustomNotificationRibbon') ? sessionStorage.getItem('persistCustomNotificationRibbon') === 'true' : false;
  }
}
