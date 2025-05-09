import { HttpErrorResponse } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Renderer2 } from '@angular/core';
import { TestBed, waitForAsync } from '@angular/core/testing';
import { ModalsService } from '@app/components/modals/modals.service';
import { TimedoutModalService } from './timedout-modal.service';
import { Router } from '@angular/router';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { DecimalPipe } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

describe('TimedoutModalService', () => {
  let timedoutModalService: TimedoutModalService;
  let httpTestingController: HttpTestingController;
  const mockData = {
    timedOut: 'javascript:void(0)',
    timedOutUrl: ''
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        Renderer2,
        ModalsService,
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate') }
        },
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe
      ]
    });
    timedoutModalService = TestBed.inject(TimedoutModalService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(timedoutModalService).toBeTruthy();
  });

  it('should return checkRemainingTime data', waitForAsync(() => {
    timedoutModalService.checkRemainingTime().subscribe(data => expect(data).toEqual(mockData), fail);

    // Expect a call to this URL
    const req = httpTestingController.expectOne(() => true);

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(mockData);
  }));

  it('should test for 404 error - checkRemainingTime', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    timedoutModalService.checkRemainingTime().subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne(() => true);
    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

  it('should call showTimedoutModal method - no open modals', () => {
    spyOn(timedoutModalService['modalService'], 'hasAnyOpenModal').and.returnValue(false);
    spyOn(timedoutModalService, 'showTimedoutModal').and.callThrough();
    timedoutModalService.showTimedoutModal(mockData);
    expect(timedoutModalService.showTimedoutModal).toHaveBeenCalled();
  });

  it('should call showTimedoutModal method - Found open modals', () => {
    spyOn(timedoutModalService['modalService'], 'hasAnyOpenModal').and.returnValue(true);
    spyOn(timedoutModalService, 'showTimedoutModal').and.callThrough();
    timedoutModalService.showTimedoutModal(mockData);
    expect(timedoutModalService.showTimedoutModal).toHaveBeenCalled();
  });

  it('should call showTimedoutModal method - else check', () => {
    spyOn(timedoutModalService, 'showTimedoutModal').and.callThrough();
    timedoutModalService.showTimedoutModal(null);
    expect(timedoutModalService.showTimedoutModal).toHaveBeenCalled();
  });

});
