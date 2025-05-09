import { TestBed } from '@angular/core/testing';
import { CustomTranslateLoaderService } from './custom-translate-loader.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

describe('CustomTranslateLoaderService', () => {
  let service: CustomTranslateLoaderService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ],
      providers: [ CustomTranslateLoaderService ]
    });
    service = TestBed.inject(CustomTranslateLoaderService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();

    const mockResponse = {
      submit : 'SUBMIT'
    };
    service.getTranslation().subscribe((data: any) => {
      expect(data.submit).toBe(mockResponse.submit);
    });
    const baseUrl = '/apple-gr/service/';
    const url = baseUrl + 'messages';
    const req = httpMock.expectOne(url, 'call to api');
    expect(req.request.method).toBe('GET');

    req.flush(mockResponse);

    httpMock.verify();
  });
});
