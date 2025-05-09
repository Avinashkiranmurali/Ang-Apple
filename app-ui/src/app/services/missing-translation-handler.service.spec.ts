import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { MissingTranslationHandlerService } from './missing-translation-handler.service';
import { MissingTranslationHandlerParams } from '@ngx-translate/core';

describe('MissingTranslationHandlerService', () => {
  let missingTranslationHandlerService: MissingTranslationHandlerService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ],
      providers: [
        { provide: MissingTranslationHandlerService }
      ]
    });

    // Inject the http test controller
    httpTestingController = TestBed.inject(HttpTestingController);
    missingTranslationHandlerService = TestBed.inject(MissingTranslationHandlerService);
  });

  afterEach(() => {
    // After every test, assert that there are no more pending requests.
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(missingTranslationHandlerService).toBeTruthy();
  });

  it('should call handle method', () => {
    spyOn(missingTranslationHandlerService, 'handle').and.callThrough();
    const params: MissingTranslationHandlerParams = Object.assign({});
    missingTranslationHandlerService.handle(params);
    expect(missingTranslationHandlerService.handle).toHaveBeenCalled();
  });
});
