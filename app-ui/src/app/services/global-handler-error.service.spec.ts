import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { GlobalHandlerErrorService } from './global-handler-error.service';
import * as StackTrace from 'stacktrace-js';
import { of, throwError } from 'rxjs';

describe('GlobalHandlerErrorService', () => {
  let service: GlobalHandlerErrorService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ]
    });
    service = TestBed.inject(GlobalHandlerErrorService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call handleError method', () => {
    spyOn(service, 'handleError').and.callThrough();
    service.handleError('')
    expect(service.handleError).toHaveBeenCalled();
  });

  it('should call handleError method with StackTrace reference', () => {
    spyOn(service, 'handleError').and.callThrough();
    spyOn(StackTrace, 'fromError').and.returnValue(Promise.resolve([]));
    spyOn(service['http'], 'post').and.returnValue(of({}));
    service.handleError('');
    expect(service.handleError).toHaveBeenCalled();
  });

  it('should call handleError method with StackTrace reference - error message', () => {
    spyOn(service, 'handleError').and.callThrough();
    spyOn(StackTrace, 'fromError').and.returnValue(Promise.resolve([]));
    spyOn(service['http'], 'post').and.returnValue(throwError({statusCode: 404, statusText: 'NotFound'}));
    service.handleError({message: 'errorOccured'});
    expect(service.handleError).toHaveBeenCalled();
  });

});
