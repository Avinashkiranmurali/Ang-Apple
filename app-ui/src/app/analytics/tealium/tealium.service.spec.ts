import { inject, TestBed } from '@angular/core/testing';
import { TealiumService } from '@app/analytics/tealium/tealium.service';
import { UserStoreService } from '@app/state/user-store.service';

describe('TealiumService', () => {
  let service: TealiumService;
  const programData = require('assets/mock/program.json');
  programData.config.tealiumEndPoint = 'true';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        { provide: UserStoreService, useValue: {
          config: programData.config }
        }
      ]
    });
    service = TestBed.inject(TealiumService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call loadInitialScript', () => {
    spyOn(service, 'loadInitialScript').and.callThrough();
    service.loadInitialScript();
    expect(service.loadInitialScript).toHaveBeenCalled();
  });

  it('should call tealiumEndPoint', () => {
    service.tealiumEndPoint = 'testing';
    expect(service.tealiumEndPoint).toBeDefined();
  });

});
