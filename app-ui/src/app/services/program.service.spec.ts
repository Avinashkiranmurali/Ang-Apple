import { TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { ProgramService } from './program.service';
import { Program } from '@app/models/program';
import { RouterTestingModule } from '@angular/router/testing';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { HeapService } from '@app/analytics/heap/heap.service';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';

@Injectable()
export class TranslateServiceStub {
  public get<T>(key: T): Observable<T> {
    return of(key);
  }
  public instant(key: string): any {
    return '';
  }
}

describe('ProgramService', () => {
  let programService: ProgramService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule, RouterTestingModule ],
      providers: [
        { provide: TranslateService, useClass: TranslateServiceStub },
        { provide: EnsightenService },
        { provide: HeapService,
          useValue: {
            addUserProperties: () => {},
            loadInitialScript: () => {}
          }
        },
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe
      ]
    });
    // Inject the http test controller
    httpTestingController = TestBed.inject(HttpTestingController);
    programService = TestBed.inject(ProgramService);
  });

  afterEach(() => {
    // After every test, assert that there are no more pending requests.
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(programService).toBeTruthy();
  });

  it('should return Program data', waitForAsync(() => {
    // Fake response data
    const fakeResponse = require('assets/mock/program.json');

    // Setup a request using the fakeResponse data
    programService.getProgram().subscribe(
      (data: Program) => expect(data).toEqual(fakeResponse, 'should return fakeResponse'), fail
    );

    // Expect a call to this URL
    const req = httpTestingController.expectOne(programService.baseUrl + 'program');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(fakeResponse);

    // Run some expectations
    expect(fakeResponse.varId).toBe('Delta');
    expect(fakeResponse.programId).toBe('b2s_qa_only');
    expect(fakeResponse.convRate).toBe(0.006);
  }));

  it('should test for 404 error', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';

    programService.getProgram().subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne(programService.baseUrl + 'program');

    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));
});
