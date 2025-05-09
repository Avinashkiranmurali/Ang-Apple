import { TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { TemplateService } from './template.service';

describe('TemplateService', () => {
  let templateService: TemplateService;
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;

  const mockConfigData = require('assets/mock/configData.json');
  const program = require('assets/mock/program.json');

  beforeEach(() => {

    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ],
      providers: [
        { provide: TemplateService }
      ]
    });

    // Inject the http test controller
    httpTestingController = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
    templateService = TestBed.inject(TemplateService);
    templateService.userStore.user = require('assets/mock/user.json');
    templateService.userStore.config = program['config'];
  });

  it('should be created', () => {
    expect(templateService).toBeTruthy();
  });

  it('should return Template data', waitForAsync(() => {
    templateService.userStore.config['loginRequired'] = true;
    templateService.getTemplate().subscribe(data => expect(data).toEqual(mockConfigData), fail);

    // Expect a call to this URL
    const req = httpTestingController.expectOne(templateService.baseUrl + 'configData');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(mockConfigData);
  }));

  it('should test for 404 error - getTemplate', waitForAsync(() => {
    const errorMsg = 'deliberate 404 error';
    templateService.getTemplate().subscribe(
      data => fail('should have failed with the 404 error'),
      (error: HttpErrorResponse) => {
        expect(error.status).toEqual(404, 'status');
        expect(error.error).toEqual(errorMsg, 'message');
      }
    );

    const req = httpTestingController.expectOne(templateService.baseUrl + 'configData');

    // Respond with mock error
    req.flush(errorMsg, { status: 404, statusText: 'Not Found' });
  }));

  it('should get property value from template object', waitForAsync(() => {
    templateService.userStore.config['loginRequired'] = true;
    templateService.getTemplate().subscribe(data => expect(data).toEqual(mockConfigData), fail);

    // Expect a call to this URL
    const req = httpTestingController.expectOne(templateService.baseUrl + 'configData');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush(mockConfigData);
    spyOn(templateService, 'getProperty').and.callThrough();
    templateService.getProperty('header');
    expect(templateService.getProperty).toHaveBeenCalled();
  }));

});
