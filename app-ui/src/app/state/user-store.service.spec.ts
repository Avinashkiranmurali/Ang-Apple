import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import { UserStoreService } from './user-store.service';
import { Config } from '@app/models/config';
import { Program } from '@app/models/program';
import { User } from '@app/models/user';
import { environment } from 'environments/environment';
import { BehaviorSubject } from 'rxjs';

describe('UserStoreService', () => {
  let userStoreService: UserStoreService;
  const userData: User = require('assets/mock/user.json');

  beforeEach(() => {
    TestBed.configureTestingModule({});
    userStoreService = TestBed.inject(UserStoreService);
    userStoreService.user = require('assets/mock/user.json');
    userStoreService.program = require('assets/mock/program.json');
  });

  afterAll(() => {
    const programData: Program = require('assets/mock/program.json');
    userStoreService.addUser(userData);
    userStoreService.addProgram(programData);
  });

  it('should be created', () => {
    expect(userStoreService).toBeTruthy();
  });

  it('should call addUser method if program exists', () => {
    spyOn(userStoreService, 'addUser').and.callThrough();
    userStoreService.addUser(userData);
    expect(userStoreService.get()).toBeDefined();
    expect(userStoreService.addUser).toHaveBeenCalled();
  });

  it('should call addUser method if program doesnot exists', () => {
    spyOn(userStoreService, 'addUser').and.callThrough();
    spyOnProperty(userStoreService, 'program').and.returnValue({});
    userStoreService.addUser(userData);
    expect(userStoreService.get()).toBeDefined();
    expect(userStoreService.addUser).toHaveBeenCalled();
  });

  it('should call addProgram method if user exists', () => {
    spyOn(userStoreService, 'addProgram').and.callThrough();
    const programData: Program = require('assets/mock/program.json');
    userStoreService.addProgram(programData);
    expect(userStoreService.addProgram).toHaveBeenCalled();
  });

  it('should call addProgram method if user doesnot exists', () => {
    spyOn(userStoreService, 'addProgram').and.callThrough();
    spyOnProperty(userStoreService, 'user').and.returnValue({});
    const programData: Program = require('assets/mock/program.json');
    userStoreService.addProgram(programData);
    expect(userStoreService.addProgram).toHaveBeenCalled();
  });

  it('should call addConfig method', () => {
    spyOn(userStoreService, 'addConfig').and.callThrough();
    const programData: Program = require('assets/mock/program.json');
    userStoreService.addConfig(programData['config'] as Config);
    expect(userStoreService.addConfig).toHaveBeenCalled();
  });

  it('should call addConfig method for anonymous user', () => {
    spyOn(userStoreService, 'addConfig').and.callThrough();
    userStoreService.user = require('assets/mock/anon-user.json');
    const programData: Program = require('assets/mock/program.json');
    programData['config']['login_required'] = null;
    userStoreService.addConfig(programData['config'] as Config);
    expect(userStoreService.addConfig).toHaveBeenCalled();
  });

  it('should call addConfig method - if login field exists', () => {
    spyOn(userStoreService, 'addConfig').and.callThrough();
    const programData: Program = require('assets/mock/program.json');
    programData['config']['login_required'] = false;
    userStoreService.addConfig(programData['config'] as Config);
    expect(userStoreService.addConfig).toHaveBeenCalled();
  });

  it('should call addConfig method for anonymous user - if login field exists and no session data', () => {
    spyOn(userStoreService, 'addConfig').and.callThrough();
    userStoreService.user = require('assets/mock/anon-user.json');
    const programData: Program = require('assets/mock/program.json');
    programData['config']['login_required'] = true;
    programData['config']['sessionTimeout'] = null;
    programData['config']['sessionTimeoutRemaining'] = null;
    programData['config']['sessionTimeoutWarning'] = null;
    environment.production = true;
    userStoreService.addConfig(programData['config'] as Config);
    expect(userStoreService.addConfig).toHaveBeenCalled();
    environment.production = false;
    expect(environment.production).toBeFalsy();
  });

  it('should call enableNotificationBanner method', () => {
    spyOn(userStoreService, 'enableNotificationBanner').and.callThrough();
    userStoreService.enableNotificationBanner(true, '');
    userStoreService.enableNotificationBanner(false, '');
    expect(userStoreService.enableNotificationBanner).toHaveBeenCalled();
  });

  it('should call onNotificationRibbonClose method', fakeAsync(() => {
    spyOn(userStoreService, 'onNotificationRibbonClose').and.callThrough();
    userStoreService.onNotificationRibbonClose();
    tick(700);
    expect(userStoreService.onNotificationRibbonClose).toHaveBeenCalled();
  }));

  it('should call isPageAccessible method for unauthorized page', () => {
    spyOn(userStoreService, 'isPageAccessible').and.callThrough();
    const programData: Program = require('assets/mock/program.json');
    programData['config']['unAuthorizedPages'] = 'sessionUrl';
    userStoreService.addConfig(programData['config'] as Config);
    userStoreService.isPageAccessible('sessionUrl');
    userStoreService.isPageAccessible('test');
    expect(userStoreService.isPageAccessible).toHaveBeenCalled();
  });

  it('should call isPageAccessible method for authorized page', () => {
    spyOn(userStoreService, 'isPageAccessible').and.callThrough();
    const programData: Program = require('assets/mock/program.json');
    programData['config']['unAuthorizedPages'] = null;
    userStoreService.addConfig(programData['config'] as Config);
    userStoreService.isPageAccessible('landing');
    expect(userStoreService.isPageAccessible).toHaveBeenCalled();
  });

  it('should call isPageAccessible method - authorized page', () => {
    spyOn(userStoreService, 'isPageAccessible').and.callThrough();
    const programData: Program = require('assets/mock/program.json');
    userStoreService.addConfig(programData['config'] as Config);
    userStoreService.isPageAccessible('sessionUrl');
    expect(userStoreService.isPageAccessible).toHaveBeenCalled();
  });
});
