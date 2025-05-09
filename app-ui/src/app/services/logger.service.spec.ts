import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { LoggerService } from './logger.service';

describe('LoggerService', () => {
  let service: LoggerService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ],
      providers: [
        { provide: LoggerService }
      ]
    });
    service = TestBed.inject(LoggerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('call log()', () => {
    spyOn(service, 'log').and.callThrough();
    service.log('error', 'Errors');
    expect(service.log).toHaveBeenCalled();
  });

  it('logError() should call log()', () => {
    spyOn(service, 'log').and.callThrough();
    service.logError('error');
    expect(service.log).toHaveBeenCalled();
  });

  it('logWarning() should call log()', () => {
    spyOn(service, 'log').and.callThrough();
    service.logWarning('error');
    expect(service.log).toHaveBeenCalled();
  });

  it('logInfo() should call log()', () => {
    spyOn(service, 'log').and.callThrough();
    service.logInfo('error');
    expect(service.log).toHaveBeenCalled();
  });

  it('logMessage() should call log()', () => {
    spyOn(service, 'log').and.callThrough();
    service.logMessage('error');
    expect(service.log).toHaveBeenCalled();
  });

  it('logVerbose() should call log()', () => {
    spyOn(service, 'log').and.callThrough();
    service.logVerbose('error');
    expect(service.log).toHaveBeenCalled();
  });
});
