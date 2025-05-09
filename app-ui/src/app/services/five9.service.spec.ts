import { HttpClientTestingModule } from '@angular/common/http/testing';
import { inject, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateModule } from '@ngx-translate/core';

import { Five9Service } from './five9.service';

describe('Five9Service', () => {
  let service: Five9Service;
  const programData = require('assets/mock/program.json');
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: UserStoreService, useValue: {
            program: programData,
            config: programData.config
          }
        }
      ]
    });
    service = TestBed.inject(Five9Service);
  });

  afterAll(() => {
    service['userStore'].config = programData.config;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should not load five9 chat based on showLiveChat', inject([UserStoreService], (userStore: UserStoreService) => {
    userStore.program.sessionConfig.five9Config.chatEnabled = false;
    expect(service.program.sessionConfig.five9Config.chatEnabled).toBeFalse();
  }));
});
