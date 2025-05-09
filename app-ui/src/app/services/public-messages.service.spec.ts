import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PublicMessagesService } from './public-messages.service';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';

describe('PublicMessagesService', () => {
  let service: PublicMessagesService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ],
    });

    httpTestingController = TestBed.inject(HttpTestingController);
    service = TestBed.inject(PublicMessagesService);
  });

  afterEach(() => {
    // After every test, assert that there are no more pending requests.
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call getPublicMessages method - success response', () => {
    spyOn(service, 'getPublicMessages').and.callThrough();
    spyOn(service['http'], 'get').and.returnValue(of({test: 'check'}));
    service.getPublicMessages();
    service.getPublicMessages().subscribe(
      (response) => expect(response).toEqual({test: 'check'}, 'should return fakeResponse'), fail
    );
    expect(service.getPublicMessages).toHaveBeenCalled();
  });

  it('should call getPublicMessages method - failure case', () => {
    spyOn(service, 'getPublicMessages').and.callThrough();
    spyOn(service['http'], 'get').and.returnValue(throwError({status: 404, statusText: 'Not Found'}));
    service.getPublicMessages();
    service.getPublicMessages().subscribe(
      data => fail,
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(undefined, 'message');
      }
    );
    expect(service.getPublicMessages).toHaveBeenCalled();
  });

});
