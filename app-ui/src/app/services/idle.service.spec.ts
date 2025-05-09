import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { IdleService } from './idle.service';
import { Idle } from '@ng-idle/core';
import { Keepalive } from '@ng-idle/keepalive';
import { NgIdleKeepaliveModule } from '@ng-idle/keepalive';

describe('IdleService', () => {
  let service: IdleService;
  let idle: Idle;
  let keepalive: Keepalive;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        HttpClientTestingModule,
        NgIdleKeepaliveModule
      ],
      providers: [Idle, Keepalive]
    });
    service = TestBed.inject(IdleService);
    idle = TestBed.inject(Idle);
    keepalive = TestBed.inject(Keepalive);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  it('should create instance', () => {
    expect(service).toBeTruthy();
  });

  it('should call init method', () => {
    spyOn(service, 'init').and.callThrough();
    const idleObj = {
      idle: 1,
      timeout: 10,
      interval: 20
    };
    service.init(idle, keepalive, idleObj);
    expect(service.init).toHaveBeenCalled();
  });

  it('should be created and emit keepalive service on ping', () => {
    const idleObj = {
      idle: 1,
      timeout: 10,
      interval: 20
    };
    service.init(idle, keepalive, idleObj);
    window['keepalive'] = () => {};
    keepalive.onPing.emit();
    // else check
    window['keepalive'] = undefined;
    keepalive.onPing.emit();
    expect(service).toBeTruthy();
  });

  it('should be created and emit idle service on timeout', () => {
    const idleObj = {
      idle: 1,
      timeout: 10,
      interval: 20
    };
    service.init(idle, keepalive, idleObj);
    idle.stop = () => {};
    idle.onTimeout.emit();
    expect(service).toBeTruthy();
  });

});
